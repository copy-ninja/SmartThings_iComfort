/**
 *	iComfort Thermostat SmartDevice
 *
 *	Author: Jason Mok
 *	Date: 2015-01-10
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
 *
 * REQUIREMENTS:
 * Refer to iComfort SmartApp
 *
 **************************
 * 
 * USAGE:
 * Put this in Device Type. Don't install until you have all other device types scripts added
 * Refer to iComfort SmartApp
 *
 */
 metadata {
	definition (name: "iComfort Thermostat", namespace: "copy-ninja", author: "Jason Mok") {
		capability "Thermostat"
		capability "Relative Humidity Measurement"
		capability "Polling"
		capability "Refresh"
	        
		attribute "temperatureDisplay", "string"
		attribute "heatingSetpointDisplay", "string"
		attribute "coolingSetpointDisplay", "string"
		attribute "thermostatProgram", "string"
	        
		command "heatLevelUp"
		command "heatLevelDown"
		command "coolLevelUp"
		command "coolLevelDown"
		command "switchMode"
		command "switchFanMode"
		command "switchProgram"
		command "setThermostatProgram"
	}

	simulator { }

	tiles {
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°', unit: "F")
		}
		valueTile("temperatureDisplay", "device.temperatureDisplay", width: 2, height: 2) {
			state("temperature", label:'${currentValue}', 
				backgroundColors:[
					[value: "31.0°F", color: "#153591"],
					[value: "44.0°F", color: "#1e9cbb"],
					[value: "59.0°F", color: "#90d2a7"],
					[value: "74.0°F", color: "#44b621"],
					[value: "84.0°F", color: "#f1d801"],
					[value: "95.0°F", color: "#d04e00"],
					[value: "96.0°F", color: "#bc2323"],
					[value: " 0.0°C", color: "#153591"],
					[value: " 6.0°C", color: "#1e9cbb"],
					[value: "15.0°C", color: "#90d2a7"],
					[value: "23.0°C", color: "#44b621"],
					[value: "29.0°C", color: "#f1d801"],
					[value: "35.0°C", color: "#d04e00"],
					[value: "37.0°C", color: "#bc2323"]
				]
			)
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false) {
			state("humidity", label:'${currentValue}% \nHumidity' , unit: "Humidity",
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
		standardTile("thermostatOperatingState", "device.thermostatOperatingState", canChangeIcon: false, decoration: "flat") {
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
		standardTile("heatLevelUp", "device.switch", canChangeIcon: false, inactiveLabel: true, decoration: "flat" ) {
			state("heatLevelUp",   action:"heatLevelUp",   icon:"st.thermostat.thermostat-up", backgroundColor:"#F7C4BA")
		}        
		valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false) {
			state("heat", label:'${currentValue}°')
		}
		valueTile("heatingSetpointDisplay", "device.heatingSetpointDisplay", inactiveLabel: false) {
			state("heat", label:'${currentValue}', 
				backgroundColors:[
					[value: "40.0°F", unit: "F", color: "#f49b88"],
					[value: "50.0°F", unit: "F", color: "#f28770"],
					[value: "60.0°F", unit: "F", color: "#f07358"],
					[value: "70.0°F", unit: "F", color: "#ee5f40"],
					[value: "80.0°F", unit: "F", color: "#ec4b28"],
					[value: "90.0°F", unit: "F", color: "#ea3811"],
					[value: "5.0°C",  unit: "C", color: "#f49b88"],
					[value: "10.0°C", unit: "C", color: "#f28770"],
					[value: "15.0°C", unit: "C", color: "#f07358"],
					[value: "20.0°C", unit: "C", color: "#ee5f40"],
					[value: "25.0°C", unit: "C", color: "#ec4b28"],
					[value: "30.0°C", unit: "C", color: "#ea3811"]
				])
		}
		standardTile("heatLevelDown", "device.switch", canChangeIcon: false, inactiveLabel: true, decoration: "flat") {
			state("heatLevelDown", action:"heatLevelDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#F7C4BA")
		}        
		standardTile("coolLevelUp", "device.switch", canChangeIcon: false, inactiveLabel: true, decoration: "flat" ) {
			state("coolLevelUp",   action:"coolLevelUp",   icon:"st.thermostat.thermostat-up" , backgroundColor:"#BAEDF7")
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false) {
			state("cool", label:'${currentValue}°')
		}
		valueTile("coolingSetpointDisplay", "device.coolingSetpointDisplay", inactiveLabel: false) {
			state("cool", label:'${currentValue}', 
				backgroundColors:[
					[value: "40°F", unit: "F", color: "#88e1f4"],
					[value: "50°F", unit: "F", color: "#70dbf2"],
					[value: "60°F", unit: "F", color: "#58d5f0"],
					[value: "70°F", unit: "F", color: "#40cfee"],
					[value: "80°F", unit: "F", color: "#28c9ec"],
					[value: "90°F", unit: "F", color: "#11c3ea"],
					[value:  "5°C", unit: "C", color: "#88e1f4"],
					[value: "10°C", unit: "C", color: "#70dbf2"],
					[value: "15°C", unit: "C", color: "#58d5f0"],
					[value: "20°C", unit: "C", color: "#40cfee"],
					[value: "25°C", unit: "C", color: "#28c9ec"],
					[value: "30°C", unit: "C", color: "#11c3ea"]
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
		main "temperatureDisplay"
		details(["temperatureDisplay", "humidity", "thermostatOperatingState",  "heatLevelUp", "coolLevelUp", "thermostatFanMode", "heatingSetpointDisplay", "coolingSetpointDisplay", "thermostatMode", "heatLevelDown", "coolLevelDown", "thermostatProgram", "refresh" ])
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

def poll() { updateThermostatData(parent.getDeviceStatus(this.device)) }

def updateThermostatData(thermostatData) {
	//update the state data
	def next = (state.polling.last?:0) + 15000  //will not update if it's not more than 15 seconds. this is to stop polling from updating all the state data.

	if ((now() > next) || (state.polling.runNow)) {
		state.polling.last = now()
		state.polling.runNow = false
		
		def thermostatProgramSelection
		def thermostatProgramMode = (device.currentValue("thermostatProgram") == "Manual")?"0":"1"
		def thermostatMode = (device.currentState("thermostatMode")?.value)?device.currentState("thermostatMode")?.value:"auto"
		
		thermostatData.each { name, value -> 
			if (name == "temperature" || name == "coolingSetpoint" || name == "heatingSetpoint") {
				def displayValue  = String.format("%.1f", (Math.round(value * 20) / 20)) + "°" + parent.getTemperatureUnit()
				def displayName = name + "Display"
				sendEvent(name: displayName, value: displayValue as String)
				log.debug "Sending Event: " + [displayName, displayValue]
				sendEvent(name: name, value: value , unit: parent.getTemperatureUnit())
				log.debug "Sending Event: " + [name, value, parent.getTemperatureUnit()]
			} else if (name == "humidity") {
				sendEvent(name: name, value: value, unit: "Humidity")
				log.debug "Sending Event: " + [name, value, "Humidity"]
			} else if (name == "thermostatProgramMode") {
				thermostatProgramMode = value
			} else if (name == "thermostatProgramSelection") {
				thermostatProgramSelection = value
			} else if (name == "thermostatMode") {
				thermostatMode = value
			} else {
				sendEvent(name: name, value: value)
				log.debug "Sending Misc Event: " + [name, value]
			}
		}
        		
		if (thermostatProgramMode == "0") {
			sendEvent(name: "thermostatMode", value: thermostatMode)
			sendEvent(name: "thermostatProgram", value: "Manual")
			log.debug "Sending Event: " + ["thermostatMode", thermostatMode]
			log.debug "Sending Event: " + ["thermostatProgram", "Manual"]			
		} else {
			sendEvent(name: "thermostatMode", value: "program") 
			log.debug "Sending Event: " + ["thermostatMode", "program"]
			if (thermostatProgramSelection) {
				sendEvent(name: "thermostatProgram", value: parent.getThermostatProgramName(this.device, thermostatProgramSelection))
				log.debug "Sending Event: " + ["thermostatProgram", parent.getThermostatProgramName(this.device, thermostatProgramSelection)]
			}
		}
	}
}

def setHeatingSetpoint(Number heatingSetpoint) {
	// define maximum & minimum for heating setpoint 
	def minHeat = parent.getSetPointLimit(this.device, "heatingSetPointLow")
	def maxHeat = parent.getSetPointLimit(this.device, "heatingSetPointHigh")
	def diffHeat = parent.getSetPointLimit(this.device, "differenceSetPoint")
	
	def temperatureUnit = parent.getTemperatureUnit()
	if (temperatureUnit == "C") { heatingSetpoint = parent.convertToUnit(heatingSetpoint, "F") }
    
	heatingSetpoint = (heatingSetpoint < minHeat)? minHeat : heatingSetpoint
	heatingSetpoint = (heatingSetpoint > maxHeat)? maxHeat : heatingSetpoint

	// check cooling setpoint 
	def heatSetpointDiff = heatingSetpoint + diffHeat
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	if (temperatureUnit == "C") { coolingSetpoint = parent.convertToUnit(coolingSetpoint, "F") }
	coolingSetpoint = (heatSetpointDiff > coolingSetpoint)? heatSetpointDiff : coolingSetpoint
	
	if (temperatureUnit == "C") { 
		coolingSetpoint = parent.convertToUnit(coolingSetpoint, "C") 
		heatingSetpoint = parent.convertToUnit(heatingSetpoint, "C")
	}
    
	setThermostatData([coolingSetpoint: coolingSetpoint, heatingSetpoint: heatingSetpoint])
}

def setCoolingSetpoint(Number coolingSetpoint) { 
	// define maximum & minimum for cooling setpoint 
	def minCool = parent.getSetPointLimit(this.device, "coolingSetPointLow")
	def maxCool = parent.getSetPointLimit(this.device, "coolingSetPointHigh")
	def diffHeat = parent.getSetPointLimit(this.device, "differenceSetPoint")

	def temperatureUnit = parent.getTemperatureUnit()
	if (temperatureUnit == "C") { coolingSetpoint = parent.convertToUnit(coolingSetpoint, "F") }

	coolingSetpoint = (coolingSetpoint < minCool)? minCool : coolingSetpoint
	coolingSetpoint = (coolingSetpoint > maxCool)? maxCool : coolingSetpoint
	
	// check heating setpoint 
	def coolSetpointDiff = coolingSetpoint - diffHeat
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	if (temperatureUnit == "C") { heatingSetpoint = parent.convertToUnit(heatingSetpoint, "F") }
	heatingSetpoint = (coolSetpointDiff < heatingSetpoint)? coolSetpointDiff : heatingSetpoint
	
	if (temperatureUnit == "C") { 
		coolingSetpoint = parent.convertToUnit(coolingSetpoint, "C") 
		heatingSetpoint = parent.convertToUnit(heatingSetpoint, "C")
	}
    
	setThermostatData([coolingSetpoint: coolingSetpoint, heatingSetpoint: heatingSetpoint])
}


def switchMode() {
	def currentMode = device.currentState("thermostatMode")?.value
	//log.debug "currentMode: " + currentMode
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
		case "program":
			setThermostatMode("auto")
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

def switchFanMode() {
	def currentFanMode = device.currentState("thermostatFanMode")?.value
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

def setThermostatMode(mode) { 
	def thermostatProgramMode = (device.currentValue("thermostatProgram") == "Manual")?"0":"1"
	if (thermostatProgramMode != "0") {
		parent.setProgram(this.device, "0", state.thermostatProgramSelection)
		setThermostatData([ thermostatProgramMode: "0", thermostatMode: mode ])
	} else {
		setThermostatData([ thermostatMode: mode ])
	}
}

def fanOn() { setThermostatFanMode("on") }
def fanAuto() { setThermostatFanMode("auto") }
def fanCirculate() { setThermostatFanMode("circulate") }
def setThermostatFanMode(fanMode) {	setThermostatData([ thermostatFanMode: fanMode ]) }
def heatLevelUp() {	
	def heatingSetpoint = device.currentValue("coolingSetpoint")
	if (parent.getTemperatureUnit() == "C") { 
		def newHeatingSetpoint = parent.convertToUnit(heatingSetpoint, "F") + 1
		heatingSetpoint = parent.convertToUnit(newHeatingSetpoint, "C")
	} else { 
		heatingSetpoint += 1 
	}
	setHeatingSetpoint(heatingSetpoint)  
}
def heatLevelDown() { 
	def heatingSetpoint = device.currentValue("coolingSetpoint")
	if (parent.getTemperatureUnit() == "C") { 
		def newHeatingSetpoint = parent.convertToUnit(heatingSetpoint, "F") - 1
		heatingSetpoint = parent.convertToUnit(newHeatingSetpoint, "C")
	} else { 
		heatingSetpoint -= 1 
	}
	setHeatingSetpoint(heatingSetpoint)  
}
def coolLevelUp() { 
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	if (parent.getTemperatureUnit() == "C") { 
		def newCoolingSetpoint = parent.convertToUnit(coolingSetpoint, "F") + 1
		coolingSetpoint = parent.convertToUnit(newCoolingSetpoint, "C")
	} else { 
		coolingSetpoint += 1 
	}
	setCoolingSetpoint(coolingSetpoint)  
}
def coolLevelDown() { 
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	if (parent.getTemperatureUnit() == "C") { 
		def newCoolingSetpoint = parent.convertToUnit(coolingSetpoint, "F") - 1
		coolingSetpoint = parent.convertToUnit(newCoolingSetpoint, "C")
	} else { 
		coolingSetpoint -= 1 
	}
	setCoolingSetpoint(coolingSetpoint)  
}

def switchProgram() {
	def currentProgram = device.currentValue("thermostatProgram")
	def nextProgramID = parent.getThermostatProgramNext(this.device, currentProgram)
	setThermostatProgram(nextProgramID)
}

def setThermostatProgram(programID) {
	state.polling.runNow = true
	parent.setProgram(this.device, "1", programID)
	updateThermostatData([thermostatProgramMode: "1", thermostatProgramSelection: programID])
}

def setThermostatData(thermostatData) {
	state.polling.runNow = true
	log.debug "ThermostatData: " + thermostatData
	parent.setThermostat(this.device, thermostatData)
	updateThermostatData(thermostatData)
}
