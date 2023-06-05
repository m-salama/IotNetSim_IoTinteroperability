package org.cloudbus.iotnetsim.iov.holon;

import java.util.ArrayList;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.iotnetsim.iot.nodes.IoTNodeType;

public class Workflow {

	private ArrayList<IoTNodeType> requiredNodes; // type,object
	private UserSmartPhoneHolon smartPhoneApp;

	public Workflow(UserSmartPhoneHolon smartPhoneApp) {
		this.smartPhoneApp = smartPhoneApp;
		requiredNodes = new ArrayList<>();
		requiredNodes.add(IoTNodeType.ELECTRIC_VEHICLE);
		requiredNodes.add(IoTNodeType.PETROL_STATION);
		requiredNodes.add(IoTNodeType.RESTAURANT);
		requiredNodes.add(IoTNodeType.PARKING);
		requiredNodes.add(IoTNodeType.TRAFFIC_CONTROL_UNIT);
		requiredNodes.add(IoTNodeType.ELECTRIC_CHARGING_STATION);
		requiredNodes.add(IoTNodeType.PETROL_VEHICLE);
		 
		//smartPhoneApp.setWorkflow();
	}

	public void execute() {
		if ((Double) smartPhoneApp.callService(new Object[1], IoTNodeType.PETROL_VEHICLE,
				IoVHolonAnnotations.CURRENT_FUEL_LEVEL) < (Double) smartPhoneApp.callService(new Object[1],
						IoTNodeType.PETROL_VEHICLE, IoVHolonAnnotations.CAR_FUEL_THRESHOLD)) {
			Boolean fueled = (Boolean) smartPhoneApp.callService(new Object[1], IoTNodeType.FUEL_STATION,
					IoVHolonAnnotations.FUEL_VEHICLE);
			if (fueled) {
				System.out.println("Vehicle fuled");
			}
		}
		Object[] parameters = new Object[1];
		parameters[0] = CloudSim.getEntityId(smartPhoneApp.getName());
		smartPhoneApp.callService(parameters, IoTNodeType.RESTAURANT, IoVHolonAnnotations.RESTAURANT_ORDER);
		Log.printLine(CloudSim.clock() + ": [ Workflow execution ] Place restaurant order");

		Object avParking = smartPhoneApp.callService(new Object[1], IoTNodeType.PARKING, IoVHolonAnnotations.AVAILABLE_PARKING_SLOTS);
		Log.printLine(CloudSim.clock() + ": [ Workflow execution ] Available parking spacces: " + avParking);

		Object alert = smartPhoneApp.callService(new Object[1], IoTNodeType.TRAFFIC_CONTROL_UNIT, IoVHolonAnnotations.IS_TRAFFIC_ALERT);
		Log.printLine(CloudSim.clock() + ": [ Workflow execution ] Traffic alert is set to " + alert);
	}

	public ArrayList<IoTNodeType> getRequiredNodes() {
		return requiredNodes;
	}

	public void setRequiredNodes(ArrayList<IoTNodeType> requiredNodes) {
		this.requiredNodes = requiredNodes;
	}

}