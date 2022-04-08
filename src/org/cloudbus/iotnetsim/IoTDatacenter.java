package org.cloudbus.iotnetsim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.iotnetsim.iot.nodes.IoTNode;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iov.IoVNodeType;
import org.cloudbus.iotnetsim.naturalenv.SensorType;

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
 * Class IoTDatacenter extends CloudSim Datacenter, with IoT-related capabilities.
 * 
 * @author Maria Salama
 * 
 */

public class IoTDatacenter extends Datacenter {

	// list of Nodes
	private List<IoTNode> lstNodes;
	
	//NaturalEnv data, data structure for storing data, k: SensorType, v: (reading time, reading value) 
	//protected Map<SensorType, Map<Double, Double>> readingsData; 	
	
	//IoV data, data structure for storing data, k: IoTNode, v: (time, value) 
	protected Map<IoTNode, Map<Double, Object>> dataIoV; 			
	
	
	/**
	 * Allocates a new IoTDatacenter object.
	 * 
	 * @param name the name to be associated with this entity (as required by Sim_entity class from
	 *            simjava package)
	 * @param characteristics an object of DatacenterCharacteristics
	 * @param storageList a LinkedList of storage elements, for data simulation
	 * @param vmAllocationPolicy the vmAllocationPolicy
	 * @throws Exception This happens when one of the following scenarios occur:
	 *             <ul>
	 *             <li>creating this entity before initializing CloudSim package
	 *             <li>this entity name is <tt>null</tt> or empty
	 *             <li>this entity has <tt>zero</tt> number of PEs (Processing Elements). <br>
	 *             No PEs mean the Cloudlets can't be processed. A CloudResource must contain one or
	 *             more Machines. A Machine must contain one or more PEs.
	 *             </ul>
	 * @pre name != null
	 * @pre resource != null
	 * @post $none
	 */
	@SuppressWarnings("unchecked")
	public IoTDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) 
					throws Exception 
	{
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

		// initialise data structures
		dataIoV = new HashMap<IoTNode, Map<Double, Object>>();
	}

	/**
	 * Processes events or services that are available for this Datacenter.
	 * 
	 * @param ev a Sim_event object
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		super.processEvent(ev);

		switch (ev.getTag()) {
		//process data received (storing, alerting..)
		case CloudSimTags.IOT_CLOUD_PROCESS_DATA_EVENT:
			processData(ev);
			break;
	// process receiving data from entities in IoV
		case CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT:
			receiveAndStoreIoVData(ev);
			break;

		}
	}
	
	@SuppressWarnings("unchecked")
	public void receiveAndStoreIoVData(SimEvent ev) {
		Map<IoTNode, Map<Double, Object>> evdata = new HashMap<IoTNode, Map<Double, Object>>();
		evdata = (Map<IoTNode, Map<Double, Object>>) ev.getData();
		
		int senderId = ev.getSource();
		
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] is receiving data from " + CloudSim.getEntityName(senderId));

		//create a data structure for each Node
		for (IoTNode t : lstNodes) {
			dataIoV.computeIfAbsent(t, ignored -> new HashMap<>());
		}

		//put data entries for each node
		for (Map.Entry<IoTNode, Map<Double, Object>> e : evdata.entrySet()) {
			dataIoV.get(e.getKey()).putAll(e.getValue());
		}
		
		//storing data
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] is storing IoV data: ");
		evdata.forEach((t, data) -> Log.printLine("Node: " + t.getName() 
											+ " values: " + data.values().toString() ));	
		Log.printLine();
	}

	public void processData(SimEvent ev) {

	}

	public Map<IoTNode, Map<Double, Object>> getIoVData() {
		return dataIoV;
	}

	public void setIoVData(Map<IoTNode, Map<Double, Object>> data) {
		this.dataIoV = data;
	}



}
