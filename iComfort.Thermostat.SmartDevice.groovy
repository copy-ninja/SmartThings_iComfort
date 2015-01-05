metadata {
	definition (name: "iComfort Thermostat", namespace: "copy-ninja", author: "Jason Mok") {
		capability "Thermostat"
		capability "Relative Humidity Measurement"
		capability "Polling"
		capability "Refresh"
	        
		attribute "thermostatProgram", "string"
	        
		command "heatLevelUp"
		command "heatLevelDown"
		command "coolLevelUp"
		command "coolLevelDown"
		command "switchMode"
		command "switchFanMode"
		command "switchProgram"
	}

	simulator { }

	tiles {
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°', unit: "F", 
				backgroundColors:[
					[value: 31, unit: "F", color: "#153591"],
					[value: 44, unit: "F", color: "#1e9cbb"],
					[value: 59, unit: "F", color: "#90d2a7"],
					[value: 74, unit: "F", color: "#44b621"],
					[value: 84, unit: "F", color: "#f1d801"],
					[value: 95, unit: "F", color: "#d04e00"],
					[value: 96, unit: "F", color: "#bc2323"],
					[value:  0, unit: "C", color: "#153591"],
					[value:  6, unit: "C", color: "#1e9cbb"],
					[value: 15, unit: "C", color: "#90d2a7"],
					[value: 23, unit: "C", color: "#44b621"],
					[value: 29, unit: "C", color: "#f1d801"],
					[value: 35, unit: "C", color: "#d04e00"],
					[value: 37, unit: "C", color: "#bc2323"]
				]
			)
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false) {
			state("humidity", label:'${currentValue}% Humidity' , unit: "Humidity",
				backgroundColors:[
					[value: 20, unit: "Humidity", color: "#b2d3f9"],
					[value: 30, unit: "Humidity", color: "#99c5f8"],
					[value: 35, unit: "Humidity", color: "#7fb6f6"],
					[value: 40, unit: "Humidity", color: "#66a8f4"],
					[value: 45, unit: "Humidity", color: "#4c99f3"],
					[value: 50, unit: "Humidity", color: "#328bf1"],
					[value: 55, unit: "Humidity", color: "#197cef"],
					[value: 60, unit: "Humidity", color: "#006eee"],
					[value: 70, unit: "Humidity", color: "#0063d6"],
				]
			)
		}
		standardTile("thermostatOperatingState", "device.thermostatOperatingState", canChangeIcon: false) {
			state("idle",            icon: "st.thermostat.ac.air-conditioning", label: "Idle")
			state("waiting",         icon: "st.thermostat.ac.air-conditioning", label: "Waiting")
			state("heating",         icon: "st.thermostat.heating")
			state("cooling",         icon: "st.thermostat.cooling")
			state("emergency heat",  icon: "st.thermostat.emergency-heat")
			
		}
		standardTile("thermostatMode", "device.thermostatMode", canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state("auto",      action:"switchMode",    nextState: "auto",      icon: "st.thermostat.auto")
			state("heat",      action:"switchMode",    nextState: "heat",      icon: "st.thermostat.heat")
			state("cool",      action:"switchMode",    nextState: "cool",      icon: "st.thermostat.cool")
			state("off",       action:"switchMode",    nextState: "off",       icon: "st.thermostat.heating-cooling-off")
			state("emergency heat",       action:"switchMode",    nextState: "emergency heat",       icon: "st.thermostat.emergency-heat")
			state("program",   action:"switchMode",    nextState: "program",   icon: "st.thermostat.ac.air-conditioning", label: "Program")
		}
		standardTile("thermostatFanMode", "device.thermostatFanMode", canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state("auto",      action:"switchFanMode", nextState: "auto",       icon: "st.thermostat.fan-auto")
			state("on",        action:"switchFanMode", nextState: "on",         icon: "st.thermostat.fan-on")
			state("circulate", action:"switchFanMode", nextState: "circulate",  icon: "st.thermostat.fan-circulate")
			state("off",       action:"switchFanMode", nextState: "off",        icon: "st.thermostat.fan-off")
		}
		standardTile("heatLevelUp", "device.switch", canChangeIcon: false, inactiveLabel: true, decoration: "flat") {
			state("heatLevelUp",   action:"heatLevelUp",   icon:"st.thermostat.thermostat-up", backgroundColor:"#F7C4BA")
		}        
		valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false) {
			state("heat", label:'${currentValue}° heat', backgroundColors:[
					[value: 40, unit: "F", color: "#f49b88"],
					[value: 50, unit: "F", color: "#f28770"],
					[value: 60, unit: "F", color: "#f07358"],
					[value: 70, unit: "F", color: "#ee5f40"],
					[value: 80, unit: "F", color: "#ec4b28"],
					[value: 90, unit: "F", color: "#ea3811"],
					[value:  5, unit: "C", color: "#f49b88"],
					[value: 10, unit: "C", color: "#f28770"],
					[value: 15, unit: "C", color: "#f07358"],
					[value: 20, unit: "C", color: "#ee5f40"],
					[value: 25, unit: "C", color: "#ec4b28"],
					[value: 30, unit: "C", color: "#ea3811"]
				])
		}
		standardTile("heatLevelDown", "device.switch", canChangeIcon: false, inactiveLabel: true, decoration: "flat") {
			state("heatLevelDown", action:"heatLevelDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#F7C4BA")
		}        
		standardTile("coolLevelUp", "device.switch", canChangeIcon: false, inactiveLabel: true, decoration: "flat") {
			state("coolLevelUp",   action:"coolLevelUp",   icon:"st.thermostat.thermostat-up" , backgroundColor:"#BAEDF7")
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false) {
			state("cool", label:'${currentValue}° cool', backgroundColors:[
					[value: 40, unit: "F", color: "#88e1f4"],
					[value: 50, unit: "F", color: "#70dbf2"],
					[value: 60, unit: "F", color: "#58d5f0"],
					[value: 70, unit: "F", color: "#40cfee"],
					[value: 80, unit: "F", color: "#28c9ec"],
					[value: 90, unit: "F", color: "#11c3ea"],
					[value:  5, unit: "C", color: "#88e1f4"],
					[value: 10, unit: "C", color: "#70dbf2"],
					[value: 15, unit: "C", color: "#58d5f0"],
					[value: 20, unit: "C", color: "#40cfee"],
					[value: 25, unit: "C", color: "#28c9ec"],
					[value: 30, unit: "C", color: "#11c3ea"]
				])
		}
		standardTile("coolLevelDown", "device.switch", canChangeIcon: false, inactiveLabel: true, decoration: "flat") {
			state("coolLevelDown", action:"coolLevelDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#BAEDF7")
		}
		valueTile("thermostatProgram", "device.thermostatProgram", inactiveLabel: false, decoration: "flat") {
			state("program", action:"switchProgram", label: '${currentValue}')
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state("default", action:"refresh.refresh",        icon:"st.secondary.refresh")
		}
		main "temperature"
		details(["temperature", "humidity", "thermostatOperatingState",  "heatLevelUp", "coolLevelUp", "thermostatFanMode", "heatingSetpoint", "coolingSetpoint", "thermostatMode", "heatLevelDown", "coolLevelDown", "thermostatProgram", "refresh" ])
	}
}

