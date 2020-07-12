package edu.usc.ict.iago.agent;
import java.util.HashMap;

import edu.usc.ict.iago.agent.StackDivide.State;
import edu.usc.ict.iago.utils.Event;

public class StateEventController<State>{ 
	
	  public HashMap<StateEvent<State>, State> stateMachine; 
	  
	  public StateEventController() { 
		  this.stateMachine = new HashMap<StateEvent<State>, State>();
	  } 	    
	    
		/**
		 * Checks if the sent event is accepted in the current state. 
		 * Used to determine if we need to switch to a new algorithm
		 * @param e: The Event to be checked.
		 * @return True if the state machine accepts the event, false otherwise
		 */
	  	public boolean doesAcceptEvent(Event e, State currState) {
//			StateEvent<State>[] eventTypes = convertEventToTypes(e, currState);
	  		StateEvent<State> eventType = new StateEvent<State>(currState, e.getType(), e.getSubClass());
			if (stateMachine.get(eventType) != null) {
				return true;
			}
		
			return false;
		}
		
		/**
		 * Converts an event to StateEvents array to be used as keys in the stateMachine. 
		 * @param e: The Event used.
		 * @return stateMachine keys to be searched
		 */
//		public StateEvent<State>[] convertEventToTypes(Event e, State currState){
//			StateEvent<State>[] eventTypes = new StateEvent[(e.getType() == Event.EventClass.SEND_MESSAGE ? 2 : 1)];
//			eventTypes[0] = new StateEvent<State>(currState, e.getType(), e.getSubClass());
//			if (e.getType() == Event.EventClass.SEND_MESSAGE) {
//				eventTypes[1] = new StateEvent<State>(currState, e.getType(), null);
//			}
//			return eventTypes;
//		}
		
		public boolean addState(State currentState, State nextState, Event.EventClass ec, Event.SubClass esc) {
			StateEvent<State> stateEvent = new StateEvent<State>(currentState, ec, esc);
			if (stateMachine.get(stateEvent) != null) {
				return false;
			}
			stateMachine.put(stateEvent, nextState);
			return true;
		}
		
		public State getState(StateEvent<State> key) {
			return stateMachine.get(key);
		}
	  
	}
	
