package experiments.examples;

import java.io.FileOutputStream;
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
import org.cloudbus.iotnetsim.iov.Parking;
import org.cloudbus.iotnetsim.iov.TrafficControlUnit;
import org.cloudbus.iotnetsim.network.NetConnection;
import org.cloudbus.iotnetsim.network.NetConnectionType;

import experiments.configurations.ExperimentsConfigurations;
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
 * In Proceedings of the IEEE/ACM 12th International Conference on Utility and Cloud Computing (UCC ’19), December 2–5, 2019, Auckland, New Zealand. 
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

public class MotorwayIoV {
	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {

		try {
			String workingDir = System.getProperty("user.dir");
			String datasetsFolder = workingDir + "//experiments//datasets//";
			String inputFolder_workload = workingDir + "/experiments//workload//";

			OutputStream output = new FileOutputStream(workingDir + "//experiments//results//MotorwayIoV_output.txt");
			Log.setOutput(output);	//Uncomment to write output log in the txt files
			
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
			createTestbed(datacenter0, ExperimentsConfigurations.READING_INTERVAL[0], datasetsFolder);

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

		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	private static void createTestbed(Datacenter datacenter, double readingInterval, String datasetsFolder) {
		//create one IoT testbed
		Log.printLine("Creating one testbed...");
		GeographicRegion region = new GeographicRegion("France", 100.00);

		//create CloudServer
		AdvHost cloudServer = Setup.createAdvHost(100, 3, 2);
		
		//create Parking
		Parking parking_0 = new Parking(
				"Parking_0",
				new Location(200*100, 200*100, 0), 
				IoTNodeType.PARKING,
				new NetConnection("wifi", new NetConnectionType(), 100.00), 
				new IoTNodePower(IoTNodePowerType.CONTINUOUS_POWER, true, false, true, 100.00, 0.00, 0.00),
				datacenter.getName(), 
				100,
				readingInterval+CloudSim.getMinTimeBetweenEvents()*3
				);

		//create Restaurant 
		
		//create Fuel Station
		
		//create Electric Charging Station
		
		//create Traffic Control Unit
//		TrafficControlUnit trafficControlUnit_0 = new TrafficControlUnit(
//				"TrafficControlUnit", 
//				new Location(200*100, 200*100, 0), 
//				IoTNodeType.GATEWAY_Node, 
//				new NetConnection("wifi", new NetConnectionType(), 100.00), 
//				new IoTNodePower(IoTNodePowerType.CONTINUOUS_POWER, true, false, true, 100.00, 0.00, 0.00),
//				datacenter.getId(), 
//				readingInterval+CloudSim.getMinTimeBetweenEvents()*3, 
//				readingInterval+CloudSim.getMinTimeBetweenEvents()*3
//				);

		//create user smart phone

		//create vehicle
		
		//create electric vehicle

	}

}