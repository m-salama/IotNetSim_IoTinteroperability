/**
 * 
 */
package org.cloudbus.iotnetsim.holon;

import java.util.HashMap;
import java.util.List;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.iotnetsim.IoTDatacenter;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTNodeHolon;
import org.semanticweb.owlapi.model.OWLOntology;

import dionasys.holon.Holon;
import dionasys.holon.HolonRegistry;

/**
 * @author m.salama
 * @author elhabbash
 *
 */
public class IoTDatacenterHolon extends IoTDatacenter {

	private HolonRegistry registry;
		
	public IoTDatacenterHolon(String name, DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval)
			throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
		// TODO Auto-generated constructor stub
		registry = new HolonRegistry();
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
		//super.processEvent(ev);
		switch (ev.getTag()) {
		// process receiving data from Gateway Node in the NaturalEnv
		case CloudSimTags.IOV_HOLON_REGISTER_HOLON:
			this.receiveAndStoreHolon(ev);
			break;
		case CloudSimTags.IOV_HOLON_REQUEST_HOLON_BY_ID:
			this.processRequestHolonByID(ev);
			break;
		case CloudSimTags.IOV_HOLON_REQUEST_HOLON_BY_TYPE:
			this.processRequestHolonByType(ev);
			break;
		case CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT:
			receiveAndStoreIoVData(ev);
			break;
		}
	}
	
	/**
	 * processRequestHolonByID
	 * @param ev
	 */
	private void processRequestHolonByID(SimEvent ev) {
		OWLOntology holonOntology = this.getHolonByName((String)ev.getData());
		schedule(ev.getSource(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_HOLON_RECEIVE_HOLON,
				holonOntology);
	}
	
	/**
	 * processRequestHolonByType
	 * @param ev
	 */
	private void processRequestHolonByType(SimEvent ev) {
		OWLOntology holonOntology = this.getHolonByType((String)ev.getData());
		schedule(ev.getSource(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_HOLON_RECEIVE_HOLON,
				holonOntology);
		
	}


	/**
	 * receives holon description and stores it in the holon registery
	 * @param ev
	 */
	public void receiveAndStoreIoVData(SimEvent ev) {
		Double evdata = (Double) ev.getData();
		int senderId = ev.getSource();
		IoTNodeHolon sender = (IoTNodeHolon)CloudSim.getEntity(senderId);
		double t = CloudSim.clock();
		Log.printLine(
				CloudSim.clock() + ": [" + getName() + "] is receiving data from " + CloudSim.getEntityName(senderId));

		dataIoV.computeIfAbsent(sender, ignored -> new HashMap<>());
		
		// put data entries for each SensorType
	
		dataIoV.get(sender).put(t, evdata);
		
		// log data
		 Log.printLine(CloudSim.clock() + ": [" + getName() + "] is storing IoV data:" + evdata 
				 + " received from " + CloudSim.getEntityName(senderId) +" at time :" + t);
	}
	
	/**
	 * receives holon description and stores it in the holon registery
	 * @param ev
	 */
	public void receiveAndStoreHolon(SimEvent ev) {
		Holon evdata = (Holon) ev.getData();		
		int senderId = ev.getSource();		
		Log.printLine(CloudSim.clock() + ": [" + getName() + "] is receiving holon from " + CloudSim.getEntityName(senderId));
		registry.registerHolon(evdata.getHolonDataModel().getData("type"),
				evdata.getHolonDataModel().getData("name"),evdata.getOntologyModel());
		Log.printLine(CloudSim.getEntityName(senderId) + " holon registered");
	}
	
	public void setRegistry(HolonRegistry registry) {
		this.registry = registry;
	}
	
	public HolonRegistry getRegistry() {
		return registry;
	}

    public OWLOntology getHolonByType(String type){
		return registry.getHolonByType(type);
    }
    
    public OWLOntology getHolonByName(String name){
		return registry.getHolonByName(name);
    }
    
}
