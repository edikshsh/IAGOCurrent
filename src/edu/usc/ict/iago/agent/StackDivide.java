package edu.usc.ict.iago.agent;

import java.util.HashMap;
import java.util.LinkedList;

import edu.usc.ict.iago.utils.Event;
import edu.usc.ict.iago.utils.Event.EventClass;
import edu.usc.ict.iago.utils.Event.SubClass;
import edu.usc.ict.iago.utils.GameSpec;
import edu.usc.ict.iago.utils.Offer;

public class StackDivide<State> extends BusinessLogic<State> {
	
	private IAGOCoreVH agentCore;
	private AgentUtilsExtension utils;
	private GameSpec game;
	private TestBehavior behavior;
	public static boolean agentOwsAFavor = false;
//	private HashMap<Tuple<State, String>, State> stateMachine;
//	private HashMap<StateEvent, State> stateMachine;
	private StateEventController<State> stateEventController; // Just here to let us use the StateEvent functions (can't be static because functions are generic)
	public State currState = State.ASKFAVORITE;
	private Offer stateStartSuggestedOffer = null;
	
	enum State {
		 ASKFAVORITE,
		 MAKEDEAL,
	 	 END
	 }
	
	@Override
	public void reset() {
		super.reset();
		stateStartSuggestedOffer = null;
		currState = State.ASKFAVORITE;
	}
	

	public StackDivide(AgentUtilsExtension utils, IAGOCoreVH agentCore, GameSpec game, TestBehavior behavior) {
		this.utils = utils;
		this.agentCore = agentCore;
		this.game = game;
		this.behavior = behavior;
		reset();
		this.stateEventController = new StateEventController<StackDivide.State>();
		stateEventController.massMachineStates(State.ASKFAVORITE, State.MAKEDEAL, Event.EventClass.SEND_MESSAGE, Event.SubClass.PREF_INFO, State.class);
		stateEventController.massMachineStates(State.MAKEDEAL, State.END, Event.EventClass.SEND_MESSAGE, Event.SubClass.OFFER_ACCEPT, State.class);
		stateEventController.massMachineStates(State.MAKEDEAL, State.END, Event.EventClass.SEND_MESSAGE, Event.SubClass.OFFER_REJECT, State.class);

	}
	
	/**
	 * Checks if the sent event is accepted in the current state. 
	 * Used to determine if we need to switch to a new algorithm
	 * @param e: The Event to be checked.
	 * @return True if the state machine accepts the event, false otherwise
	 */
	public boolean doesAcceptEvent(Event e) {
		boolean acceptEvent = stateEventController.doesAcceptEvent(e, currState); 
		// State controller does not support preference type so need to make a special check
		if (e.getPreference() != null) {
			System.out.println("StackDivide doesAcceptEvent() event contains preference, acceptEvent = " + acceptEvent + ", isQuery = " + e.getPreference().isQuery());
			acceptEvent &= !e.getPreference().isQuery();
		}
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
		System.out.println("StackDivide curr BL = " + blState);
		if (blState == BLState.START) {
			System.out.println("StackDivide first");
			blState = BLState.ONGOING;
			LinkedList<Event> returnedEvents = funcByState(e);
			return returnedEvents;
		}
		
		if (newState != null) {
//			System.out.println("Changing states: " + currState + " -> " + newState);
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
			case ASKFAVORITE:
				resp = stateAskFavorite(e);
				break;
			case MAKEDEAL:	
				resp = stateMakeDeal(e);
				break;
			case END:
				resp = stateEnd(e);
				break;

		}
		return resp;

	}
	
	private LinkedList<Event> stateAskFavorite(Event event){
		System.out.println("StackDivide ask pref");
		LinkedList<Event> resp = new LinkedList<Event>();
		Event askPref = new Event(StaticData.playerId, Event.EventClass.SEND_MESSAGE, Event.SubClass.PREF_REQUEST,
				"I would like to divide the free resources as fairly as possible, can you please tell me what is your favorite resource?",
				(int) (1000 * game.getMultiplier()));
		resp.add(askPref);
		return resp;
	}
	
