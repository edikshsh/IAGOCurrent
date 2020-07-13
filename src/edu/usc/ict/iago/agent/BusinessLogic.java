package edu.usc.ict.iago.agent;

public abstract class BusinessLogic{
	
	public BLState blState;
	public boolean continueFlow = false; // tells if the main flow should continue after getting an answer from the bl
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
