package org.cloudbus.iotnetsim.holon;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.IoTDatacenter.NearestComparator;
import org.cloudbus.iotnetsim.iot.nodes.IoTNode;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iov.TrafficControlUnit;
import org.cloudbus.iotnetsim.iov.UserSmartPhone;
import org.cloudbus.iotnetsim.iov.Vehicle;
import org.cloudbus.iotnetsim.iov.VehicleType;
import org.cloudbus.iotnetsim.iov.holon.TrafficControlUnitHolon;
import org.cloudbus.iotnetsim.iov.holon.UserSmartPhoneHolon;
import org.cloudbus.iotnetsim.iov.holon.VehicleHolon;
import org.cloudbus.iotnetsim.naturalenv.SensorType;
import org.semanticweb.owlapi.model.OWLOntology;

import dionasys.holon.Holon;
import dionasys.holon.HolonRegistry;
import dionasys.mediation.Mediator;
import dionasys.mediation.RequestType;



/**
 * Class IoTDatacenter extends CloudSim Datacenter, with IoT-related capabilities.
 * 
 * @author Maria Salama
 * @author elhabbash
 */

public class IoTDatacenterHolon extends Datacenter {

	//NaturalEnv data: data structure for storing data, k: SensorType, v: (reading time, reading value) 
	protected Map<SensorType, Map<Double, Double>> readingsData; 	
	
	//IoV data: data structure for storing data, k: IoTNodeI, v: availability 
	protected Map<IoTNode, Boolean> iovData; 	
	
	// mediator instance to be used instantiated if needed
	private Mediator mediator;
	
	private HolonRegistry registry;
	
	
	/**
	 * Allocates a new IoTDatacenter object.
	 * 
	 * @param name the name to be associated with this entity (as required by Sim_entity class from
	 *            simjava package)
	 * @param characteristics an object of DatacenterCharacteristics
	 * @param storageList a LinkedList of storage elements, for data simulation
	 * @param vmAllocationPolicy the vmAllocationPolicy
	 * @throws Exception This happens when one of the following scenarios occur:
	 *             <ul>
	 *             <li>creating this entity before initializing CloudSim package
	 *             <li>this entity name is <tt>null</tt> or empty
	 *             <li>this entity has <tt>zero</tt> number of PEs (Processing Elements). <br>
	 *             No PEs mean the Cloudlets can't be processed. A CloudResource must contain one or
	 *             more Machines. A Machine must contain one or more PEs.
	 *             </ul>
	 * @pre name != null
	 * @pre resource != null
	 * @post $none
	 */
	@SuppressWarnings("unchecked")
	public IoTDatacenterHolon(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) 
					throws Exception 
	{
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

		// initialise data structure of the NaturalEnv
		readingsData = new HashMap<SensorType, Map<Double, Double>>();
		
		// initialise data structure of the IoV
		iovData = new HashMap<IoTNode, Boolean>(); 
		
		registry = new HolonRegistry();
	}

