package org.cloudbus.iotnetsim.iov.holon;

import java.util.ArrayList;
import java.util.Random;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iot.nodes.MessagingProtocol;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTHolon;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTNodeHolon;
import org.cloudbus.iotnetsim.iov.IoVNodeType;
import org.cloudbus.iotnetsim.network.NetConnection;

import dionasys.holon.datamodel.HolonDataModel;
import dionasys.holon.datamodel.HolonServiceModel;
import dionasys.holon.datamodel.Types;
import experiments.configurations.ExperimentsConfigurations;

/**
 * Class ParkingHolon
 * 
 * @author Maria Salama
 * @author elhabbash
 * 
 */

public class ParkingHolon extends IoTNodeHolon implements IoTHolon {

	// total number of slots of the parking
	protected int totalParkingSlots;

	// number of parking slots available
	protected int availableParkingSlots;

	// interval for changing parking availability every x seconds
	protected double parkingChangeInterval;

	protected int currentExpDay;
	protected int currentChangeIndex;

	// set to true of the number of parking slots available is less than the total
	// number of parking slots
	protected boolean isAvailable;

	public ParkingHolon(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public ParkingHolon(String name, Location location, IoTNodeType nodeType, NetConnection connection,
			IoTNodePower power, MessagingProtocol messagingProtocol, String forwardNodeName) {
		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
		holon.setHolonDataModel(createDataModel(name, location, messagingProtocol));
		holon.createOntology();
	}

	public ParkingHolon(String name, Location location, IoTNodeType nodeType, NetConnection connection,
			IoTNodePower power, String forwardNodeName, MessagingProtocol messagingProtocol, int total_parkingSlots,
			double parking_interval) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub

		this.totalParkingSlots = total_parkingSlots;
		this.availableParkingSlots = total_parkingSlots;
		this.parkingChangeInterval = parking_interval;
		this.currentExpDay = 1;
		this.currentChangeIndex = 0;
		holon.setHolonDataModel(createDataModel(name, location, messagingProtocol));
		holon.createOntology();
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");

		// schedule the event for parking to register the holon
		schedule(dataCentre.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_HOLON_REGISTER_HOLON, holon);
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_NODE_CONNECTION_EVENT, isAvailable);

