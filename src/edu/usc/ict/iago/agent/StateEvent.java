package edu.usc.ict.iago.agent;
import edu.usc.ict.iago.agent.StackDivide.State;
import edu.usc.ict.iago.utils.Event;

public class StateEvent<State>{ 
	
	
	// not used but shows what EventClass and SubClass combinations are available
//	enum EventType{
//		OFFER_IN_PROGRESS__NONE,
//		FORMAL_ACCEPT__NONE,
//		GAME_START__NONE,
//		TIME__NONE,
//		SEND_EXPRESSION__NONE,
//		SEND_OFFER__NONE,
//		
//		SEND_MESSAGE__BATNA_INFO,
//		SEND_MESSAGE__BATNA_REQUEST,
//		SEND_MESSAGE__CONFUSION,
//		SEND_MESSAGE__FAVOR_ACCEPT,
//		SEND_MESSAGE__FAVOR_REJECT,
//		SEND_MESSAGE__FAVOR_REQUEST,
//		SEND_MESSAGE__FAVOR_RETURN,
//		SEND_MESSAGE__GENERIC_NEG,
//		SEND_MESSAGE__GENERIC_POS,
//		SEND_MESSAGE__NONE,
//		SEND_MESSAGE__OFFER_ACCEPT,
//		SEND_MESSAGE__OFFER_PROPOSE,
//		SEND_MESSAGE__OFFER_REJECT,
//		SEND_MESSAGE__OFFER_REQUEST_NEG,
//		SEND_MESSAGE__OFFER_REQUEST_POS,
//		SEND_MESSAGE__PREF_INFO,
//		SEND_MESSAGE__PREF_REQUEST,
//		SEND_MESSAGE__PREF_SPECIFIC_REQUEST,
//		SEND_MESSAGE__PREF_WITHHOLD,
//		SEND_MESSAGE__THREAT_POS,
//		SEND_MESSAGE__TIMING,
//		SEND_MESSAGE__ANY;
//	}

		
	  public final State state; 
	  public final Event.EventClass ec; 
	  public final Event.SubClass esc; 
	  public StateEvent(State state, Event.EventClass ec, Event.SubClass esc) { 
	    this.state = state; 
	    this.ec = ec; 
	    this.esc = esc; 
	  } 
	  
	  public String toString() {
		  if (esc != null)
			  return state + "__" + ec + "__" + esc;
		  else
			  return state + "__" + ec + "__" + "ANY";
	  }
	  
	  @Override
	  public boolean equals(Object o) {
		  if (o.getClass() ==StateEvent.class) 
			  return this.toString().equals(((StateEvent)o).toString());
		  return false;
	  }
	  

	 @Override    
	 public int hashCode() {   
	 	 int total = 23 * state.hashCode() + 61 * ec.hashCode();
		 if (esc != null) {
			 total += 89 * esc.hashCode();
		 }
		 return total;
	 }
	     
	}
	
