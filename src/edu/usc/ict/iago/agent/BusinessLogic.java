package edu.usc.ict.iago.agent;

import edu.usc.ict.iago.utils.Event;

public abstract class BusinessLogic<State>{
	
	public BLState blState;
	public enum BLState {
		 START,
		 ONGOING,
		 SUCCESS,
	 	 FAILURE,
	 	 TIMEOUT
	 }
	
	
	public void reset() {
		blState = BLState.START;
	}
	
}
