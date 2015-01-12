/**
 *  iComfort Service Manager SmartApp
 * 
 *  Author: Jason Mok
 *  Date: 2015-01-10
 *
 ***************************
 *
 *  Copyright 2015 Jason Mok
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 **************************
 */
definition(
	name: "iComfort",
	namespace: "copy-ninja",
	author: "Jason Mok",
	description: "Connect iComfort to control your thermostats",
	category: "SmartThings Labs",
	iconUrl:   "http://smartthings.copyninja.net/icons/iComfort@1x.png",
	iconX2Url: "http://smartthings.copyninja.net/icons/iComfort@2x.png",
	iconX3Url: "http://smartthings.copyninja.net/icons/iComfort@3x.png"
)

preferences {
	page(name: "prefLogIn", title: "iComfort")    
	page(name: "prefListDevice", title: "iComfort")
}

/* Preferences */
def prefLogIn() {
	def showUninstall = username != null && password != null 
	return dynamicPage(name: "prefLogIn", title: "Connect to iComfort", nextPage:"prefListDevice", uninstall:showUninstall, install: false) {
		section("Login Credentials"){
			input("username", "text", title: "Username", description: "iComfort Username")
			input("password", "password", title: "Password", description: "iComfort password")
		}
		section("Display"){
			input(name: "temperatureUnit", title: "Temperature Unit", type: "enum", defaultValue: "F", metadata:[values:[F:"°F",C:"°C"]] )
		}  
		section("Connectivity"){
			input(name: "polling", title: "Server Polling (in Minutes)", type: "int", description: "in minutes", defaultValue: "5" )
		}              
	}
}

def prefListDevice() {
	if (forceLogin()) {
		def thermostatList = getThermostatList()
		if (thermostatList) {
			return dynamicPage(name: "prefListDevice",  title: "Thermostats", install:true, uninstall:true) {
				section("Select which thermostat/zones to use"){
					input(name: "thermostat", type: "enum", required:false, multiple:true, metadata:[values:thermostatList])
				}
			}
		} else {
			return dynamicPage(name: "prefListDevice",  title: "Error!", install:false, uninstall:true) {
				section(""){
					paragraph "Could not find any devices " 
				}
			}
		}
	} else {
		return dynamicPage(name: "prefListDevice",  title: "Error!", install:false, uninstall:true) {
			section(""){
				paragraph "The username or password you entered is incorrect. Try again. " 
			}
		}  
	}
}

/* Initialization */
def installed() { initialize() }
def updated() { initialize() }

def uninstalled() {
	unschedule()
	def deleteDevices = getAllChildDevices()
	deleteDevices.each { deleteChildDevice(it.deviceNetworkId) }
}	

def initialize() {    
	unsubscribe()
	login()
    
	// Get initial device status in state.data
	state.polling = [ 
		last: now(),
		runNow: true
	]
	state.data = [:]
	state.lookup = [
		thermostatOperatingState: [:],
		thermostatFanMode: [:],
		thermostatMode: [:],
		program: [:],
		temperatureRangeC: [:],
		temperatureRangeF: [:],
		coolingSetPointHigh: [:],
		coolingSetPointLow: [:],
		heatingSetPointHigh: [:],
		heatingSetPointLow: [:],
		differenceSetPoint: [:]
	]
	state.list = [
		thermostatOperatingState: [],
		thermostatFanMode: [],
		thermostatMode: [],
		program: [:]
	]
    
	// Create new devices for each selected doors
	def selectedDevices = []
	def thermostatList = getThermostatList()
	def deleteDevices 
   	 
	if (settings.thermostat) {
		if (settings.thermostat[0].size() > 1) {
			selectedDevices = settings.thermostat
		} else {
			selectedDevices.add(settings.thermostat)
		}
	}
     
	selectedDevices.each { dni ->    	
		def childDevice = getChildDevice(dni)
		if (!childDevice) {
            addChildDevice("copy-ninja", "iComfort Thermostat", dni, null, ["name": "iComfort: " + thermostatList[dni],  "completedSetup": true])
		} 
	}
    
	//Remove devices that are not selected in the settings
	if (!selectedDevices) {
		deleteDevices = getAllChildDevices()
	} else {
		deleteDevices = getChildDevices().findAll { !selectedDevices.contains(it.deviceNetworkId) }
	}
	deleteDevices.each { deleteChildDevice(it.deviceNetworkId) } 
	
	//Get lookup lists
	getLookups()
	
	//Refresh device
	refresh()
    
	// Schedule polling
	unschedule()
	schedule("0 0/" + ((settings.polling.toInteger() > 0 )? settings.polling.toInteger() : 1)  + " * * * ?", refresh )
}

