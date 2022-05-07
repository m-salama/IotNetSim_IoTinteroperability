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
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeMobile;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iot.nodes.MessagingProtocol;
import org.cloudbus.iotnetsim.network.NetConnection;

import configurations.ExperimentsConfigurations;

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
		
		scheduleParkingRequestRandom();
		scheduleRestaurantRequestRandom();
		scheduleCheckTrafficAlertRandom();
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
			processVehicleFuelAlert();
			break;
		case CloudSimTags.IOV_FIND_NEAREST_STATION_EVENT: 
			processFindNearestStation(ev.getSource());
			break;
		case CloudSimTags.IOV_RECEIVE_STATION_DATA_EVENT: 
			processReceiveStationData((int) ev.getData());
			break;
		case CloudSimTags.IOV_RECEIVE_PETROLSTATION_PRICE_EVENT:
			processReceiveStationPrice(ev);
			break;
		case CloudSimTags.IOV_RECEIVE_ELECTRICCHARGINGSTATION_AVAILABILITY_EVENT:
			processReceiveStationAvailability(ev);
			break;

		case CloudSimTags.IOV_FIND_NEAREST_PARKING_EVENT: 
			processFindNearestParking(ev.getSource());
			break;
		case CloudSimTags.IOV_RECEIVE_PARKING_DATA_EVENT: 
			processReceiveParkingData((int) ev.getData());
			break;
		case CloudSimTags.IOV_RECEIVE_PARKING_AVAILABILITY_EVENT:
			processReceiveParkingAvailability(ev);
			break;
			
		case CloudSimTags.IOV_CHECK_TRAFFICALERT:
			processCheckTrafficAlert();
			break;
		case CloudSimTags.IOV_RECEIVE_TRAFFICALERT_DATA_EVENT:
			processReceiveTrafficAlertData(ev);
			break;

		case CloudSimTags.IOV_FIND_NEAREST_RESTAURANT_EVENT: 
			processFindNearestRestaurant(ev.getSource());
			break;
		case CloudSimTags.IOV_RECEIVE_RESTAURANT_DATA_EVENT: 
			processReceiveRestaurantData((int) ev.getData());
			break;
		case CloudSimTags.IOV_RESTAURANT_ORDER_CONFIRMATION_EVENT:
			processRestaurantOrderConfirmation(ev);
			break;
		case CloudSimTags.IOV_RESTAURANT_ORDER_READY_EVENT:
			processRestaurantOrderReady(ev);
			break;
		case CloudSimTags.IOV_RESTAURANT_BOOKING_CONFIRMATION_EVENT:
			processRestaurantBookingConfirmation(ev);
			break;
			
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
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] updated location to " 
				+ "(" + Double.toString(this.currentLocation.getX())
				+ ", " + Double.toString(this.currentLocation.getY()) 
				+")"
			);
	}
	
	/**
	 * processVehicleFuelAlert()
	 */
	private void processVehicleFuelAlert() {
		Vehicle v = (Vehicle) CloudSim.getEntity(connectedVehicleID);
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is processing the fuel alert received from the Vehicle " 
				+ CloudSim.getEntityName(connectedVehicleID)
			);
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is finding the nearest fuel station for the vehicle " 
				+ CloudSim.getEntityName(connectedVehicleID)
			);

		// send request to the Datacenter to find for nearest available fuel station
		if (v.getVehicleType() == VehicleType.PETROL_VEHICLE) {
			schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_FIND_NEAREST_PETROLSTATION_EVENT, 
					this.getCurrentLocation());		
		} else if (v.getVehicleType() == VehicleType.ELECTRIC_VEHICLE) {
			schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_FIND_NEAREST_ELECTRICCHARGINGSTATION_EVENT, 
					this.getCurrentLocation());		
		}
	}
	
	/**
	 * processFindNearestStation()
	 * to handle a reuqest for finding nearest station, based on the vehicle type
	 */
	private void processFindNearestStation(int vehicleID) {
		Vehicle v = (Vehicle) CloudSim.getEntity(vehicleID);
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is finding the nearest fuel station for the vehicle " 
				+ CloudSim.getEntityName(vehicleID)
			);

		// send request to the Datacenter to find for nearest fuel station
		if (v.getVehicleType() == VehicleType.PETROL_VEHICLE) {
			schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_FIND_NEAREST_PETROLSTATION_EVENT, 
					this.getCurrentLocation());		
		} else if (v.getVehicleType() == VehicleType.ELECTRIC_VEHICLE) {
			schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_FIND_NEAREST_ELECTRICCHARGINGSTATION_EVENT, 
					this.getCurrentLocation());		
		}		
	}
	
	/**
	 * processReceiveStationData()
	 */
	private void processReceiveStationData(int stationID) {
		IoTNode station = (IoTNode) CloudSim.getEntity(stationID);
		Location stationLocation = station.getLocation();
		Vehicle connectedVehicle = (Vehicle) CloudSim.getEntity(connectedVehicleID);
			
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received data of the nearest fuel station " 
				+ CloudSim.getEntityName(stationID)
			);
		
		if (connectedVehicle.getVehicleType() == VehicleType.PETROL_VEHICLE) {
			checkFuelPrice(stationID);
		} else if (connectedVehicle.getVehicleType() == VehicleType.ELECTRIC_VEHICLE) {
			checkStationAvailability(stationID);
		}
		
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
		PetrolStation station = (PetrolStation) CloudSim.getEntity(stationID);
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is checking the price of the station " 
				+ CloudSim.getEntityName(stationID)
			);

		if (this.getMessagingProtocol() == station.getMessagingProtocol()) {
			sendNow(stationID, CloudSimTags.IOV_PETROLSTATION_CHECK_PRICE_EVENT);
		} else {
			sendNow(getForwardNodeId(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}
	}
	
	private void checkStationAvailability(int stationID) {
		ElectricChargingStation station = (ElectricChargingStation) CloudSim.getEntity(stationID);
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is checking the availability of the station " 
				+ CloudSim.getEntityName(stationID)
			);

		if (this.getMessagingProtocol() == station.getMessagingProtocol()) {
			sendNow(stationID, CloudSimTags.IOV_ELECTRICCHARGINGSTATION_CHECK_AVAILABILITY_EVENT);
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
	
	private void processReceiveStationAvailability(SimEvent ev) {
		boolean availability = (boolean) ev.getData();
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] has received the availability " + availability
				+ " of the station " + CloudSim.getEntityName(ev.getSource())
			);
	}
	
	/**
	 * processFindNearestParking()
	 * to handle a reuqest for finding nearest parking, based on the vehicle type
	 */
	private void processFindNearestParking(int vehicleID) {

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is finding the nearest parking for the vehicle " 
				+ CloudSim.getEntityName(vehicleID)
			);

		// send request to the Datacenter to find for nearest parking
			schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_FIND_NEAREST_PARKING_EVENT, 
					this.getCurrentLocation());		
	}

	/**
	 * processReceiveParkingData()
	 */
	private void processReceiveParkingData(int parkingID) {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received data of the nearest parking " 
				+ CloudSim.getEntityName(parkingID)
			);
		
		checkParkingAvailability(parkingID);		
	}
	
	private void checkParkingAvailability(int parkingID) {
		Parking parking = (Parking) CloudSim.getEntity(parkingID);
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is checking the availability of the parking " 
				+ CloudSim.getEntityName(parkingID)
			);

		if (this.getMessagingProtocol() == parking.getMessagingProtocol()) {
			sendNow(parkingID, CloudSimTags.IOV_PARKING_CHECK_AVAILABILITY_EVENT);
		} else {
			sendNow(getForwardNodeId(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}
	}

	private void processReceiveParkingAvailability(SimEvent ev) {
		boolean availability = (boolean) ev.getData();
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received the availability " + availability
				+ " of the parking " + CloudSim.getEntityName(ev.getSource())
			);
	}
	
	/**
	 * processFindNearestRestaurant()
	 * to handle a request for finding nearest restaurant, based on the vehicle type
	 */
	private void processFindNearestRestaurant(int vehicleID) {

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is finding the nearest restaurant for the vehicle " 
				+ CloudSim.getEntityName(vehicleID)
			);

		// send request to the Datacenter to find for nearest restaurant
			schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_FIND_NEAREST_RESTAURANT_EVENT, 
					this.getCurrentLocation());		
	}

	/**
	 * processReceiveRestaurantData()
	 */
	private void processReceiveRestaurantData(int restaurantID) {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received data of the nearest restaurant " 
				+ CloudSim.getEntityName(restaurantID)
			);
		//pick one request randomly
		Random random = new Random();
		boolean b = random.nextBoolean();
		//b ? placeOrder(restaurantID) : bookTable(restaurantID);
		if (b) {
			placeOrder(restaurantID);
		} else {
			bookTable(restaurantID);
		}
	}
	
	private void placeOrder(int restaurantID) {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] placed order at the nearest restaurant " 
				+ CloudSim.getEntityName(restaurantID)
			);
		
		// schedule request to place order 
		schedule(restaurantID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RESTAURANT_ORDER_EVENT);
	}
	
	private void bookTable(int restaurantID) {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] booked a table at the nearest restaurant " 
				+ CloudSim.getEntityName(restaurantID)
			);
		
		// schedule request to book table
		schedule(restaurantID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RESTAURANT_BOOKING_EVENT);
	}
	
	/**
	 * processRestaurantOrderConfirmation()
	 */
	private void processRestaurantOrderConfirmation(SimEvent ev) {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received order confirmation from restaurant " 
				+ CloudSim.getEntityName(ev.getSource())
			);
	}
	
	/**
	 * processRestaurantOrderConfirmation()
	 */
	private void processRestaurantOrderReady(SimEvent ev) {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received order ready from restaurant " 
				+ CloudSim.getEntityName(ev.getSource())
			);
	}
	
	/**
	 * processRestaurantOrderConfirmation()
	 */
	private void processRestaurantBookingConfirmation(SimEvent ev) {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received confirmation of table booking from restaurant " 
				+ CloudSim.getEntityName(ev.getSource())
			);
	}
	
	/**
	 * processCheckTrafficAlert()
	 * to check traffic alert from the nearest Traffic Control Unit
	 */
	private void processCheckTrafficAlert() {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is checking the traffic alert ");
		
		// send request to the Datacenter to find for nearest TrafficControlUnit and check its alert
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CHECK_TRAFFICALERT, 
				this.getCurrentLocation());		
	}
	
	/**
	 * processReceiveTrafficAlertData()
	 */
	private void processReceiveTrafficAlertData(SimEvent ev) {
		boolean trafficAlert = (boolean) ev.getData();
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received the traffic alert set to " + trafficAlert);
	}

	/**
	 * scheduleParkingRequestRandom()
	 * schedule parking request at a random time  
	 */
	private void scheduleParkingRequestRandom() {
		Random random = new Random();  		
		double eventTime = random.nextDouble() * (((ExperimentsConfigurations.EXP_NO_OF_DAYS*24*60*60) - CloudSim.getMinTimeBetweenEvents()) + CloudSim.getMinTimeBetweenEvents());  
		schedule(this.getId(), eventTime, CloudSimTags.IOV_FIND_NEAREST_PARKING_EVENT);
	}
	
	/**
	 * scheduleRestaurantRequestRandom()
	 * schedule restaurant request at a random time  
	 */
	private void scheduleRestaurantRequestRandom() {
		Random random = new Random();  		
		double eventTime = random.nextDouble() * (((ExperimentsConfigurations.EXP_NO_OF_DAYS*24*60*60) - CloudSim.getMinTimeBetweenEvents()) + CloudSim.getMinTimeBetweenEvents());  
		schedule(this.getId(), eventTime, CloudSimTags.IOV_FIND_NEAREST_RESTAURANT_EVENT);
	}
	
	/**
	 * scheduleCheckTrafficAlertRandom()
	 * schedule check traffic alert request at a random time  
	 */
	private void scheduleCheckTrafficAlertRandom() {
		Random random = new Random();  		
		double eventTime = random.nextDouble() * (((ExperimentsConfigurations.EXP_NO_OF_DAYS*24*60*60) - CloudSim.getMinTimeBetweenEvents()) + CloudSim.getMinTimeBetweenEvents());  
		schedule(this.getId(), eventTime, CloudSimTags.IOV_CHECK_TRAFFICALERT);
		
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
