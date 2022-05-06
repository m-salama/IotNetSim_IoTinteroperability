/**
 * 
 */
package org.cloudbus.iotnetsim.iov.holon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeMobile;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iot.nodes.MessagingProtocol;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTNodeHolon;
import org.cloudbus.iotnetsim.iov.IoVNodeType;
import org.cloudbus.iotnetsim.network.NetConnection;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import dionasys.holon.datamodel.HolonDataModel;
import dionasys.holon.datamodel.HolonServiceModel;
import dionasys.holon.datamodel.OntologyParser;
import dionasys.holon.datamodel.ParameterModel;
import dionasys.holon.datamodel.TypeMapper;
import dionasys.holon.datamodel.Types;

/**
 * @author m.salama
 *
 */
public class UserSmartPhoneHolon extends IoTNodeHolon implements IoTNodeMobile {

	private Location currentLocation;
	private String vehicleName;
	private Map<IoVNodeType, HolonDataModel> nodeHolons;
	private MessagingProtocol messagingProtocol;
	private Workflow workflow;
	private int connectedVehicleID;
	private boolean isAvailable;

	public UserSmartPhoneHolon(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	
	public UserSmartPhoneHolon(String name, String vehicleName, Location location, IoTNodeType nodeType,
			NetConnection connection, IoTNodePower power, MessagingProtocol messagingProtocol, String forwardNodeName) {
		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub

		this.currentLocation = location;
		this.vehicleName = vehicleName;
		this.currentLocation = location;
		this.isAvailable = true;
		this.nodeHolons = new HashMap<>();
		this.messagingProtocol = messagingProtocol;
		workflow = new Workflow(this);
		holon.setHolonDataModel(createDataModel(name, location, messagingProtocol));
		holon.createOntology();
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");
		// request vehicle holon
		// connect to the datacenter
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is connecting to the Datacenter " 
				+ CloudSim.getEntityName(getForwardNodeId())
				);
		schedule(dataCentre.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_HOLON_REQUEST_HOLON_BY_ID,
				vehicleName);
		schedule(this.getId(), CloudSim.getMinTimeBetweenEvents() *100 , CloudSimTags.IOV_HOLON_WORKFLOW);
		send(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_NODE_CONNECTION_EVENT, this.isAvailable);

		//System.out.println(this.getName() + ": id = " + this.getId());
	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is shutting down...");
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Execute sending sensor data
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
		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}
	}

	private void processWorkflowEvent(SimEvent ev) {
		boolean allNodesConnected = true;
		for (IoVNodeType type : workflow.getRequiredNodes()) {
			if (nodeHolons.get(type) == null) {
				allNodesConnected = false;
				schedule(dataCentre.getId(), CloudSim.getMinTimeBetweenEvents(),
						CloudSimTags.IOV_HOLON_REQUEST_HOLON_BY_TYPE, type.toString());
			}
		}
		if (allNodesConnected) {
			workflow.execute();
		} else {
			schedule(this.getId(), CloudSim.getMinTimeBetweenEvents() * 4, CloudSimTags.IOV_HOLON_WORKFLOW);
		}
		
		//schedule next workflow execution
		//schedule(this.getId(), CloudSim.getMinTimeBetweenEvents() * 100, CloudSimTags.IOV_HOLON_WORKFLOW);
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
				case "FUEL_VEHICLE":
					nodeHolons.put(IoVNodeType.VEHICLE, holonModel);
					break;
				case "STATION":
					nodeHolons.put(IoVNodeType.FUEL_STATION, holonModel);
					break;
				case "PARKING":
					nodeHolons.put(IoVNodeType.PARKING, holonModel);
					break;
				case "RESTAURANT":
					nodeHolons.put(IoVNodeType.RESTAURANT, holonModel);
					break;
				case "TRAFFIC_CONTROL_UNIT":
					nodeHolons.put(IoVNodeType.TRAFFIC_CONTROL_UNIT, holonModel);
					break;
				}
			}
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();

		}

	}

	public Object callService(Object[] arguments, IoVNodeType holonType, int serviceAnnotation) {
	
		// using the annotation get the service from the holon data model
		HolonDataModel dataModel = this.nodeHolons.get(holonType);
		HolonServiceModel service = dataModel.getServiceByAnnotation(serviceAnnotation);
		
		
		//mediation
		String myMessagingProtocol = dataModel.getData("messagingProtocol");
		String entityMessagingProtocol = this.getHolon().getHolonDataModel().getData("messagingProtocol");
		if(myMessagingProtocol.equals(entityMessagingProtocol)){
			sytheizeMediator(myMessagingProtocol,entityMessagingProtocol);
		}
		
		// Rest service call is not supported by the simulator so obtain the node
		// reference from CloudSim using the node name
		IoTNodeHolon node = (IoTNodeHolon) CloudSim.getEntity(dataModel.getData("name"));
		Object[] carId = new Object[1];
		arguments[0] = Integer.valueOf(CloudSim.getEntityId(vehicleName));

		int requiredArguments = 0;
		if (service.getParameters() != null) {
			requiredArguments = service.getParameters().size();
		}
		try {
			if (arguments != null && arguments.length < requiredArguments) {
				Log.printLine(getName() + ": Unable to cal service [" + service.getName()
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
			
			System.out.println("SmartPhone: method = " + method);

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
		service2.setUrl("http://10.10.10.2/getLocation");
		service2.setReturnType(Types.STRING);

		HolonServiceModel service3 = new HolonServiceModel();
		service3.setName("getMessagingProtocol");
		service3.setCost(0);
		service3.setUrl("http://10.10.10.2/getMessagingProtocol");
		service3.setReturnType(Types.STRING);

		ArrayList<HolonServiceModel> services = new ArrayList<>();
		services.add(service2);
		services.add(service3);
		dataModel.setServices(services);

		return dataModel;
	}/**
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
		
		IoTNodeHolon station = (IoTNodeHolon) CloudSim.getEntity(stationID);
		Location stationLocation = station.getLocation();
		
		// calculate distance to the station
		double distanceToStation = Math.sqrt(
				(stationLocation.getX()-currentLocation.getX())*(stationLocation.getX()-currentLocation.getX()) + 
				(stationLocation.getY()-currentLocation.getY())*(stationLocation.getY()-currentLocation.getY())
				);
		// calculate time to the station
		VehicleHolon vehicle = (VehicleHolon) CloudSim.getEntity(connectedVehicleID);
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
		StationHolon station = (StationHolon) CloudSim.getEntity(stationID);
		
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
	
	/**
	 * @param messagingProtocol the messagingProtocol to set
	 */
	public void setMessagingProtocol(MessagingProtocol messagingProtocol) {
		this.messagingProtocol = messagingProtocol;
	}
	
	/**
	 * @param return the messagingProtocol
	 */
	public MessagingProtocol getMessagingProtocol() {
		return messagingProtocol;
	}
	
	
}
