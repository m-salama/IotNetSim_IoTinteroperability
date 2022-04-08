package org.cloudbus.iotnetsim.iov.holon;

import java.util.ArrayList;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.iot.nodes.IoTNode;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeMobile;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTNodeHolon;
import org.cloudbus.iotnetsim.iov.IoVNodeType;
import org.cloudbus.iotnetsim.iov.VehicleType;
import org.cloudbus.iotnetsim.network.NetConnection;

import dionasys.holon.datamodel.HolonDataModel;
import dionasys.holon.datamodel.HolonServiceModel;
import dionasys.holon.datamodel.Types;
import experiments.configurations.ExperimentsConfigurations;

/**
 * Class
 * 
 * @author m.salama
 * 
 */

public class VehicleHolon extends IoTNodeHolon  implements IoTNodeMobile {

	private Location currentLocation;	
	
	//vehicleType enum attribute to specify the type of the vehicle, either fuel or electric 
	private VehicleType vehicleType;
	private double currentRange;
	private double consumptionRate;
	private double maxRange;
	private double rangeThreshold;

	
	public VehicleHolon(String name, String messagingProtocol) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public VehicleHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String messagingProtocol, String forwardNodeName) {
		
		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
		
		this.currentLocation = location;
		holon.setHolonDataModel(createDataModel(name, location,messagingProtocol));
		holon.createOntology();
	}

	public VehicleHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String messagingProtocol, String forwardNodeName,
			VehicleType vType, double maxRange, double rangeThreshold, double consumptionRate) {
		
		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
		
		this.currentLocation = location;
		this.vehicleType = vType;
		this.maxRange = maxRange;
		this.currentRange = maxRange;
		this.rangeThreshold = rangeThreshold;
		this.consumptionRate = consumptionRate;
		holon.setHolonDataModel(createDataModel(name, location,messagingProtocol));
		holon.createOntology();
	}

	
	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");

		// schedule the first event for parking to register the holon
		schedule(dataCentre.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_HOLON_REGISTER_HOLON,
				holon);
		// schedule the first event for vehicle
		schedule(this.getId(), CloudSim.getMinTimeBetweenEvents()*20, CloudSimTags.IOV_VEHICLE_FUEL_CONSUMPTION);
		System.out.println(this.getName() + ": id = " + this.getId());
	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is shutting down...");		
	}
	
	@Override
	public void processEvent(SimEvent ev) {
		//super.processEvent(ev);
		// TODO Auto-generated method stub
		switch (ev.getTag()) {
		// Execute sending sensor data 
		case CloudSimTags.IOV_VEHICLE_FUEL_CONSUMPTION:
			processFuelConsumption();
			break;
		case CloudSimTags.IOV_VEHICLE_FUEL_REFILL:
			processFuelRefill();
			break;
		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}
	
	private void processFuelConsumption() {
		currentRange -= this.consumptionRate;
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is changing range to " 
				+ " to " + currentRange 
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		//send data to Datacenter, if holon changed then resend holon
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents()*100, CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, currentRange);

		// schedule the next event for sending data 
		scheduleNextFuelConsumptionEvent();
	}
	
	/**
	 * processFuelRefill event
	 */
	private void processFuelRefill() {
		currentRange = this.maxRange;
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is changing range to " + currentRange 
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId()));
		System.out.println(CloudSim.clock() + ": [" + this.getName() + "] is changing range to " 
				+ " to " + currentRange);
		
		//send data to Datacenter, if holon changed then resend holon
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT, currentRange);
	}
	
	private void scheduleNextFuelConsumptionEvent(){
		schedule(this.getId(), CloudSim.getMinTimeBetweenEvents()*100, CloudSimTags.IOV_VEHICLE_FUEL_CONSUMPTION);
	}
	
	private HolonDataModel createDataModel(String name, 
			Location location, String messagingProtocol) {
		
		HolonDataModel dataModel = new HolonDataModel();
        dataModel.putData("name",name);
        dataModel.putData("type", this.vehicleType.toString());
        dataModel.putData("latitude", location.getX()+"");
        dataModel.putData("longitude", location.getY()+"");
        dataModel.putData("messagingProtocol",messagingProtocol);
        dataModel.putData("connecitonType", this.getConnection().getConnectionType().toString());
        dataModel.putData("powerType", this.getPower().getPowerType().toString());
        dataModel.putData("range", currentRange+"");
        
        HolonServiceModel service1 = new HolonServiceModel();
        service1.setName("getCurrentRange");
        service1.setCost(0);
        service1.setUrl("getCurrentRange");
        service1.setReturnType(Types.DOUBLE);
        service1.setAnnotation(IoVHolonAnnotations.CAR_RANGE);

        HolonServiceModel service2 = new HolonServiceModel();
        service2.setName("getRangeThreshold");
        service2.setCost(0);
        service2.setUrl("getRangeThreshold");
        service2.setReturnType(Types.DOUBLE);
        service2.setAnnotation(IoVHolonAnnotations.CAR_RANGE_THRESHOLD);

        HolonServiceModel service3 = new HolonServiceModel();
        service3.setName("getMessagingProtocol");
        service3.setCost(0);
        service3.setUrl("getMessagingProtocol");
        service3.setReturnType(Types.STRING);
        service3.setAnnotation(IoVHolonAnnotations.MESSAGING_PROTOCOL);
                
        ArrayList<HolonServiceModel> services = new ArrayList<>();
        services.add(service1);
        services.add(service2);
        services.add(service3);
        dataModel.setServices(services);

        return dataModel;         
	} 
	

	public Location getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(Location currentLocation) {
		this.currentLocation = currentLocation;
	}

	public void changeAltitude(double newZ) {
		
	}

	/**
	 * @return the vehicleType
	 */
	public VehicleType getVehicleType() {
		return vehicleType;
	}

	/**
	 * @param vehicleType the vehicleType to set
	 */
	public void setVehicleType(VehicleType vehicleType) {
		this.vehicleType = vehicleType;
	}
	
	public double getCurrentRange() {
		return currentRange;
	}

	public void setCurrentRange(double currentRange) {
		this.currentRange = currentRange;
	}

	public double getConsumptionRate() {
		return consumptionRate;
	}

	public void setConsumptionRate(double consumptionRate) {
		this.consumptionRate = consumptionRate;
	}

	public double getMaxRange() {
		return maxRange;
	}

	public void setMaxRange(double maxRange) {
		this.maxRange = maxRange;
	}

	public double getRangeThreshold() {
		return rangeThreshold;
	}

	public void setRangeThreshold(double rangeThreshold) {
		this.rangeThreshold = rangeThreshold;
	}

	public boolean refuel() {
		// TODO Auto-generated method stub
		return true;
	}
	
	
	
}
