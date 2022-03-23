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
			String forwardNodeName) {
		
		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
	}

	public TrafficControlUnit(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName, double forward_interval,
			double traffic_alert_interval) {
		
		super(name, location, nodeType, connection, power, forwardNodeName);
		
		this.trafficAlertInterval = traffic_alert_interval;
		this.isTrafficAlert = false;
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");				
		
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
		if (currentExpDay < configurations.ExperimentsConfigurations.EXP_NO_OF_DAYS) {
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
