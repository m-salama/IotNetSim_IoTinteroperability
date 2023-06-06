package org.cloudbus.iotnetsim.iov.holon;

import java.util.ArrayList;
import java.util.Random;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.holon.IoTDatacenterHolon;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iot.nodes.MessagingProtocol;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTHolon;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTNodeHolon;
import org.cloudbus.iotnetsim.iov.IoVNodeType;
import org.cloudbus.iotnetsim.iov.UserSmartPhone;
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
	
	protected int totalParkingSlots;	// total number of slots of the parking
	protected int availableParkingSlots;	// number of parking slots available
	protected boolean isAvailable;	// set to true of the number of parking slots available  is less than the total number of parking slots 
	protected double availabilityChangeInterval;	// interval for changing availability every x seconds		

	protected int currentExpDay;
	protected int currentChangeIndex;

	
	public ParkingHolon(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public ParkingHolon(String name, Location location, IoTNodeType nodeType, NetConnection connection,
			IoTNodePower power, IoTDatacenterHolon dataCentre, String forwardNodeName, MessagingProtocol messagingProtocol) {
		super(name, location, nodeType, connection, power, dataCentre, forwardNodeName, messagingProtocol);
		
		holon.setHolonDataModel(createDataModel(name, location, messagingProtocol));
		holon.createOntology();
	}

	public ParkingHolon(String name, Location location, IoTNodeType nodeType, NetConnection connection,
			IoTNodePower power, IoTDatacenterHolon dataCentre, 
			String forwardNodeName, MessagingProtocol messagingProtocol, 
			int total_parkingSlots, double availability_change_interval) {

		super(name, location, nodeType, connection, power, dataCentre, forwardNodeName,messagingProtocol);

		this.totalParkingSlots = total_parkingSlots;
		this.availableParkingSlots = total_parkingSlots;
		this.isAvailable = true;
		this.availabilityChangeInterval = availability_change_interval;

		this.currentExpDay = 1;
		this.currentChangeIndex = 0;

		holon.setHolonDataModel(createDataModel(name, location, messagingProtocol));
		holon.createOntology();
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");
		// connect to the datacenter
				Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is connecting to the Datacenter " 
						+ CloudSim.getEntityName(getForwardNodeId())
						);

		// schedule the event for parking to register the holon
		schedule(dataCentre.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_HOLON_REGISTER_HOLON, holon);
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_NODE_CONNECTION_EVENT, isAvailable);

		// schedule the first event for parking
		schedule(this.getId(), this.availabilityChangeInterval, CloudSimTags.IOV_PARKING_CHANGE_AVAILABILITY_EVENT);
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
		case CloudSimTags.IOV_PARKING_CHANGE_AVAILABILITY_EVENT:
			processChangeParkingAvailability();
			break;
		case CloudSimTags.IOV_PARKING_CHECK_AVAILABILITY_EVENT:
			processCheckAvailability(ev);
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}
	}

	/**
	 * processChangeParkingAvailability()
	 */
	private void processChangeParkingAvailability() {	
		if (ExperimentsConfigurations.IOV_EXP_ServiceEntities_CHANGE == "Frequent") {
			if (this.isAvailable) {
				this.availableParkingSlots = this.totalParkingSlots;
			} else {
				this.availableParkingSlots = 0;
			}
		} else if (ExperimentsConfigurations.IOV_EXP_ServiceEntities_CHANGE == "Random") {
			// get random availability
			Random random = new Random();  		
			int newAvailability = Math.abs(random.nextInt(this.totalParkingSlots));  

			this.availableParkingSlots = Math.abs(random.nextInt(this.totalParkingSlots)); 
		}

		this.isAvailable = (this.availableParkingSlots > 0);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is changing parking availability" 
				+ " for Day " + currentExpDay
				+ " to " + Integer.toString(availableParkingSlots) 
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		// send data to Datacenter
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, isAvailable);

		if (currentChangeIndex < (24/(this.availabilityChangeInterval/60/60))-1) {		//to get all the changes for this day
			currentChangeIndex +=1;
		} else {	//reset the change index to 0 and start a new day
			currentExpDay +=1;
			currentChangeIndex = 0;
		}
		
		// schedule the next event for sending data 
		if (currentExpDay < ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			schedule(this.getId(), this.availabilityChangeInterval, CloudSimTags.IOV_PARKING_CHANGE_AVAILABILITY_EVENT);
		}
	}

	/**
	 * processCheckAvailability()
	 */
	private void processCheckAvailability(SimEvent ev) {
		int userID = ev.getSource();

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is sending availability " + this.isAvailable
				+ " to " + CloudSim.getEntityName(userID)
				);

		UserSmartPhoneHolon user = (UserSmartPhoneHolon) CloudSim.getEntity(userID);

		// send the availability to the user
		if (this.getMessagingProtocol() == user.getMessagingProtocol()) {
			schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RECEIVE_PARKING_AVAILABILITY_EVENT, 
					this.isAvailable);
		} else {
			schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}
	}
	

	private HolonDataModel createDataModel(String name, Location location, MessagingProtocol messagingProtocol) {

		HolonDataModel dataModel = new HolonDataModel();
		dataModel.putData("name", name);
		dataModel.putData("type", this.getNodeType().toString());
		dataModel.putData("latitude", location.getX() + "");
		dataModel.putData("longitude", location.getY() + "");
		dataModel.putData("messagingProtocol", messagingProtocol.toString());
		dataModel.putData("totalParkingSlots", totalParkingSlots + "");
		dataModel.putData("connecitonType", this.getConnection().getConnectionType().toString());
		dataModel.putData("powerType", this.getPower().getPowerType().toString());
		dataModel.putData("availableParkingSlots", this.availableParkingSlots + "");
		dataModel.putData("availabilityChangeInterval", this.availabilityChangeInterval + "");
		dataModel.putData("currentExpDay", currentExpDay + "");
		dataModel.putData("currentChangeIndex", currentChangeIndex + "");

		HolonServiceModel service1 = new HolonServiceModel();
		service1.setName("getAvailableParkingSlots");
		service1.setCost(0);
		service1.setUrl("getAvailableParkingSlots");
		service1.setReturnType(Types.INTEGER);
		service1.setAnnotation(IoVHolonAnnotations.AVAILABLE_PARKING_SLOTS);

		HolonServiceModel service2 = new HolonServiceModel();
		service2.setName("getLocation");
		service2.setCost(0);
		service2.setUrl("getLocation");
		service2.setReturnType(Types.STRING);
		service2.setAnnotation(IoVHolonAnnotations.GET_LOCATION);

		HolonServiceModel service3 = new HolonServiceModel();
		service3.setName("getMessagingProtocol");
		service3.setCost(0);
		service3.setUrl("getMessagingProtocol");
		service3.setReturnType(Types.STRING);
		

		ArrayList<HolonServiceModel> services = new ArrayList<>();
		services.add(service1);
		services.add(service2);
		services.add(service3);
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
	public double getAvailabilityChangeInterval() {
		return availabilityChangeInterval;
	}

	/**
	 * @param parkingChangeInterval the parkingChangeInterval to set
	 */
	public void setAvailabilityChangeInterval(double availabilityChangeInterval) {
		this.availabilityChangeInterval = availabilityChangeInterval;
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
