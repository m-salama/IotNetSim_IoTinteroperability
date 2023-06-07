/**
 * 
 */
package org.cloudbus.iotnetsim.iov.holon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.holon.IoTDatacenterHolon;
import org.cloudbus.iotnetsim.iot.nodes.IoTNode;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeMobile;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iot.nodes.MessagingProtocol;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTNodeHolon;
import org.cloudbus.iotnetsim.iov.ElectricChargingStation;
import org.cloudbus.iotnetsim.iov.Parking;
import org.cloudbus.iotnetsim.iov.PetrolStation;
import org.cloudbus.iotnetsim.iov.Restaurant;
import org.cloudbus.iotnetsim.iov.TrafficControlUnit;
import org.cloudbus.iotnetsim.iov.Vehicle;
import org.cloudbus.iotnetsim.iov.VehicleType;
import org.cloudbus.iotnetsim.network.NetConnection;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import dionasys.holon.datamodel.HolonDataModel;
import dionasys.holon.datamodel.HolonServiceModel;
import dionasys.holon.datamodel.OntologyParser;
import dionasys.holon.datamodel.ParameterModel;
import dionasys.holon.datamodel.TypeMapper;
import dionasys.holon.datamodel.Types;
import experiments.configurations.ExperimentsConfigurations;

/**
 * @author m.salama
 *
 */
public class UserSmartPhoneHolon extends IoTNodeHolon implements IoTNodeMobile {

	private Location currentLocation;
	private int connectedVehicleID;

	private boolean isAvailable;

	private int countFuelAlerts;
	private int countFuelRequests;
	private int fuelRequestsTotalMessages;
	private List<Double> lstFuelRequestsResponseTime;

	private int countRestaurantRequests;
	private int restaurantRequestsTotalMessages;
	private List<Double> lstRestaurantRequestsResponseTime;

	private int countParkingRequests;
	private int parkingRequestsTotalMessages;
	private List<Double> lstParkingRequestsResponseTime;

	private int countTrafficAlertRequests;
	private int trafficAlertsTotalMessages;
	private List<Double> lstTrafficAlertsResponseTime;

	private int nearestStationIndex;
	private int nearestParkingIndex;
	private int nearestRestaurantIndex;

	private int currentExpDay;

	private Map<IoTNodeType, HolonDataModel> nodeHolons;
	private Workflow workflow;