	//Started an offer
	private LinkedList<Event> stateMakeDeal(Event event){
		LinkedList<Event> resp = new LinkedList<Event>();
		if (event.getPreference()!= null && !event.getPreference().isQuery()) {
			utils.addPref(event.getPreference());
			utils.reconcileContradictionsAll();
		}

		
		// Get the last allocated offer, and continue from there
		Offer offer = behavior.allocated;
		
		int agentFave = this.utils.getAgentFavoriteFreeResourceInOffer(offer);
		int playerFave = this.utils.getPlayerFavoriteFreeResourceInOffer(offer);
		int[][] offerMat = utils.offerToMatrix(offer);
		
		
		int itemsGivenToAgent = -1;
		int itemsGivenToPlayer = -1;

		
		// Are the stacks the same
		if (agentFave == playerFave) {
			
			//Is the number of items in the stack even?
			if (offerMat[utils.freeRow][agentFave] % 2 == 0) {
				itemsGivenToAgent = itemsGivenToPlayer = offerMat[utils.freeRow][agentFave] / 2;
			}
			else {
				// Does the player own the agent a favor, or no one owns anyone a favor
				if (agentOwsAFavor) {
					// Give the bigger part in exchange for a favor
					resp.add(returnFavor());
					itemsGivenToAgent = (int)(offerMat[utils.freeRow][agentFave] / 2);
					itemsGivenToPlayer = offerMat[utils.freeRow][agentFave] - itemsGivenToAgent;

				} else {
					// Ask for the bigger part in exchange for a favor
					resp.add(askFavor());
					itemsGivenToAgent = (int)(offerMat[utils.freeRow][agentFave] / 2) + 1;
					itemsGivenToPlayer = offerMat[utils.freeRow][agentFave] - itemsGivenToAgent;
				}
				
				
				agentOwsAFavor =! agentOwsAFavor;
			}
		}
		// Stacks are different
		// Distribute the min amount of free items in each stack
		else {
			itemsGivenToAgent = itemsGivenToPlayer = Math.min(offerMat[utils.freeRow][playerFave], offerMat[utils.freeRow][playerFave]);
		}
			
		offerMat[utils.adversaryRow][playerFave] += itemsGivenToPlayer;
		offerMat[utils.freeRow][playerFave] -= itemsGivenToPlayer;
		
		offerMat[utils.myRow][agentFave] += itemsGivenToAgent;
		offerMat[utils.freeRow][agentFave] -= itemsGivenToAgent;
		
		Offer stackOffer = utils.matrixToOffer(offerMat);
		resp.add(new Event(StaticData.playerId, Event.EventClass.SEND_OFFER, stackOffer, (int) (700*game.getMultiplier())));
		stateStartSuggestedOffer = stackOffer;
			
		
		return resp;
	}
	
	private LinkedList<Event> stateEnd(Event e){
//		System.out.println("StackDivide stateEnd()");
		LinkedList<Event> resp = new LinkedList<Event>();
		
		if (e.getSubClass() == Event.SubClass.OFFER_REJECT) {
			
			resp.add(new Event(StaticData.playerId, Event.EventClass.SEND_MESSAGE, Event.SubClass.GENERIC_NEG,
					"Aww",(int) (1000 * game.getMultiplier())));

			resp.add(new Event(StaticData.playerId, Event.EventClass.SEND_EXPRESSION, "sad", 2000, (int) (100*game.getMultiplier())));	
			this.blState = BLState.FAILURE;
			
		} else {
			resp.add(new Event(StaticData.playerId, Event.EventClass.SEND_MESSAGE, Event.SubClass.GENERIC_POS,
					"Yay",(int) (1000 * game.getMultiplier())));

			resp.add(new Event(StaticData.playerId, Event.EventClass.SEND_EXPRESSION, "happy", 2000, (int) (100*game.getMultiplier())));	
			behavior.allocated = stateStartSuggestedOffer;
			this.blState = BLState.SUCCESS;

		}
		return resp;
	}

	
	
	private Event askFavor() {
		return new Event(StaticData.playerId, Event.EventClass.SEND_MESSAGE, Event.SubClass.NONE,
				"We seem to both want the same resource, but there is an odd amount of it."
				+ " Would you mind giving me the larger part now and get the larger part next time?",
				(int) (1000 * game.getMultiplier()));
	}
	
	private Event returnFavor() {
		return new Event(StaticData.playerId, Event.EventClass.SEND_MESSAGE, Event.SubClass.NONE,
				"We seem to both want the same resource, but there is an odd amount of it."
				+ " It's your turn to get the larger part, enjoy :)",
				(int) (1000 * game.getMultiplier()));
	}

}
