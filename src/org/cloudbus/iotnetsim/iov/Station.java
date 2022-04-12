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

public class Station extends IoTNode  {

	//vehicleType enum attribute to specify if the station offers fuel or electric charging for vehicles
	private VehicleType vehicleType; 

	private boolean isAvailable;
	private double price;		//cost per unit (e.g. per litre of fuel)

	protected int currentExpDay;
	protected int currentChangeIndex;


	public Station(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public Station(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName, MessagingProtocol msgProtocol) {

		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub
	}

	public Station(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName, MessagingProtocol msgProtocol,
			VehicleType vType, double price) {

		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub

		this.vehicleType = vType;
		this.isAvailable = true;
		this.price = price;
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");

		// schedule the first event for the station to change its availability
		schedule(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_STATION_CHANGE_AVAILABILITY, true);

		// schedule the first event for the station to change the price
		schedule(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_STATION_CHANGE_PRICE, true);
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

		case CloudSimTags.IOV_STATION_CHANGE_AVAILABILITY:
			processChangeStationvailability();
			break;
		case CloudSimTags.IOV_STATION_CHANGE_PRICE:
			processChangePriceRandom();

			// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}

	private void processChangeStationvailability() {
		this.isAvailable = !(this.isAvailable);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is changing availability" 
				+ " to " + Boolean.toString(this.isAvailable) 
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

	private void processChangePriceRandom() {
		// get random price within a range -- min + (randomValue * (max - min))
		double random = new Random().nextDouble();  		
		double rPrice = (this.price-1.0) + (random * ((this.price+1.0) - (this.price-1.0)));

		this.price = rPrice;
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is changing the price" 
				+ " to " + Double.toString(this.price) 
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		//send update to Datacenter
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, price);

		//schedule the next change on the next day
		schedule(this.getId(), ((currentExpDay+1)*24*60*60), CloudSimTags.IOV_STATION_CHANGE_PRICE);
	}

	/**
	 * schedule next event for changing station availability at random time
	 */
	private void scheduleNextAvailabilityChangeRandom(){
		// get random time within the same day -- min + (randomValue * (max - min))
		double random = new Random().nextDouble();  		
		double rTime = CloudSim.getMinTimeBetweenEvents() + (random * ((24*60*60) - CloudSim.getMinTimeBetweenEvents()));  

		schedule(this.getId(), rTime, CloudSimTags.IOV_STATION_CHANGE_AVAILABILITY);
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
