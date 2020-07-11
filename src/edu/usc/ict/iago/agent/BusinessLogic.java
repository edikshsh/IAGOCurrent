package edu.usc.ict.iago.agent;

public class BusinessLogic {
	
	public BLState blState;
	public enum BLState {
		 ONGOING,
		 SUCCESS,
	 	 FAILURE,
	 	 TIMEOUT
	 }
}