	/**
	 * Processes events or services that are available for this Datacenter.
	 * 
	 * @param ev a Sim_event object
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		super.processEvent(ev);

		switch (ev.getTag()) {
		case CloudSimTags.IOV_HOLON_REGISTER_HOLON:
			this.receiveAndStoreHolon(ev);
			break;
		case CloudSimTags.IOV_HOLON_REQUEST_HOLON_BY_ID:
			this.processRequestHolonByID(ev);
			break;
		case CloudSimTags.IOV_HOLON_REQUEST_HOLON_BY_TYPE:
			this.processRequestHolonByType(ev);
			break;
		// process receiving data from Gateway Node in the NaturalEnv
		case CloudSimTags.IOT_CLOUD_RECEIVE_DATA_EVENT:
			receiveAndStoreData(ev);
			break;
		//process data received (storing, alerting..)
		case CloudSimTags.IOT_CLOUD_PROCESS_DATA_EVENT:
			processData(ev);
			break;
			
		// IoV nodes connection with the datacenter
		case CloudSimTags.IOV_NODE_CONNECTION_EVENT:
			processIoVNodeConnection(ev.getSource(), (boolean) ev.getData());
		// process receiving data from entities in IoV
		case CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT:
			receiveAndStoreIoVData(ev);
			break;
			// process user request to find nearest petrol station
		case CloudSimTags.IOV_SEARCH_NEAREST_PETROLSTATION_EVENT:
			processFindNearestStation(ev);
			break;
			// process user request to find nearest electric charging station
		case CloudSimTags.IOV_SEARCH_NEAREST_ELECTRICCHARGINGSTATION_EVENT:
			processFindNearestStation(ev);
			break;
		// process user request to find nearest fuel station
		case CloudSimTags.IOV_SEARCH_NEAREST_STATION_EVENT:
			processFindNearestStation(ev);
			break;
			// process user request to find nearest parking
		case CloudSimTags.IOV_SEARCH_NEAREST_PARKING_EVENT:
			processFindNearestParking(ev);
			break;
			// process user request to find nearest restaurant
		case CloudSimTags.IOV_SEARCH_NEAREST_RESTAURANT_EVENT:
			processFindNearestRestaurant(ev);
			break;
		//to check traffic alert from the datacenter
		case CloudSimTags.IOV_TRAFFIC_CHECK_ALERT_EVENT:
			processCheckTrafficAlert(ev);
			break;
			
		//mediator related tags
		case CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR:
			processRequestMediator(ev);
		}
	}
	
	/**
	 * receiveAndStoreData for the NaturalEnv
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void receiveAndStoreData(SimEvent ev) {
		Map<SensorType, Map<Double, Double>> evdata = new HashMap<SensorType, Map<Double, Double>>();
		evdata = (Map<SensorType, Map<Double, Double>>) ev.getData();
		int senderId = ev.getSource();
		
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] is receiving aggregated data from " + CloudSim.getEntityName(senderId));

		//create a data structure for each SensorType
		for (SensorType t : SensorType.values()) {
			readingsData.computeIfAbsent(t, ignored -> new HashMap<>());
		}

		//put data entries for each SensorType
		for (Map.Entry<SensorType, Map<Double, Double>> e : evdata.entrySet()) {
			readingsData.get(e.getKey()).putAll(e.getValue());
		}
		//evdata.forEach((t, readings) -> readingsData.putIfAbsent(t, readings));
		
		//storing data
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] is storing the readings: ");
		evdata.forEach((t, readings) -> Log.printLine("Sensor Type: " + t.name() 
											+ " values: " + readings.values().toString() ));	
		Log.printLine();
	}

	/**
	 * data processing for the NaturalEnv
	 * 
	 */
	private void processData(SimEvent ev) {

	}

