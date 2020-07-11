package edu.usc.ict.iago.agent;

public class BusinessLogic {
	
	public BLState blState;
	public enum BLState {
		 START,
		 ONGOING,
		 SUCCESS,
	 	 FAILURE,
	 	 TIMEOUT
	 }
	
	public void resetBLState() {
		blState = BLState.START;
	}
}
