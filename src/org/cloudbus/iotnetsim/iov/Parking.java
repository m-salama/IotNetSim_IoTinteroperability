package org.cloudbus.iotnetsim.iov;

import java.util.Random;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.iot.nodes.IoTNode;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.network.NetConnection;

/**
 * Class Parking
 * 
 * @author Maria Salama
 * 
 */

public class Parking extends IoTNode {
	
	// total number of slots of the parking
	protected int totalParkingSlots;
	
	// number of parking slots available 
	protected int availableParkingSlots;
	
	// interval for changing parking availability every x seconds
	protected double parkingChangeInterval;			

	protected int currentExpDay;
	protected int currentChangeIndex;
	
	
	public Parking(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public Parking(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, String forwardNodeName) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
	}

	public Parking(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, String forwardNodeName,
			int total_parkingSlots, double parking_change_interval) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
		
		this.totalParkingSlots = total_parkingSlots;
		this.availableParkingSlots = total_parkingSlots;
		this.parkingChangeInterval = parking_change_interval;
		this.currentExpDay = 1;
		this.currentChangeIndex = 0;
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");

		// schedule the first event for parking
		schedule(this.getId(), this.parkingChangeInterval, CloudSimTags.IOV_PARKING_CHANGE_AVAILABILITY);
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
		// Execute sending sensor data 
		case CloudSimTags.IOV_PARKING_CHANGE_AVAILABILITY:
			processChangeParkingAvailability();
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}

	private void processChangeParkingAvailability() {
		
		int pAvailability = getNewAvailabilityRandom(); 

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is changing parking availability" 
				+ " for Day " + currentExpDay
				+ " to " + Integer.toString(pAvailability) 
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		//send data to Datacenter
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, pAvailability);

		if (currentExpDay < configurations.ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			// schedule the next event for sending data 
			scheduleNextChange();
		}
	}

	private int getNewAvailabilityRandom() {
		Random random = new Random();  		
		int newAvailability = random.nextInt(this.totalParkingSlots);  
		
		if (currentChangeIndex < (24/(this.parkingChangeInterval/60/60))-1) {		//to get all the changes for this day
			currentChangeIndex +=1;
		} else {	//reset the change index to 0 and start a new day
			currentExpDay +=1;
			currentChangeIndex = 0;
		}
		return newAvailability;
	}

	private void scheduleNextChange(){
		schedule(this.getId(), this.getParkingChangeInterval(), CloudSimTags.IOV_PARKING_CHANGE_AVAILABILITY);
	}

	/**
	 * @return the totalParkingSlots
	 */
	public int getTotalParkingSlots() {
		return totalParkingSlots;
	}

	/**
	 * @param totalParkingSlots the totalParkingSlots to set
	 */
	public void setTotalParkingSlots(int totalParkingSlots) {
		this.totalParkingSlots = totalParkingSlots;
	}

	/**
	 * @return the availableParkingSlots
	 */
	public int getAvailableParkingSlots() {
		return availableParkingSlots;
	}

	/**
	 * @param availableParkingSlots the availableParkingSlots to set
	 */
	public void setAvailableParkingSlots(int availableParkingSlots) {
		this.availableParkingSlots = availableParkingSlots;
	}

	/**
	 * @return the parkingChangeInterval
	 */
	public double getParkingChangeInterval() {
		return parkingChangeInterval;
	}

	/**
	 * @param parkingChangeInterval the parkingChangeInterval to set
	 */
	public void setParkingChangeInterval(double parkingChangeInterval) {
		this.parkingChangeInterval = parkingChangeInterval;
	}

	/**
	 * @return the currentExpDay
	 */
	public int getCurrentExpDay() {
		return currentExpDay;
	}

	/**
	 * @param currentExpDay the currentExpDay to set
	 */
	public void setCurrentExpDay(int currentExpDay) {
		this.currentExpDay = currentExpDay;
	}

	/**
	 * @return the currentChangeIndex
	 */
	public int getCurrentChangeIndex() {
		return currentChangeIndex;
	}

	/**
	 * @param currentChangeIndex the currentChangeIndex to set
	 */
	public void setCurrentChangeIndex(int currentChangeIndex) {
		this.currentChangeIndex = currentChangeIndex;
	}



}
