package edu.usc.ict.iago.agent;

import java.util.HashMap;
import java.util.LinkedList;

import edu.usc.ict.iago.agent.EventHelper.Expression;
import edu.usc.ict.iago.agent.StackDivide.State;
import edu.usc.ict.iago.utils.Event;
import edu.usc.ict.iago.utils.Event.EventClass;
import edu.usc.ict.iago.utils.Event.SubClass;
import edu.usc.ict.iago.utils.GameSpec;
import edu.usc.ict.iago.utils.Offer;
import edu.usc.ict.iago.utils.Preference;
import edu.usc.ict.iago.utils.Preference.Relation;

public class PlayerOffer<State> extends BusinessLogic {
	
	private IAGOCoreVH agentCore;
	private AgentUtilsExtension utils;
	private GameSpec game;
	private TestBehavior behavior;
	private StateEventController<State> stateEventController; // Just here to let us use the StateEvent functions (can't be static because functions are generic)
	public State currState = State.PLAYEROFFER;
	private Offer suggestedOffer = null;
	
	
	public enum State {
		PLAYEROFFER,
		EVALUATEOFFER,
		COUNTEROFFER,
	 	END
	 }
	
	@Override
	public void reset() {
		super.reset();
		suggestedOffer = null;
		currState = State.PLAYEROFFER;
	}
	
	public void changeToEvaluateOfferState() {
		reset();
		currState = State.EVALUATEOFFER;
	}
	
	public PlayerOffer(AgentUtilsExtension utils, IAGOCoreVH agentCore, GameSpec game, TestBehavior behavior) {
		this.utils = utils;
		this.agentCore = agentCore;
		this.game = game;
		this.behavior = behavior;
		reset();
		this.stateEventController = new StateEventController<PlayerOffer.State>();
		stateEventController.massMachineStates(State.PLAYEROFFER, State.EVALUATEOFFER, Event.EventClass.SEND_OFFER, null, State.class);
		stateEventController.massMachineStates(State.EVALUATEOFFER, State.END, Event.EventClass.SEND_MESSAGE, Event.SubClass.PREF_INFO, State.class);

		stateEventController.massMachineStates(State.COUNTEROFFER, State.END, Event.EventClass.SEND_MESSAGE, Event.SubClass.OFFER_ACCEPT, State.class);
		stateEventController.massMachineStates(State.COUNTEROFFER, State.END, Event.EventClass.SEND_MESSAGE, Event.SubClass.OFFER_REJECT, State.class);
		stateEventController.massMachineStates(State.COUNTEROFFER, State.END, Event.EventClass.SEND_OFFER, null, State.class);

	}
	
	/**
	 * Checks if the sent event is accepted in the current state. 
	 * Used to determine if we need to switch to a new algorithm
	 * @param e: The Event to be checked.
	 * @return True if the state machine accepts the event, false otherwise
	 */
	public boolean doesAcceptEvent(Event e) {
		boolean acceptEvent = stateEventController.doesAcceptEvent(e, currState); 
		return acceptEvent;
	}
	
	/**
	 * Starting point of the algorithm
	 * @param e: The Event received from the parent flow.
	 * @return list of events generated by the algorithm (can be null)
	 */
	public LinkedList<Event> start(Event e){
//		System.out.println("StackDivide start()");
		StateEvent<State> stateEvent = new StateEvent<State>(currState, e.getType(), e.getSubClass());
		State newState = stateEventController.getState(stateEvent);
		if (blState == BLState.START) {
			blState = BLState.ONGOING;
			LinkedList<Event> returnedEvents = funcByState(e);
			return returnedEvents;
		}
		
		if (newState != null) {
			currState = newState;
			LinkedList<Event> returnedEvents = funcByState(e);
			return returnedEvents;
		}
		return null;
	}
	
	/**
	 * Switch case over the state we are currently in. 
	 * There is probably a much better way to do this
	 * @param e: The Event received from the parent flow.
	 * @return list of events generated by the algorithm (can be null)
	 */
	private LinkedList<Event> funcByState(Event e) {
		LinkedList<Event> resp = null;
		switch (currState)
		{
			case PLAYEROFFER:
				resp = statePlayerOffer(e);
				break;
			case EVALUATEOFFER:	
				resp = stateEvaluateOffer(e);
				break;
			case COUNTEROFFER:	
				resp = stateCounterOffer(e);
				break;
			case END:
				resp = stateEnd(e);
				break;

		}
		return resp;

	}
	
	private LinkedList<Event> statePlayerOffer(Event event){
		LinkedList<Event> resp = new LinkedList<Event>();	
		resp.add(EventHelper.message("Please suggest an offer"));
		return resp;
	}
	
	
	//Started an offer
	private LinkedList<Event> stateEvaluateOffer(Event event){
		LinkedList<Event> resp = new LinkedList<Event>();
		suggestedOffer = event.getOffer();
		
		boolean isOfferGood = utils.isOfferGood(behavior.allocated,suggestedOffer);
		if (isOfferGood) {
			resp.add(EventHelper.expression(Expression.HAPPY));
			resp.add(EventHelper.offerAccept("Offer seems good"));
			if (utils.isFullOffer(suggestedOffer)) {
				resp.add(EventHelper.formalAccept());
			}
			blState = BLState.SUCCESS;
		} else {
			currState = State.COUNTEROFFER;
			resp.add(EventHelper.message("Lets adjust that offer a bit..."));
			resp.addAll(stateCounterOffer(event));
		}
		return resp;
	}
	
	//Started an offer
	private LinkedList<Event> stateCounterOffer(Event event){
		LinkedList<Event> resp = new LinkedList<Event>();
		suggestedOffer = event.getOffer();
		Offer counterOffer = behavior.getCounterOffer(suggestedOffer);
		resp.add(EventHelper.message("What do you think about that?"));
		resp.add(EventHelper.offer(counterOffer));
		suggestedOffer = counterOffer;
		
		return resp;
	}
	
	private LinkedList<Event> stateEnd(Event e){
//		System.out.println("StackDivide stateEnd()");
		LinkedList<Event> resp = new LinkedList<Event>();
		
		// Stack divide offer was rejected by the player
		if (e.getSubClass() == Event.SubClass.OFFER_REJECT) {
			
			resp.add(EventHelper.expression(Expression.SAD));
			resp.add(EventHelper.message("Aww"));

			this.blState = BLState.FAILURE;
			
		// Stack divide offer was accepted by the player
		} else if (e.getSubClass() == Event.SubClass.OFFER_ACCEPT){	
			
			resp.add(EventHelper.expression(Expression.HAPPY));
			resp.add(EventHelper.message("Yay"));

			behavior.allocated = suggestedOffer;
			if (utils.isFullOffer(suggestedOffer)) {
				resp.add(EventHelper.formalAccept());
			}
			this.blState = BLState.SUCCESS;

		// Stack divide offer was interrupted by the player by making a new offer
		} else {
			resp.add(EventHelper.expression(Expression.ANGRY));
			resp.add(EventHelper.message("How rude!"));

			continueFlow=true; // Tell the main flow that we want for it to continue handling the event (probably with default bl)
			this.blState = BLState.FAILURE;

		}
		return resp;
	}
	
}