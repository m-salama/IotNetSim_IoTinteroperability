package org.cloudbus.iotnetsim.iov;

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
			VehicleType vType, boolean is_available) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
		
		this.vehicleType = vType;
		this.isAvailable = is_available;
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");

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
			processChangeStationvailability(ev);
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}

	private void processChangeStationvailability(SimEvent ev) {
		boolean is_available = (boolean) ev.getData();
		this.isAvailable = is_available;

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is changing availability" 
				+ " to " + Boolean.toString(this.isAvailable) 
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		//send update to Datacenter
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, is_available);
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
