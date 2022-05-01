/**
 * 
 */
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
 * @author m.salama
 *
 */
public class UserSmartPhone extends IoTNode  implements IoTNodeMobile {

	private Location currentLocation;
	private int connectedVehicleID;
	
	private boolean isAvailable;

	public UserSmartPhone(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public UserSmartPhone(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName, MessagingProtocol msgProtocol) {
		
		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub
		
		this.currentLocation = location;
		this.connectedVehicleID = -1;
		this.isAvailable = true;
	}
	
	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");				

		// connect to the datacenter
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is connecting to the Datacenter " 
				+ CloudSim.getEntityName(getForwardNodeId())
				);
		send(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_NODE_CONNECTION_EVENT, this.isAvailable);
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
		case CloudSimTags.IOV_CONNECT_VEHICLE:
			processConnectVehicle(ev.getSource());
			break;
		case CloudSimTags.IOV_VEHICLE_LOCATION_UPDATE_EVENT:
			processVehicleLocationUpdate(ev.getData());
			break;
		case CloudSimTags.IOV_VEHICLE_FUEL_ALERT_EVENT: 
			processVehicleFuelAlert(ev.getSource());
			break;
		case CloudSimTags.IOV_FIND_NEAREST_STATION_EVENT: 
			processFindNearestStation(ev.getSource());
			break;
		case CloudSimTags.IOV_RECEIVE_STATION_DATA_EVENT: 
			processReceiveStationData((int) ev.getData());
			break;
		case CloudSimTags.IOV_RECEIVE_STATION_PRICE_EVENT:
			processReceiveStationPrice(ev);
			break;

//		case CloudSimTags.IOV_FIND_NEAREST_PARKING_EVENT: 
//			processFindNearestParking(ev.getSource());
//			break;
//		case CloudSimTags.IOV_RECEIVE_PARKING_DATA_EVENT: 
//			processReceiveParkingData(ev.getData());
//			break;
//		case CloudSimTags.IOV_PARKING_CHECK_AVAILABILITY_EVENT:
//			processCheckParkingAvailability(ev);
//			break;
//		case CloudSimTags.IOV_RECEIVE_PARKING_AVAILABILITY_EVENT:
//			processReceiveParkingAvailability(ev);
//			break;
//		case CloudSimTags.IOV_FIND_NEAREST_RESTAURANT_EVENT: 
//			processFindNearestRestaurant(ev.getSource());
//			break;
//		case CloudSimTags.IOV_RECEIVE_RESTAURANT_DATA_EVENT: 
//			processReceiveRestaurantData(ev.getData());
//			break;
//		case CloudSimTags.IOV_RESTAURANT_ORDER_EVENT:
//			processRestaurantOrder(ev);
//			break;
//		case CloudSimTags.IOV_RESTAURANT_ORDER_CONFIRMATION_EVENT:
//			processRestaurantOrderConfirmation(ev);
//			break;
//		case CloudSimTags.IOV_RESTAURANT_ORDER_READY_EVENT:
//			processRestaurantOrderReady(ev);
//			break;
//		case CloudSimTags.IOV_RESTAURANT_BOOKING_EVENT:
//			processRestaurantBooking(ev);
//			break;
//		case CloudSimTags.IOV_RESTAURANT_BOOKING_CONFIRMATION_EVENT:
//			processRestaurantBookingConfirmation(ev);
//			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}
	
	/**
	 * processConnectVehicle()
	 */
	private void processConnectVehicle(int vehicleID) {
		this.connectedVehicleID = vehicleID;
	}
	
	/**
	 * processVehicleLocationUpdate()
	 */
	private void processVehicleLocationUpdate(Object evData) {
		Location newLocation = (Location) evData;

		this.currentLocation = newLocation; 
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is updating location to " 
				+ Double.toString(this.currentLocation.getX())
			);
	}
	
	/**
	 * processVehicleFuelAlert()
	 */
	private void processVehicleFuelAlert(int vehicleID) {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is processing the fuel alert received from the Vehicle " 
				+ CloudSim.getEntityName(vehicleID)
			);
		
		// send request to the Datacenter to find for nearest available fuel station
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_FIND_NEAREST_STATION_EVENT, 
				this.getCurrentLocation());		
	}
	
	/**
	 * processFindNearestStation()
	 */
	private void processFindNearestStation(int vehicleID) {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is finding the nearest fuel station for the vehicle " 
				+ CloudSim.getEntityName(vehicleID)
			);
		
		// send request to the Datacenter to find for nearest available fuel station
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_FIND_NEAREST_STATION_EVENT, 
				this.getCurrentLocation());		
	}

	/**
	 * processReceiveStationData()
	 */
	private void processReceiveStationData(int stationID) {
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received data of the nearest fuel station " 
				+ CloudSim.getEntityName(stationID)
			);
		
		checkFuelPrice(stationID);
		
		IoTNode station = (IoTNode) CloudSim.getEntity(stationID);
		Location stationLocation = station.getLocation();
		
		// calculate distance to the station
		double distanceToStation = Math.sqrt(
				(stationLocation.getX()-currentLocation.getX())*(stationLocation.getX()-currentLocation.getX()) + 
				(stationLocation.getY()-currentLocation.getY())*(stationLocation.getY()-currentLocation.getY())
				);
		// calculate time to the station
		Vehicle vehicle = (Vehicle) CloudSim.getEntity(connectedVehicleID);
		double speed = vehicle.getAvgSpeed();
		double timeToStation = distanceToStation / speed;
		
		//update the vehicle fuel to full and send update to the vehicle after timeToStation
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is sending update of full fuel of the Vehicle " 
				+ CloudSim.getEntityName(this.connectedVehicleID)
			);
		
		schedule(connectedVehicleID, timeToStation + CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_VEHICLE_FUEL_FULL_EVENT);
	}
	
	/**
	 *  checkFuelPrice()
	 *  
	 */
	private void checkFuelPrice(int stationID) {
		Station station = (Station) CloudSim.getEntity(stationID);
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is checking the price of the station " 
				+ CloudSim.getEntityName(stationID)
			);

		if (this.getMessagingProtocol() == station.getMessagingProtocol()) {
			sendNow(stationID, CloudSimTags.IOV_STATION_CHECK_PRICE_EVENT);
		} else {
			sendNow(getForwardNodeId(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}
	}

	private void processReceiveStationPrice(SimEvent ev) {
		double price = (double) ev.getData();
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] has received the price " + price
				+ " of the station " + CloudSim.getEntityName(ev.getSource())
			);
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
	 * @return the connectedVehicleID
	 */
	public int getConnectedVehicleID() {
		return connectedVehicleID;
	}

	/**
	 * @param connectedVehicleID the connectedVehicleID to set
	 */
	public void setConnectedVehicleID(int connectedVehicleID) {
		this.connectedVehicleID = connectedVehicleID;
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
