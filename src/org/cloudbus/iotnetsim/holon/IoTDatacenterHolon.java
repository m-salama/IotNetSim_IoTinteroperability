/**
 * 
 */
package org.cloudbus.iotnetsim.holon;

import java.util.List;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.iotnetsim.IoTDatacenter;

/**
 * @author m.salama
 * @author elhabbash
 *
 */
public class IoTDatacenterHolon extends IoTDatacenter {

	public IoTDatacenterHolon(String name, DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval)
			throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
		// TODO Auto-generated constructor stub
	}
	
	//pubsub service
	//HolonRegistry
	//sendHolon()
	
	//send information to node

}
