package edu.usc.ict.iago.agent;
import java.util.HashMap;

import edu.usc.ict.iago.agent.StackDivide.State;
import edu.usc.ict.iago.utils.Event;

public class StateEventController<State>{ 
	
	  public HashMap<StateEvent, State> stateMachine; 
	  
	  public StateEventController(HashMap<StateEvent, State> stateMachine) { 
		  this.stateMachine = stateMachine;
	  } 	    
	    
		// checks if the sent event is accepted in the current state
		public boolean doesAcceptEvent(Event e, State currState) {
			System.out.println("StackDivide acceptsEvent()");
			StateEvent<State>[] eventTypes = convertEventToTypes(e, currState);
			for (StateEvent eventType : eventTypes) {
				if (stateMachine.get(eventType) != null) {
					return true;
				}
			}
			return false;
		}
		
		public StateEvent<State>[] convertEventToTypes(Event e, State currState){
			System.out.println("StackDivide convertEventToTypes()");
			StateEvent<State>[] eventTypes = new StateEvent[(e.getType() == Event.EventClass.SEND_MESSAGE ? 2 : 1)];
			eventTypes[0] = new StateEvent<State>(currState, e.getType(), e.getSubClass());
			if (e.getType() == Event.EventClass.SEND_MESSAGE) {
				eventTypes[1] = new StateEvent<State>(currState, e.getType(), null);
			}
			return eventTypes;
		}
	  
	}
	
