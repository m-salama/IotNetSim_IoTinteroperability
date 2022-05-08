/**
 * 
 */
package dionasys.mediation;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.iotnetsim.iot.nodes.IoTNode;
import org.cloudbus.iotnetsim.iov.ElectricChargingStation;
import org.cloudbus.iotnetsim.iov.Parking;
import org.cloudbus.iotnetsim.iov.PetrolStation;
import org.cloudbus.iotnetsim.iov.Restaurant;
import org.cloudbus.iotnetsim.iov.TrafficControlUnit;

/**
 * @author m.salama
 *
 */
public class Mediator {

	String name;
	
	int senderId;
	int receiverId;
	
	Object request;
	Object response;
	
	boolean isAssigned;
	
	public Mediator() {
		
	}
	
	public Mediator (String mediator_name) {
		this.name = mediator_name;
	}
	
	public Mediator (
			String mediator_name,
			int sender, int receiver,
			RequestType request) {

		this.name = mediator_name;
		this.senderId = sender;
		this.receiverId = receiver;
		this.request = request;
		
		this.isAssigned = true;
	}
	
	public Object processRequest(int senderId, int receiverId, RequestType request) {
		this.isAssigned = true;
		
		Object response = new Object();
		
		IoTNode sender = (IoTNode) CloudSim.getEntity(senderId);
		IoTNode receiver = (IoTNode) CloudSim.getEntity(receiverId);
		
		switch(receiver.getNodeType()) {
		case ELECTRIC_CHARGING_STATION :
			ElectricChargingStation elecChargingSt = (ElectricChargingStation) receiver;
			response = elecChargingSt.isAvailable();
			break;
		case PETROL_STATION :
			PetrolStation petrolStation = (PetrolStation) receiver;
			response = petrolStation.getPrice();
			break;
		case PARKING :
			Parking p = (Parking) receiver;
			response = p.isAvailable();
			break;
		case TRAFFIC_CONTROL_UNIT :
			TrafficControlUnit tcu = (TrafficControlUnit) receiver;
			response = tcu.isTrafficAlert();
			break;
		case RESTAURANT :
			Restaurant r = (Restaurant) receiver;
			response = r.isOpen();
			break;
			
		default:
			Log.printLine("Mediator could not process this request.");
			break;
		}
		
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] processed the request of " 
				+ CloudSim.getEntityName(senderId)
			);
				
		this.isAssigned = false;
		
		return response;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the senderId
	 */
	public int getSenderId() {
		return senderId;
	}

	/**
	 * @param senderId the senderId to set
	 */
	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	/**
	 * @return the receiverId
	 */
	public int getReceiverId() {
		return receiverId;
	}

	/**
	 * @param receiverId the receiverId to set
	 */
	public void setReceiverId(int receiverId) {
		this.receiverId = receiverId;
	}

	/**
	 * @return the request
	 */
	public Object getRequest() {
		return request;
	}

	/**
	 * @param request the request to set
	 */
	public void setRequest(Object request) {
		this.request = request;
	}

	/**
	 * @return the response
	 */
	public Object getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(Object response) {
		this.response = response;
	}

	/**
	 * @return the isAssigned
	 */
	public boolean isAssigned() {
		return isAssigned;
	}

	/**
	 * @param isAssigned the isAssigned to set
	 */
	public void setAssigned(boolean isAssigned) {
		this.isAssigned = isAssigned;
	}
	
	
}
