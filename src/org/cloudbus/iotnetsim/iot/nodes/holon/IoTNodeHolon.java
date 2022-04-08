/**
 * 
 */
package org.cloudbus.iotnetsim.iot.nodes.holon;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.holon.IoTDatacenterHolon;
import org.cloudbus.iotnetsim.iot.nodes.IoTNode;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.network.NetConnection;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import dionasys.holon.Holon;
import dionasys.holon.datamodel.HolonDataModel;
import dionasys.holon.datamodel.OntologyParser;

/**
 * Class IoTNodeHolon
 * extends IoTNode with Holons functions
 * 
 * @author m.salama
 * @author elhabbash
 *
 */
public class IoTNodeHolon extends IoTNode implements IoTHolon  {

	protected Holon holon;
	protected IoTDatacenterHolon dataCentre;
	protected HolonDataModel dataModel;
	
	public IoTNodeHolon(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public IoTNodeHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, String forwardNodeName) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		dataCentre = (IoTDatacenterHolon) CloudSim.getEntity(forwardNodeName);
		dataModel = new HolonDataModel();
		holon = new Holon();		
	}

	@Override
	public void processEvent(SimEvent ev) {
		// TODO Auto-generated method stub
		switch (ev.getTag()) {
		// Execute sending sensor data 
		case CloudSimTags.IOV_HOLON_CREATE_HOLON:
			processCreateHolon(ev);
			break;
		case CloudSimTags.IOV_HOLON_REGISTER_HOLON:
			processRegisterHolon();
			break;
		case CloudSimTags.IOV_HOLON_PARSE_HOLON:
			processParseHolon();
			break;
		case CloudSimTags.IOV_HOLON_REQUEST_HOLON_BY_TYPE:
			processRequestHolon();
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}
	
	private void processRequestHolon() {
		// TODO Auto-generated method stub
		
	}

	private void processParseHolon() {
		// TODO Auto-generated method stub
		
	}

	private void processRegisterHolon() {
		schedule(dataCentre.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_HOLON_REGISTER_HOLON,
				holon);
	}

	private void processCreateHolon(SimEvent ev) {
		Holon evdata = (Holon) ev.getData();
		
	}

	
	//extends Holon
	
	//createHolon()
	
	//parse holon
	private HolonDataModel parseHolon(OWLOntology ontology) {
		try {
			return OntologyParser.parse(ontology);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			return null;
		}
	}
	//requestHolon()
	//receiveHolon()
	
	//request information from datacenter
	public void setDataModel(HolonDataModel dm) {
		this.holon.setHolonDataModel(dm);
	}
	
	public void setHolon(Holon holon) {
		this.holon = holon;
	}
	
	public Holon getHolon() {
		return this.holon;
	}
	
}
