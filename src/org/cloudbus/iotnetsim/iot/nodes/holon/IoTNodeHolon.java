/**
 * 
 */
package org.cloudbus.iotnetsim.iot.nodes.holon;

import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.iot.nodes.IoTNode;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iot.nodes.MessagingProtocol;
import org.cloudbus.iotnetsim.network.NetConnection;

/**
 * Class IoTNodeHolon
 * extends IoTNode with Holons functions
 * 
 * @author m.salama
 * @author elhabbash
 *
 */
public class IoTNodeHolon extends IoTNode implements IoTHolon  {

	public IoTNodeHolon(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public IoTNodeHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String forwardNodeName, MessagingProtocol msgProtocol) {

		super(name, location, nodeType, connection, power, forwardNodeName, msgProtocol);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processEvent(SimEvent ev) {
		// TODO Auto-generated method stub
		switch (ev.getTag()) {
		// Execute sending sensor data 
		case CloudSimTags.IOT_HOLON_CREATE_HOLON:
			processCreateHolon(SimEvent ev);
			break;
		case CloudSimTags.IOT_HOLON_REGISTER_HOLON:
			processRegisterHolon();
			break;
		case CloudSimTags.IOT_HOLON_PARSE_HOLON:
			processParseHolon();
			break;
		case CloudSimTags.IOT_HOLON_REQUEST_HOLON:
			processRequestHolon();
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}
	
	private void processCreateHolon(SimEvent ev) {
		Holon evdata = ev.getData();
		
	}

	
	//extends Holon
	
	//createHolon()
	//parseHolon(xml)
	//requestHolon()
	//receiveHolon()
	
	//request information from datacenter
	
}
