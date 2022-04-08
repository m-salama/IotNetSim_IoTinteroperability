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
import org.cloudbus.iotnetsim.iot.nodes.LinkNode;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTNodeHolon;
import org.cloudbus.iotnetsim.iov.IoVNodeType;
import org.cloudbus.iotnetsim.network.NetConnection;

import dionasys.holon.datamodel.HolonDataModel;
import dionasys.holon.datamodel.HolonServiceModel;
import dionasys.holon.datamodel.Types;
import experiments.configurations.*;
/** 
 * Class TrafficControlUnit
 * 
 * @author Maria Salama
 * 
 */

public class TrafficControlUnitHolon extends IoTNodeHolon {

	//boolean variable to be set if the traffic alert in on/off (true/false)
	private boolean isTrafficAlert;
	
	// interval for sending traffic alerts every x seconds
	private double trafficAlertInterval;			

	protected int currentExpDay;
	protected int currentChangeIndex;
	

	public TrafficControlUnitHolon(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public TrafficControlUnitHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String messagingProtocol, String forwardNodeName) {
		
		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
		holon.setHolonDataModel(createDataModel(name, location,messagingProtocol));
		holon.createOntology();
	}

	public TrafficControlUnitHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String messagingProtocol, String forwardNodeName, double forward_interval,
			double traffic_alert_interval) {
		
		super(name, location, nodeType, connection, power, forwardNodeName);
		
		this.trafficAlertInterval = traffic_alert_interval;
		this.isTrafficAlert = false;
		holon.setHolonDataModel(createDataModel(name, location,messagingProtocol));
		holon.createOntology();
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");
		
		// schedule the first event for parking to register the holon
		schedule(dataCentre.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_HOLON_REGISTER_HOLON,
				holon);
		// schedule the first event for sending traffic alert
		scheduleSendTrafficAlert();	
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
		case CloudSimTags.IOV_TRAFFIC_ALERT_SEND_EVENT:
			sendTrafficAlert();
			break;
		case CloudSimTags.IOV_TRAFFIC_ALERT_CANCEL_EVENT:
			cancelTrafficAlert();
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
	public void sendTrafficAlert() {
		this.isTrafficAlert = true;
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is setting traffic alert" 
			+ " in Day " + currentExpDay
			+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
			);

		// send data to Datacenter
		scheduleNextSendDataEvent();

		// schedule the event for cancelling this traffic alert at random time
		scheduleCancelTrafficAlertEventRandom();		
	}
	
	/**
	 * cancelTrafficAlert()
	 * method for setting the traffic alert off at random time
	 */	
	public void cancelTrafficAlert() {
		this.isTrafficAlert = false;

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is cancelling traffic alert" 
			+ " in Day " + currentExpDay
			+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
			);

		// send data to Datacenter
		scheduleNextSendDataEvent();
		
		if (CloudSim.clock() >= currentExpDay*24*60*60) {
			currentExpDay +=1;
		}
		if (currentExpDay < ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			// schedule the next event for sending traffic alert
			scheduleSendTrafficAlert();
		}		
	}
	
	private void scheduleSendTrafficAlert() {
		schedule(this.getId(), this.trafficAlertInterval, CloudSimTags.IOV_TRAFFIC_ALERT_SEND_EVENT);
	}
	
	private void scheduleCancelTrafficAlertEventRandom() {
		// schedule the event for setting this traffic alert off
		Random random = new Random();  		
		double cancelTime = random.nextDouble() * ((this.trafficAlertInterval - CloudSim.getMinTimeBetweenEvents()) + CloudSim.getMinTimeBetweenEvents());  
		schedule(this.getId(), cancelTime, CloudSimTags.IOV_TRAFFIC_ALERT_CANCEL_EVENT);

	}
	private void scheduleNextSendDataEvent() {
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, isTrafficAlert);		
	}

	
	private HolonDataModel createDataModel(String name, 
			Location location, String messagingProtocol) {
		
		HolonDataModel dataModel = new HolonDataModel();
        dataModel.putData("name",name);
        dataModel.putData("type", this.getNodeType().toString());
        dataModel.putData("latitude", location.getX()+"");
        dataModel.putData("longitude", location.getY()+"");
        dataModel.putData("messagingProtocol",messagingProtocol);
        dataModel.putData("connecitonType", this.getConnection().getConnectionType().toString());
        dataModel.putData("powerType", this.getPower().getPowerType().toString());
        dataModel.putData("currentExpDay", currentExpDay +"");
        dataModel.putData("currentChangeIndex", currentChangeIndex+"");
        
        HolonServiceModel service1 = new HolonServiceModel();
        service1.setName("getTrafficAlert");
        service1.setCost(0);
        service1.setUrl("http://10.10.10.2/getTrafficAlert");
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
                
        ArrayList<HolonServiceModel> services = new ArrayList<>();
        services.add(service1);
        services.add(service2);
        services.add(service3);
        dataModel.setServices(services);

        return dataModel;         
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
	public double getTrafficAlertInterval() {
		return trafficAlertInterval;
	}

	/**
	 * @param trafficAlertInterval the trafficAlertInterval to set
	 */
	public void setTrafficAlertInterval(double trafficAlertInterval) {
		this.trafficAlertInterval = trafficAlertInterval;
	}


}
