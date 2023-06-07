package org.cloudbus.iotnetsim.iov.holon;

import java.util.ArrayList;
import java.util.Random;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.holon.IoTDatacenterHolon;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeMobile;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iot.nodes.MessagingProtocol;
import org.cloudbus.iotnetsim.iot.nodes.holon.IoTNodeHolon;
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
 * @author elhabbas
 * 
 */

public class VehicleHolon extends IoTNodeHolon implements IoTNodeMobile {

	private Location currentLocation;

	// vehicleType enum attribute to specify the type of the vehicle, either fuel,
	// electric or hybrid
	private VehicleType vehicleType;

	private double fuelTankSize; // in gallon or kWh
	private double fuelConsumptionRate; // in unit miles-per-gallon (mpg) or kWh-per-mile
	private double fuelThreshold; // in unit gallon, when threshold is reached, the fuel alert is on

	private double avgSpeed; // in unit mile/hour
	private double remainingRoadTripDistance; // in miles, to set the current road trip or the remaining of the current
												// trip
	private double currentFuelLevel; // in unit gallon, when full, set to the maxFuelCapacity
	private boolean fuelAlert; // boolean variable set to true if the currentFuelLevel is <= the fuelThreshold

	private int countRoadTrips;
	
	private int currentExpDay;

	
	public VehicleHolon(String name, String messagingProtocol) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public VehicleHolon(String name, Location location, IoTNodeType nodeType, NetConnection connection,
			IoTNodePower power,IoTDatacenterHolon dataCentre, String forwardNodeName, MessagingProtocol messagingProtocol) {

		super(name, location, nodeType, connection, power, dataCentre, forwardNodeName, messagingProtocol);
		// TODO Auto-generated constructor stub

		this.currentLocation = location;
		holon.setHolonDataModel(createDataModel(name, location, messagingProtocol));
		holon.createOntology();
	}

	public VehicleHolon(String name, Location location, IoTNodeType nodeType, NetConnection connection,
			IoTNodePower power, IoTDatacenterHolon dataCentre, String forwardNodeName, MessagingProtocol messagingProtocol, VehicleType vType,
			double tank_size, double fuel_consumption_rate, double avg_speed, double fuel_level) {

		super(name, location, nodeType, connection, power,dataCentre, forwardNodeName, messagingProtocol);
		// TODO Auto-generated constructor stub

		this.currentLocation = location;
		this.setVehicleType(vType);
		this.fuelTankSize = tank_size;
		this.setFuelConsumptionRate(fuel_consumption_rate);
		this.avgSpeed = avg_speed;
		this.setCurrentFuelLevel(fuel_level);

		this.currentExpDay = 1;

		// calculate the threshold
		if (this.vehicleType == VehicleType.PETROL_VEHICLE) {
			// as 25% of the tank size
			this.setFuelThreshold(fuelTankSize * 0.25);
		} else if (this.vehicleType == VehicleType.ELECTRIC_VEHICLE) {
			// as 20% of the battery size
			this.setFuelThreshold(fuelTankSize * 0.20);
		}
		this.fuelAlert = (this.currentFuelLevel <= this.fuelThreshold);
		
		this.countRoadTrips = 0;		
		this.currentExpDay = 1;

		holon.setHolonDataModel(createDataModel(name, location, messagingProtocol));
		holon.createOntology();
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is starting...");

		// schedule the first event for parking to register the holon
		schedule(dataCentre.getId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_HOLON_REGISTER_HOLON, holon);

		// establish connection between the vehicle and the UserSmartPhone when the
		// vehicle starts
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.IOV_CONNECT_VEHICLE);

