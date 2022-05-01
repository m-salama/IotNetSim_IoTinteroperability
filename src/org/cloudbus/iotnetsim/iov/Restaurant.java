/**
 * 
 */
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
import org.cloudbus.iotnetsim.iot.nodes.MessagingProtocol;
import org.cloudbus.iotnetsim.network.NetConnection;

/**
 * Class Restaurant
 * 
 * @author Maria Salama
 * 
 */

public class Restaurant extends IoTNode {
	
	// opening time of the restaurant set by the seconds within the day
	protected double openingTime;
	// closing time of the restaurant set by the seconds within the day
	protected double closingTime;
	
	protected boolean isOpen;

	protected int currentExpDay;


	public Restaurant(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public Restaurant(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName, MessagingProtocol msgProtocol) {

		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub
	}

	public Restaurant(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName, MessagingProtocol msgProtocol,
			double opening_time, double closing_time) {

		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub
		this.openingTime = opening_time;
		this.closingTime = closing_time;
		this.isOpen = true;
		this.currentExpDay = 1;
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");

		// connect to the datacenter
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is connecting to the Datacenter " 
				+ CloudSim.getEntityName(getForwardNodeId())
				);
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_NODE_CONNECTION_EVENT, isOpen);

		// schedule event for restaurant opening
		schedule(this.getId(), this.closingTime, CloudSimTags.IOV_RESTAURANT_CLOSE_EVENT);
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
		// send updates to the Datacenter 
		case CloudSimTags.IOV_RESTAURANT_OPEN_EVENT:
			processOpenRestaurant();
			break;
		// send updates to the Datacenter 
		case CloudSimTags.IOV_RESTAURANT_CLOSE_EVENT:
			processCloseRestaurant();
			break;
		// confirm receiving order
		case CloudSimTags.IOV_RESTAURANT_ORDER_EVENT:
			processReceiveOrder(ev.getSource());
			break;
		// confirm booking table
		case CloudSimTags.IOV_RESTAURANT_BOOKING_EVENT:
			processBookTable(ev.getSource());
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}
	
	/**
	 * processOpenRestaurant()
	 */
	private void processOpenRestaurant() {
		this.isOpen = true;

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is open"
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		//send data to Datacenter
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, isOpen);

		// schedule event for restaurant closing for later
		schedule(this.getId(), this.closingTime*(currentExpDay*24*60*60), CloudSimTags.IOV_RESTAURANT_CLOSE_EVENT);
	}
	
	/**
	 * processCloseRestaurant()
	 */
	private void processCloseRestaurant() {		
		this.isOpen = false;
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is closed"
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		//send data to Datacenter
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, isOpen);

		if (currentExpDay < configurations.ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			// schedule event for restaurant opening for next day
			schedule(this.getId(), this.openingTime*(currentExpDay*24*60*60), CloudSimTags.IOV_RESTAURANT_OPEN_EVENT);		
		}
		currentExpDay +=1;
	}
	
	/**
	 * processReceiveOrder()
	 */

	private void processReceiveOrder(int userID) {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is receiving the order"
				+ " from User " + CloudSim.getEntityName(userID)
				+ " and sending confirmation"
				);
		
		// send confirmation to the user
		schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RESTAURANT_ORDER_CONFIRMATION_EVENT);	
		
		// get a random time for the order preparation
		double preparationTime = getOrderPreparationTimeRandom();
		
		// send notification to the user after the preparation time
		schedule(userID, preparationTime, CloudSimTags.IOV_RESTAURANT_ORDER_READY_EVENT);
	}
	
	/**
	 * processBookTable()
	 */
	private void processBookTable(int userID) {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is confirming booking a table "
				+ "from User " + CloudSim.getEntityName(userID)
				);
		
		// send confirmation to the user
		schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RESTAURANT_BOOKING_CONFIRMATION_EVENT);	
	}
	

	private double getOrderPreparationTimeRandom() {
		// get random time within a hour
		Random random = new Random();  		
		return random.nextDouble() * ((60*60) + CloudSim.getMinTimeBetweenEvents());
	}

	/**
	 * @return the openingTime
	 */
	public double getOpeningTime() {
		return openingTime;
	}

	/**
	 * @param openingTime the openingTime to set
	 */
	public void setOpeningTime(double openingTime) {
		this.openingTime = openingTime;
	}

	/**
	 * @return the closingTime
	 */
	public double getClosingTime() {
		return closingTime;
	}

	/**
	 * @param closingTime the closingTime to set
	 */
	public void setClosingTime(double closingTime) {
		this.closingTime = closingTime;
	}

	/**
	 * @return the isOpen
	 */
	public boolean isOpen() {
		return isOpen;
	}

	/**
	 * @param isOpen the isOpen to set
	 */
	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
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


}
