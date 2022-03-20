package org.cloudbus.iotnetsim.iov;

import dionasys.holon.Holon;
import dionasys.holon.HolonRegistry;
import dionasys.holon.datamodel.HolonDataModel;

/**
 * Class
 * 
 * @author Maria Salama
 * 
 */

public class Station extends Holon  {

	private VehicleType vehicleType;

	public Station(HolonDataModel model, String type, HolonRegistry registry) {
		super(model, type, registry);
		// TODO Auto-generated constructor stub
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



}
