package org.cloudbus.iotnetsim.iov.holon;

import java.util.ArrayList;

import org.cloudbus.iotnetsim.iov.IoVNodeType;

public class Workflow {

	private ArrayList<IoVNodeType> requiredNodes; // type,object
	private UserSmartPhoneHolon smartPhoneApp;

	public Workflow(UserSmartPhoneHolon smartPhoneApp) {
		this.smartPhoneApp = smartPhoneApp;
		requiredNodes = new ArrayList<>();
		requiredNodes.add(IoVNodeType.VEHICLE);
		requiredNodes.add(IoVNodeType.FUEL_STATION);
		requiredNodes.add(IoVNodeType.RESTAURANT);
		//smartPhoneApp.setWorkflow();
	}

	public void execute() {
//		double res = (Double) smartPhoneApp.callService(new Object[1], IoVNodeType.VEHICLE,IoVHolonAnnotations.CAR_RANGE);
//		System.out.println("Workflow: res = " + res);

	if ((Double) smartPhoneApp.callService(new Object[1], IoVNodeType.VEHICLE,
				IoVHolonAnnotations.CAR_RANGE) < (Double) smartPhoneApp.callService(new Object[1], IoVNodeType.VEHICLE,
						IoVHolonAnnotations.CAR_RANGE_THRESHOLD)) {
		Boolean fueled = (Boolean)smartPhoneApp.callService(new Object[1], IoVNodeType.FUEL_STATION, IoVHolonAnnotations.FUEL_VEHICLE);
		if(fueled) {
			System.out.println("Vehicle fuled");
		}
		}
	smartPhoneApp.callService(new Object[1], IoVNodeType.RESTAURANT, IoVHolonAnnotations.BOOK_TABLE);
	}

	public ArrayList<IoVNodeType> getRequiredNodes() {
		return requiredNodes;
	}

	public void setRequiredNodes(ArrayList<IoVNodeType> requiredNodes) {
		this.requiredNodes = requiredNodes;
	}

}