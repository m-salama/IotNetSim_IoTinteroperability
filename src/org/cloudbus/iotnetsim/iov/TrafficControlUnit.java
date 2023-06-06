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

import experiments.configurations.ExperimentsConfigurations;

/** 
 * Class TrafficControlUnit
 * 
 * @author Maria Salama
 * 
 */

public class TrafficControlUnit extends IoTNode {
	
	private boolean isTrafficAlert;		//boolean variable to be set if the traffic alert in on/off (true/false)
	private double alertChangeInterval;		// interval for changing traffic alerts every x seconds		

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
			String forwardNodeName, MessagingProtocol msgProtocol, double alert_change_interval) {
		
		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		
		this.alertChangeInterval = alert_change_interval;
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


}