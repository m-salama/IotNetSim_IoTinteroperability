package org.cloudbus.iotnetsim.iov.holon;

import java.util.ArrayList;
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
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTNodeHolon;
import org.cloudbus.iotnetsim.iov.IoVNodeType;
import org.cloudbus.iotnetsim.iov.VehicleType;
import org.cloudbus.iotnetsim.network.NetConnection;

import dionasys.holon.datamodel.HolonDataModel;
import dionasys.holon.datamodel.HolonServiceModel;
import dionasys.holon.datamodel.ParameterModel;
import dionasys.holon.datamodel.Types;
import experiments.configurations.*;

/**
 * Class Station for service station of fuel and electric cars based on the
 * VehicleType
 * 
 * @author Maria Salama
 * @author Abdessalam Elhabbash
 */

public class StationHolon extends IoTNodeHolon {

	// vehicleType enum attribute to specify if the station offers fuel or electric
	// charging for vehicles
	private VehicleType vehicleType;
	private MessagingProtocol messagingProtocol;
	private boolean isAvailable;
	private double price; // cost per unit (e.g. per litre of fuel)

	protected int currentExpDay;

	public StationHolon(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public StationHolon(String name, Location location, IoTNodeType nodeType, NetConnection connection,
			IoTNodePower power, MessagingProtocol messagingProtocol, String forwardNodeName) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		holon.setHolonDataModel(createDataModel(name, location, messagingProtocol));
		holon.createOntology();
	}

	public StationHolon(String name, Location location, IoTNodeType nodeType, NetConnection connection,
			IoTNodePower power, MessagingProtocol messagingProtocol, String forwardNodeName, VehicleType vType, double price) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub

		this.vehicleType = vType;
		this.isAvailable = true;
		this.price = price;
		this.messagingProtocol = messagingProtocol;
		holon.setHolonDataModel(createDataModel(name, location, messagingProtocol));
		holon.createOntology();
	}

	@Override
	public void startEntity() {

		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");

		// connect to the datacenter
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is connecting to the Datacenter "
				+ CloudSim.getEntityName(getForwardNodeId()));

		// schedule the first event for station to register the holon
		schedule(dataCentre.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_HOLON_REGISTER_HOLON, holon);

		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_NODE_CONNECTION_EVENT,
				isAvailable);

		// schedule the first event for the station to change its availability
		schedule(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_STATION_CHANGE_AVAILABILITY_EVENT);

		// schedule the first event for the station to change the price
		schedule(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_STATION_CHANGE_PRICE_EVENT);
	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is shutting down...");
	}

	@Override
	public void processEvent(SimEvent ev) {
		super.processEvent(ev);
		// TODO Auto-generated method stub
		switch (ev.getTag()) {
		case CloudSimTags.IOV_STATION_CHANGE_AVAILABILITY_EVENT:
			processChangeStationvailability();
			break;
		case CloudSimTags.IOV_STATION_CHANGE_PRICE_EVENT:
			processChangePriceRandom();
			break;
		case CloudSimTags.IOV_STATION_CHECK_PRICE_EVENT:
			processCheckPrice(ev.getSource());
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

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is changing availability" + " to "
				+ Boolean.toString(this.isAvailable) + " and sending data to "
				+ CloudSim.getEntityName(getForwardNodeId()));

		// send update to Datacenter
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT,
				isAvailable);

		if (CloudSim.clock() >= currentExpDay * 24 * 60 * 60) {
			currentExpDay += 1;
		}
		if (currentExpDay < ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			// schedule the next event for updating the station availability at random time
			scheduleNextAvailabilityChangeRandom();
		}
	}

	/**
	 * processChangePriceRandom()
	 */
	private void processChangePriceRandom() {
		// get random price within a range -- min + (randomValue * (max - min))
		double random = new Random().nextDouble();
		double rPrice = (this.price - 1.0) + (random * ((this.price + 1.0) - (this.price - 1.0)));

		this.price = rPrice;

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is changing the price" + " to "
				+ Double.toString(this.price) + " and sending data to " + CloudSim.getEntityName(getForwardNodeId()));

		// schedule the next change on the next day
		schedule(this.getId(), ((currentExpDay + 1) * 24 * 60 * 60), CloudSimTags.IOV_STATION_CHANGE_PRICE_EVENT);
	}

	/**
	 * processCheckPrice()
	 */
	private void processCheckPrice(int userID) {
		// send the price to the user
		schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RECEIVE_STATION_PRICE_EVENT, this.price);
	}

	/**
	 * schedule next event for changing station availability at random time
	 */
	private void scheduleNextAvailabilityChangeRandom() {
		// get random time within the same day -- min + (randomValue * (max - min))
		double random = new Random().nextDouble();
		double rTime = CloudSim.getMinTimeBetweenEvents()
				+ (random * ((24 * 60 * 60) - CloudSim.getMinTimeBetweenEvents()));

		schedule(this.getId(), rTime, CloudSimTags.IOV_STATION_CHANGE_AVAILABILITY_EVENT);
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
		dataModel.putData("isAvailable", isAvailable + "");
		dataModel.putData("currentExpDay", currentExpDay + "");

		HolonServiceModel service1 = new HolonServiceModel();
		service1.setName("isAvailable");
		service1.setCost(0);
		service1.setUrl("isAvailable");
		service1.setReturnType(Types.DOUBLE);
		service1.setAnnotation(IoVHolonAnnotations.IS_FUEL_AVAILABLE);

		HolonServiceModel service2 = new HolonServiceModel();
		service2.setName("fuelVehicle");
		service2.setCost(0);
		service2.setUrl("fuelVehicle");
		ParameterModel par1 = new ParameterModel("vehicleId", Types.INTEGER);
		ArrayList<ParameterModel> parameters = new ArrayList<>();
		parameters.add(par1);
		service2.setParameters(parameters);
		service2.setReturnType(Types.BOOLEAN);
		service2.setAnnotation(IoVHolonAnnotations.FUEL_VEHICLE);

		HolonServiceModel service3 = new HolonServiceModel();
		service3.setName("getMessagingProtocol");
		service3.setCost(0);
		service3.setUrl("http://10.10.10.2/getMessagingProtocol");
		service3.setReturnType(Types.DOUBLE);

		ArrayList<HolonServiceModel> services = new ArrayList<>();
		services.add(service1);
		services.add(service2);
		services.add(service3);
		dataModel.setServices(services);

		return dataModel;
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

	/**
	 * Fuel a vehicle
	 * 
	 * @param vehcileId
	 */
	public boolean fuelVehicle(Integer vehicleId) {
		System.out.println(this.getName() + ": vehicleId = " + vehicleId.intValue());
		schedule(vehicleId.intValue(), CloudSim.getMinTimeBetweenEvents() * 2, CloudSimTags.IOV_VEHICLE_FUEL_REFILL);
		return true;
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
