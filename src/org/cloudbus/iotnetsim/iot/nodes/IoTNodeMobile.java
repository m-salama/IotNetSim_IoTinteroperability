package org.cloudbus.iotnetsim.iot.nodes;

import org.cloudbus.iotnetsim.Location;

/**
 * Interface IoTNodeMobile
 * 
 * @author Maria Salama
 * 
 */

public interface IoTNodeMobile {
	
	public void changeAltitude(double newZ);
	
	public Location getCurrentLocation();
	public void setCurrentLocation(Location currentLocation);

}
