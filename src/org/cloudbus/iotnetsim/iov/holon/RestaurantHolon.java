/**
 * 
 */
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
import org.cloudbus.iotnetsim.network.NetConnection;

import dionasys.holon.datamodel.HolonDataModel;
import dionasys.holon.datamodel.HolonServiceModel;
import dionasys.holon.datamodel.Types;
import experiments.configurations.ExperimentsConfigurations;

/**
 * Class Restaurant
 * 
 * @author Maria Salama
 * @author elhabbash
 * 
 */

public class RestaurantHolon extends IoTNodeHolon implements IoTHolon {
	
	// opening time of the restaurant set by the seconds within the day
	protected double openingTime;
	// closing time of the restaurant set by the seconds within the day
	protected double closingTime;
	
	protected boolean isOpen;

	protected int currentExpDay;
	
	public RestaurantHolon(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public RestaurantHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			MessagingProtocol messagingProtocol, String forwardNodeName) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
		holon.setHolonDataModel(createDataModel(name, location,messagingProtocol));
		holon.createOntology();
	}

	public RestaurantHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			MessagingProtocol messagingProtocol, String forwardNodeName,
			double opening_time, double closing_time, boolean open) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
		this.openingTime = opening_time;
		this.closingTime = closing_time;
		this.isOpen = open;
		this.currentExpDay = 1;
		holon.setHolonDataModel(createDataModel(name, location,messagingProtocol));
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
				
		// schedule the first event for parking to register the holon
		schedule(dataCentre.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_HOLON_REGISTER_HOLON,
				holon);

		// schedule event for restaurant opening
		schedule(this.getId(), this.openingTime, CloudSimTags.IOV_RESTAURANT_OPEN);

	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is shutting down...");		
	}

	@Override
	public void processEvent(SimEvent ev) {
		// TODO Auto-generated method stub
		super.processEvent(ev);
		
		switch (ev.getTag()) {
		// Execute sending sensor data 
		case CloudSimTags.IOV_RESTAURANT_OPEN_EVENT:
			processOpenRestaurant();
			break;
		case CloudSimTags.IOV_RESTAURANT_CLOSE_EVENT:
			processCloseRestaurant();
			break;

		// confirm receiving order
		case CloudSimTags.IOV_RESTAURANT_ORDER_EVENT:
			processReceiveOrder(ev.getSource());
			break;
		// confirm booking table
		case CloudSimTags.IOV_RESTAURANT_BOOKING_EVENT:
			processBookTable(ev.getSource());
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
			
		}				
	}

	
	
//	public void bookTable() {
	//	Log.printLine(CloudSim.clock() + ": " + getName() + " table booked");
	//	System.out.println(CloudSim.clock() + ": " + getName() + " table booked");
//	}

	/**
	 * processOpenRestaurant()
	 */
	private void processOpenRestaurant() {
		this.isOpen = true;

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is open"
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		//send data to Datacenter
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, isOpen);

		// schedule event for restaurant closing for later
		schedule(this.getId(), this.closingTime*(currentExpDay*24*60*60), CloudSimTags.IOV_RESTAURANT_CLOSE_EVENT);
	}
	
	/**
	 * processCloseRestaurant()
	 */
	private void processCloseRestaurant() {		
		this.isOpen = false;
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is closed"
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		//send data to Datacenter
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, isOpen);

		if (currentExpDay < ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			// schedule event for restaurant opening for next day
			schedule(this.getId(), this.openingTime*(currentExpDay*24*60*60), CloudSimTags.IOV_RESTAURANT_OPEN_EVENT);		
		}
		currentExpDay +=1;
	}
	
	/**
	 * processReceiveOrder()
	 */

	private void processReceiveOrder(int userID) {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is receiving the order"
				+ " from User " + CloudSim.getEntityName(userID)
				+ " and sending confirmation"
				);
		
		// send confirmation to the user
		schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RESTAURANT_ORDER_CONFIRMATION_EVENT);	
		
		// get a random time for the order preparation
		double preparationTime = getOrderPreparationTimeRandom();
		
		// send notification to the user after the preparation time
		schedule(userID, preparationTime, CloudSimTags.IOV_RESTAURANT_ORDER_READY_EVENT);
	}
	
	/**
	 * processBookTable()
	 */
	private void processBookTable(int userID) {
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is confirming booking a table "
				+ "from User " + CloudSim.getEntityName(userID)
				);
		
		// send confirmation to the user
		schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RESTAURANT_BOOKING_CONFIRMATION_EVENT);	
	}
	

	private double getOrderPreparationTimeRandom() {
		// get random time within a hour
		Random random = new Random();  		
		return random.nextDouble() * ((60*60) + CloudSim.getMinTimeBetweenEvents());
	}

	/**
	 * @return the openingTime
	 */
	public double getOpeningTime() {
		return openingTime;
	}

	/**
	 * @param openingTime the openingTime to set
	 */
	public void setOpeningTime(double openingTime) {
		this.openingTime = openingTime;
	}

	/**
	 * @return the closingTime
	 */
	public double getClosingTime() {
		return closingTime;
	}

	/**
	 * @param closingTime the closingTime to set
	 */
	public void setClosingTime(double closingTime) {
		this.closingTime = closingTime;
	}

	/**
	 * @return the isOpen
	 */
	public boolean isOpen() {
		return isOpen;
	}

	/**
	 * @param isOpen the isOpen to set
	 */
	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
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
	
	private HolonDataModel createDataModel(String name, 
			Location location, MessagingProtocol messagingProtocol) {
		
		HolonDataModel dataModel = new HolonDataModel();
        dataModel.putData("name",name);
        dataModel.putData("type", this.getNodeType().toString());
        dataModel.putData("latitude", location.getX()+"");
        dataModel.putData("longitude", location.getY()+"");
        dataModel.putData("messagingProtocol",messagingProtocol.toString());
        dataModel.putData("connecitonType", this.getConnection().getConnectionType().toString());
        dataModel.putData("powerType", this.getPower().getPowerType().toString());
        dataModel.putData("currentExpDay", currentExpDay +"");
        
        HolonServiceModel service1 = new HolonServiceModel();
        service1.setName("bookTable");
        service1.setCost(0);
        service1.setUrl("bookTable");
        service1.setReturnType(Types.BOOLEAN);
        service1.setAnnotation(IoVHolonAnnotations.BOOK_TABLE);

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
        services.add(service1);
        services.add(service2);
        services.add(service3);
        dataModel.setServices(services);

        return dataModel;         
	}

}
