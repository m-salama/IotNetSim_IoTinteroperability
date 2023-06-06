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
import org.cloudbus.iotnetsim.holon.IoTDatacenterHolon;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iot.nodes.MessagingProtocol;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTHolon;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTNodeHolon;
import org.cloudbus.iotnetsim.iov.UserSmartPhone;
import org.cloudbus.iotnetsim.network.NetConnection;

import dionasys.holon.datamodel.HolonDataModel;
import dionasys.holon.datamodel.HolonServiceModel;
import dionasys.holon.datamodel.ParameterModel;
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
	
	
	protected double openingTime;	// opening time of the restaurant set by the seconds within the day
	protected double closingTime;	// closing time of the restaurant set by the seconds within the day
	protected boolean isOpen;
	protected double orderPreparationTime;

	protected int currentExpDay;
	
	public RestaurantHolon(String name) {
		super(name);
	}

	public RestaurantHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			IoTDatacenterHolon dataCentre,
		 String forwardNodeName, MessagingProtocol messagingProtocol) {

		super(name, location, nodeType, connection, power,dataCentre, forwardNodeName, messagingProtocol);
		
		holon.setHolonDataModel(createDataModel(name, location,messagingProtocol));
		holon.createOntology();
	}

	public RestaurantHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			IoTDatacenterHolon dataCentre,
			String forwardNodeName, MessagingProtocol messagingProtocol, 
			double opening_time, double closing_time, double order_preparation_time) {

		super(name, location, nodeType, connection, power, dataCentre, forwardNodeName,messagingProtocol);
		
		this.openingTime = opening_time;
		this.closingTime = closing_time;
		this.isOpen = true;
		this.orderPreparationTime = order_preparation_time;
		
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

		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_NODE_CONNECTION_EVENT, isOpen);

		// schedule event for restaurant opening
		schedule(this.getId(), this.closingTime, CloudSimTags.IOV_RESTAURANT_CLOSE_EVENT);

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
		// send updates to the Datacenter 
		case CloudSimTags.IOV_RESTAURANT_OPEN_EVENT:
			processOpenRestaurant();
			break;
		// send updates to the Datacenter 
		case CloudSimTags.IOV_RESTAURANT_CLOSE_EVENT:
			processCloseRestaurant();
			break;
		// confirm receiving order
		case CloudSimTags.IOV_RESTAURANT_ORDER_EVENT:
			processReceiveOrder(ev.getSource());
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}

		
	private void processOpenRestaurant() {
		this.isOpen = true;

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is open"
				+ " at day " + currentExpDay
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
				+ " at day " + currentExpDay
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

	private void processReceiveOrder(int userId) {
		UserSmartPhoneHolon user = (UserSmartPhoneHolon) CloudSim.getEntity(userId);
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is receiving the order"
				+ " from User " + CloudSim.getEntityName(userId)
				+ " and sending confirmation"
				);
		
		// send confirmation to the user
		if (this.getMessagingProtocol() == user.getMessagingProtocol()) {
			schedule(userId, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RESTAURANT_ORDER_CONFIRMATION_EVENT);	
		} else {
			schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}
		
		double preparationTime = CloudSim.getMinTimeBetweenEvents();
		// get time for the order preparation
		if (ExperimentsConfigurations.IOV_EXP_ServiceEntities_CHANGE == "Frequent") {
			preparationTime = this.orderPreparationTime;
		} else if (ExperimentsConfigurations.IOV_EXP_ServiceEntities_CHANGE == "Random") {
			// get random time within a hour
			Random random = new Random(); 
			preparationTime = random.nextDouble() * ((60*60) + CloudSim.getMinTimeBetweenEvents());
		}
		
		// send notification to the user after the preparation time
		if (this.getMessagingProtocol() == user.getMessagingProtocol()) {
			schedule(userId, preparationTime, CloudSimTags.IOV_RESTAURANT_ORDER_READY_EVENT);
		} else {
			schedule(getForwardNodeId(), preparationTime, CloudSimTags.IOV_CLOUD_REQUEST_MEDIATOR);
		}
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
        dataModel.putData("orderPreparationTime", this.orderPreparationTime + "");
        dataModel.putData("currentExpDay", currentExpDay +"");
        
        HolonServiceModel service1 = new HolonServiceModel();
        service1.setName("processReceiveOrder");
        service1.setCost(0);
        service1.setUrl("processReceiveOrder");
        service1.setReturnType(Types.VOID);
        ParameterModel par1 = new ParameterModel("userId",Types.INTEGER);
        ArrayList<ParameterModel> parameters = new ArrayList<ParameterModel>();
        parameters.add(par1);
        service1.setParameters(parameters);
        service1.setAnnotation(IoVHolonAnnotations.RESTAURANT_ORDER);

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
        
        HolonServiceModel service4 = new HolonServiceModel();
        service4.setName("isOpen");
        service4.setCost(0);
        service4.setUrl("isOpen");
        service4.setReturnType(Types.BOOLEAN);
        service4.setAnnotation(IoVHolonAnnotations.IS_OPEN);
                
        ArrayList<HolonServiceModel> services = new ArrayList<>();
        services.add(service1);
        services.add(service2);
        services.add(service3);
        services.add(service4);
        dataModel.setServices(services);

        return dataModel;         
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

	public double getOrderPreparationTime() {
		return orderPreparationTime;
	}

	public void setOrderPreparationTime(double orderPreparationTime) {
		this.orderPreparationTime = orderPreparationTime;
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
	
	
}
