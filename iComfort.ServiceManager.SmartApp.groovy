/**
 *  iComfort Service Manager SmartApp
 * 
 *  Author: Jason Mok
 *  Date: 2015-01-01
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
  iconUrl:   "http://smartthings.copyninja.net/icons/Lennox_iComfort@1x.png",
  iconX2Url: "http://smartthings.copyninja.net/icons/Lennox_iComfort@2x.png",
  iconX3Url: "http://smartthings.copyninja.net/icons/Lennox_iComfort@3x.png"
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
		section("Connectivity"){
			input(name: "polling", title: "Server Polling (in Minutes)", type: "int", description: "in minutes", defaultValue: "5" )
		}              
  }
}

def prefListDevice() {
	if (forceLogin()) {
		def thermostatList = getThermostatList()
		return dynamicPage(name: "prefListDevice",  title: "Thermostats", install:true, uninstall:true) {
			if (thermostatList) {
				section("Select which thermostat to use"){
					input(name: "thermostat", type: "enum", required:false, multiple:true, metadata:[values:thermostatList])
				}
			} else {
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
def installed() {
	initialize()
    
	// Schedule polling
	unschedule()
	schedule("0 0/" + ((settings.polling.toInteger() > 0 )? settings.polling.toInteger() : 1)  + " * * * ?", refresh )
}

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
	apiGet("/DBAcessService.svc/GetSystemsInfo", [userID: settings.username]) { response ->
		if (response.status == 200) {
			response.data.Systems.each { device ->
                def dni = [ app.id, device.Gateway_SN ].join('|')
                thermostatList[dni] = device.System_Name   
			}
		}
	}    
  return thermostatList
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
    log.debug "HTTP PUT request: " + apiParams
    
	try {
		httpPut(apiParams) { response ->
        	log.debug "HTTP PUT response: " + response.data
			callback(response)
		}
	} catch (Error e)	{
		log.debug "API Error: $e"
	}
}

// Updates data for devices
def updateDeviceData() {    
	// automatically checks if the token has expired, if so login again
	if (login()) {        
		// Next polling time, defined in settings
		def next = (state.polling.last?:0) + ((settings.polling.toInteger() > 0 ? settings.polling.toInteger() : 1) * 60 * 1000)
		if ((now() > next) || (state.polling.runNow)) {
			// set polling states
			state.polling.last = now()
			state.polling.runNow = false
			
			// update data for child devices
            updateDeviceChildDate()
		}
	}
    return true
}

// update child device
private updateDeviceChildDate() {
	def childDevices = getAllChildDevices()
	childDevices.each { device ->
		def childDevicesGateway = device.deviceNetworkId.split("\\|")[1]
		apiGet("/DBAcessService.svc/GetTStatInfoList", [GatewaySN: childDevicesGateway, TempUnit: "0", Cancel_Away: "-1"]) { response ->
			if (response.status == 200) {
            	
				response.data.tStatInfo.each { 
                	//log.debug "response: " + it
					state.data[device.deviceNetworkId] = [
						fanMode: lookupInfo( "fanMode", it.Fan_Mode ),
						coolingSetpoint: it.Cool_Set_Point.toInteger(),
						heatingSetpoint: it.Heat_Set_Point.toInteger(),
						humidity: it.Indoor_Humidity,
						temperature: it.Indoor_Temp,
						thermostatMode: lookupInfo( "thermostatMode", it.Operation_Mode ),
						operatingState: lookupInfo( "operatingState", it.System_Status )
                    ]
                }
            }
        } 
	}
}

// lookup value translation
def lookupInfo( name, value ) {
	if (name == "fanMode") {
		switch (value) {
			case 0 : return "auto"
			case 1 : return "on"
			case 2 : return "circulate" 
			default: return "auto"
		}
	}
	if (name == "thermostatMode") {
		switch (value) {
			case 0 : return "off"
			case 1 : return "heat"
			case 2 : return "cool" 
			case 3 : return "auto" 
			default: return "auto"
		}		
	}
	if (name == "operatingState") {
		switch (value) {
			case 0 : return "idle"
			case 1 : return "heating"
			case 2 : return "cooling" 
			case 3 : return "idle" 	  //waiting
			case 4 : return "heating" //emergency heat 
			default: return "idle"
		}		
	}
}
// lookup value translation
def lookupInfoReverse( name, value ) {
	if (name == "fanMode") {
		switch (value) {
			case "auto"       : return 0
			case "on"         : return 1
			case  "circulate" : return 2
			default: return 0
		}
	}
	if (name == "thermostatMode") {
		switch (value) {
			case "off"  : return 0
			case "heat" : return 1
			case "cool" : return 2 
			case "auto" : return 3
			default: return 3
		}		
	}
}

//Poll all the child
def pollAllChild() {
	// get all the children and send updates
	def childDevice = getAllChildDevices()
	childDevice.each { 
		log.debug "Polling " + it.deviceNetworkId
		it.poll()
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
	
	//force devices to poll to get the latest status
    if (updated) { pollAllChild() }
}

// Get Device Gateway SN
def getDeviceGatewaySN(child) {
	return child.device.deviceNetworkId.split("\\|")[1]
}

// Get single device status
def getDeviceStatus(child) {
	return state.data[child.device.deviceNetworkId]
}

// Send command to start or stop
def sendCommand(child, thermostatValue = []) {
	
	def apiBody = [ 
    	Cool_Set_Point: thermostatValue.coolingSetpoint,
        Heat_Set_Point: thermostatValue.heatingSetpoint,
        Fan_Mode: lookupInfoReverse("fanMode",thermostatValue.fanMode),
        Operation_Mode: lookupInfoReverse("thermostatMode",thermostatValue.thermostatMode),
        Pref_Temp_Units: 0,
        Zone_Number: 0,
		GatewaySN: getDeviceGatewaySN(child) 
	]    
	
	//Send command
	apiPut("/DBAcessService.svc/SetTStatInfo", [], apiBody) 	
	
	return true
}
