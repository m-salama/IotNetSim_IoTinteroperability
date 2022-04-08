package org.cloudbus.iotnetsim.iov.holon;

import java.util.ArrayList;
import java.util.Random;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.iot.nodes.IoTNode;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTNodeHolon;
import org.cloudbus.iotnetsim.iov.IoVNodeType;
import org.cloudbus.iotnetsim.iov.VehicleType;
import org.cloudbus.iotnetsim.network.NetConnection;

import dionasys.holon.datamodel.HolonDataModel;
import dionasys.holon.datamodel.HolonServiceModel;
import dionasys.holon.datamodel.ParameterModel;
import dionasys.holon.datamodel.Types;
import experiments.configurations.*;

/**
 * Class Station
 * for service station of fuel and electric cars based on the VehicleType
 * 
 * @author Maria Salama
 * @author Abdessalam Elhabbash
 */

public class StationHolon extends IoTNodeHolon  {

	//vehicleType enum attribute to specify if the station offers fuel or electric charging for vehicles
	private VehicleType vehicleType; 
	
	private boolean isAvailable;

	protected int currentExpDay;
	protected int currentChangeIndex;

	
	public StationHolon(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public StationHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power, 
			String messagingProtocol, String forwardNodeName) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		holon.setHolonDataModel(createDataModel(name, location,messagingProtocol));
		holon.createOntology();	
	}

	public StationHolon(String name, 
			Location location, IoTNodeType nodeType, NetConnection connection, IoTNodePower power,
			String messagingProrocol, String forwardNodeName,
			VehicleType vType) {

		super(name, location, nodeType, connection, power, forwardNodeName);
		// TODO Auto-generated constructor stub
		
		this.vehicleType = vType;
		this.isAvailable = true;
		holon.setHolonDataModel(createDataModel(name, location,messagingProrocol));
		holon.createOntology();
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");

		// schedule the first event for station to register the holon
		schedule(dataCentre.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_HOLON_REGISTER_HOLON,
						holon);
				
		// schedule the first event for the station
		schedule(this.getId(), CloudSim.getMinTimeBetweenEvents()*100, CloudSimTags.IOV_STATION_CHANGE_AVAILABILITY, true);
		System.out.println(this.getName() + ": id = " + this.getId());
	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is shutting down...");		
	}

	@Override
	public void processEvent(SimEvent ev) {
		super.processEvent(ev);
		// TODO Auto-generated method stub
		switch (ev.getTag()) {
		// Execute sending sensor data 
		case CloudSimTags.IOV_STATION_CHANGE_AVAILABILITY:
			processChangeStationvailability();
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}				
	}

	private void processChangeStationvailability() {
		this.isAvailable = !(this.isAvailable);

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is changing availability" 
				+ " to " + Boolean.toString(this.isAvailable) 
				+ " and sending data to " + CloudSim.getEntityName(getForwardNodeId())
				);

		//send update to Datacenter
		schedule(getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CLOUD_RECEIVE_DATA_EVENT);
		
		if (CloudSim.clock() >= currentExpDay*24*60*60) {
			currentExpDay +=1;
		}
		if (currentExpDay < ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			//schedule the next event for updating the station availability at random time
			scheduleNextChangeRandom(); 
		}
		
	}

	/**
	 * schedule next event for changing station availability at random time
	 */
	private void scheduleNextChangeRandom(){
		// get random time within the same day
		Random random = new Random();  		
		double rTime = random.nextDouble() * (((24*60*60) - CloudSim.getMinTimeBetweenEvents()) + CloudSim.getMinTimeBetweenEvents());  
		
		schedule(this.getId(), rTime, CloudSimTags.IOV_STATION_CHANGE_AVAILABILITY);
	}

	private HolonDataModel createDataModel(String name, 
			Location location, String messagingProtocol) {
		
		HolonDataModel dataModel = new HolonDataModel();
        dataModel.putData("name",name);
        dataModel.putData("type", this.getNodeType().toString());
        dataModel.putData("latitude", location.getX()+"");
        dataModel.putData("longitude", location.getY()+"");
        dataModel.putData("messagingProtocol",messagingProtocol);
        dataModel.putData("connecitonType", this.getConnection().getConnectionType().toString());
        dataModel.putData("powerType", this.getPower().getPowerType().toString());
        dataModel.putData("isAvailable", isAvailable + "");
        dataModel.putData("currentExpDay", currentExpDay +"");
        dataModel.putData("currentChangeIndex", currentChangeIndex+"");
        
        HolonServiceModel service1 = new HolonServiceModel();
        service1.setName("isAvailable");
        service1.setCost(0);
        service1.setUrl("isAvailable");
        service1.setReturnType(Types.DOUBLE);
        service1.setAnnotation(IoVHolonAnnotations.IS_FUEL_AVAILABLE);

        HolonServiceModel service2 = new HolonServiceModel();
        service2.setName("fuelVehicle");
        service2.setCost(0);
        service2.setUrl("fuelVehicle");
        ParameterModel par1 = new ParameterModel("vehicleId",Types.INTEGER);
        ArrayList<ParameterModel> parameters = new ArrayList<>();
        parameters.add(par1);
        service2.setParameters(parameters);
        service2.setReturnType(Types.BOOLEAN);
        service2.setAnnotation(IoVHolonAnnotations.FUEL_VEHICLE);

        HolonServiceModel service3 = new HolonServiceModel();
        service3.setName("getMessagingProtocol");
        service3.setCost(0);
        service3.setUrl("http://10.10.10.2/getMessagingProtocol");
        service3.setReturnType(Types.DOUBLE);
                
        ArrayList<HolonServiceModel> services = new ArrayList<>();
        services.add(service1);
        services.add(service2);
        services.add(service3);
        dataModel.setServices(services);

        return dataModel;         
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

	/**
	 * @return the isAvailable
	 */
	public boolean isAvailable() {
		return isAvailable;
	}

	/**
	 * @param isAvailable the isAvailable to set
	 */
	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}
	
	/**
	 * Fuel a vehicle
	 * @param vehcileId
	 */
	public boolean fuelVehicle(Integer vehicleId) {
		System.out.println(this.getName() + ": vehicleId = " + vehicleId.intValue());
		schedule(vehicleId.intValue(), CloudSim.getMinTimeBetweenEvents()*2, CloudSimTags.IOV_VEHICLE_FUEL_REFILL);
		return true;
	}


}
