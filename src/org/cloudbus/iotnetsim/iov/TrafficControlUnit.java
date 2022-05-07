package org.cloudbus.iotnetsim.iov;

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
import org.cloudbus.iotnetsim.iot.nodes.MessagingProtocol;
import org.cloudbus.iotnetsim.network.NetConnection;

/** 
 * Class TrafficControlUnit
 * 
 * @author Maria Salama
 * 
 */

public class TrafficControlUnit extends IoTNode {

	//boolean variable to be set if the traffic alert in on/off (true/false)
	private boolean isTrafficAlert;
	
	// interval for sending traffic alerts every x seconds
	private double trafficAlertInterval;			

	protected int currentExpDay;
	protected int currentChangeIndex;
	

	public TrafficControlUnit(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public TrafficControlUnit(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName, MessagingProtocol msgProtocol) {
		
		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub
	}

	public TrafficControlUnit(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName, MessagingProtocol msgProtocol, double forward_interval,
			double traffic_alert_interval) {
		
		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		
		this.trafficAlertInterval = traffic_alert_interval;
		this.isTrafficAlert = false;

		this.currentExpDay = 1;
		this.currentChangeIndex = 0;
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");				
		
		// connect to the datacenter
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is connecting to the Datacenter " 
				+ CloudSim.getEntityName(getForwardNodeId())
				);
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_NODE_CONNECTION_EVENT, isTrafficAlert);

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
		switch (ev.getTag()) {
		case CloudSimTags.IOV_TRAFFIC_ALERT_ON_EVENT:
			processSendTrafficAlert();
			break;
		case CloudSimTags.IOV_TRAFFIC_ALERT_OFF_EVENT:
			processCancelTrafficAlert();
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
	public void processSendTrafficAlert() {
		this.isTrafficAlert = true;
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is setting traffic alert" 
			+ " in Day " + currentExpDay
			+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
			);

		// send data to Datacenter
		scheduleSendDataEvent();

		// schedule the event for cancelling this traffic alert at random time
		scheduleCancelTrafficAlertEventRandom();		
	}
	
	/**
	 * cancelTrafficAlert()
	 * method for setting the traffic alert off at random time
	 */	
	public void processCancelTrafficAlert() {
		this.isTrafficAlert = false;

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is cancelling traffic alert" 
			+ " in Day " + currentExpDay
			+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
			);

		// send data to Datacenter
		scheduleSendDataEvent();
		
		if (CloudSim.clock() >= currentExpDay*24*60*60) {
			currentExpDay +=1;
		}
		if (currentExpDay < configurations.ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			// schedule the next event for sending traffic alert
			scheduleSendTrafficAlert();
		}		
	}
	
	/**
	 * processCheckAvailability()
	 */
	private void processCheckAlert(int userID) {
		// send the price to the user
		schedule(userID, CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_RECEIVE_TRAFFICALERT_DATA_EVENT, 
				this.isTrafficAlert);	
	}

	private void scheduleCancelTrafficAlertEventRandom() {
		// schedule the event for setting this traffic alert off
		Random random = new Random();  		
		double cancelTime = random.nextDouble() * ((this.trafficAlertInterval - CloudSim.getMinTimeBetweenEvents()) + CloudSim.getMinTimeBetweenEvents());  
		schedule(this.getId(), cancelTime, CloudSimTags.IOV_TRAFFIC_ALERT_OFF_EVENT);
	}

	private void scheduleSendDataEvent() {
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, isTrafficAlert);		
	}

	private void scheduleSendTrafficAlert() {
		schedule(this.getId(), this.trafficAlertInterval, CloudSimTags.IOV_TRAFFIC_ALERT_ON_EVENT);
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
