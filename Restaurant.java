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

import experiments.configurations.ExperimentsConfigurations;

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
	
	protected double orderPreparationTime;

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
			double opening_time, double closing_time, double order_preparation_time) {

		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub
		this.openingTime = opening_time;
		this.closingTime = closing_time;
		this.isOpen = true;
		this.orderPreparationTime = order_preparation_time;
		
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
		case CloudSimTags.IOV_RESTAURANT_CHECK_AVAILABILITY_EVENT:
			processCheckAvailability(ev);
			break;
		// confirm receiving order
		case CloudSimTags.IOV_RESTAURANT_ORDER_EVENT:
			processReceiveOrder(ev.getSource());
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
				+ " at day " + currentExpDay
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
				+ " at day " + currentExpDay
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		//send data to Datacenter
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, isOpen);

		if (currentExpDay < ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			// schedule event for restaurant opening for next day
			schedule(this.getId(), this.openingTime*(currentExpDay*24*60*60), CloudSimTags.IOV_RESTAURANT_OPEN_EVENT);		
		}
		currentExpDay +=1;
	}
	
	/**
	 * processCheckAvailability()
	 */
	private void processCheckAvailability(SimEvent ev) {
		int userID = ev.getSource();

		UserSmartPhone user = (UserSmartPhone) CloudSim.getEntity(userID);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is sending availability " + this.isOpen
				+ " to " + CloudSim.getEntityName(userID)
				);

		// send the availability to the user
		if (this.getMessagingProtocol() == user.getMessagingProtocol()) {
			schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RECEIVE_RESTAURANT_AVAILABILITY_EVENT, 
					this.isOpen);
		} else {
			schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}
	}

	/**
	 * processReceiveOrder()
	 */

	private void processReceiveOrder(int userID) {
		UserSmartPhone user = (UserSmartPhone) CloudSim.getEntity(userID);
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is receiving the order"
				+ " from User " + CloudSim.getEntityName(userID)
				+ " and sending confirmation"
				);
		
		// send confirmation to the user
		if (this.getMessagingProtocol() == user.getMessagingProtocol()) {
			schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RESTAURANT_ORDER_CONFIRMATION_EVENT);
		} else {
			schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}
		
		double preparationTime = CloudSim.getMinTimeBetweenEvents();
		// get time for the order preparation
		if (ExperimentsConfigurations.IOV_EXP_ServiceEntities_CHANGE == "Frequent") {
			preparationTime = this.orderPreparationTime;
		} else if (ExperimentsConfigurations.IOV_EXP_ServiceEntities_CHANGE == "Random") {
			// get random time within a hour
			Random random = new Random(); 
			preparationTime = random.nextDouble() * ((60*60) + CloudSim.getMinTimeBetweenEvents());
		}
				
		// send notification to the user after the preparation time
		if (this.getMessagingProtocol() == user.getMessagingProtocol()) {
			schedule(userID, preparationTime, CloudSimTags.IOV_RESTAURANT_ORDER_READY_EVENT);
		} else {
			schedule(getForwardNodeId(), preparationTime, CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}
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
	
	public double getOrderPreparationTime() {
		return orderPreparationTime;
	}

	public void setOrderPreparationTime(double orderPreparationTime) {
		this.orderPreparationTime = orderPreparationTime;
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