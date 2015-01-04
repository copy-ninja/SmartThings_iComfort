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
			state("temperature", label:'${currentValue}°',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
			)
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false) {
			state "humidity", label:'${currentValue}% humidity', unit:""
		}
		standardTile("operatingState", "device.thermostatOperatingState", canChangeIcon: false, decoration: "flat") {
			state("idle",            icon: "st.thermostat.ac.air-conditioning", label: "Idle")
			state("waiting",         icon: "st.thermostat.ac.air-conditioning", label: "Waiting")
			state("heating",         icon: "st.thermostat.heat")
			state("cooling",         icon: "st.thermostat.cool")
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
		standardTile("heatLevelUp", "device.switch", canChangeIcon: false, inactiveLabel: true ) {
			state("heatLevelUp",   action:"heatLevelUp",   icon:"st.thermostat.thermostat-up", backgroundColor:"#F7C4BA")
		}        
		valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false) {
			state("heat", label:'${currentValue}° heat', backgroundColor:"#F7C4BA")
		}
		standardTile("heatLevelDown", "device.switch", canChangeIcon: false, inactiveLabel: true) {
			state("heatLevelDown", action:"heatLevelDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#F7C4BA")
		}        
		standardTile("coolLevelUp", "device.switch", canChangeIcon: false, inactiveLabel: true ) {
			state("coolLevelUp",   action:"coolLevelUp",   icon:"st.thermostat.thermostat-up" , backgroundColor:"#BAEDF7")
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false) {
			state("cool", label:'${currentValue}° cool', backgroundColor:"#BAEDF7")
		}
		standardTile("coolLevelDown", "device.switch", canChangeIcon: false, inactiveLabel: true) {
			state("coolLevelDown", action:"coolLevelDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#BAEDF7")
		}
		standardTile("thermostatProgram", "device.thermostatProgram", inactiveLabel: false, decoration: "flat") {
			state("manual", action:"switchProgram", icon:"st.secondary.secondary", label: "Manual")
			state("program", action:"switchProgram", icon:"st.secondary.secondary", label: '${currentValue}')
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state("default", action:"refresh.refresh",        icon:"st.secondary.refresh")
		}
		main "temperature"
		details(["temperature", "humidity", "operatingState",  "heatLevelUp", "coolLevelUp", "thermostatFanMode", "heatingSetpoint", "coolingSetpoint", "thermostatMode", "heatLevelDown", "coolLevelDown", "thermostatProgram", "refresh" ])
	}
}
def installed() { initialize() }

def updated() { initialize() }

def initialize() {
	state.polling = [ 
		last: 0,
		runNow: true
	]
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
			sendEvent(name: name, value: value)
		}
	}
}

def setHeatingSetpoint(Number heatingSetpoint) {
	// define maximum & minimum for heating setpoint 
	heatingSetpoint = (heatingSetpoint < 40)? 40 : heatingSetpoint
	heatingSetpoint = (heatingSetpoint > 90)? 90 : heatingSetpoint
	
	
	// check cooling setpoint 
	def heatSetpointDiff = heatingSetpoint + 3
	def coolingSetpoint = device.currentValue("coolingSetpoint").toInteger()
	coolingSetpoint = (heatSetpointDiff > coolingSetpoint)? heatSetpointDiff : coolingSetpoint
	
	def thermostatData = [ 
		coolingSetpoint: coolingSetpoint, 
		heatingSetpoint: heatingSetpoint
	]
    
	setThermostatData(thermostatData)
}

def setCoolingSetpoint(Number coolingSetpoint) { 
	// define maximum & minimum for cooling setpoint 
	coolingSetpoint = (coolingSetpoint < 60)? 60 : coolingSetpoint
	coolingSetpoint = (coolingSetpoint > 99)? 99 : coolingSetpoint
	
	// check heating setpoint 
	def coolSetpointDiff = coolingSetpoint - 3
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
	def setpoint = device.currentValue("heatingSetpoint") + 1
	//log.debug "Setpoint: " + setpoint
	setHeatingSetpoint(setpoint.toInteger())    
}
def heatLevelDown() {
	def setpoint = device.currentValue("heatingSetpoint") - 1 
	//log.debug "Setpoint: " + setpoint
	setHeatingSetpoint(setpoint.toInteger())    
}
def coolLevelUp() {
	def setpoint = device.currentValue("coolingSetpoint") + 1
	//log.debug "Setpoint: " + setpoint 
	setCoolingSetpoint(setpoint.toInteger())
}

def coolLevelDown() {
	def setpoint = device.currentValue("coolingSetpoint") - 1
	//log.debug "Setpoint: " + setpoint
	setCoolingSetpoint(setpoint.toInteger())
}

def switchProgram() {
}

def setThermostatData(thermostatData) {
	state.polling.runNow = true
	log.debug "ThermostatData: " + thermostatData
	updateThermostatData(thermostatData)
	parent.sendCommand(this, thermostatData)
}
