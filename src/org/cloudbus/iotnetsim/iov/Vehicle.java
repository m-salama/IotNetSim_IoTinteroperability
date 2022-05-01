package org.cloudbus.iotnetsim.iov;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.iot.nodes.IoTNode;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeMobile;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iot.nodes.MessagingProtocol;
import org.cloudbus.iotnetsim.network.NetConnection;

/**
 * Class Vehicle represents any type of moving vehicle
 * 
 * ForwardNode for the vehicle should be UserSmartPhone
 * 
 * @author m.salama
 * 
 */

public class Vehicle extends IoTNode  implements IoTNodeMobile {

	private Location currentLocation;	
	
	//vehicleType enum attribute to specify the type of the vehicle, either fuel, electric or hybrid 
	private VehicleType vehicleType;
	
	private double fuelTankSize;			//in gallon
	private double fuelConsumptionRate;		//in unit miles-per-gallon (mpg) 
	private double fuelThreshold;			//in unit gallon, when threshold is reached, the fuel alert is on 

	private double avgSpeed;				//in unit mile/hour
	private double currentFuelLevel;		//in unit gallon, when full, set to the maxFuelCapacity
	private boolean fuelAlert;				//boolean variable set to true if the currentFuelLevel is <= the fuelThreshold

	
	public Vehicle(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public Vehicle(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName, MessagingProtocol msgProtocol) {
		
		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub
		
		this.currentLocation = location;
	}

	public Vehicle(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName, MessagingProtocol msgProtocol,
			VehicleType vType, double tank_size, double fuel_consumption_rate, double fuel_threshold, double avg_speed, double fuel_level) {
		
		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub
		
		this.currentLocation = location;
		this.setVehicleType(vType);
		this.setFuelConsumptionRate(fuel_consumption_rate);
		this.setFuelThreshold(fuel_threshold);
		this.avgSpeed = avg_speed;
		this.setCurrentFuelLevel(fuel_level);
		this.fuelAlert = (this.currentFuelLevel <= this.fuelThreshold);
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");				
		
		// establish connection between the vehicle and the UserSmartPhone when the vehicle starts
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CONNECT_VEHICLE);

		// schedule the first event for moving the vehicle
		scheduleVehicleMove();	
	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is shutting down...");		
	}

	@Override
	public void processEvent(SimEvent ev) {
		// TODO Auto-generated method stub
		switch (ev.getTag()) {
		case CloudSimTags.IOV_VEHICLE_MOVE_EVENT:
			processMoveVehicle();
			break;
		case CloudSimTags.IOV_VEHICLE_FUEL_FULL_EVENT:
			processFuelFull();
			break;
			
		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}
	
	private void processMoveVehicle() {
		// calculate the distance based on the average speed
		double distance = (this.avgSpeed / (CloudSim.getMinTimeBetweenEvents()*60*60));
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] moved the distance of " 
				+ Double.toString(distance)
			);
		
		// update fuel level
		this.currentFuelLevel = this.currentFuelLevel - (distance/this.fuelConsumptionRate);
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] updated the current fuel level to " 
				+ Double.toString(currentFuelLevel)
			);

		// calculate the new location based on the average speed of the vehicle
		this.currentLocation.setX(this.currentLocation.getX() + avgSpeed); 
		
		// moving vehicle in X axis only (for simplicity)
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] updated its location to " 
				+ Double.toString(this.currentLocation.getX())
			);
		
		// send location update to the UserSmartPhone (the forwardNode)
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_VEHICLE_LOCATION_UPDATE_EVENT, this.currentLocation);
		
		if (this.currentFuelLevel > 0) {			
			// check current fuel level 
			if (this.currentFuelLevel <= this.fuelThreshold) {
				// set the fuelAlert to true
				this.fuelAlert = true;
				Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is setting the fuel alert to true");
				
				// send the fuel alert to the UserSmartPhone (the forwardNode)
				schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_VEHICLE_FUEL_ALERT_EVENT);
			}
			// schedule the next event for moving the vehicle
			scheduleVehicleMove();	
		}			
	}

	private void processFuelFull() {
		this.currentFuelLevel = this.fuelTankSize;
		this.fuelAlert = false;
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] updated the current fuel level to the full tank size" );
	}
	
	private void scheduleVehicleMove() {
		// schedule the next event for the vehicle move after 1 hour 
		schedule(this.getId(), CloudSim.getMinTimeBetweenEvents()*60*60, CloudSimTags.IOV_VEHICLE_MOVE_EVENT);
	}
	
	
	public Location getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(Location currentLocation) {
		this.currentLocation = currentLocation;
	}

	public void changeAltitude(double newZ) {
		
	}

	/**
	 * @return the fuelTankSize
	 */
	public double getFuelTankSize() {
		return fuelTankSize;
	}

	/**
	 * @param tankSize the fuelTankSize to set
	 */
	public void setTankSize(double tankSize) {
		this.fuelTankSize = tankSize;
	}

	/**
	 * @return the vehicleType
	 */
	public VehicleType getVehicleType() {
		return vehicleType;
	}

	/**
	 * @param vehicleType the vehicleType to set
	 */
	public void setVehicleType(VehicleType vehicleType) {
		this.vehicleType = vehicleType;
	}

	/**
	 * @return the fuelConsumptionRate
	 */
	public double getFuelConsumptionRate() {
		return fuelConsumptionRate;
	}

	/**
	 * @param fuelConsumptionRate the fuelConsumptionRate to set
	 */
	public void setFuelConsumptionRate(double fuelConsumptionRate) {
		this.fuelConsumptionRate = fuelConsumptionRate;
	}

	/**
	 * @return the fuelThreshold
	 */
	public double getFuelThreshold() {
		return fuelThreshold;
	}

	/**
	 * @param fuelThreshold the fuelThreshold to set
	 */
	public void setFuelThreshold(double fuelThreshold) {
		this.fuelThreshold = fuelThreshold;
	}

	/**
	 * @return the avgSpeed
	 */
	public double getAvgSpeed() {
		return avgSpeed;
	}

	/**
	 * @param avgSpeed the avgSpeed to set
	 */
	public void setAvgSpeed(double avgSpeed) {
		this.avgSpeed = avgSpeed;
	}

	/**
	 * @return the currentFuelLevel
	 */
	public double getCurrentFuelLevel() {
		return currentFuelLevel;
	}

	/**
	 * @param currentFuelLevel the currentFuelLevel to set
	 */
	public void setCurrentFuelLevel(double currentFuelLevel) {
		this.currentFuelLevel = currentFuelLevel;
	}

	/**
	 * @return the fuelAlert
	 */
	public boolean isFuelAlert() {
		return fuelAlert;
	}

	/**
	 * @param fuelAlert the fuelAlert to set
	 */
	public void setFuelAlert(boolean fuelAlert) {
		this.fuelAlert = fuelAlert;
	}


	
}
