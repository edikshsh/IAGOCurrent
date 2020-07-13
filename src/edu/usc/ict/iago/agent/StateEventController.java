package edu.usc.ict.iago.agent;
import java.util.ArrayList;
import java.util.HashMap;

import edu.usc.ict.iago.agent.StackDivide.State;
import edu.usc.ict.iago.utils.Event;

public class StateEventController<State extends Enum<State>>{ 
	
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
	  		StateEvent<State> eventType = new StateEvent<State>(currState, e.getType(), e.getSubClass());
			if (stateMachine.get(eventType) != null) {
				return true;
			}
		
			return false;
		}
		
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
		
		public void massMachineStates(State start, State target, Event.EventClass ec, Event.SubClass esc, Class<State> enumType) {
			ArrayList<State> startingStates = new ArrayList<State>();
			startingStates.add(start);
			if (start == null) {
				startingStates = enumValues(enumType);
			}
			
			Event.EventClass[] eventClasses = new Event.EventClass[] {ec};
			if (ec == null) {
				eventClasses = Event.EventClass.values();
			}
			
			Event.SubClass[] eventSubClasses = new Event.SubClass[] {esc};
			if (esc == null) {
				eventSubClasses = Event.SubClass.values();
			}
			
			for (State startingState: startingStates){
				for (Event.EventClass eventClass: eventClasses){
					for (Event.SubClass eventSubClass: eventSubClasses){
						// Skip unnecessary entries
						if (eventClass == Event.EventClass.SEND_MESSAGE || eventSubClass == Event.SubClass.NONE) {
							addState(startingState, target, eventClass, eventSubClass);
						}
					}
				}
			}
			
		}
		
		// Get an arraylist of the values in our enum
		private ArrayList<State> enumValues(Class<State> enumType) {
			ArrayList<State> ret = new ArrayList<State>();
			var arr = enumType.getEnumConstants();
			for (State state : arr) {
				ret.add(state);
			}
			return ret;
		}

	  
	}
	
