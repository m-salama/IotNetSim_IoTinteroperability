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
 * Class
 * 
 * @author m.salama
 * 
 */

public class Vehicle extends IoTNode  implements IoTNodeMobile {

	private Location currentLocation;	
	
	//vehicleType enum attribute to specify the type of the vehicle, either fuel, electric or hybrid 
	private VehicleType vehicleType;
	
	private double avgSpeed;			//in unit mile/hour
	private double fuelConsumptionRate;		//in unit miles-per-gallon (mpg) 

	
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
			VehicleType vType, double avg_speed, double fuel_consumption_rate) {
		
		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub
		
		this.currentLocation = location;
		this.vehicleType = vType;
		this.avgSpeed = avg_speed;
		this.fuelConsumptionRate = fuel_consumption_rate;
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");				
		
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
		case CloudSimTags.IOV_VEHICLE_MOVE:
			processMoveVehicle();
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}
	
	private void processMoveVehicle() {
		//calculate the new location based on the average speed of the vehicle
		this.currentLocation.setX(this.currentLocation.getX() + avgSpeed); 
	}

	private void scheduleVehicleMove() {
		// schedule the next event for the vehicle move
		schedule(this.getId(), CloudSim.getMinTimeBetweenEvents()*60*60, CloudSimTags.IOV_VEHICLE_MOVE);
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
	
}
