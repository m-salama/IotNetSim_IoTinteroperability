package org.cloudbus.iotnetsim;

/**
 * Title:        IoTNetSim Toolkit
 * Description:  Modelling and Simulation for End-to-End IoT Services & Networking 
 * 
 * Author: Maria Salama, Lancaster University
 * Contact: m.salama@lancaster.ac.uk
 *
 * If you are using any algorithms, policies or workload included in the SAd/SAw CloudSim Toolkit,
 * please cite the following paper:
 * 
 * Maria Salama, Yehia Elkhatib, and Gordon Blair. 2019. 
 * IoTNetSim: A Modelling and Simulation Platform for End-to-End IoT Services and Networking.
 * In Proceedings of the IEEE/ACM 12th International Conference on Utility and Cloud Computing (UCC ’19), December 2–5, 2019, Auckland, New Zealand. 
 * ACM, NewYork,NY, USA, 11 pages. 
 * https://doi.org/10.1145/3344341.3368820
 * 
 */

/**
 * Enum
 * 
 * @author Maria Salama
 * 
 */

public enum IoTNodeType {
	SENSOR,
	MOBILE_SENSOR,
	
	LINK_NODE,
	GATEWAY_Node,
	
	SMART_PHONE,
	SMART_WATCH,
	MOBILE_PHONE,
	
	SMART_DEVICE,
	SMART_HOME_DEVICE,
	
	VEHICLE,
	VEHICLE_ELECTRIC,
	PARKING,
	TRAFFIC_CONTROL_UNIT,

	FUEL_STATION,
	ELEC_CHARGING_STATION,
	RESTAURANT
}
