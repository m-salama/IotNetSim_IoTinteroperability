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

public class PetrolStationHolon extends IoTNodeHolon  {

	private boolean isAvailable;
	private double price;		//cost per unit (e.g. per litre of fuel)
	
	protected double priceChangeInterval;	// interval for changing price every x seconds		

	protected int currentExpDay;


	public PetrolStationHolon(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public PetrolStationHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			IoTDatacenterHolon dataCentre,
			String forwardNodeName, MessagingProtocol msgProtocol) {

		super(name, location, nodeType, connection, power, dataCentre, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub
		holon.setHolonDataModel(createDataModel(name, location, msgProtocol));
		holon.createOntology();
	}

	public PetrolStationHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			IoTDatacenterHolon dataCentre,
			String forwardNodeName, MessagingProtocol msgProtocol,
			double price, double price_change_interval) {

		super(name, location, nodeType, connection, power,dataCentre, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub

		this.isAvailable = true;
		this.price = price;
		this.priceChangeInterval = price_change_interval;
		
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
		schedule(this.getId(), this.priceChangeInterval, CloudSimTags.IOV_PETROLSTATION_CHANGE_PRICE_EVENT);

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

		case CloudSimTags.IOV_PETROLSTATION_CHANGE_PRICE_EVENT:
			processChangePrice();
			break;
		case CloudSimTags.IOV_PETROLSTATION_CHECK_PRICE_EVENT:
			processCheckPrice(ev.getSource());
			break;
			
			// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}

	/**
	 * processChangePrice()
	 */
	private void processChangePrice() {
		// get random price within a range -- min + (randomValue * (max - min))
		double random = new Random().nextDouble();  		
		double rPrice = Math.abs((this.price-1.0) + (random * ((this.price+1.0) - (this.price-1.0))));

		this.price = rPrice;
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is changing the price" 
				+ " of day " + currentExpDay
				+ " to " + Double.toString(this.price) 
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		if (CloudSim.clock() >= currentExpDay*24*60*60) {
			currentExpDay +=1;
		}
		if (currentExpDay < ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			//schedule the next change on the next day
			schedule(this.getId(), ((currentExpDay+1)*24*60*60), CloudSimTags.IOV_PETROLSTATION_CHANGE_PRICE_EVENT);
		}	
	}
	
	/**
	 * processCheckPrice()
	 */
	private void processCheckPrice(int userID) {
		// send the price to the user
		schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RECEIVE_PETROLSTATION_PRICE_EVENT, this.price);	
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

	public double getPriceChangeInterval() {
		return priceChangeInterval;
	}

	public void setPriceChangeInterval(double priceChangeInterval) {
		this.priceChangeInterval = priceChangeInterval;
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
		dataModel.putData("priceChangeInterval", this.priceChangeInterval + "");
		dataModel.putData("currentExpDay", currentExpDay + "");

		HolonServiceModel service1 = new HolonServiceModel();
		service1.setName("getPrice");
		service1.setCost(0);
		service1.setUrl("getPrice");
		service1.setReturnType(Types.DOUBLE);
		service1.setAnnotation(IoVHolonAnnotations.GET_PRICE);

		HolonServiceModel service2 = new HolonServiceModel();
		service2.setName("processChangePrice");
		service2.setCost(0);
		service2.setUrl("processChangePrice");
		ParameterModel par1 = new ParameterModel("vehicleId", Types.INTEGER);
		ArrayList<ParameterModel> parameters = new ArrayList<>();
		parameters.add(par1);
		service2.setParameters(parameters);
		service2.setReturnType(Types.VOID);
		service2.setAnnotation(IoVHolonAnnotations.CHANGE_PRICE);

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

	
}