/* Access Management */
private forceLogin() {
	//Reset token and expiry
	state.polling = [ 
		last: now(),
		runNow: true
	]  
	state.data = [:]
	return doLogin()
}

private login() {
	return doLogin()
}

private doLogin() { 
	apiPut("/DBAcessService.svc/ValidateUser", [UserName: settings.username, lang_nbr: "1"], [] ) { response ->
		if (response.status == 200) {
			if (response.data.msg_code == "SUCCESS") {
				return true
			} else {
				return false
			}
		} else {
			return false
		}
	} 	
}

// Listing all the thermostats you have in iComfort
private getThermostatList() { 	    
	def thermostatList = [:]
    def gatewayList = [:]
	//Retrieve all the gateways
	apiGet("/DBAcessService.svc/GetSystemsInfo", [userID: settings.username]) { response ->
		if (response.status == 200) {
			response.data.Systems.each { device ->
				gatewayList.putAt(device.Gateway_SN,device.System_Name)
			}
		}
	}   
	//Retrieve all the Zones
	gatewayList.each { gatewaySN, gatewayName ->		
		apiGet("/DBAcessService.svc/GetTStatInfoList", [GatewaySN: gatewaySN, TempUnit: (getTemperatureUnit()=="F")?0:1, Cancel_Away: "-1"]) { response ->
			if (response.status == 200) {
            	//log.debug "zones: " response.data.tStatInfo
				response.data.tStatInfo.each { 
					def dni = [ app.id, gatewaySN, it.Zone_Number ].join('|')
                    thermostatList[dni] = ( it.Zones_Installed > 1 )? gatewayName + ": " + it.Zone_Name : gatewayName
				}
			}
		}
	}
	return thermostatList
}

// Get all Lookups
private getLookups() { 	    
	//Get Thermostat Mode lookups
	apiGet("/DBAcessService.svc/GetTstatLookupInfo", [name: "Operation_Mode", langnumber: 0]) { response ->
		response.data.tStatlookupInfo.each {
			state.lookup.thermostatMode.putAt(it.value, translateDesc(it.description))
			state.list.thermostatMode.add(translateDesc(it.description))
		}
	}
	
	//Get Fan Modes lookups
	apiGet("/DBAcessService.svc/GetTstatLookupInfo", [name: "Fan_Mode", langnumber: 0]) { response ->
		response.data.tStatlookupInfo.each {
			state.lookup.thermostatFanMode.putAt(it.value, translateDesc(it.description))
			state.list.thermostatFanMode.add(translateDesc(it.description))
		}
	}
	
	//Get System Status lookups
	apiGet("/DBAcessService.svc/GetTstatLookupInfo", [name: "System_Status", langnumber: 0]) { response ->
		response.data.tStatlookupInfo.each {
			state.lookup.thermostatOperatingState.putAt(it.value, translateDesc(it.description))
			state.list.thermostatOperatingState.add(translateDesc(it.description))
		}
	}    
    
	//Get Temperature lookups
	apiGet("/DBAcessService.svc/GetTemperatureRange", [highpoint: 37, lowpoint: 4]) { response ->
		response.data.each {
			def temperatureLookup = it.Value.split("\\|")
			state.lookup.temperatureRangeC.putAt(temperatureLookup[0].toString(), temperatureLookup[1].toString())
            state.lookup.temperatureRangeF.putAt(temperatureLookup[1].toString(), temperatureLookup[0].toString())
		}
	}    
	
	def childDevices = getAllChildDevices()
	childDevices.each { device ->
		def childDevicesGateway = getDeviceGatewaySN(device)
		//Get Program lookups
		state.lookup.program.putAt(device.deviceNetworkId, [:])
		state.list.program.putAt(device.deviceNetworkId, [])
		apiGet("/DBAcessService.svc/GetTStatScheduleInfo", [GatewaySN: childDevicesGateway]) { response ->
			if (response.status == 200) {
				response.data.tStatScheduleInfo.each {
					state.lookup.program[device.deviceNetworkId].putAt(it.Schedule_Number.toString(), "Program " + (it.Schedule_Number + 1) + ":\n" + it.Schedule_Name)
					state.list.program[device.deviceNetworkId].add("Program " + (it.Schedule_Number + 1) + ":\n" + it.Schedule_Name)
				}      
			}
		}
		
		//Get Limit Lookups
		apiGet("/DBAcessService.svc/GetGatewayInfo", [GatewaySN: childDevicesGateway, TempUnit: "0"]) { response ->
			if (response.status == 200) {
				state.lookup.coolingSetPointHigh.putAt(device.deviceNetworkId, response.data.Cool_Set_Point_High_Limit)
				state.lookup.coolingSetPointLow.putAt(device.deviceNetworkId, response.data.Cool_Set_Point_Low_Limit)
				state.lookup.heatingSetPointHigh.putAt(device.deviceNetworkId, response.data.Heat_Set_Point_High_Limit)
				state.lookup.heatingSetPointLow.putAt(device.deviceNetworkId, response.data.Heat_Set_Point_Low_Limit)
				state.lookup.differenceSetPoint.putAt(device.deviceNetworkId, response.data.Heat_Cool_Dead_Band)
			}
		}
	}
}