		// schedule the first road trip
		scheduleRoadTrip();
	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		Log.printLine(getName() + " is shutting down...");
	}

	@Override
	public void processEvent(SimEvent ev) {
		// TODO Auto-generated method stub
		switch (ev.getTag()) {
		case CloudSimTags.IOV_VEHICLE_MOVE_EVENT:
			processMoveVehicle();
			break;
		case CloudSimTags.IOV_VEHICLE_SET_FUEL_FULL_EVENT:
			processFuelFull();
			break;

		// other unknown tags are processed by this method
		default:
			processOtherEvent(ev);
			break;
		}
	}

	private void processMoveVehicle() {
		// calculate the distance that the vehicle moved based on the average speed
		double movedDistance = (this.avgSpeed / (CloudSim.getMinTimeBetweenEvents() * 60 * 60));
		if (movedDistance > remainingRoadTripDistance) {
			movedDistance = remainingRoadTripDistance;
		}
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] moved the distance of "
				+ Double.toString(movedDistance));

		// update fuel level
		this.currentFuelLevel = this.currentFuelLevel - (movedDistance / this.fuelConsumptionRate);
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] updated the current fuel level to "
				+ Double.toString(currentFuelLevel));

		// calculate the new location based on the average speed of the vehicle
		// moving the vehicle in X axis only (for simplicity)
		this.currentLocation.setX(this.currentLocation.getX() + avgSpeed);
		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] updated location to " + "("
				+ Double.toString(this.currentLocation.getX()) + ", " + Double.toString(this.currentLocation.getY())
				+ ")");

		// send location update to the UserSmartPhone (the forwardNode)
		schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(),
				CloudSimTags.IOV_VEHICLE_LOCATION_UPDATE_EVENT, this.currentLocation);

		// update the remaining road trip distance
		this.remainingRoadTripDistance = remainingRoadTripDistance - movedDistance;

		// check the fuel level and set the alert if needed
		if (this.currentFuelLevel > 0) {
			if (this.currentFuelLevel <= this.fuelThreshold) {
				// set the fuelAlert to true
				this.fuelAlert = true;
				Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is setting the fuel alert to true");

				// send the fuel alert to the UserSmartPhone (the forwardNode)
				schedule(this.getForwardNodeId(), CloudSim.getMinTimeBetweenEvents(),
						CloudSimTags.IOV_VEHICLE_FUEL_ALERT_EVENT);
			}
		}

		if (remainingRoadTripDistance > 0) {
			if(this.currentFuelLevel > 0) {
				// schedule the next event for the vehicle move after 1 hour 
				schedule(this.getId(), CloudSim.getMinTimeBetweenEvents()*60*60, CloudSimTags.IOV_VEHICLE_MOVE_EVENT);
			} else {
				Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] is running out of fuel and stopping the journey ");
			}
		} else {
			// schedule new road trip if the previous trip has been completed
			if (this.currentExpDay <= ExperimentsConfigurations.EXP_NO_OF_DAYS) {
				scheduleRoadTrip();	
			}
		}
	}

	private void processFuelFull() {
		this.currentFuelLevel = this.fuelTankSize;
		this.fuelAlert = false;

		Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] updated the current fuel level to full");
	}

	private void scheduleRoadTrip() {
		//update experiment day 
		if (CloudSim.clock() > (this.currentExpDay*24*60*60)) {
			this.currentExpDay = (int) (CloudSim.clock() / (24*60*60));
		}

		if (this.currentExpDay <= ExperimentsConfigurations.EXP_NO_OF_DAYS) {
			//get trip length
			if (ExperimentsConfigurations.IOV_EXP_TRIP_TYPE == "FixedLength") {
				remainingRoadTripDistance = ExperimentsConfigurations.IOV_EXP_TRIP_LENGTH;
			} else if (ExperimentsConfigurations.IOV_EXP_TRIP_TYPE == "RandomLength") {
				//get random distance within a range of EXP_TRIP_MAX_LENGTH -- min + (randomValue * (EXP_TRIP_MAX_LENGTH - min))
				double random = new Random().nextDouble();
				remainingRoadTripDistance = 1.0 + (random * (ExperimentsConfigurations.IOV_EXP_TRIP_LENGTH - 1.0));
			}

			//get delay to start the next road trip
			double startDelay = CloudSim.getMinTimeBetweenEvents();
			if (ExperimentsConfigurations.IOV_EXP_TRIP_FREQUENCY == "Continuous") {
				startDelay = CloudSim.getMinTimeBetweenEvents() + ExperimentsConfigurations.IOV_EXP_TRIP_INTERVAL;
			} else if (ExperimentsConfigurations.IOV_EXP_TRIP_FREQUENCY == "Random") {
				double random = new Random().nextDouble();
				//min + (r *(max - min))
				startDelay = CloudSim.getMinTimeBetweenEvents() + 
						(random * ((ExperimentsConfigurations.EXP_NO_OF_DAYS*24*60*60) - CloudSim.getMinTimeBetweenEvents() ));
			}

			//schedule event to start the road trip
			schedule(this.getId(), startDelay, CloudSimTags.IOV_VEHICLE_MOVE_EVENT);

			Log.printLine(CloudSim.clock() + ": [" + this.getName() + "] will start a new road trip "
					+ " after " + startDelay + " ms"
					+ " for distance of " + remainingRoadTripDistance + " miles"
					);
			
			this.countRoadTrips += 1;
		}
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
	 * @return the fuelTankSize
	 */
	public double getFuelTankSize() {
		return fuelTankSize;
	}

	/**
	 * @param tankSize the fuelTankSize to set
	 */
	public void setTankSize(double tankSize) {
		this.fuelTankSize = tankSize;
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
	 * @return the fuelConsumptionRate
	 */
	public double getFuelConsumptionRate() {
		return fuelConsumptionRate;
	}

	/**
	 * @param fuelConsumptionRate the fuelConsumptionRate to set
	 */
	public void setFuelConsumptionRate(double fuelConsumptionRate) {
		this.fuelConsumptionRate = fuelConsumptionRate;
	}

	/**
	 * @return the fuelThreshold
	 */
	public double getFuelThreshold() {
		return fuelThreshold;
	}

	/**
	 * @param fuelThreshold the fuelThreshold to set
	 */
	public void setFuelThreshold(double fuelThreshold) {
		this.fuelThreshold = fuelThreshold;
	}

	/**
	 * @return the avgSpeed
	 */
	public double getAvgSpeed() {
		return avgSpeed;
	}

	/**
	 * @param avgSpeed the avgSpeed to set
	 */
	public void setAvgSpeed(double avgSpeed) {
		this.avgSpeed = avgSpeed;
	}

	/**
	 * @return the currentFuelLevel
	 */
	public double getCurrentFuelLevel() {
		return currentFuelLevel;
	}

	/**
	 * @param currentFuelLevel the currentFuelLevel to set
	 */
	public void setCurrentFuelLevel(double currentFuelLevel) {
		this.currentFuelLevel = currentFuelLevel;
	}

	/**
	 * @return the fuelAlert
	 */
	public boolean isFuelAlert() {
		return fuelAlert;
	}

	/**
	 * @param fuelAlert the fuelAlert to set
	 */
	public void setFuelAlert(boolean fuelAlert) {
		this.fuelAlert = fuelAlert;
	}

	/**
	 * @return the countRoadTrips
	 */
	public int getCountRoadTrips() {
		return countRoadTrips;
	}

	/**
	 * @param countRoadTrips the countRoadTrips to set
	 */
	public void setCountRoadTrips(int countRoadTrips) {
		this.countRoadTrips = countRoadTrips;
	}

	private HolonDataModel createDataModel(String name, Location location, MessagingProtocol messagingProtocol) {

		HolonDataModel dataModel = new HolonDataModel();
		dataModel.putData("name", name);
		dataModel.putData("type", this.vehicleType.toString());
		dataModel.putData("latitude", location.getX() + "");
		dataModel.putData("longitude", location.getY() + "");
		dataModel.putData("messagingProtocol", messagingProtocol.toString());
		dataModel.putData("connecitonType", this.getConnection().getConnectionType().toString());
		dataModel.putData("powerType", this.getPower().getPowerType().toString());

		HolonServiceModel service1 = new HolonServiceModel();
		service1.setName("getCurrentFuelLevel");
		service1.setCost(0);
		service1.setUrl("getCurrentFuelLevel");
		service1.setReturnType(Types.DOUBLE);
		service1.setAnnotation(IoVHolonAnnotations.CURRENT_FUEL_LEVEL);

		HolonServiceModel service2 = new HolonServiceModel();
		service2.setName("getFuelThreshold");
		service2.setCost(0);
		service2.setUrl("getFuelThreshold");
		service2.setReturnType(Types.DOUBLE);
		service2.setAnnotation(IoVHolonAnnotations.CAR_FUEL_THRESHOLD);

		HolonServiceModel service3 = new HolonServiceModel();
		service3.setName("getMessagingProtocol");
		service3.setCost(0);
		service3.setUrl("getMessagingProtocol");
		service3.setReturnType(Types.STRING);
		service3.setAnnotation(IoVHolonAnnotations.MESSAGING_PROTOCOL);

		HolonServiceModel service4 = new HolonServiceModel();
		service4.setName("getFuelTankSize");
		service4.setCost(0);
		service4.setUrl("getFuelTankSize");
		service4.setReturnType(Types.DOUBLE);
		service4.setAnnotation(IoVHolonAnnotations.FUEL_TANK_SIZE);
				
		ArrayList<HolonServiceModel> services = new ArrayList<>();
		services.add(service1);
		services.add(service2);
		services.add(service3);
		services.add(service4);
		dataModel.setServices(services);

		return dataModel;
	}

}
