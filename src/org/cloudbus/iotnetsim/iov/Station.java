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

	protected int currentExpDay;
	protected int currentChangeIndex;

	
	public Station(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public Station(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, String forwardNodeName) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
	}

	public Station(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, String forwardNodeName,
			VehicleType vType) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
		
		this.vehicleType = vType;
		this.isAvailable = true;
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");

		// schedule the first event for the station
		schedule(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_STATION_CHANGE_AVAILABILITY, true);
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
		case CloudSimTags.IOV_STATION_CHANGE_AVAILABILITY:
			processChangeStationvailability();
			break;

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
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT);
		
		if (CloudSim.clock() >= currentExpDay*24*60*60) {
			currentExpDay +=1;
		}
		if (currentExpDay < configurations.ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			//schedule the next event for updating the station availability at random time
			scheduleNextChangeRandom(); 
		}
		
	}

	/**
	 * schedule next event for changing station availability at random time
	 */
	private void scheduleNextChangeRandom(){
		// get random time within the same day
		Random random = new Random();  		
		double rTime = random.nextDouble() * (((24*60*60) - CloudSim.getMinTimeBetweenEvents()) + CloudSim.getMinTimeBetweenEvents());  
		
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


}