/* api connection */
	
// HTTP GET call
private apiGet(apiPath, apiQuery = [], callback = {}) {	
	// set up parameters
	def apiParams = [ 
		uri: "https://" + settings.username + ":" + settings.password + "@services.myicomfort.com",
		path: apiPath,
		query: apiQuery
	]
	log.debug "HTTP GET request: " + apiParams
	// try to call 
	try {
		httpGet(apiParams) { response ->
			log.debug "HTTP GET response: " + response.data
			callback(response)
		}
	}	catch (Error e)	{
		log.debug "API Error: $e"
	}
}

// HTTP PUT call
private apiPut(apiPath, apiQuery = [], apiBody = [], callback = {}) {    
	// set up final parameters
	def apiParams = [ 
		uri: "https://" + settings.username + ":" + settings.password + "@services.myicomfort.com",
		path: apiPath,
		contentType: "application/json; charset=utf-8",
		query: apiQuery,
		body: apiBody
	]
    
	try {
		httpPut(apiParams) { response ->
			callback(response)
		}
	} catch (Error e)	{
		log.debug "API Error: $e"
	}
}

// Updates data for devices
def updateDeviceData() {    
	// Next polling time, defined in settings
	def next = (state.polling.last?:0) + ((settings.polling.toInteger() > 0 ? settings.polling.toInteger() : 1) * 60 * 1000)
	if ((now() > next) || (state.polling.runNow)) {
		// set polling states
		state.polling.last = now()
		state.polling.runNow = false
		
		// update data for child devices
		updateDeviceChildData()
	}
	return true
}

// update child device data
private updateDeviceChildData() {
	def childDevices = getAllChildDevices()
	childDevices.each { device ->
		def childDevicesGateway = getDeviceGatewaySN(device)
		apiGet("/DBAcessService.svc/GetTStatInfoList", [GatewaySN: childDevicesGateway, TempUnit: (getTemperatureUnit()=="F")?0:1, Cancel_Away: "-1"]) { response ->
			if (response.status == 200) {
				response.data.tStatInfo.each { 
					//log.debug "response: " + it
					state.data[device.deviceNetworkId] = [
						temperature: it.Indoor_Temp,
						humidity: it.Indoor_Humidity,
						coolingSetpoint: it.Cool_Set_Point,
						heatingSetpoint: it.Heat_Set_Point,
						thermostatMode: lookupInfo( "thermostatMode", it.Operation_Mode.toString(), true ),
						thermostatFanMode: lookupInfo( "thermostatFanMode", it.Fan_Mode.toString(), true ),
						thermostatOperatingState: lookupInfo( "thermostatOperatingState", it.System_Status.toString(), true ),
						thermostatProgramMode: it.Program_Schedule_Mode,
						thermostatProgramSelection: it.Program_Schedule_Selection
					]
				}
			}
		}
	}
}

