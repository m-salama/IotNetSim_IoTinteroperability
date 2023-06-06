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
import dionasys.holon.datamodel.Types;
import experiments.configurations.ExperimentsConfigurations;
/** 
 * Class TrafficControlUnit
 * 
 * @author Maria Salama
 * @author Abdessalam Elhabbash
 * 
 */

public class TrafficControlUnitHolon extends IoTNodeHolon {

	//boolean variable to be set if the traffic alert in on/off (true/false)
	private boolean isTrafficAlert;
	private double alertChangeInterval;		// interval for changing traffic alerts every x seconds		

	protected int currentExpDay;
	protected int currentChangeIndex;
	

	public TrafficControlUnitHolon(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public TrafficControlUnitHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			IoTDatacenterHolon dataCentre,
			String forwardNodeName, MessagingProtocol messagingProtocol) {
		
		super(name, location, nodeType, connection, power, dataCentre,forwardNodeName,messagingProtocol);
		
		holon.setHolonDataModel(createDataModel(name, location,messagingProtocol));
		holon.createOntology();
	}

	public TrafficControlUnitHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			IoTDatacenterHolon dataCentre,
			String forwardNodeName, MessagingProtocol messagingProtocol, double traffic_alert_interval) {
		
		super(name, location, nodeType, connection, power, dataCentre, forwardNodeName, messagingProtocol);
		
		this.isTrafficAlert = false;
		this.alertChangeInterval = traffic_alert_interval;
		
		this.currentExpDay = 1;
		this.currentChangeIndex = 0;
		
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
		
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_NODE_CONNECTION_EVENT, isTrafficAlert);
		
		// schedule the first event for sending traffic alert
		scheduleNextAlertChangeEvent();
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
		case CloudSimTags.IOV_TRAFFIC_ALERT_CHANGE_EVENT:
			processChangeTrafficAlert();
			break;
		case CloudSimTags.IOV_TRAFFIC_CHECK_ALERT_EVENT:
			processCheckAlert(ev.getSource());
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}
	
	/**
	 * sendTrafficAlert()
	 * method for setting the traffic alert on
	 */	
	public void processChangeTrafficAlert() {
		this.isTrafficAlert = !(this.isTrafficAlert);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is setting traffic alert "
				+ " to " + this.isTrafficAlert 
				+ " in Day " + currentExpDay
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		// send data to Datacenter
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, isTrafficAlert);

		if (CloudSim.clock() >= currentExpDay*24*60*60) {
			currentExpDay +=1;
		}

		// schedule the next event for changing traffic alert
		scheduleNextAlertChangeEvent();
	}
	
	private void scheduleNextAlertChangeEvent() {
		if (currentExpDay < ExperimentsConfigurations.EXP_NO_OF_DAYS) {			
			if (ExperimentsConfigurations.IOV_EXP_ServiceEntities_CHANGE == "Frequent") {
				schedule(this.getId(), this.alertChangeInterval, CloudSimTags.IOV_TRAFFIC_ALERT_CHANGE_EVENT);
			} else if (ExperimentsConfigurations.IOV_EXP_ServiceEntities_CHANGE == "Random") {
				Random random = new Random();  		
				double changeTime = random.nextDouble() * ((this.alertChangeInterval - CloudSim.getMinTimeBetweenEvents()) + CloudSim.getMinTimeBetweenEvents());  
				schedule(this.getId(), changeTime, CloudSimTags.IOV_TRAFFIC_ALERT_CHANGE_EVENT);
			}
		}
	}
	
	/**
	 * processCheckAlert()
	 */
	private void processCheckAlert(int userID) {
		// send the traffic alert data to the user
		schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RECEIVE_TRAFFICALERT_DATA_EVENT, 
				this.isTrafficAlert);	
	}

	
	/**
	 * @return the isTrafficAlert
	 */
	public boolean isTrafficAlert() {
		return isTrafficAlert;
	}

	/**
	 * @param isTrafficAlert the isTrafficAlert to set
	 */
	public void setTrafficAlert(boolean isTrafficAlert) {
		this.isTrafficAlert = isTrafficAlert;
	}

	/**
	 * @return the trafficAlertInterval
	 */
	public double getAlertChangeInterval() {
		return alertChangeInterval;
	}

	/**
	 * @param trafficAlertInterval the trafficAlertInterval to set
	 */
	public void setAlertChangeInterval(double alertChangeInterval) {
		this.alertChangeInterval = alertChangeInterval;
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
        dataModel.putData("currentChangeIndex", currentChangeIndex+"");
        
        HolonServiceModel service1 = new HolonServiceModel();
        service1.setName("isTrafficAlert");
        service1.setCost(0);
        service1.setUrl("isTrafficAlert");
        service1.setReturnType(Types.BOOLEAN);
        service1.setAnnotation(IoVHolonAnnotations.IS_TRAFFIC_ALERT);

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
        services.add(service1);
        services.add(service2);
        services.add(service3);
        dataModel.setServices(services);

        return dataModel;         
	} 
	
}
