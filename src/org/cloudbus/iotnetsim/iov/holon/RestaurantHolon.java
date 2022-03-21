/**
 * 
 */
package org.cloudbus.iotnetsim.iov.holon;

import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTHolon;
import org.cloudbus.iotnetsim.iov.Restaurant;
import org.cloudbus.iotnetsim.network.NetConnection;

/**
 * Class Restaurant
 * 
 * @author Maria Salama
 * @author elhabbash
 * 
 */

public class RestaurantHolon extends Restaurant implements IoTHolon {
	
	public RestaurantHolon(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public RestaurantHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, String forwardNodeName) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
	}

	public RestaurantHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, String forwardNodeName,
			double opening_time, double closing_time, boolean open) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
		this.openingTime = opening_time;
		this.closingTime = closing_time;
		this.isOpen = open;
		this.currentExpDay = 1;
	}



}