// lookup value translation
def lookupInfo( name, value, mode ) {
	if (name == "thermostatFanMode") {
		if (mode) {
			return state.lookup.thermostatFanMode.getAt(value)
		} else {
			return state.lookup.thermostatFanMode.find{it.value==value}?.key
		}
	}
	if (name == "thermostatMode") {
		if (mode) {
			return state.lookup.thermostatMode.getAt(value)
		} else {
			return state.lookup.thermostatMode.find{it.value==value}?.key
		}	
	}
	if (name == "thermostatOperatingState") {
		if (mode) {
			return state.lookup.thermostatOperatingState.getAt(value)
		} else {
			return state.lookup.thermostatOperatingState.find{it.value==value}?.key
		}	
	}
}

/* for SmartDevice to call */
// Refresh data
def refresh() {
	state.polling = [ 
		last: now(),
		runNow: true
	]
	
	//update device to state data
	def updated = updateDeviceData()
	
	log.debug "state data: " + state.data
	log.debug "state lookup: " + state.lookup
	log.debug "state list: " + state.list
    
	//force devices to poll to get the latest status
	if (updated) { 
		// get all the children and send updates
		def childDevice = getAllChildDevices()
		childDevice.each { 
			//log.debug "Polling " + it.deviceNetworkId
			//it.poll()
			it.updateThermostatData(state.data[it.deviceNetworkId])
		}
	}
}

// Get Device Gateway SN
def getDeviceGatewaySN(childDevice) { return childDevice.deviceNetworkId.split("\\|")[1] }

// Get Device Zone
def getDeviceZone(childDevice) { return childDevice.deviceNetworkId.split("\\|")[2] }

// Get single device status
def getDeviceStatus(childDevice) { return state.data[childDevice.deviceNetworkId] }

// Send thermostat
def setThermostat(childDevice, thermostatData = []) {
	thermostatData.each { key, value -> 
		if (key=="coolingSetpoint") { state.data[childDevice.deviceNetworkId].coolingSetpoint = value }
		if (key=="heatingSetpoint") { state.data[childDevice.deviceNetworkId].heatingSetpoint = value }
		if (key=="thermostatFanMode") { state.data[childDevice.deviceNetworkId].thermostatFanMode = value }
		if (key=="thermostatMode") { state.data[childDevice.deviceNetworkId].thermostatMode = value }
	}
		
	// set up final parameters
	def apiParams = [ 
		uri: "https://" + settings.username + ":" + settings.password + "@services.myicomfort.com",
		path: "/DBAcessService.svc/SetTStatInfo",
		contentType: "application/x-www-form-urlencoded",
		requestContentType: "application/json; charset=utf-8",
		body: [
			Cool_Set_Point: state.data[childDevice.deviceNetworkId].coolingSetpoint,
			Heat_Set_Point: state.data[childDevice.deviceNetworkId].heatingSetpoint,
			Fan_Mode: lookupInfo("thermostatFanMode",state.data[childDevice.deviceNetworkId].thermostatFanMode,false),
			Operation_Mode: lookupInfo("thermostatMode",state.data[childDevice.deviceNetworkId].thermostatMode,false),
			Pref_Temp_Units: (getTemperatureUnit()=="F")?0:1,
			Zone_Number: getDeviceZone(childDevice),
			GatewaySN: getDeviceGatewaySN(childDevice) 
        	]
	]
    
	try {
		httpPut(apiParams) 
	} catch (Error e)	{
		log.debug "API Error: $e"
	}
	
	return true
}

