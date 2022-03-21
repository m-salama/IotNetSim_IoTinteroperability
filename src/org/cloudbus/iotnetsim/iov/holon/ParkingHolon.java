package org.cloudbus.iotnetsim.iov.holon;

import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTHolon;
import org.cloudbus.iotnetsim.iov.Parking;
import org.cloudbus.iotnetsim.network.NetConnection;

/**
 * Class ParkingHolon
 * 
 * @author Maria Salama
 * @author elhabbash
 * 
 */

public class ParkingHolon extends Parking implements IoTHolon {
	
	public ParkingHolon(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public ParkingHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, String forwardNodeName) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
	}

	public ParkingHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, String forwardNodeName,
			int total_parkingSlots, double parking_interval) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
		
		this.totalParkingSlots = total_parkingSlots;
		this.availableParkingSlots = total_parkingSlots;
		this.parkingChangeInterval = parking_interval;
		this.currentExpDay = 1;
		this.currentChangeIndex = 0;
	}


}
