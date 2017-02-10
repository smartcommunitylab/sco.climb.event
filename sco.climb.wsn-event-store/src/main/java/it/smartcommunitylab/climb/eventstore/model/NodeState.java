package it.smartcommunitylab.climb.eventstore.model;

public class NodeState {
	private String passengerId;
	private String wsnNodeId;
	private String battery;
	private boolean manualCheckIn;
	
	public String getPassengerId() {
		return passengerId;
	}
	public void setPassengerId(String passengerId) {
		this.passengerId = passengerId;
	}
	public String getWsnNodeId() {
		return wsnNodeId;
	}
	public void setWsnNodeId(String wsnNodeId) {
		this.wsnNodeId = wsnNodeId;
	}
	public String getBattery() {
		return battery;
	}
	public void setBattery(String battery) {
		this.battery = battery;
	}
	public boolean isManualCheckIn() {
		return manualCheckIn;
	}
	public void setManualCheckIn(boolean manualCheckIn) {
		this.manualCheckIn = manualCheckIn;
	}
}
