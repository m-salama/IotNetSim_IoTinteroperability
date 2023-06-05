
package experiments.examples;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.adv.AdvHost;
import org.cloudbus.cloudsim.adv.AdvVm;
import org.cloudbus.cloudsim.adv.DynamicDatacenterBroker;
import org.cloudbus.cloudsim.adv.Service;
import org.cloudbus.cloudsim.adv.ServiceRequest;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.iotnetsim.GeographicRegion;
import org.cloudbus.iotnetsim.IoTDatacenter;
import org.cloudbus.iotnetsim.IoTNodePower;
import org.cloudbus.iotnetsim.IoTNodePowerType;
import org.cloudbus.iotnetsim.Location;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;
import org.cloudbus.iotnetsim.iot.nodes.MessagingProtocol;
import org.cloudbus.iotnetsim.iov.ElectricChargingStation;
import org.cloudbus.iotnetsim.iov.Parking;
import org.cloudbus.iotnetsim.iov.PetrolStation;
import org.cloudbus.iotnetsim.iov.Restaurant;
import org.cloudbus.iotnetsim.iov.TrafficControlUnit;
import org.cloudbus.iotnetsim.iov.UserSmartPhone;
import org.cloudbus.iotnetsim.iov.Vehicle;
import org.cloudbus.iotnetsim.iov.VehicleType;
import org.cloudbus.iotnetsim.network.NetConnection;
import org.cloudbus.iotnetsim.network.NetConnectionType;

import experiments.configurations.ExperimentsConfigurations;
import dionasys.mediation.Mediator;
import experiments.helper.Setup;
import experiments.helper.Workload;

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
 * In Proceedings of the IEEE/ACM 12th International Conference on Utility and Cloud Computing (UCC �19), December 2�5, 2019, Auckland, New Zealand. 
 * ACM, NewYork,NY, USA, 11 pages. 
 * https://doi.org/10.1145/3344341.3368820
 * 
 */

/**
 * MotorwayIoV simple experiment
 * simulating 1 testbed
 * running for x time
 *  
 */