def installed() { initialize() }

def updated() { initialize() }

def initialize() {
	state.polling = [ 
		last: 0,
		runNow: true
	]
	state.thermostatProgramMode = parent.getThermostatProgramMode(this)
	state.thermostatProgramSelection = parent.getThermostatProgramSelection(this)
	
	refresh() 
}

def parse(String description) { }

def refresh() { 
	state.polling.runNow = true
	parent.refresh() 
}

def poll() { 
	updateThermostatData(parent.getDeviceStatus(this))
}

def updateThermostatData(thermostatData) {
	//update the state data
	def next = (state.polling.last?:0) + 15000  //will not update if it's not more than 15 seconds. this is to stop polling from updating all the state data.
	//if (true) {
	if ((now() > next) || (state.polling.runNow)) {
		state.polling.last = now()
		state.polling.runNow = false
		
		thermostatData.each { name, value -> 
			log.debug "Sending Event: " + name + " Value: " + value
			if (name == "temperature" || name == "coolingSetpoint" || name == "heatingSetpoint") {
				sendEvent(name: name, value: value, unit: parent.getTemperatureUnit())
			} else if (name == "humidity") {
				sendEvent(name: name, value: value, unit: "Humidity")
			} else if (name == "thermostatProgramMode") {
				state.thermostatProgramMode = value
			} else if (name == "thermostatProgramSelection") {
				state.thermostatProgramSelection = value
			} else if (name == "thermostatMode") {
				state.thermostatMode = value
			} else {
				sendEvent(name: name, value: value)
			}
			
		}
		
		if (state.thermostatProgramMode == "0") {
			sendEvent(name: "thermostatProgram", value: "Manual")
			sendEvent(name: "thermostatMode", value: state.thermostatMode)
		} else {
			sendEvent(name: "thermostatMode", value: "program") 
			sendEvent(name: "thermostatProgram", value: "Program" + (state.thermostatProgramSelection + 1).toString())
		}
	}
}

