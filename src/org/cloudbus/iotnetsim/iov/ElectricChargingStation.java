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
 * Class Station
 * for service station of fuel and electric cars based on the VehicleType
 * 
 * @author Maria Salama
 * 
 */

public class ElectricChargingStation extends IoTNode  {

	private boolean isAvailable;
	private double price;		//cost per unit (e.g. per litre of fuel)

	protected int currentExpDay;


	public ElectricChargingStation(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public ElectricChargingStation(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName, MessagingProtocol msgProtocol) {

		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub
	}

	public ElectricChargingStation(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName, MessagingProtocol msgProtocol,
			double price) {

		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub

		this.isAvailable = true;
		this.price = price;
		
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

		// schedule the first event for the station to change its availability
		schedule(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_ELECTRICCHARGINGSTATION_CHANGE_AVAILABILITY_EVENT);

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

		case CloudSimTags.IOV_ELECTRICCHARGINGSTATION_CHANGE_AVAILABILITY_EVENT:
			processChangeStationvailability();
			break;
		case CloudSimTags.IOV_ELECTRICCHARGINGSTATION_CHECK_AVAILABILITY_EVENT:
			processCheckAvailability(ev.getSource());
			break;
			
			// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}

	/**
	 * processChangeStationvailability()
	 */
	private void processChangeStationvailability() {
		this.isAvailable = !(this.isAvailable);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is changing availability" 
				+ " to " + Boolean.toString(this.isAvailable) 
				+ " during day " + currentExpDay
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		//send update to Datacenter
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, isAvailable);

		if (CloudSim.clock() >= currentExpDay*24*60*60) {
			currentExpDay +=1;
		}
		if (currentExpDay < configurations.ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			//schedule the next event for updating the station availability at random time
			scheduleNextAvailabilityChangeRandom(); 
		}	
	}

	/**
	 * processCheckAvailability()
	 */
	private void processCheckAvailability(int userID) {
		// send the price to the user
		schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RECEIVE_ELECTRICCHARGINGSTATION_AVAILABILITY_EVENT, 
				this.isAvailable);	
	}
	
	/**
	 * schedule next event for changing station availability at random time
	 */
	private void scheduleNextAvailabilityChangeRandom(){
		// get random time within the same day -- min + (randomValue * (max - min))
		double random = new Random().nextDouble();  		
		double rTime = CloudSim.getMinTimeBetweenEvents() + (random * ((24*60*60) - CloudSim.getMinTimeBetweenEvents()));  

		schedule(this.getId(), rTime, CloudSimTags.IOV_ELECTRICCHARGINGSTATION_CHECK_AVAILABILITY_EVENT);
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


}