public class MotorwayIoVExample {
	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {

		//list of user smart phones
		List<UserSmartPhone> lstUsers = new ArrayList<UserSmartPhone>();

		try {
			String workingDir = System.getProperty("user.dir");
			String datasetsFolder = workingDir + "//src//experiments//datasets//";
			String inputFolder_workload = workingDir + "/src//experiments//workload//";

			//Comment to stop writing output log in the txt files
			OutputStream output = new FileOutputStream(
					workingDir + "//src//experiments//results//MotorwayIoVexampleClassic_output.txt");
			Log.setOutput(output); 
			
			Log.printLine("Starting MotorwayIoV simple experiment...");

			//record the start local time
			LocalTime startTime = java.time.LocalTime.now();

			//initialise the CloudSim package. It should be called before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			//initialise the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			//create Hosts
			List<AdvHost> hostList = new ArrayList<AdvHost>();
			hostList = Setup.createAdvHostList(1, 3, 2);  //2 = VmSchedulerTimeShared
			//create Datacenter
			IoTDatacenter datacenter0 = Setup.createIoTDatacenter("Datacenter_0", hostList);

			//create Broker
			DynamicDatacenterBroker broker = Setup.createDynamicBroker();
			int brokerId = broker.getId();

			//create VMs
			List<? extends Vm> vmlist = new ArrayList<AdvVm>();
			int[] vmType = {1};
			int[] vmsNumber = {1};
			vmlist = Setup.createHeterogenousAdvVmList(brokerId, vmsNumber, vmType, 1);
			// submit VMs list to the broker
			broker.submitVmList(vmlist);

			//create ServiceType
			int serviceType = 0;
			int serviceId = 1;
			Service service = Workload.createService(serviceId, serviceType);

			//create Cloudlets
			UtilizationModel utilizationModel = new UtilizationModelFull();
			List<ServiceRequest> cloudletList = new ArrayList<ServiceRequest>();
			//cloudletList = Workload.createServiceRequest(brokerId, serviceId, 100, serviceType, utilizationModel);

			cloudletList = Workload.generateWorkloadRuntime(
					inputFolder_workload + "iot//" + "iot_workload_minimal.txt", ExperimentsConfigurations.WORKLOAD_INTERVAL[0],
					brokerId, serviceId, serviceType, utilizationModel);
		
			// submit cloudlet list to the broker
			broker.submitServiceRequestList(cloudletList);

			//create one IoV testbed
			createTestbed(datacenter0, ExperimentsConfigurations.IOV_EXP_ServiceEntities_INTERVAL, datasetsFolder);

			// create users (SmartPhones and Vehicles)
			//we create 2 users; one petrol and one electric
			lstUsers = createUsers(datacenter0, lstUsers); 

			double lastClock = CloudSim.startSimulation();

			CloudSim.stopSimulation();

			Log.printLine();
			Log.printLine("Experiment finished!");	

			LocalTime finishTime = java.time.LocalTime.now();
			
			//calculate actual simulation time in Nanoseconds
			Log.printLine();
			Log.printLine("Actual simulation time: " + Duration.between(startTime,finishTime).getNano());

			//get used memory
			Runtime runtime = Runtime.getRuntime();
			Log.printLine("Used memory: " + (runtime.totalMemory() - runtime.freeMemory()/1024*1024));
			Log.printLine();
			
			lstUsers.forEach(user->{
				Log.printLine(user.getName() + ": ");
				Log.printLine("FuelAlerts count " + user.getCountFuelAlerts());
				Log.printLine("FuelRequests count " + user.getCountFuelRequests());
				Log.printLine("FuelRequests TotalMessages " + user.getFuelRequestsTotalMessages());
				Log.printLine("FuelRequests AverageResponseTime " + user.getFuelRequestsAverageResponseTime());
				Log.print("FuelRequests ResponseTime: ");
				user.getLstFuelRequestsResponseTime().forEach(rt->{Log.print(rt + ", ");});
				Log.printLine();
				
				Log.printLine("RestaurantRequests count " + user.getCountRestaurantRequests());
				Log.printLine("RestaurantRequests TotalMessages " + user.getRestaurantRequestsTotalMessages());
				Log.printLine("RestaurantRequests AverageResponseTime " + user.getRestaurantRequestsAverageResponseTime());
				Log.print("RestaurantRequests ResponseTime: ");
				user.getLstRestaurantRequestsResponseTime().forEach(rt->{Log.print(rt + ", ");});
				Log.printLine();
				
				Log.printLine("ParkingRequests count " + user.getCountParkingRequests());
				Log.printLine("ParkingRequests TotalMessages " + user.getParkingRequestsTotalMessages());
				Log.printLine("ParkingRequests AverageResponseTime " + user.getParkingRequestsAverageResponseTime());
				Log.print("ParkingRequests ResponseTime: ");
				user.getLstParkingRequestsResponseTime().forEach(rt->{Log.print(rt + ", ");});
				Log.printLine();

				Log.printLine("TrafficAlerts count " + user.getCountTrafficAlertRequests());
				Log.printLine("TrafficAlerts TotalMessages " + user.getTrafficAlertsTotalMessages());
				Log.printLine("TrafficAlerts AverageResponseTime " + user.getTrafficAlertsAverageResponseTime());
				Log.print("TrafficAlerts ResponseTime: ");
				user.getLstTrafficAlertsResponseTime().forEach(rt->{Log.print(rt + ", ");});
				Log.printLine();
			});
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	private static void createTestbed(Datacenter datacenter, double change_interval, String datasetsFolder) {
		//create one IoT testbed
		Log.printLine("Creating one testbed...");
		GeographicRegion region = new GeographicRegion("France", 100.00);

		//create CloudServer
		AdvHost cloudServer = Setup.createAdvHost(100, 3, 2);
		
		//create Petrol Station
		PetrolStation petrolStation_0 = new PetrolStation(
				"PetrolStation_0",
				new Location(10, 10, 0),
				IoTNodeType.PETROL_STATION,
				new NetConnection("wifi", new NetConnectionType(), 100.00), 
				new IoTNodePower(IoTNodePowerType.CONTINUOUS_POWER, true, false, true, 100.00, 0.00, 0.00),
				datacenter.getName(), 
				MessagingProtocol.HTTP,
				0.153,		//153p per litre
				change_interval
				);
		
		//create Electric Charging Station
		ElectricChargingStation electricChargingStation_0 = new ElectricChargingStation(
				"ElectricChargingStation_0",
				new Location(10, 10, 0),
				IoTNodeType.ELECTRIC_CHARGING_STATION,
				new NetConnection("wifi", new NetConnectionType(), 100.00), 
				new IoTNodePower(IoTNodePowerType.CONTINUOUS_POWER, true, false, true, 100.00, 0.00, 0.00),
				datacenter.getName(), 
				MessagingProtocol.HTTP,
				0.28,		//28p per kWh
				change_interval
				);
		
		//create Parking
		Parking parking_0 = new Parking(
				"Parking_0",
				new Location(200*100, 200*100, 0), 
				IoTNodeType.PARKING,
				new NetConnection("wifi", new NetConnectionType(), 100.00), 
				new IoTNodePower(IoTNodePowerType.CONTINUOUS_POWER, true, false, true, 100.00, 0.00, 0.00),
				datacenter.getName(), 
				MessagingProtocol.HTTP,
				100,
				change_interval
				);

		//create Restaurant 
		Restaurant restaurant_0 = new Restaurant(
				"Restaurant_0", 
				new Location(10, 15, 0), 
				IoTNodeType.RESTAURANT, 
				new NetConnection("wifi", new NetConnectionType(), 100.00), 
				new IoTNodePower(IoTNodePowerType.CONTINUOUS_POWER, true, false, true, 100.00, 0.00, 0.00),
				datacenter.getName(), 
				MessagingProtocol.HTTP,
				6*60*60,	//opening time 6h
				22*60*60,	//closing time 22h
				10*60		//order preparation time 10min
				);
		
		//create Traffic Control Unit
		TrafficControlUnit trafficControlUnit_0 = new TrafficControlUnit(
				"TrafficControlUnit_0", 
				new Location(100, 100, 0), 
				IoTNodeType.TRAFFIC_CONTROL_UNIT, 
				new NetConnection("wifi", new NetConnectionType(), 100.00), 
				new IoTNodePower(IoTNodePowerType.CONTINUOUS_POWER, true, false, true, 100.00, 0.00, 0.00),
				datacenter.getName(), 
				MessagingProtocol.HTTP,
				change_interval
				);
		
//		Mediator m = new Mediator("mediator_0", userSmartPhone_1.getId(), restaurant_0.getId(), new Object(), new Object());
//		m.processRequest();
	}
	
	private static List<UserSmartPhone> createUsers(Datacenter datacenter, List<UserSmartPhone> lstUsers) {
		//create user smart phone
		UserSmartPhone userSmartPhone_0 = new UserSmartPhone(
				"UserSmartPhone_P0",
				new Location(0, 0, 0),
				IoTNodeType.USER_SMART_PHONE,
				new NetConnection("4G", new NetConnectionType(), 100.00), 
				new IoTNodePower(IoTNodePowerType.BATTERY, true, false, true, 100.00, 0.00, 0.00),
				datacenter.getName(), 
				MessagingProtocol.HTTP
				);		
		//create petrol vehicle
		Vehicle petrolVehicle_0 = new Vehicle(
				"PetrolVehicle_0",
				new Location(0, 0, 0),
				IoTNodeType.VEHICLE,
				new NetConnection("4G", new NetConnectionType(), 100.00), 
				new IoTNodePower(IoTNodePowerType.FUEL, true, false, true, 100.00, 0.00, 0.00),
				userSmartPhone_0.getName(), 
				MessagingProtocol.HTTP,
				VehicleType.PETROL_VEHICLE,
				45.0,	//tank size 
				30.0,	//consumption rate 
				50.0,	//average speed 
				11.25	//current fuel level
				);
		
		//create user smart phone
		UserSmartPhone userSmartPhone_1 = new UserSmartPhone(
				"UserSmartPhone_E0",
				new Location(0, 0, 0),
				IoTNodeType.USER_SMART_PHONE,
				new NetConnection("4G", new NetConnectionType(), 100.00), 
				new IoTNodePower(IoTNodePowerType.BATTERY, true, false, true, 100.00, 0.00, 0.00),
				datacenter.getName(), 
				MessagingProtocol.HTTP
				);
		//create electric vehicle
		Vehicle electricVehicle_1 = new Vehicle(
				"ElectricVehicle_E0",
				new Location(0, 0, 0),
				IoTNodeType.VEHICLE,
				new NetConnection("4G", new NetConnectionType(), 100.00), 
				new IoTNodePower(IoTNodePowerType.BATTERY, true, false, true, 100.00, 0.00, 0.00),
				userSmartPhone_1.getName(), 
				MessagingProtocol.HTTP,
				VehicleType.ELECTRIC_VEHICLE,
				35.0,	//battery size 
				2.45,	//consumption rate 
				50.0,	//average speed 
				7.0	//current battery level
				);

		//add created users to the list
		lstUsers.add(userSmartPhone_0);
		lstUsers.add(userSmartPhone_1);		
		return lstUsers;
	}

}