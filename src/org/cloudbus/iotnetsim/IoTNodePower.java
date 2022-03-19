package org.cloudbus.iotnetsim;

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
 * Class IoTNodePower
 * 
 * @author Maria Salama
 * 
 */

public class IoTNodePower {

	private IoTNodePowerType powerType;
	private boolean isContinuousPower;
	//private boolean isBatteryPower;
	
	private boolean isCurrentPlugged;

	private double currentPowerLevel;
	private double powerConsumptionRate;
	private double powerLowThreshold;
	
	public IoTNodePower() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public IoTNodePower(IoTNodePowerType powerType, boolean isContinuousPower, boolean isBatteryPower, boolean isCurrentPlugged,
			double currentBatteryLevel, double batteryConsumptionRate, double batteryLowThreshold) {
		super();
		this.powerType = powerType;
		this.isContinuousPower = isContinuousPower;
		//this.isBatteryPower = isBatteryPower;
		this.isCurrentPlugged = isCurrentPlugged;
		this.currentPowerLevel = currentBatteryLevel;
		this.powerConsumptionRate = batteryConsumptionRate;
		this.powerLowThreshold = batteryLowThreshold;
	}

	public void plugPower() {
		isCurrentPlugged = true;
	}

	public void UnplugPower() {
		isCurrentPlugged = false;
	}

	public void consumeBatteryLevel() {
		currentPowerLevel -= powerConsumptionRate;
	}
	
	public void chargeBattery() {
		currentPowerLevel += powerConsumptionRate;
	}
	
	public void chargeBatteryToFull() {
		currentPowerLevel = 100.00;
	}

	public boolean isBatteryFull() {
		return this.currentPowerLevel == 100.00;
	}
	
	public boolean isBatteryLow() {
		return this.currentPowerLevel <= powerLowThreshold;
	}

	public IoTNodePowerType getPowerType() {
		return powerType;
	}

	public void setPowerType(IoTNodePowerType powerType) {
		this.powerType = powerType;
	}

	public boolean isContinuousPower() {
		return isContinuousPower;
	}

	public void setContinuousPower(boolean isContinuousPower) {
		this.isContinuousPower = isContinuousPower;
	}

	/*
	 * public boolean isBatteryPower() { return isBatteryPower; }
	 * 
	 * public void setBatteryPower(boolean isBatteryPower) { this.isBatteryPower =
	 * isBatteryPower; }
	 */
	
	public boolean isCurrentPlugged() {
		return isCurrentPlugged;
	}

	public void setCurrentPlugged(boolean isCurrentPlugged) {
		this.isCurrentPlugged = isCurrentPlugged;
	}

	public double getCurrentBatteryLevel() {
		return currentPowerLevel;
	}

	public void setCurrentBatteryLevel(double currentBatteryLevel) {
		this.currentPowerLevel = currentBatteryLevel;
	}

	public double getBatteryConsumptionRate() {
		return powerConsumptionRate;
	}

	public void setBatteryConsumptionRate(double batteryConsumptionRate) {
		this.powerConsumptionRate = batteryConsumptionRate;
	}

	public double getBatteryLowThreshold() {
		return powerLowThreshold;
	}

	public void setBatteryLowThreshold(double batteryLowThreshold) {
		this.powerLowThreshold = batteryLowThreshold;
	}

	

}
