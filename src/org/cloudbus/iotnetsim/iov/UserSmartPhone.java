/**
 * 
 */
package org.cloudbus.iotnetsim.iov;

import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.IoTNodeType;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.iot.nodes.IoTNode;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeMobile;
import org.cloudbus.iotnetsim.network.NetConnection;

/**
 * @author m.salama
 *
 */
public class UserSmartPhone extends IoTNode  implements IoTNodeMobile {

	private Location currentLocation;


	public UserSmartPhone(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public UserSmartPhone(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, String forwardNodeName) {
		
		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
		
		this.currentLocation = location;
	}

	public Location getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(Location currentLocation) {
		this.currentLocation = currentLocation;
	}

	public void changeAltitude(double newZ) {
		
	}
	
}