		// schedule the first event for parking
		schedule(this.getId(), parkingChangeInterval, CloudSimTags.IOV_PARKING_CHANGE_AVAILABILITY);
	}

	@Override
	public void shutdownEntity() {
		// if we need to destroy the holon

		// TODO Auto-generated method stub
		Log.printLine(getName() + " is shutting down...");

	}

	@Override
	public void processEvent(SimEvent ev) {
		super.processEvent(ev);
		// TODO Auto-generated method stub
		switch (ev.getTag()) {
		// Execute sending sensor data
		case CloudSimTags.IOV_PARKING_CHANGE_AVAILABILITY:
			processChangeParkingAvailability();
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}
	}

	public void processChangeParkingAvailability() {

		// get new availability
		this.availableParkingSlots = getNewAvailabilityRandom();

		this.isAvailable = (availableParkingSlots > 0);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is changing parking availability" + " for Day "
				+ currentExpDay + " to " + Integer.toString(availableParkingSlots) + " and sending data to "
				+ CloudSim.getEntityName(getForwardNodeId()));

		// send data to Datacenter
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT,
				isAvailable);

		if (currentExpDay < ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			// schedule the next event for sending data
			scheduleNextChange();
		}
		//System.out.println("Parking changed");
	}

	private int getNewAvailabilityRandom() {
		Random random = new Random();
		int newAvailability = random.nextInt(this.totalParkingSlots);

		if (currentChangeIndex < (24 / (this.parkingChangeInterval / 60 / 60)) - 1) { // to get all the changes for this
																						// day
			currentChangeIndex += 1;
		} else { // reset the change index to 0 and start a new day
			currentExpDay += 1;
			currentChangeIndex = 0;
		}
		return newAvailability;
	}

	private void scheduleNextChange() {
		schedule(this.getId(), this.getParkingChangeInterval(), CloudSimTags.IOV_PARKING_CHANGE_AVAILABILITY);
	}

	private HolonDataModel createDataModel(String name, Location location, MessagingProtocol messagingProtocol) {

		HolonDataModel dataModel = new HolonDataModel();
		dataModel.putData("name", name);
		dataModel.putData("type", this.getNodeType().toString());
		dataModel.putData("latitude", location.getX() + "");
		dataModel.putData("longitude", location.getY() + "");
		dataModel.putData("messagingProtocol", messagingProtocol.toString());
		dataModel.putData("totalParkingSlots", totalParkingSlots + "");
		dataModel.putData("parking_interval", parkingChangeInterval + "");
		dataModel.putData("connecitonType", this.getConnection().getConnectionType().toString());
		dataModel.putData("powerType", this.getPower().getPowerType().toString());
		dataModel.putData("availableParkingSlots", availableParkingSlots + "");
		dataModel.putData("parkingChangeInterval", parkingChangeInterval + "");
		dataModel.putData("currentExpDay", currentExpDay + "");
		dataModel.putData("currentChangeIndex", currentChangeIndex + "");

		HolonServiceModel service1 = new HolonServiceModel();
		service1.setName("getTotalParkingSlots");
		service1.setCost(0);
		service1.setUrl("http://10.10.10.2/getTotalParkingSlots");
		service1.setReturnType(Types.DOUBLE);

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
		
		HolonServiceModel service4 = new HolonServiceModel();
	    service4.setName("processChangeParkingAvailability");
	    service4.setCost(0);
	    service4.setUrl("processChangeParkingAvailability");
	    service4.setReturnType(Types.BOOLEAN);
	    service4.setAnnotation(IoVHolonAnnotations.BOOK_PARKING_SLOT);

		ArrayList<HolonServiceModel> services = new ArrayList<>();
		services.add(service1);
		services.add(service2);
		services.add(service3);
		services.add(service4);
		dataModel.setServices(services);

		return dataModel;
	}

	/**
	 * @return the totalParkingSlots
	 */
	public int getTotalParkingSlots() {
		return totalParkingSlots;
	}

	/**
	 * @param totalParkingSlots the totalParkingSlots to set
	 */
	public void setTotalParkingSlots(int totalParkingSlots) {
		this.totalParkingSlots = totalParkingSlots;
	}

	/**
	 * @return the availableParkingSlots
	 */
	public int getAvailableParkingSlots() {
		return availableParkingSlots;
	}

	/**
	 * @param availableParkingSlots the availableParkingSlots to set
	 */
	public void setAvailableParkingSlots(int availableParkingSlots) {
		this.availableParkingSlots = availableParkingSlots;
	}

	/**
	 * @return the parkingChangeInterval
	 */
	public double getParkingChangeInterval() {
		return parkingChangeInterval;
	}

	/**
	 * @param parkingChangeInterval the parkingChangeInterval to set
	 */
	public void setParkingChangeInterval(double parkingChangeInterval) {
		this.parkingChangeInterval = parkingChangeInterval;
	}

	/**
	 * @return the currentExpDay
	 */
	public int getCurrentExpDay() {
		return currentExpDay;
	}

	/**
	 * @param currentExpDay the currentExpDay to set
	 */
	public void setCurrentExpDay(int currentExpDay) {
		this.currentExpDay = currentExpDay;
	}

	/**
	 * @return the currentChangeIndex
	 */
	public int getCurrentChangeIndex() {
		return currentChangeIndex;
	}

	/**
	 * @param currentChangeIndex the currentChangeIndex to set
	 */
	public void setCurrentChangeIndex(int currentChangeIndex) {
		this.currentChangeIndex = currentChangeIndex;
	}

}
