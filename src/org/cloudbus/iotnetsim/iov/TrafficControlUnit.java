package org.cloudbus.iotnetsim.iov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.IoTNodeType;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.iot.nodes.GatewayNode;
import org.cloudbus.iotnetsim.iot.nodes.SensorReading;
import org.cloudbus.iotnetsim.iot.nodes.SensorType;
import org.cloudbus.iotnetsim.network.NetConnection;

/**
 * Class
 * 
 * @author Maria Salama
 * 
 */

public class TrafficControlUnit extends GatewayNode {

	public TrafficControlUnit(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public TrafficControlUnit(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			int forwardNodeId) {
		
		super(name, location, nodeType, connection, power, forwardNodeId);
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
			int forwardNodeId,
			double forwardInterval, double dataProcessingInterval) {
		
		super(name, location, nodeType, connection, power, forwardNodeId);
		
		this.forwardInterval = forwardInterval;
		this.dataProcessingInterval = dataProcessingInterval;
		
		// initialise data structures
		readingsDataReceived = new ArrayList<SensorReading>();
		readingsDataAggregated = new HashMap<SensorType, Map<Double, Double>>();
	}

	public TrafficControlUnit(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName,
			double forwardInterval, double dataProcessingInterval) {
		
		super(name, location, nodeType, connection, power, forwardNodeName);
		
		this.forwardInterval = forwardInterval;
		this.dataProcessingInterval = dataProcessingInterval;
		
		// initialise data structures
		readingsDataReceived = new ArrayList<SensorReading>();
		readingsDataAggregated = new HashMap<SensorType, Map<Double, Double>>();
	}


}