	public UserSmartPhoneHolon(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public UserSmartPhoneHolon(String name, Location location, IoTNodeType nodeType, NetConnection connection,
			IoTNodePower power, IoTDatacenterHolon dataCentre, String forwardNodeName,
			MessagingProtocol messagingProtocol) {
		super(name, location, nodeType, connection, power, dataCentre, forwardNodeName, messagingProtocol);
		// TODO Auto-generated constructor stub

		this.currentLocation = location;
		this.currentLocation = location;
		this.isAvailable = true;
		this.nodeHolons = new HashMap<>();
		this.messagingProtocol = messagingProtocol;

		workflow = new Workflow(this);
		holon.setHolonDataModel(createDataModel(name, location, messagingProtocol));
		holon.createOntology();

		this.countFuelAlerts = 0;
		this.countFuelRequests = 0;
		this.fuelRequestsTotalMessages = 0;
		this.lstFuelRequestsResponseTime = new ArrayList<Double>();

		this.countRestaurantRequests = 0;
		this.restaurantRequestsTotalMessages = 0;
		this.lstRestaurantRequestsResponseTime = new ArrayList<Double>();

		this.countParkingRequests = 0;
		this.parkingRequestsTotalMessages = 0;
		this.lstParkingRequestsResponseTime = new ArrayList<Double>();

		this.countTrafficAlertRequests = 0;
		this.trafficAlertsTotalMessages = 0;
		this.lstTrafficAlertsResponseTime = new ArrayList<Double>();

		// to find the first nearest node and incremented to find the next available
		// node
		this.nearestStationIndex = 0;
		this.nearestParkingIndex = 0;
		this.nearestRestaurantIndex = 0;

		this.currentExpDay = 1;
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");

		// connect to the datacenter
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is connecting to the Datacenter "
				+ CloudSim.getEntityName(getForwardNodeId()));
		send(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_NODE_CONNECTION_EVENT,
				this.isAvailable);

		// request vehicle holon
		schedule(this.dataCentre.getId(), CloudSim.getMinTimeBetweenEvents(),
				CloudSimTags.IOV_HOLON_REQUEST_HOLON_BY_ID, CloudSim.getEntityName(connectedVehicleID));
		schedule(this.getId(), CloudSim.getMinTimeBetweenEvents() * 100, CloudSimTags.IOV_HOLON_WORKFLOW);

		scheduleRestaurantRequest();
		scheduleParkingRequest();
		scheduleCheckTrafficAlert();
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
		case CloudSimTags.IOV_HOLON_RECEIVE_HOLON:
			processHolonReceiveEvent(ev);
			break;
		case CloudSimTags.IOV_HOLON_WORKFLOW:
			processWorkflowEvent(ev);
			break;
			
		case CloudSimTags.IOV_CONNECT_VEHICLE:
			processConnectVehicle(ev.getSource());
			break;
		case CloudSimTags.IOV_VEHICLE_LOCATION_UPDATE_EVENT:
			processVehicleLocationUpdate(ev.getData());
			break;
			
		case CloudSimTags.IOV_VEHICLE_FUEL_ALERT_EVENT:
			processVehicleFuelAlert();
			break;
		case CloudSimTags.IOV_SEARCH_NEAREST_STATION_EVENT:
			processSearchNearestStation(ev.getSource());
			break;
		case CloudSimTags.IOV_RECEIVE_STATION_DATA_EVENT:
			processReceiveStationData(ev);
			break;
		case CloudSimTags.IOV_RECEIVE_PETROLSTATION_PRICE_EVENT:
			processReceiveStationPrice(ev);
			break;
		case CloudSimTags.IOV_RECEIVE_ELECTRICCHARGINGSTATION_AVAILABILITY_EVENT:
			processReceiveStationAvailability(ev);
			break;

		case CloudSimTags.IOV_SEARCH_NEAREST_PARKING_EVENT: 
			processSearchNearestParking();
			break;
		case CloudSimTags.IOV_RECEIVE_PARKING_DATA_EVENT: 
			processReceiveParkingData(ev);
			break;
		case CloudSimTags.IOV_RECEIVE_PARKING_AVAILABILITY_EVENT:
			processReceiveParkingAvailability(ev);
			break;


		case CloudSimTags.IOV_SEARCH_NEAREST_TRAFFICCONTROLUNIT_EVENT:
			processSearchNearestTrafficControlUnit();
			break;
		case CloudSimTags.IOV_RECEIVE_TRAFFICCONTROLUNIT_DATA_EVENT:
			processReceiveTrafficControlUnitData(ev);
			break;
		case CloudSimTags.IOV_RECEIVE_TRAFFICALERT_DATA_EVENT:
			processReceiveTrafficAlertData(ev);
			break;
		case CloudSimTags.IOV_TRAFFIC_CHECK_ALERT_EVENT:
			// processCheckTrafficAlert();
			break;

		case CloudSimTags.IOV_SEARCH_NEAREST_RESTAURANT_EVENT: 
			processSearchNearestRestaurant();
			break;
		case CloudSimTags.IOV_RECEIVE_RESTAURANT_DATA_EVENT: 
			processReceiveRestaurantData(ev);
			break;
		case CloudSimTags.IOV_RECEIVE_RESTAURANT_AVAILABILITY_EVENT:
			processReceiveRestaurantAvailability(ev);
			break;			
		case CloudSimTags.IOV_RESTAURANT_ORDER_CONFIRMATION_EVENT:
			processReceiveRestaurantOrderConfirmation(ev);
			break;
		case CloudSimTags.IOV_RESTAURANT_ORDER_READY_EVENT:
			processReceiveRestaurantOrderReady(ev);
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}
	}

	private void processWorkflowEvent(SimEvent ev) {
		boolean allNodesConnected = true;
		for (IoTNodeType type : workflow.getRequiredNodes()) {
			if (nodeHolons.get(type) == null) {
				allNodesConnected = false;
				schedule(this.dataCentre.getId(), CloudSim.getMinTimeBetweenEvents(),
						CloudSimTags.IOV_HOLON_REQUEST_HOLON_BY_TYPE, type.toString());
			}
		}
		if (allNodesConnected) {
			workflow.execute();
		} else {
			schedule(this.getId(), CloudSim.getMinTimeBetweenEvents() * 4, CloudSimTags.IOV_HOLON_WORKFLOW);
		}

		// schedule next workflow execution
		// schedule(this.getId(), CloudSim.getMinTimeBetweenEvents() * 100,
		// CloudSimTags.IOV_HOLON_WORKFLOW);
	}

	private void processHolonReceiveEvent(SimEvent ev) {
		try {
			OWLOntology ontology = (OWLOntology) ev.getData();
			if (ontology == null) {
				Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] ERROR: null Ontology received");
			} else {
				HolonDataModel holonModel = OntologyParser.parse(ontology);
				Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] " + holonModel.getData("type")
						+ " ontology received");

				switch (holonModel.getData("type")) {
				case "PETROL_VEHICLE":
					nodeHolons.put(IoTNodeType.PETROL_VEHICLE, holonModel);
					break;
				case "ELECTRIC_VEHICLE":
					nodeHolons.put(IoTNodeType.ELECTRIC_VEHICLE, holonModel);
					break;
				case "PETROL_STATION":
					nodeHolons.put(IoTNodeType.PETROL_STATION, holonModel);
					break;
				case "PARKING":
					nodeHolons.put(IoTNodeType.PARKING, holonModel);
					break;
				case "RESTAURANT":
					nodeHolons.put(IoTNodeType.RESTAURANT, holonModel);
					break;
				case "TRAFFIC_CONTROL_UNIT":
					nodeHolons.put(IoTNodeType.TRAFFIC_CONTROL_UNIT, holonModel);
					break;
				case "ELECTRIC_CHARGING_STATION":
					nodeHolons.put(IoTNodeType.ELECTRIC_CHARGING_STATION, holonModel);
					break;
				}
			}
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

	public Object callService(Object[] arguments, IoTNodeType holonType, int serviceAnnotation) {

		// using the annotation get the service from the holon data model
		HolonDataModel dataModel = this.nodeHolons.get(holonType);
		HolonServiceModel service = dataModel.getServiceByAnnotation(serviceAnnotation);

		// mediation
		String myMessagingProtocol = dataModel.getData("messagingProtocol");
		String entityMessagingProtocol = this.getHolon().getHolonDataModel().getData("messagingProtocol");
		if (myMessagingProtocol.equals(entityMessagingProtocol)) {
			sytheizeMediator(myMessagingProtocol, entityMessagingProtocol);
		}

		// Rest service call is not supported by the simulator so obtain the node
		// reference from CloudSim using the node name
		IoTNodeHolon node = (IoTNodeHolon) CloudSim.getEntity(dataModel.getData("name"));
		Object[] carId = new Object[1];
		arguments[0] = Integer.valueOf(connectedVehicleID);

		int requiredArguments = 0;
		if (service.getParameters() != null) {
			requiredArguments = service.getParameters().size();
		}
		try {
			if (arguments != null && arguments.length < requiredArguments) {
				Log.printLine(getName() + ": Unable to call service [" + service.getName()
						+ "], Insuffcient number of arguments");
				return null;
			}

			Class returnType = TypeMapper.mapToJavaTypes(service.getReturnType());
			Method method = null;
			Class[] methodArgumentTypes = null;
			if (service.getParameters().size() > 0) {
				methodArgumentTypes = new Class[service.getParameters().size()];
				int i = 0;
				for (ParameterModel par : service.getParameters()) {
					methodArgumentTypes[i] = TypeMapper.mapToJavaTypes(par.getDataType());
					arguments[i] = methodArgumentTypes[i].cast(arguments[i]);
					i++;
				}
				method = node.getClass().getDeclaredMethod(service.getName(), methodArgumentTypes);
			} else {
				method = node.getClass().getDeclaredMethod(service.getName());
			}

			Log.printLine(CloudSim.clock() + ": SmartPhone: calling method = " + method);

			if (service.getParameters().size() > 0) {
				return returnType.cast(method.invoke(node, arguments));
			} else {
				return returnType.cast(method.invoke(node));
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
			return null;
		}

	}

	private void sytheizeMediator(String myMessagingProtocol, String entityMessagingProtocol) {
		// TODO Auto-generated method stub

	}

	private HolonDataModel createDataModel(String name, Location location, MessagingProtocol messagingProtocol) {

		HolonDataModel dataModel = new HolonDataModel();
		dataModel.putData("name", name);
		dataModel.putData("type", this.getNodeType().toString());
		dataModel.putData("latitude", location.getX() + "");
		dataModel.putData("longitude", location.getY() + "");
		dataModel.putData("messagingProtocol", messagingProtocol.toString());
		dataModel.putData("connecitonType", this.getConnection().getConnectionType().toString());
		dataModel.putData("powerType", this.getPower().getPowerType().toString());

		HolonServiceModel service2 = new HolonServiceModel();
		service2.setName("getLocation");
		service2.setCost(0);
		service2.setUrl("getLocation");
		service2.setReturnType(Types.STRING);

		HolonServiceModel service3 = new HolonServiceModel();
		service3.setName("getMessagingProtocol");
		service3.setCost(0);
		service3.setUrl("getMessagingProtocol");
		service3.setReturnType(Types.STRING);

		ArrayList<HolonServiceModel> services = new ArrayList<>();
		services.add(service2);
		services.add(service3);
		dataModel.setServices(services);

		return dataModel;
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

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] updated location to " + "("
				+ Double.toString(this.currentLocation.getX()) + ", " + Double.toString(this.currentLocation.getY())
				+ ")");
	}

	/**
	 * processVehicleFuelAlert()
	 */
	private void processVehicleFuelAlert() {
		// record starting time of the new request
		this.lstFuelRequestsResponseTime.add(CloudSim.clock());

		VehicleHolon v = (VehicleHolon) CloudSim.getEntity(connectedVehicleID);

		Log.printLine(
				CloudSim.clock() + ": [" + this.getName() + "] is processing the fuel alert received of the vehicle "
						+ CloudSim.getEntityName(connectedVehicleID));

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is finding the nearest fuel station");

		/*
		 * // send request to the Datacenter to find for nearest available fuel station
		 * if (v.getVehicleType() == VehicleType.PETROL_VEHICLE) {
		 * schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(),
		 * CloudSimTags.IOV_SEARCH_NEAREST_PETROLSTATION_EVENT,
		 * this.getCurrentLocation()); } else if (v.getVehicleType() ==
		 * VehicleType.ELECTRIC_VEHICLE) { schedule(this.getForwardNodeId(),
		 * CloudSim.getMinTimeBetweenEvents(),
		 * CloudSimTags.IOV_SEARCH_NEAREST_ELECTRICCHARGINGSTATION_EVENT,
		 * this.getCurrentLocation()); }
		 */
		// update counters
		// send request to the Datacenter to find the next nearest station

		Object[] newData = new Object[4];
		newData[0] = this.getCurrentLocation().getX();
		newData[1] = this.getCurrentLocation().getY();
		newData[2] = this.getCurrentLocation().getZ();
		newData[3] = this.nearestStationIndex;

		// send request to the Datacenter to find for nearest fuel station
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(),
				CloudSimTags.IOV_SEARCH_NEAREST_STATION_EVENT, newData);

		// update counters
		this.countFuelAlerts += 1;
		this.countFuelRequests += 1;
		this.fuelRequestsTotalMessages += 1;
	}

	/**
	 * processFindNearestStation() to handle a reuqest for finding nearest station,
	 * based on the vehicle type
	 */
	private void processSearchNearestStation(int vehicleID) {
		// record starting time of the new request
		this.lstFuelRequestsResponseTime.add(CloudSim.clock());

		VehicleHolon v = (VehicleHolon) CloudSim.getEntity(vehicleID);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is finding the nearest fuel station ");

		/*
		 * // send request to the Datacenter to find for nearest fuel station if
		 * (v.getVehicleType() == VehicleType.PETROL_VEHICLE) {
		 * schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(),
		 * CloudSimTags.IOV_SEARCH_NEAREST_PETROLSTATION_EVENT,
		 * this.getCurrentLocation()); } else if (v.getVehicleType() ==
		 * VehicleType.ELECTRIC_VEHICLE) { schedule(this.getForwardNodeId(),
		 * CloudSim.getMinTimeBetweenEvents(),
		 * CloudSimTags.IOV_SEARCH_NEAREST_ELECTRICCHARGINGSTATION_EVENT,
		 * this.getCurrentLocation()); }
		 */

		// send request to the Datacenter to find the next nearest station
		Object[] newData = new Object[4];
		newData[0] = this.getCurrentLocation().getX();
		newData[1] = this.getCurrentLocation().getY();
		newData[2] = this.getCurrentLocation().getZ();
		newData[3] = this.nearestStationIndex;

		// send request to the Datacenter to find for nearest fuel station
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(),
				CloudSimTags.IOV_SEARCH_NEAREST_STATION_EVENT, newData);

		// update counters
		this.countFuelRequests += 1;
		this.fuelRequestsTotalMessages += 1;
	}

	/**
	 * processReceiveStationData()
	 */
	private void processReceiveStationData(SimEvent ev) {
		/*
		 * IoTNodeHolon station = (IoTNodeHolon) CloudSim.getEntity(stationID); Location
		 * stationLocation = station.getLocation(); VehicleHolon connectedVehicle =
		 * (VehicleHolon) CloudSim.getEntity(connectedVehicleID);
		 * 
		 * Log.printLine(CloudSim.clock() + ": [" + this.getName() +
		 * "] received data of the nearest fuel station " +
		 * CloudSim.getEntityName(stationID) );
		 * 
		 * if (connectedVehicle.getVehicleType() == VehicleType.PETROL_VEHICLE) {
		 * checkFuelPrice(stationID); } else if (connectedVehicle.getVehicleType() ==
		 * VehicleType.ELECTRIC_VEHICLE) { checkStationAvailability(stationID); }
		 * 
		 * // calculate distance to the station double distanceToStation = Math.sqrt(
		 * (stationLocation.getX()-currentLocation.getX())*(stationLocation.getX()-
		 * currentLocation.getX()) +
		 * (stationLocation.getY()-currentLocation.getY())*(stationLocation.getY()-
		 * currentLocation.getY()) ); // calculate time to the station VehicleHolon
		 * vehicle = (VehicleHolon) CloudSim.getEntity(connectedVehicleID); double speed
		 * = vehicle.getAvgSpeed(); double timeToStation = distanceToStation / speed;
		 * 
		 * //update the vehicle fuel to full and send update to the vehicle after
		 * timeToStation Log.printLine(CloudSim.clock() + ": [" + this.getName() +
		 * "] is sending update of full fuel of the Vehicle " +
		 * CloudSim.getEntityName(this.connectedVehicleID) );
		 * 
		 * schedule(connectedVehicleID, timeToStation +
		 * CloudSim.getMinTimeBetweenEvents(),
		 * CloudSimTags.IOV_VEHICLE_FUEL_FULL_EVENT);
		 */
		int stationID = (int) ev.getData();

		// update counters
		this.fuelRequestsTotalMessages += 1;

		if (stationID == -1) { // no station found
			Log.printLine(
					CloudSim.clock() + ": [" + this.getName() + "] haven't received data of nearest fuel station ");
		} else {
			Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received data of the nearest fuel station "
					+ CloudSim.getEntityName(stationID));

			VehicleHolon connectedVehicle = (VehicleHolon) CloudSim.getEntity(connectedVehicleID);

			if (connectedVehicle.getVehicleType() == VehicleType.PETROL_VEHICLE) {
				checkFuelPrice(stationID);
			} else if (connectedVehicle.getVehicleType() == VehicleType.ELECTRIC_VEHICLE) {
				checkStationAvailability(stationID);
			}
		}
	}

	/**
	 * checkFuelPrice()
	 * 
	 */
	private void checkFuelPrice(int stationID) {
		/*
		 * PetrolStationHolon station = (PetrolStationHolon)
		 * CloudSim.getEntity(stationID);
		 * 
		 * Log.printLine(CloudSim.clock() + ": [" + this.getName() +
		 * "] is checking the price of the station " + CloudSim.getEntityName(stationID)
		 * );
		 * 
		 * if (this.getMessagingProtocol() == station.getMessagingProtocol()) {
		 * sendNow(stationID, CloudSimTags.IOV_PETROLSTATION_CHECK_PRICE_EVENT); } else
		 * { sendNow(getForwardNodeId(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR); }
		 */
		PetrolStationHolon station = (PetrolStationHolon) CloudSim.getEntity(stationID);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is checking the price of the station "
				+ CloudSim.getEntityName(stationID));

		if (this.getMessagingProtocol() == station.getMessagingProtocol()) {
			// update counters
			this.fuelRequestsTotalMessages += 1;
			schedule(stationID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_PETROLSTATION_CHECK_PRICE_EVENT);
		} else {
			// update counters
			this.fuelRequestsTotalMessages += 2;
			schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}
	}

	private void checkStationAvailability(int stationID) {
		/*
		 * ElectricChargingStationHolon station = (ElectricChargingStationHolon)
		 * CloudSim.getEntity(stationID);
		 * 
		 * Log.printLine(CloudSim.clock() + ": [" + this.getName() +
		 * "] is checking the availability of the station " +
		 * CloudSim.getEntityName(stationID) );
		 * 
		 * if (this.getMessagingProtocol() == station.getMessagingProtocol()) {
		 * sendNow(stationID,
		 * CloudSimTags.IOV_ELECTRICCHARGINGSTATION_CHECK_AVAILABILITY_EVENT); } else {
		 * sendNow(getForwardNodeId(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR); }
		 */
		ElectricChargingStationHolon station = (ElectricChargingStationHolon) CloudSim.getEntity(stationID);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is checking the availability of the station "
				+ CloudSim.getEntityName(stationID));

		if (this.getMessagingProtocol() == station.getMessagingProtocol()) {
			// update counters
			this.fuelRequestsTotalMessages += 1;
			schedule(stationID, CloudSim.getMinTimeBetweenEvents(),
					CloudSimTags.IOV_ELECTRICCHARGINGSTATION_CHECK_AVAILABILITY_EVENT);
		} else {
			// update counters
			this.fuelRequestsTotalMessages += 2;
			schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}
	}

	private void processReceiveStationPrice(SimEvent ev) {
		/*
		 * double price = (double) ev.getData();
		 * 
		 * Log.printLine(CloudSim.clock() + ": [" + this.getName() +
		 * "] has received the price " + price + " of the station " +
		 * CloudSim.getEntityName(ev.getSource()) );
		 */
		double price = (double) ev.getData();
		int stationID = ev.getSource();

		PetrolStationHolon station = (PetrolStationHolon) CloudSim.getEntity(stationID);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received the price " + price
				+ " of the petrol station " + CloudSim.getEntityName(stationID));

		updateFuel(stationID);

		// update counters
		if (this.getMessagingProtocol() == station.getMessagingProtocol()) {
			this.fuelRequestsTotalMessages += 1;
		} else {
			this.fuelRequestsTotalMessages += 2;
		}
		// calculate the service time of the request
		calculateFuelRequestResponseTime();

		// reset nearest station index
		this.nearestStationIndex = 0;

	}

	private void processReceiveStationAvailability(SimEvent ev) {
		/*
		 * boolean availability = (boolean) ev.getData();
		 * 
		 * Log.printLine(CloudSim.clock() + ": [" + this.getName() +
		 * "] has received the availability " + availability + " of the station " +
		 * CloudSim.getEntityName(ev.getSource()) );
		 */
		boolean isAvailabile = (boolean) ev.getData();
		int stationID = ev.getSource();

		ElectricChargingStationHolon station = (ElectricChargingStationHolon) CloudSim.getEntity(stationID);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received the availability " + isAvailabile
				+ " of the electric charging station " + CloudSim.getEntityName(stationID));

		if (isAvailabile) {
			updateFuel(stationID);

			// update counters
			if (this.getMessagingProtocol() == station.getMessagingProtocol()) {
				this.fuelRequestsTotalMessages += 1;
			} else {
				this.fuelRequestsTotalMessages += 2;
			}
			// calculate the service time of the request
			calculateFuelRequestResponseTime();

			// reset nearest station index
			this.nearestStationIndex = 0;
		} else {
			this.nearestStationIndex += 1;
			if (this.nearestStationIndex < 5) {
				Log.printLine(
						CloudSim.clock() + ": [" + this.getName() + "] still searching for the nearest station...");

				// send request to the Datacenter to find the next nearest station
				Object[] newData = new Object[4];
				newData[0] = this.getCurrentLocation().getX();
				newData[1] = this.getCurrentLocation().getY();
				newData[2] = this.getCurrentLocation().getZ();
				newData[3] = this.nearestStationIndex;

				schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(),
						CloudSimTags.IOV_SEARCH_NEAREST_ELECTRICCHARGINGSTATION_EVENT, newData);
			} else {
				Log.printLine(CloudSim.clock() + ": [" + this.getName()
						+ "] will try later searching for the nearest station...");

				// reset nearest station index
				this.nearestStationIndex = 0;
			}
		}

	}

	private void updateFuel(int stationID) {
		IoTNode station = (IoTNode) CloudSim.getEntity(stationID);
		Location stationLocation = station.getLocation();

		// calculate distance to the station
		double distanceToStation = Math.sqrt(
				(stationLocation.getX() - currentLocation.getX()) * (stationLocation.getX() - currentLocation.getX())
						+ (stationLocation.getY() - currentLocation.getY())
								* (stationLocation.getY() - currentLocation.getY()));
		// calculate time to the station
		VehicleHolon vehicle = (VehicleHolon) CloudSim.getEntity(connectedVehicleID);
		double speed = vehicle.getAvgSpeed();
		double timeToStation = distanceToStation / speed;

		// update the vehicle fuel to full and send update to the vehicle after
		// timeToStation
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is sending update of full fuel of the Vehicle "
				+ CloudSim.getEntityName(this.connectedVehicleID));

		schedule(connectedVehicleID, timeToStation + CloudSim.getMinTimeBetweenEvents(),
				CloudSimTags.IOV_VEHICLE_SET_FUEL_FULL_EVENT);
	}

	private void calculateFuelRequestResponseTime() {
		// calculate the service time of the request
		double serviceTime = ((CloudSim.clock()
				- lstFuelRequestsResponseTime.get(lstFuelRequestsResponseTime.size() - 1))
				+ CloudSim.getMinTimeBetweenEvents());

		// update service time of the request
		this.lstFuelRequestsResponseTime.set(lstFuelRequestsResponseTime.size() - 1, serviceTime);
	}

	/**
	 * processFindNearestRestaurant() to handle a request for finding nearest
	 * restaurant, based on the vehicle type
	 */
	private void processSearchNearestRestaurant() {
		/*
		 * Log.printLine(CloudSim.clock() + ": [" + this.getName() +
		 * "] is finding the nearest restaurant for the vehicle " +
		 * CloudSim.getEntityName(vehicleID) );
		 * 
		 * // send request to the Datacenter to find for nearest restaurant
		 * schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(),
		 * CloudSimTags.IOV_SEARCH_NEAREST_RESTAURANT_EVENT, this.getCurrentLocation());
		 * 
		 * //update counters this.restaurantRequests += 1;
		 * 
		 * //record starting time of the new request
		 * this.restaurantRequestsResponseTime.add(System.currentTimeMillis());
		 */
		// record starting time of the new request
		this.lstRestaurantRequestsResponseTime.add(CloudSim.clock());

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is finding the nearest restaurant ");

		Object[] data = new Object[4];
		data[0] = this.getCurrentLocation().getX();
		data[1] = this.getCurrentLocation().getY();
		data[2] = this.getCurrentLocation().getZ();
		data[3] = this.nearestRestaurantIndex; // to find the first/next nearest parking

		// send request to the Datacenter to find for nearest restaurant
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(),
				CloudSimTags.IOV_SEARCH_NEAREST_RESTAURANT_EVENT, data);

		// update counters
		this.countRestaurantRequests += 1;
		this.restaurantRequestsTotalMessages += 1;
	}

	/**
	 * processReceiveRestaurantData()
	 */
	private void processReceiveRestaurantData(SimEvent ev) {
		/*
		 * Log.printLine(CloudSim.clock() + ": [" + this.getName() +
		 * "] received data of the nearest restaurant " +
		 * CloudSim.getEntityName(restaurantID) ); //pick one request randomly Random
		 * random = new Random(); boolean b = random.nextBoolean(); //b ?
		 * placeOrder(restaurantID) : bookTable(restaurantID); if (b) {
		 * placeOrder(restaurantID); } else { bookTable(restaurantID); }
		 */
		int restaurantID = (int) ev.getData();

		// update counters
		this.restaurantRequestsTotalMessages += 1;

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received data of the nearest restaurant "
				+ CloudSim.getEntityName(restaurantID));

		if (restaurantID == -1) { // no restaurant found
			Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] haven't received data of nearest restaurant ");
		} else {
			Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received data of the nearest restaurant "
					+ CloudSim.getEntityName(restaurantID));

			checkRestaurantAvailability(restaurantID);
		}

	}

	private void checkRestaurantAvailability(int restaurantID) {
		RestaurantHolon restaurant = (RestaurantHolon) CloudSim.getEntity(restaurantID);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is checking the availability of restaurant "
				+ CloudSim.getEntityName(restaurantID));

		if (this.getMessagingProtocol() == restaurant.getMessagingProtocol()) {
			this.restaurantRequestsTotalMessages += 1;
			schedule(restaurantID, CloudSim.getMinTimeBetweenEvents(),
					CloudSimTags.IOV_RESTAURANT_CHECK_AVAILABILITY_EVENT);
		} else {
			this.restaurantRequestsTotalMessages += 2;
			schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}
	}

	private void processReceiveRestaurantAvailability(SimEvent ev) {
		boolean isOpen = (boolean) ev.getData();
		int restaurantID = ev.getSource();

		// update counters
		this.restaurantRequestsTotalMessages += 1;

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received restaurant  "
				+ CloudSim.getEntityName(restaurantID) + " availability " + isOpen);

		if (isOpen) {
			placeOrder(restaurantID);
		} else {
			this.nearestRestaurantIndex += 1;
			if (this.nearestRestaurantIndex < 5) {
				Log.printLine(
						CloudSim.clock() + ": [" + this.getName() + "] still searching for the nearest restaurant...");

				// send request to the Datacenter to find the next nearest restaurant
				Object[] newData = new Object[4];
				newData[0] = this.getCurrentLocation().getX();
				newData[1] = this.getCurrentLocation().getY();
				newData[2] = this.getCurrentLocation().getZ();
				newData[3] = this.nearestRestaurantIndex;

				schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(),
						CloudSimTags.IOV_SEARCH_NEAREST_RESTAURANT_EVENT, newData);
			} else {
				Log.printLine(CloudSim.clock() + ": [" + this.getName()
						+ "] will try later searching for the nearest restaurant...");

				// reset nearest station index
				this.nearestRestaurantIndex = 0;
			}
		}
	}

	private void placeOrder(int restaurantID) {
		RestaurantHolon restaurant = (RestaurantHolon) CloudSim.getEntity(restaurantID);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] placed order at the nearest restaurant "
				+ CloudSim.getEntityName(restaurantID));

		double requestTime = CloudSim.getMinTimeBetweenEvents();
		if (ExperimentsConfigurations.IOV_EXP_ServiceEntities_CHANGE == "Frequent") {
			requestTime = CloudSim.getMinTimeBetweenEvents();
		} else if (ExperimentsConfigurations.IOV_EXP_ServiceEntities_CHANGE == "Random") {
			// get random time within a hour
			Random random = new Random(); 
			requestTime = random.nextDouble() * ((60*60) + CloudSim.getMinTimeBetweenEvents());					
		}

		// schedule request to place order
		if (this.getMessagingProtocol() == restaurant.getMessagingProtocol()) {
			this.restaurantRequestsTotalMessages += 1;
			schedule(restaurantID, requestTime + CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RESTAURANT_ORDER_EVENT);
		} else {
			this.restaurantRequestsTotalMessages += 2;
			schedule(getForwardNodeId(), requestTime + CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}

	}
	
	/**
	 * processRestaurantOrderConfirmation()
	 */
	private void processReceiveRestaurantOrderConfirmation(SimEvent ev) {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received order confirmation from restaurant "
				+ CloudSim.getEntityName(ev.getSource()));

		// update counters
		this.restaurantRequestsTotalMessages += 1;
	}

	/**
	 * processRestaurantOrderConfirmation()
	 */
	private void processReceiveRestaurantOrderReady(SimEvent ev) {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received order ready from restaurant "
				+ CloudSim.getEntityName(ev.getSource()));

		// update counters
		this.restaurantRequestsTotalMessages += 1;

		// calculate the service time of the request
		calculateRestaurantRequestResponseTime();

		// schedule the next request
		scheduleRestaurantRequest();
	}

	private void calculateRestaurantRequestResponseTime() {
		// calculate the service time of the request
		double serviceTime = ((CloudSim.clock()
				- lstRestaurantRequestsResponseTime.get(lstRestaurantRequestsResponseTime.size() - 1))
				+ CloudSim.getMinTimeBetweenEvents());

		// update service time of the request
		this.lstRestaurantRequestsResponseTime.set(lstRestaurantRequestsResponseTime.size() - 1, serviceTime);
	}

	/**
	 * processFindNearestParking() to handle a reuqest for finding nearest parking,
	 * based on the vehicle type
	 */
	private void processSearchNearestParking() {
		/*
		 * Log.printLine(CloudSim.clock() + ": [" + this.getName() +
		 * "] is finding the nearest parking for the vehicle " +
		 * CloudSim.getEntityName(vehicleID) );
		 * 
		 * // send request to the Datacenter to find for nearest parking
		 * schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(),
		 * CloudSimTags.IOV_SEARCH_NEAREST_PARKING_EVENT, this.getCurrentLocation());
		 * 
		 * //update counters this.parkingRequests += 1;
		 */
		// record starting time of the new request
		this.lstParkingRequestsResponseTime.add(CloudSim.clock());

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is finding the nearest parking");

		Object[] data = new Object[4];
		data[0] = this.getCurrentLocation().getX();
		data[1] = this.getCurrentLocation().getY();
		data[2] = this.getCurrentLocation().getZ();
		data[3] = this.nearestParkingIndex; // to find the first/next nearest parking

		// send request to the Datacenter to find for nearest parking
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(),
				CloudSimTags.IOV_SEARCH_NEAREST_PARKING_EVENT, data);

		// update counters
		this.countParkingRequests += 1;
		this.parkingRequestsTotalMessages += 1;
	}

	/**
	 * processReceiveParkingData()
	 */
	private void processReceiveParkingData(SimEvent ev) {
		/*
		 * Log.printLine(CloudSim.clock() + ": [" + this.getName() +
		 * "] received data of the nearest parking " + CloudSim.getEntityName(parkingID)
		 * );
		 * 
		 * checkParkingAvailability(parkingID);
		 */
		int parkingID = (int) ev.getData();

		// update counters
		this.parkingRequestsTotalMessages += 1;

		if (parkingID == -1) { // no parking found
			Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] haven't received data of nearest parking ");
		} else {
			Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received data of the nearest parking "
					+ CloudSim.getEntityName(parkingID));

			checkParkingAvailability(parkingID);
		}

	}

	private void checkParkingAvailability(int parkingID) {
		/*
		 * ParkingHolon parking = (ParkingHolon) CloudSim.getEntity(parkingID);
		 * 
		 * Log.printLine(CloudSim.clock() + ": [" + this.getName() +
		 * "] is checking the availability of the parking " +
		 * CloudSim.getEntityName(parkingID) );
		 * 
		 * if (this.getMessagingProtocol() == parking.getMessagingProtocol()) {
		 * sendNow(parkingID, CloudSimTags.IOV_PARKING_CHECK_AVAILABILITY_EVENT); } else
		 * { sendNow(getForwardNodeId(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR); }
		 */
		ParkingHolon parking = (ParkingHolon) CloudSim.getEntity(parkingID);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is checking the availability of the parking "
				+ CloudSim.getEntityName(parkingID));

		if (this.getMessagingProtocol() == parking.getMessagingProtocol()) {
			this.parkingRequestsTotalMessages += 1;
			schedule(parkingID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_PARKING_CHECK_AVAILABILITY_EVENT);
		} else {
			this.parkingRequestsTotalMessages += 2;
			schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}

	}

	private void processReceiveParkingAvailability(SimEvent ev) {
		/*
		 * boolean availability = (boolean) ev.getData();
		 * 
		 * Log.printLine(CloudSim.clock() + ": [" + this.getName() +
		 * "] received the availability " + availability + " of the parking " +
		 * CloudSim.getEntityName(ev.getSource()) );
		 */
		boolean isAvailabile = (boolean) ev.getData();
		int parkingID = ev.getSource();

		ParkingHolon parking = (ParkingHolon) CloudSim.getEntity(parkingID);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received parking  "
				+ CloudSim.getEntityName(parkingID) + " availability " + isAvailabile);

		if (isAvailabile) {
			// update counters
			if (this.getMessagingProtocol() == parking.getMessagingProtocol()) {
				this.fuelRequestsTotalMessages += 1;
			} else {
				this.fuelRequestsTotalMessages += 2;
			}
			// calculate the service time of the request
			calculateParkingRequestResponseTime();

			// reset nearest station index
			this.nearestParkingIndex = 0;

			// schedule the next request
			scheduleParkingRequest();
		} else {
			this.nearestParkingIndex += 1;
			if (this.nearestParkingIndex < 5) {
				Log.printLine(
						CloudSim.clock() + ": [" + this.getName() + "] still searching for the nearest parking...");

				// send request to the Datacenter to find the next nearest parking
				Object[] newData = new Object[4];
				newData[0] = this.getCurrentLocation().getX();
				newData[1] = this.getCurrentLocation().getY();
				newData[2] = this.getCurrentLocation().getZ();
				newData[3] = nearestParkingIndex;

				schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(),
						CloudSimTags.IOV_SEARCH_NEAREST_PARKING_EVENT, newData);
			} else {
				Log.printLine(CloudSim.clock() + ": [" + this.getName()
						+ "] will try later searching for the nearest parking...");

				// reset nearest station index
				this.nearestParkingIndex = 0;
			}
		}
	}

	private void calculateParkingRequestResponseTime() {
		// calculate the service time of the request
		double serviceTime = ((CloudSim.clock()
				- lstParkingRequestsResponseTime.get(lstParkingRequestsResponseTime.size() - 1))
				+ CloudSim.getMinTimeBetweenEvents());

		// update service time of the request
		this.lstParkingRequestsResponseTime.set(lstParkingRequestsResponseTime.size() - 1, serviceTime);
	}

	/**
	 * processCheckTrafficAlert() to check traffic alert from the nearest Traffic
	 * Control Unit
	 */
	private void processSearchNearestTrafficControlUnit() {
		// record starting time of the new request
		this.lstTrafficAlertsResponseTime.add(CloudSim.clock());

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is finding the nearest Traffic Control Unit");

		Object[] data = new Object[4];
		data[0] = this.getCurrentLocation().getX();
		data[1] = this.getCurrentLocation().getY();
		data[2] = this.getCurrentLocation().getZ();
		data[3] = 0; // to find the first nearest traffic control unit

		// send request to the Datacenter to find for nearest TrafficControlUnit and
		// check its alert
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(),
				CloudSimTags.IOV_SEARCH_NEAREST_TRAFFICCONTROLUNIT_EVENT, data);

		// update counters
		this.countTrafficAlertRequests += 1;
		this.trafficAlertsTotalMessages += 1;
	}

	/**
	 * processReceiveTrafficControlUnitData()
	 */
	private void processReceiveTrafficControlUnitData(SimEvent ev) {
		int trafficControlUnitID = (int) ev.getData();

		// update counters
		this.trafficAlertsTotalMessages += 1;

		if (trafficControlUnitID == -1) { // no traffic control unit found
			Log.printLine(CloudSim.clock() + ": [" + this.getName()
					+ "] haven't received data of nearest traffic control unit ");
		} else {
			Log.printLine(
					CloudSim.clock() + ": [" + this.getName() + "] received data of the nearest traffic control unit "
							+ CloudSim.getEntityName(trafficControlUnitID));

			checkTrafficAlert(trafficControlUnitID);
		}
	}

	/**
	 * processCheckTrafficAlert() to check traffic alert from the nearest Traffic
	 * Control Unit
	 */
	private void checkTrafficAlert(int trafficControlUnitID) {
		/*
		 * Log.printLine(CloudSim.clock() + ": [" + this.getName() +
		 * "] is checking the traffic alert ");
		 * 
		 * // send request to the Datacenter to find for nearest TrafficControlUnit and
		 * check its alert schedule(this.getForwardNodeId(),
		 * CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CHECK_TRAFFICALERT,
		 * this.getCurrentLocation());
		 */
		TrafficControlUnitHolon trafficControlUnit = (TrafficControlUnitHolon) CloudSim.getEntity(trafficControlUnitID);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is checking for traffic alert "
				+ CloudSim.getEntityName(trafficControlUnitID));

		if (this.getMessagingProtocol() == trafficControlUnit.getMessagingProtocol()) {
			this.trafficAlertsTotalMessages += 1;
			schedule(trafficControlUnitID, CloudSim.getMinTimeBetweenEvents(),
					CloudSimTags.IOV_TRAFFIC_CHECK_ALERT_EVENT);
		} else {
			this.trafficAlertsTotalMessages += 2;
			schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}

	}

	/**
	 * processReceiveTrafficAlertData()
	 */
	private void processReceiveTrafficAlertData(SimEvent ev) {
		/*
		 * boolean trafficAlert = (boolean) ev.getData();
		 * 
		 * Log.printLine(CloudSim.clock() + ": [" + this.getName() +
		 * "] received the traffic alert set to " + trafficAlert);
		 */
		boolean isTrafficAlert = (boolean) ev.getData();
		int trafficControlUnitID = ev.getSource();

		TrafficControlUnit trafficControlUnit = (TrafficControlUnit) CloudSim.getEntity(trafficControlUnitID);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] received the traffic alert set to "
				+ isTrafficAlert + " from " + CloudSim.getEntityName(trafficControlUnitID));

		if (this.getMessagingProtocol() == trafficControlUnit.getMessagingProtocol()) {
			this.trafficAlertsTotalMessages += 1;
		} else {
			this.trafficAlertsTotalMessages += 2;
		}

		// calculate the service time of the request
		calculateTrafficAlertResponseTime();

		// schedule the next request
		scheduleCheckTrafficAlert();

	}

	private void calculateTrafficAlertResponseTime() {
		// calculate the service time of the request
		double serviceTime = ((CloudSim.clock()
				- lstTrafficAlertsResponseTime.get(lstTrafficAlertsResponseTime.size() - 1))
				+ CloudSim.getMinTimeBetweenEvents());

		// update service time of the request
		this.lstTrafficAlertsResponseTime.set(lstTrafficAlertsResponseTime.size() - 1, serviceTime);
	}

	/*	*//**
			 * scheduleParkingRequestRandom() schedule parking request at a random time
			 *//*
				 * private void scheduleParkingRequestRandom() { Random random = new Random();
				 * double eventTime = random.nextDouble() *
				 * (((ExperimentsConfigurations.EXP_NO_OF_DAYS*24*60*60) -
				 * CloudSim.getMinTimeBetweenEvents()) + CloudSim.getMinTimeBetweenEvents());
				 * schedule(this.getId(), eventTime,
				 * CloudSimTags.IOV_SEARCH_NEAREST_PARKING_EVENT); }
				 */

	/*	*//**
			 * scheduleRestaurantRequestRandom() schedule restaurant request at a random
			 * time
			 *//*
				 * private void scheduleRestaurantRequestRandom() { Random random = new
				 * Random(); double eventTime = random.nextDouble()
				 * (((ExperimentsConfigurations.EXP_NO_OF_DAYS * 24 * 60 * 60) -
				 * CloudSim.getMinTimeBetweenEvents()) + CloudSim.getMinTimeBetweenEvents());
				 * schedule(this.getId(), eventTime,
				 * CloudSimTags.IOV_SEARCH_NEAREST_RESTAURANT_EVENT); }
				 */

	/*	*//**
			 * scheduleCheckTrafficAlertRandom() schedule check traffic alert request at a
			 * random time
			 *//*
				 * private void scheduleCheckTrafficAlertRandom() { Random random = new
				 * Random(); double eventTime = random.nextDouble()
				 * (((ExperimentsConfigurations.EXP_NO_OF_DAYS * 24 * 60 * 60) -
				 * CloudSim.getMinTimeBetweenEvents()) + CloudSim.getMinTimeBetweenEvents());
				 * schedule(this.getId(), eventTime, CloudSimTags.IOV_CHECK_TRAFFICALERT);
				 * 
				 * }
				 */

	/**
	 * scheduleRestaurantRequest() schedule restaurant request
	 */
	private void scheduleRestaurantRequest() {
		// increment experiment day
		if (CloudSim.clock() > (this.currentExpDay * 24 * 60 * 60)) {
			this.currentExpDay = (int) (CloudSim.clock() / (24 * 60 * 60));
		}

		if (this.currentExpDay <= ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			double startDelay = CloudSim.getMinTimeBetweenEvents();
			if (ExperimentsConfigurations.IOV_EXP_ServiceRequests == "Continuous") {
				startDelay = CloudSim.getMinTimeBetweenEvents()
						+ ExperimentsConfigurations.IOV_EXP_ServiceRequests_INTERVAL;
			} else if (ExperimentsConfigurations.IOV_EXP_ServiceRequests == "Random") {
				double random = new Random().nextDouble();
				// min + (r *(max - min))
				startDelay = CloudSim.getMinTimeBetweenEvents()
						+ (random * ((ExperimentsConfigurations.EXP_NO_OF_DAYS * 24 * 60 * 60)
								- CloudSim.getMinTimeBetweenEvents()));
			}
			schedule(this.getId(), startDelay, CloudSimTags.IOV_SEARCH_NEAREST_RESTAURANT_EVENT);
		}
	}

	/**
	 * scheduleParkingRequest() schedule parking request
	 */
	private void scheduleParkingRequest() {
		// increment experiment day
		if (CloudSim.clock() > (this.currentExpDay * 24 * 60 * 60)) {
			this.currentExpDay = (int) (CloudSim.clock() / (24 * 60 * 60));
		}

		if (this.currentExpDay <= ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			double startDelay = CloudSim.getMinTimeBetweenEvents();
			if (ExperimentsConfigurations.IOV_EXP_ServiceRequests == "Continuous") {
				startDelay = CloudSim.getMinTimeBetweenEvents()
						+ ExperimentsConfigurations.IOV_EXP_ServiceRequests_INTERVAL;
			} else if (ExperimentsConfigurations.IOV_EXP_ServiceRequests == "Random") {
				double random = new Random().nextDouble();
				// min + (r *(max - min))
				startDelay = CloudSim.getMinTimeBetweenEvents()
						+ (random * ((ExperimentsConfigurations.EXP_NO_OF_DAYS * 24 * 60 * 60)
								- CloudSim.getMinTimeBetweenEvents()));
			}
			schedule(this.getId(), startDelay, CloudSimTags.IOV_SEARCH_NEAREST_PARKING_EVENT);
		}
	}

	/**
	 * scheduleCheckTrafficAlert() schedule check traffic alert request
	 */
	private void scheduleCheckTrafficAlert() {
		// increment experiment day
		if (CloudSim.clock() > (this.currentExpDay * 24 * 60 * 60)) {
			this.currentExpDay = (int) (CloudSim.clock() / (24 * 60 * 60));
		}

		if (this.currentExpDay <= ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			double startDelay = CloudSim.getMinTimeBetweenEvents();
			if (ExperimentsConfigurations.IOV_EXP_ServiceRequests == "Continuous") {
				startDelay = CloudSim.getMinTimeBetweenEvents()
						+ ExperimentsConfigurations.IOV_EXP_ServiceRequests_INTERVAL;
			} else if (ExperimentsConfigurations.IOV_EXP_ServiceRequests == "Random") {
				double random = new Random().nextDouble();
				// min + (r *(max - min))
				startDelay = CloudSim.getMinTimeBetweenEvents()
						+ (random * ((ExperimentsConfigurations.EXP_NO_OF_DAYS * 24 * 60 * 60)
								- CloudSim.getMinTimeBetweenEvents()));

			}
			// from the traffic control unit
			schedule(this.getId(), startDelay, CloudSimTags.IOV_SEARCH_NEAREST_TRAFFICCONTROLUNIT_EVENT);
			// from the datacenter
			// schedule(this.getId(), eventTime,
			// CloudSimTags.IOV_TRAFFIC_CHECK_ALERT_EVENT);
		}
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

	/**
	 * @return the fuelAlerts
	 */
	public int getCountFuelAlerts() {
		return countFuelAlerts;
	}

	/**
	 * @param fuelAlerts the fuelAlerts to set
	 */
	public void setCountFuelAlerts(int fuelAlerts) {
		this.countFuelAlerts = fuelAlerts;
	}

	/**
	 * @return the fuelRequests
	 */
	public int getCountFuelRequests() {
		return countFuelRequests;
	}

	/**
	 * @param fuelRequests the fuelRequests to set
	 */
	public void setCountFuelRequests(int fuelRequests) {
		this.countFuelRequests = fuelRequests;
	}

	/**
	 * @return the fuelRequestsTotalMessages
	 */
	public int getFuelRequestsTotalMessages() {
		return fuelRequestsTotalMessages;
	}

	/**
	 * @param fuelRequestsTotalMessages the fuelRequestsTotalMessages to set
	 */
	public void setFuelRequestsTotalMessages(int fuelRequestsTotalMessages) {
		this.fuelRequestsTotalMessages = fuelRequestsTotalMessages;
	}

	/**
	 * @return the parkingRequests
	 */
	public int getCountParkingRequests() {
		return countParkingRequests;
	}

	/**
	 * @param parkingRequests the parkingRequests to set
	 */
	public void setCountParkingRequests(int parkingRequests) {
		this.countParkingRequests = parkingRequests;
	}

	/**
	 * @return the parkingRequestsTotalMessages
	 */
	public int getParkingRequestsTotalMessages() {
		return parkingRequestsTotalMessages;
	}

	/**
	 * @param parkingRequestsTotalMessages the parkingRequestsTotalMessages to set
	 */
	public void setParkingRequestsTotalMessages(int parkingRequestsTotalMessages) {
		this.parkingRequestsTotalMessages = parkingRequestsTotalMessages;
	}

	/**
	 * @return the restaurantRequests
	 */
	public int getCountRestaurantRequests() {
		return countRestaurantRequests;
	}

	/**
	 * @param restaurantRequests the restaurantRequests to set
	 */
	public void setCountRestaurantRequests(int restaurantRequests) {
		this.countRestaurantRequests = restaurantRequests;
	}

	/**
	 * @return the restaurantRequestsTotalMessages
	 */
	public int getRestaurantRequestsTotalMessages() {
		return restaurantRequestsTotalMessages;
	}

	/**
	 * @param restaurantRequestsTotalMessages the restaurantRequestsTotalMessages to set
	 */
	public void setRestaurantRequestsTotalMessages(int restaurantRequestsTotalMessages) {
		this.restaurantRequestsTotalMessages = restaurantRequestsTotalMessages;
	}

	/**
	 * @return the trafficAlertsTotalMessages
	 */
	public int getTrafficAlertsTotalMessages() {
		return trafficAlertsTotalMessages;
	}

	/**
	 * @param trafficAlertsTotalMessages the trafficAlertsTotalMessages to set
	 */
	public void setTrafficAlertsTotalMessages(int trafficAlertsTotalMessages) {
		this.trafficAlertsTotalMessages = trafficAlertsTotalMessages;
	}

	/**
	 * @return the trafficAlertRequests
	 */
	public int getCountTrafficAlertRequests() {
		return countTrafficAlertRequests;
	}

	/**
	 * @param trafficAlertRequests the trafficAlertRequests to set
	 */
	public void setCountTrafficAlertRequests(int trafficAlertRequests) {
		this.countTrafficAlertRequests = trafficAlertRequests;
	}

	/**
	 * @return the lstFuelRequestsResponseTime
	 */
	public List<Double> getLstFuelRequestsResponseTime() {
		return lstFuelRequestsResponseTime;
	}

	/**
	 * @return the list of restaurantRequestsResponseTime
	 */
	public List<Double> getLstRestaurantRequestsResponseTime() {
		return lstRestaurantRequestsResponseTime;
	}

	/**
	 * @return the lstParkingRequestsResponseTime
	 */
	public List<Double> getLstParkingRequestsResponseTime() {
		return lstParkingRequestsResponseTime;
	}

	/**
	 * @return the lstTrafficAlertsResponseTime
	 */
	public List<Double> getLstTrafficAlertsResponseTime() {
		return lstTrafficAlertsResponseTime;
	}

	/**
	 * @return the average of the list of fuelRequestsResponseTime
	 */
	public double getFuelRequestsAverageResponseTime() {
		double average = lstFuelRequestsResponseTime
				.stream()
				.mapToDouble(d -> d)
				.average()
				.orElse(CloudSim.getMinTimeBetweenEvents());
		return average;
	}

	/**
	 * @return the average of the list of restaurantRequestsResponseTime
	 */
	public double getRestaurantRequestsAverageResponseTime() {
		
		  double average = lstRestaurantRequestsResponseTime 
				  .stream() 
				  .mapToDouble(d -> d) 
				  .average() 
				  .orElse(CloudSim.getMinTimeBetweenEvents());
		 
		return average;
	}

	/**
	 * @return the average of the list of parkingRequestsResponseTime
	 */
	public double getParkingRequestsAverageResponseTime() {
		double average = lstParkingRequestsResponseTime
				.stream()
				.mapToDouble(d -> d)
				.average()
				.orElse(CloudSim.getMinTimeBetweenEvents());
		return average;
	}

	/**
	 * @return the average of the list of trafficAlertsResponseTime
	 */
	public double getTrafficAlertsAverageResponseTime() {
		double average = lstTrafficAlertsResponseTime
				.stream()
				.mapToDouble(d -> d)
				.average()
				.orElse(CloudSim.getMinTimeBetweenEvents());
		return average;
	}

}