// Set program
def setProgram(childDevice, scheduleMode, scheduleSelection) {
	//Retrieve program info
	if (scheduleMode == "1") {
		apiGet("/DBAcessService.svc/GetProgramInfo", [GatewaySN: getDeviceGatewaySN(childDevice), ScheduleNum: scheduleSelection, TempUnit: (getTemperatureUnit()=="F")?0:1]) { response ->
			if (response.status == 200) {
				state.data[childDevice.deviceNetworkId].coolingSetpoint = response.data.Cool_Set_Point
				state.data[childDevice.deviceNetworkId].heatingSetpoint = response.data.Heat_Set_Point
				state.data[childDevice.deviceNetworkId].thermostatFanMode = lookupInfo("thermostatFanMode",response.data.Fan_Mode,true)
			}
		}
		state.data[childDevice.deviceNetworkId].thermostatProgramSelection = scheduleSelection
	}
	state.data[childDevice.deviceNetworkId].thermostatProgramMode = scheduleMode
		
	// set up final parameters for program
	def apiParamsProgram = [ 
		uri: "https://" + settings.username + ":" + settings.password + "@services.myicomfort.com",
		path: "/DBAcessService.svc/SetProgramInfoNew",
		contentType: "application/x-www-form-urlencoded",
		requestContentType: "application/json; charset=utf-8",
		body: [
			Cool_Set_Point: state.data[childDevice.deviceNetworkId].coolingSetpoint,
			Heat_Set_Point: state.data[childDevice.deviceNetworkId].heatingSetpoint,
			Fan_Mode: lookupInfo("thermostatFanMode",state.data[childDevice.deviceNetworkId].thermostatFanMode,false),
			Operation_Mode: lookupInfo("thermostatMode",state.data[childDevice.deviceNetworkId].thermostatMode,false),
			Pref_Temp_Units: (getTemperatureUnit()=="F")?0:1,
			Program_Schedule_Mode: scheduleMode,
			Program_Schedule_Selection: scheduleSelection,
			Zone_Number: getDeviceZone(childDevice),
			GatewaySN: getDeviceGatewaySN(childDevice) 
		]
	]
	
	// set up final parameters for thermostat
	def apiParamsThermostat = [ 
		uri: "https://" + settings.username + ":" + settings.password + "@services.myicomfort.com",
		path: "/DBAcessService.svc/SetTStatInfo",
		contentType: "application/x-www-form-urlencoded",
		requestContentType: "application/json; charset=utf-8",
		body: [
			Cool_Set_Point: state.data[childDevice.deviceNetworkId].coolingSetpoint,
			Heat_Set_Point: state.data[childDevice.deviceNetworkId].heatingSetpoint,
			Fan_Mode: lookupInfo("thermostatFanMode",state.data[childDevice.deviceNetworkId].thermostatFanMode,false),
			Operation_Mode: lookupInfo("thermostatMode",state.data[childDevice.deviceNetworkId].thermostatMode,false),
			Pref_Temp_Units: (getTemperatureUnit()=="F")?0:1,
			Zone_Number: getDeviceZone(childDevice),
			GatewaySN: getDeviceGatewaySN(childDevice) 
		]
	]
        
	try {
    	httpPut(apiParamsProgram) 
		httpPut(apiParamsThermostat) 
	} catch (Error e)	{
		log.debug "API Error: $e"
	}
	
	//refresh()
	return true
}


def translateDesc(value) {
	switch (value) {
		case "cool only"     : return "cool"
		case "heat only"     : return "heat"
		case "heat or cool"  : return "auto"
		default: return value
	}
}

def getTemperatureUnit() {
	return (settings["temperatureUnit"])?settings["temperatureUnit"]:"F"
}

def getThermostatProgramName(childDevice, thermostatProgramSelection) {
	def thermostatProgramSelectionName = state?.lookup?.program[childDevice.deviceNetworkId]?.getAt(thermostatProgramSelection.toString())
	return thermostatProgramSelectionName?thermostatProgramSelectionName:"Unknown"
}

def getThermostatProgramNext(childDevice, value) {
	def sizeProgramIndex = state.list.program[childDevice.deviceNetworkId].size() - 1
	def currentProgramIndex = (state?.list?.program[childDevice.deviceNetworkId]?.findIndexOf { it == value })?state?.list?.program[childDevice.deviceNetworkId]?.findIndexOf { it == value } : 0
	def nextProgramIndex = ((currentProgramIndex + 1) <= sizeProgramIndex)? (currentProgramIndex + 1) : 0
	def nextProgramName = state?.list?.program[childDevice.deviceNetworkId]?.getAt(nextProgramIndex)
	return state?.lookup?.program[childDevice.deviceNetworkId]?.find{it.value==nextProgramName}?.key
}

def getSetPointLimit( childDevice, limitType ) { return  state?.lookup?.getAt(limitType)?.getAt(childDevice.deviceNetworkId) }

def convertToUnit(value, unit) {
	def returnValue
	if (unit == "F") {
		returnValue = new BigDecimal(state?.lookup?.temperatureRangeC[value.toString()])
	}
    if (unit == "C") {
		returnValue = new BigDecimal(state?.lookup?.temperatureRangeF[value.toString()])
	}
	return returnValue
}