def setHeatingSetpoint(Number heatingSetpoint) {
	// define maximum & minimum for heating setpoint 
    def minHeat = (parent.getTemperatureUnit()=="C")? 4.50 : 40.00
    def maxHeat = (parent.getTemperatureUnit()=="C")? 32.00 : 90.00
    def diffHeat = (parent.getTemperatureUnit()=="C")? 1.50 : 3.00
	heatingSetpoint = (heatingSetpoint < minHeat)? minHeat : heatingSetpoint
	heatingSetpoint = (heatingSetpoint > maxHeat)? maxHeat : heatingSetpoint
	
	
	// check cooling setpoint 
	def heatSetpointDiff = heatingSetpoint + diffHeat
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	coolingSetpoint = (heatSetpointDiff > coolingSetpoint)? heatSetpointDiff : coolingSetpoint
	
	def thermostatData = [ 
		coolingSetpoint: coolingSetpoint, 
		heatingSetpoint: heatingSetpoint
	]
    
	setThermostatData(thermostatData)
}

def setCoolingSetpoint(Number coolingSetpoint) { 
	// define maximum & minimum for cooling setpoint 
    def minCool = (parent.getTemperatureUnit()=="C")? 15.50 : 60.00
    def maxCool = (parent.getTemperatureUnit()=="C")? 37.00 : 99.00
    def diffHeat = (parent.getTemperatureUnit()=="C")? 1.50 : 3.00
	coolingSetpoint = (coolingSetpoint < minCool)? minCool : coolingSetpoint
	coolingSetpoint = (coolingSetpoint > maxCool)? maxCool : coolingSetpoint
	
	// check heating setpoint 
	def coolSetpointDiff = coolingSetpoint - diffHeat
	def heatingSetpoint = device.currentValue("heatingSetpoint").toInteger()
	heatingSetpoint = (coolSetpointDiff < heatingSetpoint)? coolSetpointDiff : heatingSetpoint
	
	def thermostatData = [ 
		coolingSetpoint: coolingSetpoint, 
		heatingSetpoint: heatingSetpoint
	]
	
	setThermostatData(thermostatData)
}


def switchMode() {
	log.debug "in switchMode"
	def currentMode = device.currentState("thermostatMode")?.value
	def returnCommand

	switch (currentMode) {
		case "off":
			setThermostatMode("heat")
			break
		case "heat":
			setThermostatMode("cool")
			break
		case "cool":
			setThermostatMode("auto")
			break
		case "auto":
			setThermostatMode("off")
			break
		default:
			setThermostatMode("auto")
	}
	if(!currentMode) { setThermostatMode("auto") }
}

def off() { setThermostatMode("off") }
def heat() { setThermostatMode("heat") }
def emergencyHeat() { setThermostatMode("emergency heat") }
def cool() { setThermostatMode("cool") }
def auto() { setThermostatMode("auto") }

def setThermostatMode(mode) {
	def thermostatData = [ thermostatMode: mode ]
	setThermostatData(thermostatData)
}


def switchFanMode() {
	def currentFanMode = device.currentState("thermostatFanMode")?.value
	log.debug "switching fan from current mode: $currentFanMode"
	def returnCommand

	switch (currentFanMode) {
		case "auto":
			setThermostatFanMode("on")
			break
		case "on":
			setThermostatFanMode("circulate")
			break
		case "circulate":
			setThermostatFanMode("auto")
			break
		default:
			setThermostatFanMode("auto")
	}
	if(!currentFanMode) { setThermostatFanMode("auto") }
}

def fanOn() { setThermostatFanMode("on") }
def fanAuto() { setThermostatFanMode("auto") }
def fanCirculate() { setThermostatFanMode("circulate") }

def setThermostatFanMode(fanMode) {
	def thermostatData = [ thermostatFanMode: fanMode ]
	setThermostatData(thermostatData)
}

def heatLevelUp() {
	def setpoint = device.currentValue("heatingSetpoint") + stepLevel()
	//log.debug "Setpoint: " + setpoint
	setHeatingSetpoint(setpoint.toInteger())    
}
def heatLevelDown() {
	def setpoint = device.currentValue("heatingSetpoint") - stepLevel() 
	//log.debug "Setpoint: " + setpoint
	setHeatingSetpoint(setpoint.toInteger())    
}
def coolLevelUp() {
	def setpoint = device.currentValue("coolingSetpoint") + stepLevel()
	//log.debug "Setpoint: " + setpoint 
	setCoolingSetpoint(setpoint.toInteger())
}

def coolLevelDown() {
	def setpoint = device.currentValue("coolingSetpoint") - stepLevel()
	//log.debug "Setpoint: " + setpoint
	setCoolingSetpoint(setpoint.toInteger())
}

def stepLevel() {
	return (parent.getTemperatureUnit()=="C")? 0.5 : 1
}

def switchProgram() {
}

def setThermostatData(thermostatData) {
	state.polling.runNow = true
	log.debug "ThermostatData: " + thermostatData
	updateThermostatData(thermostatData)
	parent.sendCommand(this, thermostatData)
}
