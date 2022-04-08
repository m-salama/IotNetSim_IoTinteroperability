/**
 * 
 */
package org.cloudbus.iotnetsim.iov;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.iot.nodes.IoTNode;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.network.NetConnection;

import experiments.configurations.*;

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
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, String forwardNodeName) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
	}

	public Restaurant(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, String forwardNodeName,
			double opening_time, double closing_time, boolean open) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
		this.openingTime = opening_time;
		this.closingTime = closing_time;
		this.isOpen = open;
		this.currentExpDay = 1;
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");

		// schedule event for restaurant opening
		schedule(this.getId(), this.openingTime, CloudSimTags.IOV_RESTAURANT_OPEN);
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
		case CloudSimTags.IOV_RESTAURANT_OPEN:
			processOpenRestaurant();
			break;
		case CloudSimTags.IOV_RESTAURANT_CLOSE:
			processCloseRestaurant();
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}
	
	private void processOpenRestaurant() {
		this.isOpen = true;

		// schedule event for restaurant closing for later
		schedule(this.getId(), this.closingTime*(currentExpDay*24*60*60), CloudSimTags.IOV_RESTAURANT_CLOSE);
	}
	
	private void processCloseRestaurant() {		
		this.isOpen = false;
		
		if (currentExpDay < ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			// schedule event for restaurant opening for next day
			schedule(this.getId(), this.openingTime*(currentExpDay*24*60*60), CloudSimTags.IOV_RESTAURANT_OPEN);		
		}
		currentExpDay +=1;
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
