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
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTNodeHolon;
import org.cloudbus.iotnetsim.iov.UserSmartPhone;
import org.cloudbus.iotnetsim.network.NetConnection;

import dionasys.holon.datamodel.HolonDataModel;
import dionasys.holon.datamodel.HolonServiceModel;
import dionasys.holon.datamodel.ParameterModel;
import dionasys.holon.datamodel.Types;
import experiments.configurations.ExperimentsConfigurations;

/**
 * Class Station
 * for service station of fuel and electric cars based on the VehicleType
 * 
 * @author Maria Salama
 * @author Abdessalam Elhabbash
 * 
 */

public class ElectricChargingStationHolon extends IoTNodeHolon  {

	private boolean isAvailable;
	protected double availabilityChangeInterval;	// interval for changing availability every x seconds		

	private double price;		//cost per unit (e.g. per litre of fuel)

	protected int currentExpDay;


	public ElectricChargingStationHolon(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public ElectricChargingStationHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			IoTDatacenterHolon dataCentre,
			String forwardNodeName, MessagingProtocol msgProtocol) {

		super(name, location, nodeType, connection, power, dataCentre, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub
		holon.setHolonDataModel(createDataModel(name, location, msgProtocol));
		holon.createOntology();
	}

	public ElectricChargingStationHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power,
			IoTDatacenterHolon dataCentre,
			String forwardNodeName, MessagingProtocol msgProtocol,
			double price, double availability_change_interval) {

		super(name, location, nodeType, connection, power, dataCentre, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub

		this.price = price;
		this.isAvailable = true;
		this.availabilityChangeInterval = availability_change_interval;
		
		this.currentExpDay = 1;
		holon.setHolonDataModel(createDataModel(name, location, msgProtocol));
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
		
		// schedule the first event for station to register the holon
		schedule(dataCentre.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_HOLON_REGISTER_HOLON, holon);
		
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_NODE_CONNECTION_EVENT, isAvailable);

		// schedule the first event for the station to change its availability
		schedule(this.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_ELECTRICCHARGINGSTATION_CHANGE_AVAILABILITY_EVENT);

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

		case CloudSimTags.IOV_ELECTRICCHARGINGSTATION_CHANGE_AVAILABILITY_EVENT:
			processChangeStationvailability();
			break;
		case CloudSimTags.IOV_ELECTRICCHARGINGSTATION_CHECK_AVAILABILITY_EVENT:
			processCheckAvailability(ev);
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

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is changing availability" 
				+ " to " + Boolean.toString(this.isAvailable) 
				+ " during day " + currentExpDay
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		//send update to Datacenter
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, isAvailable);

		if (CloudSim.clock() >= currentExpDay*24*60*60) {
			currentExpDay +=1;
		}
		//schedule the next event for updating the station availability at random time
		scheduleNextAvailabilityChange(); 
	}

	/**
	 * schedule next event for changing station availability 
	 */
	private void scheduleNextAvailabilityChange(){
		if (currentExpDay < ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			if (ExperimentsConfigurations.IOV_EXP_ServiceEntities_CHANGE == "Frequent") {
				schedule(this.getId(), this.availabilityChangeInterval, CloudSimTags.IOV_ELECTRICCHARGINGSTATION_CHANGE_AVAILABILITY_EVENT);
			} else if (ExperimentsConfigurations.IOV_EXP_ServiceEntities_CHANGE == "Random") {
				// get random time within the same day -- min + (randomValue * (max - min))
				double random = new Random().nextDouble();  		
				double rTime = CloudSim.getMinTimeBetweenEvents() + (random * ((24*60*60) - CloudSim.getMinTimeBetweenEvents()));  
				schedule(this.getId(), rTime, CloudSimTags.IOV_ELECTRICCHARGINGSTATION_CHANGE_AVAILABILITY_EVENT);				
			}
		}
	}

	/**
	 * processCheckAvailability()
	 */
	private void processCheckAvailability(SimEvent ev) {
		int userID = ev.getSource();

		UserSmartPhoneHolon user = (UserSmartPhoneHolon) CloudSim.getEntity(userID);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is sending availability " + this.isAvailable
				+ " to " + CloudSim.getEntityName(userID)
				);

		// send the availability to the user
		if (this.getMessagingProtocol() == user.getMessagingProtocol()) {
			schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RECEIVE_ELECTRICCHARGINGSTATION_AVAILABILITY_EVENT, 
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
		dataModel.putData("connecitonType", this.getConnection().getConnectionType().toString());
		dataModel.putData("powerType", this.getPower().getPowerType().toString());
		dataModel.putData("isAvailable", isAvailable + "");
		dataModel.putData("availabilityChangeInterval", this.availabilityChangeInterval + "");
		dataModel.putData("currentExpDay", currentExpDay + "");

		HolonServiceModel service1 = new HolonServiceModel();
		service1.setName("processChangeStationvailability");
		service1.setCost(0);
		service1.setUrl("processChangeStationvailability");
		service1.setReturnType(Types.VOID);
		service1.setAnnotation(IoVHolonAnnotations.CHANGE_AVAILABILITY);

		HolonServiceModel service2 = new HolonServiceModel();
		service2.setName("processCheckAvailability");
		service2.setCost(0);
		service2.setUrl("processCheckAvailability");
		ParameterModel par1 = new ParameterModel("vehicleId", Types.INTEGER);
		ArrayList<ParameterModel> parameters = new ArrayList<>();
		parameters.add(par1);
		service2.setParameters(parameters);
		service2.setReturnType(Types.BOOLEAN);
		service2.setAnnotation(IoVHolonAnnotations.IS_ELEC_AVAILABLE);

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

	public double getAvailabilityChangeInterval() {
		return availabilityChangeInterval;
	}

	public void setAvailabilityChangeInterval(double availabilityChangeInterval) {
		this.availabilityChangeInterval = availabilityChangeInterval;
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


}