	/**
	 * IoVNodeConnection for IoV
	 * 
	 */
	private void processIoVNodeConnection(int nodeID, boolean isAvailable) {
		// insert new entry for the node with availability set to true
		iovData.putIfAbsent((IoTNode) CloudSim.getEntity(nodeID), isAvailable);
		
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] connected with IoV node " + CloudSim.getEntityName(nodeID));
	}

	/**
	 * receiveAndStoreData for ioV
	 * 
	 */
	@SuppressWarnings({ "unlikely-arg-type" })
	private void receiveAndStoreIoVData(SimEvent ev) {
		int senderId = ev.getSource();
		boolean availability = (boolean) ev.getData();
		
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] received data from " + CloudSim.getEntityName(senderId));
		
		// add the received data to the IoV data store
		iovData.put((IoTNode) CloudSim.getEntity(senderId), availability);
		
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] stored latest IoV data from " + CloudSim.getEntityName(senderId));
	}
	
	/**
	 * FindNearestStation for IoV
	 * 
	 */
	private void processFindNearestStation(SimEvent ev) {
		/*
		 * Location userLocation = (Location) ev.getData(); int userID = ev.getSource();
		 * UserSmartPhoneHolon user = (UserSmartPhoneHolon) CloudSim.getEntity(userID);
		 * VehicleHolon vehicle = (VehicleHolon)
		 * CloudSim.getEntity(user.getConnectedVehicleID());
		 * 
		 * int stationID = -1;
		 * 
		 * if (vehicle.getVehicleType() == VehicleType.PETROL_VEHICLE) { stationID =
		 * searchNearestNode(IoTNodeType.PETROL_STATION, userLocation); } else
		 * if(vehicle.getVehicleType() == VehicleType.ELECTRIC_VEHICLE) { stationID =
		 * searchNearestNode(IoTNodeType.ELECTRIC_CHARGING_STATION, userLocation); }
		 * 
		 * if (stationID != -1) { // send the nearest station data to the UserSmartPhone
		 * schedule(userID, CloudSim.getMinTimeBetweenEvents(),
		 * CloudSimTags.IOV_RECEIVE_STATION_DATA_EVENT, stationID); } else {
		 * Log.printLine(CloudSim.clock() + ": [" + getName() +
		 * "] couldn't find a station"); }
		 */		
		Object[] data = new Object[4];
		data = (Object[]) ev.getData();
		Location userLocation = new Location((double) data[0], (double) data[1], (double) data[2]);
		int nearestStationIndex = (int) data[3]; //to find the first/next nearest station  

		int userID = ev.getSource();
		UserSmartPhoneHolon user = (UserSmartPhoneHolon) CloudSim.getEntity(userID);
		VehicleHolon vehicle = (VehicleHolon) CloudSim.getEntity(user.getConnectedVehicleID());

		//LinkedList<Entry> nodesListSorted = new LinkedList<>();
		IoTNode nearestNode = null;

		if (vehicle.getVehicleType() == VehicleType.PETROL_VEHICLE) {
			nearestNode = searchNearestNode(IoTNodeType.PETROL_STATION, userLocation, nearestStationIndex);
		} else if(vehicle.getVehicleType() == VehicleType.ELECTRIC_VEHICLE) {
			nearestNode = searchNearestNode(IoTNodeType.ELECTRIC_CHARGING_STATION, userLocation, nearestStationIndex);
		}		

		int nearestStationID;
		if (nearestNode == null) { // no node found
			nearestStationID = -1;
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] couldn't find a station");
		} else {
			nearestStationID = nearestNode.getId();
		}

		// send the nearest station data to the UserSmartPhone
		schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RECEIVE_STATION_DATA_EVENT, 
				nearestStationID);	

	}
	
	/**
	 * processFindNearestRestaurant for IoV
	 * 
	 */
	private void processFindNearestRestaurant(SimEvent ev) {
		/*
		 * Location userLocation = (Location) ev.getData(); int userID = ev.getSource();
		 * 
		 * int restaurantID = -1;
		 * 
		 * restaurantID = searchNearestNode(IoTNodeType.RESTAURANT, userLocation);
		 * 
		 * if (restaurantID != -1) { // send the nearest station data to the
		 * UserSmartPhone schedule(userID, CloudSim.getMinTimeBetweenEvents(),
		 * CloudSimTags.IOV_RECEIVE_RESTAURANT_DATA_EVENT, restaurantID); } else {
		 * Log.printLine(CloudSim.clock() + ": [" + getName() +
		 * "] couldn't find a restaurant"); }
		 */		
		Object[] data = new Object[4];
		data = (Object[]) ev.getData();
		Location userLocation = new Location((double) data[0], (double) data[1], (double) data[2]);
		int nearestRestaurantIndex = (int) data[3]; //to find the first nearest restaurant

		int userID = ev.getSource();

		IoTNode nearestNode = null;

		nearestNode = searchNearestNode(IoTNodeType.RESTAURANT, userLocation, nearestRestaurantIndex);

		int nearestRestaurantID;

		if (nearestNode == null) { // no node found
			nearestRestaurantID = -1;
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] couldn't find a restaurant");
		} else {
			nearestRestaurantID = nearestNode.getId();
		}

		// send the nearest parking data to the UserSmartPhone
		schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RECEIVE_RESTAURANT_DATA_EVENT, 
				nearestRestaurantID);	

	}
	
	/**
	 * FindNearestParking for IoV
	 * 
	 */
	private void processFindNearestParking(SimEvent ev) {
		/*
		 * Location userLocation = (Location) ev.getData(); int userID = ev.getSource();
		 * 
		 * int parkingID = -1;
		 * 
		 * parkingID = searchNearestNode(IoTNodeType.PARKING, userLocation);
		 * 
		 * if (parkingID != -1) { // send the nearest station data to the UserSmartPhone
		 * schedule(userID, CloudSim.getMinTimeBetweenEvents(),
		 * CloudSimTags.IOV_RECEIVE_PARKING_DATA_EVENT, parkingID); } else {
		 * Log.printLine(CloudSim.clock() + ": [" + getName() +
		 * "] couldn't find a parking"); }
		 */		
		Object[] data = new Object[4];
		data = (Object[]) ev.getData();
		Location userLocation = new Location((double) data[0], (double) data[1], (double) data[2]);
		int nearestParkingIndex = (int) data[3]; //to find the first/next nearest parking

		int userID = ev.getSource();

		IoTNode nearestNode = null;

		nearestNode = searchNearestNode(IoTNodeType.PARKING, userLocation, nearestParkingIndex);

		int nearestParkingID;

		if (nearestNode == null) { // no node found
			nearestParkingID = -1;
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] couldn't find a parking");
		} else {
			nearestParkingID = nearestNode.getId();
		}

		// send the nearest parking data to the UserSmartPhone
		schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RECEIVE_PARKING_DATA_EVENT, 
				nearestParkingID);	

	}
	
	/**
	 * processSearchNearestTrafficControlUnit for IoV
	 * 
	 */
	private void processSearchNearestTrafficControlUnit(SimEvent ev) {
		Object[] data = new Object[4];
		data = (Object[]) ev.getData();
		Location userLocation = new Location((double) data[0], (double) data[1], (double) data[2]);
		int nearestTrafficControlUnitIndex = (int) data[3]; //to find the first nearest traffic control unit

		int userID = ev.getSource();

		IoTNode nearestNode = null;

		nearestNode = searchNearestNode(IoTNodeType.TRAFFIC_CONTROL_UNIT, userLocation, nearestTrafficControlUnitIndex);

		int nearestNodeID;

		if (nearestNode == null) { // no node found
			nearestNodeID = -1;
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] couldn't find a Traffic Control Unit");
		} else {
			nearestNodeID = nearestNode.getId();
		}
		// send the nearest Traffic Control Unit info to the UserSmartPhone
		schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RECEIVE_TRAFFICCONTROLUNIT_DATA_EVENT, 
				nearestNodeID);	
	}

	/**
	 * processCheckTrafficAlert for IoV
	 * 
	 */
	private void processCheckTrafficAlert(SimEvent ev) {
		/*
		 * Location userLocation = (Location) ev.getData(); int userID = ev.getSource();
		 * 
		 * int trafficControlUnitID = -1; boolean isTrafficAlert;
		 * 
		 * trafficControlUnitID = searchNearestNode(IoTNodeType.TRAFFIC_CONTROL_UNIT,
		 * userLocation);
		 * 
		 * if (trafficControlUnitID != -1) { TrafficControlUnitHolon trafficControlUnit
		 * = (TrafficControlUnitHolon) CloudSim.getEntity(trafficControlUnitID);
		 * isTrafficAlert = trafficControlUnit.isTrafficAlert();
		 * 
		 * // send the traffic alert info to the UserSmartPhone schedule(userID,
		 * CloudSim.getMinTimeBetweenEvents(),
		 * CloudSimTags.IOV_RECEIVE_TRAFFICALERT_DATA_EVENT, isTrafficAlert); } else {
		 * Log.printLine(CloudSim.clock() + ": [" + getName() +
		 * "] couldn't check traffic alert"); }
		 */
		Object[] data = new Object[4];
		data = (Object[]) ev.getData();
		Location userLocation = new Location((double) data[0], (double) data[1], (double) data[2]);
		int nearestTrafficControlUnitIndex = (int) data[3]; //to find the first nearest traffic control unit

		int userID = ev.getSource();

		IoTNode nearestNode = null;

		nearestNode = searchNearestNode(IoTNodeType.TRAFFIC_CONTROL_UNIT, userLocation, nearestTrafficControlUnitIndex);

		int nearestNodeID;

		if (nearestNode == null) { // no node found
			nearestNodeID = -1;
			Log.printLine(CloudSim.clock() + ": [" + getName() + "] couldn't find a Traffic Control Unit");
		} else {
			nearestNodeID = nearestNode.getId();
			TrafficControlUnit trafficControlUnit = (TrafficControlUnit) CloudSim.getEntity(nearestNodeID);

			// send the nearest Traffic Control Unit info to the UserSmartPhone
			schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RECEIVE_TRAFFICALERT_DATA_EVENT, 
					trafficControlUnit.isTrafficAlert());	
		}
	}
	
	private IoTNode searchNearestNode(IoTNodeType nodeType, Location userLocation, int nearestIndex) {
		/*
		 * int nearestNodeID = -1; //userLocation as 2D point Point2D userLocationPoint
		 * = new Point.Double(userLocation.getX(), userLocation.getY());
		 * 
		 * // temp data structure to store the nodes of the same type requested
		 * Map<Point2D, IoTNode> nodesList = new HashMap<Point2D, IoTNode>(); // get the
		 * list of nodes of the same type requested for (Map.Entry<IoTNode, Boolean> e :
		 * iovData.entrySet()) { if(e.getKey().getNodeType().equals(nodeType)) {
		 * nodesList.put(new Point.Double(e.getKey().getLocation().getX(),
		 * e.getKey().getLocation().getY()), e.getKey()); } }
		 * 
		 * // temp data structure to store the distance between each node and
		 * userLocation
		 * 
		 * @SuppressWarnings("rawtypes") LinkedList<Entry> nodesListSorted = new
		 * LinkedList<>(); // calculate distance between userLocation and each node if
		 * (nodesList.size() > 0) { for (Point2D point : nodesList.keySet()) {
		 * Entry<IoTNode, Double> e = new SimpleEntry<>(nodesList.get(point),
		 * point.distance(userLocationPoint)); nodesListSorted.add(e); } // sort list by
		 * distance from destination Collections.sort(nodesListSorted, new
		 * NearestComparator());
		 * 
		 * IoTNode nearestNode = (IoTNode) nodesListSorted.get(0).getKey();
		 * nearestNodeID = nearestNode.getId(); }
		 * 
		 * return nearestNodeID;
		 */		
		//userLocation as 2D point 
		Point2D userLocationPoint = new Point.Double(userLocation.getX(), userLocation.getY());

		// temp data structure to store the nodes of the same type requested
		Map<Point2D, IoTNode> nodesList = new HashMap<Point2D, IoTNode>();
		// get the list of nodes of the same type requested
		for (Map.Entry<IoTNode, Boolean> e : iovData.entrySet()) {
			if(e.getKey().getNodeType().equals(nodeType)) {
				nodesList.put(new Point.Double(e.getKey().getLocation().getX(), e.getKey().getLocation().getY()), e.getKey());
			}
		}

		// temp data structure to store the distance between each node and userLocation
		@SuppressWarnings("rawtypes")
		LinkedList<Entry> nodesListSorted = new LinkedList<>();
		// calculate distance between userLocation and each node
		if (nodesList.size() > 0) {
			for (Point2D point : nodesList.keySet()) {			
				Entry<IoTNode, Double> e = new SimpleEntry<>(nodesList.get(point), point.distance(userLocationPoint));
				nodesListSorted.add(e);
			}			
			// sort list by distance from destination
			Collections.sort(nodesListSorted, new NearestComparator());
		}

		IoTNode nearestNode = null;
		if (nodesListSorted.size() > nearestIndex) {
			nearestNode = (IoTNode) nodesListSorted.get(nearestIndex).getKey();
		}

		return nearestNode; 

	}
	
	public class NearestComparator implements Comparator<Entry> {

		@Override
		public int compare(Entry e1, Entry e2) {
			return Double.compare((Double) e1.getValue(), (Double) e2.getValue());
		}
	}

	/**
	 * processRequestMediator 
	 * 
	 */
	private void processRequestMediator(SimEvent ev) {
		int senderId = ev.getSource();
		Object[] data = (Object[]) ev.getData();
		int receiverId = (int) data[0];
		RequestType rType = (RequestType) data[1];
				
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] is processing the request of mediator for user "
				+ CloudSim.getEntityName(senderId)
				);
		
		Object response = null;
		//instantiate mediator, if needed
		if (mediator == null) {
			this.mediator = new Mediator("mediator_0");
		}
		if (!mediator.isAssigned()) {
			response = mediator.processRequest(senderId, receiverId, rType);
		}
		if (response != null) {
			schedule(senderId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_SEND_MEDIATOR_RESPONSE_EVENT, response);
		}
	}

	
	public Map<SensorType, Map<Double, Double>> getReadingsData() {
		return readingsData;
	}

	public void setReadingsData(Map<SensorType, Map<Double, Double>> readingsData) {
		this.readingsData = readingsData;
	}

	public Map<IoTNode, Boolean> getIoVData() {
		return iovData;
	}

	public void setIoVData(Map<IoTNode, Boolean> data) {
		this.iovData = data;
	}

	/**
	 * processRequestHolonByID
	 * @param ev
	 */
	private void processRequestHolonByID(SimEvent ev) {
		OWLOntology holonOntology = this.getHolonByName((String)ev.getData());
		schedule(ev.getSource(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_HOLON_RECEIVE_HOLON,
				holonOntology);
	}
	
	/**
	 * processRequestHolonByType
	 * @param ev
	 */
	private void processRequestHolonByType(SimEvent ev) {
		OWLOntology holonOntology = this.getHolonByType((String)ev.getData());
		schedule(ev.getSource(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_HOLON_RECEIVE_HOLON,
				holonOntology);
	}


	
	/**
	 * receives holon description and stores it in the holon registery
	 * @param ev
	 */
	public void receiveAndStoreHolon(SimEvent ev) {
		Holon evdata = (Holon) ev.getData();		
		int senderId = ev.getSource();		
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] is receiving holon from " + CloudSim.getEntityName(senderId));
		registry.registerHolon(evdata.getHolonDataModel().getData("type"),
				evdata.getHolonDataModel().getData("name"),evdata.getOntologyModel());
		Log.printLine(CloudSim.getEntityName(senderId) + " holon registered");
	}
	
	public void setRegistry(HolonRegistry registry) {
		this.registry = registry;
	}
	
	public HolonRegistry getRegistry() {
		return registry;
	}

    public OWLOntology getHolonByType(String type){
		return registry.getHolonByType(type);
    }
    
    public OWLOntology getHolonByName(String name){
		return registry.getHolonByName(name);
    }
    


}
	