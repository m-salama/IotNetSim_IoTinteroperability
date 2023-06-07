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
 * Class Station
 * for service station of fuel and electric cars based on the VehicleType
 * 
 * @author Maria Salama
 * 
 */

public class PetrolStation extends IoTNode  {

	private boolean isAvailable;
	private double price;		//cost per unit (e.g. per litre of fuel)
	protected double priceChangeInterval;	// interval for changing price every x seconds		

	protected int currentExpDay;


	public PetrolStation(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public PetrolStation(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName, MessagingProtocol msgProtocol) {

		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub
	}

	public PetrolStation(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName, MessagingProtocol msgProtocol,
			double price, double price_change_interval) {

		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub

		this.isAvailable = true;
		this.price = price;
		this.priceChangeInterval = price_change_interval;

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
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_NODE_CONNECTION_EVENT, isAvailable);

		// schedule the first event for the station to change the price
		schedule(this.getId(), this.priceChangeInterval, CloudSimTags.IOV_PETROLSTATION_CHANGE_PRICE_EVENT);
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

		case CloudSimTags.IOV_PETROLSTATION_CHANGE_PRICE_EVENT:
			processChangePrice();
			break;
		case CloudSimTags.IOV_PETROLSTATION_CHECK_PRICE_EVENT:
			processCheckPrice(ev);
			break;
			
			// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}

	/**
	 * processChangePrice()
	 */
	private void processChangePrice() {
		// get random price within a range -- min + (randomValue * (max - min))
		double random = new Random().nextDouble();  		
		double rPrice = Math.abs((this.price-1.0) + (random * ((this.price+1.0) - (this.price-1.0))));

		this.price = rPrice;

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is changing the price" 
				+ " of day " + currentExpDay
				+ " to " + Double.toString(this.price) 
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		if (CloudSim.clock() >= currentExpDay*24*60*60) {
			currentExpDay +=1;
		}

		//schedule the next change event
		if (currentExpDay < ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			schedule(this.getId(), this.priceChangeInterval, CloudSimTags.IOV_PETROLSTATION_CHANGE_PRICE_EVENT);
		}
	}
	
	/**
	 * processCheckPrice()
	 */
	private void processCheckPrice(SimEvent ev) {
		int userID = ev.getSource();

		UserSmartPhone user = (UserSmartPhone) CloudSim.getEntity(userID);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is sending fuel price to " +
				CloudSim.getEntityName(userID)
				);

		// send the price to the user
		if (this.getMessagingProtocol() == user.getMessagingProtocol()) {
			schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RECEIVE_PETROLSTATION_PRICE_EVENT, 
					this.price);
		} else {
			schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}
	}
	

	/**
	 * @return the isAvailable
	 */
	public boolean isAvailable() {
		return isAvailable;
	}

	/**
	 * @param isAvailable the isAvailable to set
	 */
	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	/**
	 * @return the price
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * @param price the price to set
	 */
	public void setPrice(double price) {
		this.price = price;
	}

	public double getPriceChangeInterval() {
		return priceChangeInterval;
	}

	public void setPriceChangeInterval(double priceChangeInterval) {
		this.priceChangeInterval = priceChangeInterval;
	}


}