package edu.usc.ict.iago.agent;

import edu.usc.ict.iago.utils.Event;
import edu.usc.ict.iago.utils.GameSpec;
import edu.usc.ict.iago.utils.Offer;

public class EventHelper {

	public static GameSpec game;
	
	public static enum Expression{
		HAPPY,
		NEUTRAL,
		SAD,
		ANGRY,
		SURPRISED;
	}
	
	public static Event expression(Expression expression) {
		String expressionString = expression.toString().toLowerCase();
		return new Event(StaticData.playerId, Event.EventClass.SEND_EXPRESSION, expressionString, 2000, (int) (100*game.getMultiplier()));	
	}
	
	public static Event message(String message) {
		return new Event(StaticData.playerId, Event.EventClass.SEND_MESSAGE, Event.SubClass.NONE,message,
				(int) ((700 +  message.length() * 20)* game.getMultiplier()));
	}
	
	public static Event offer(Offer offer) {
		return new Event(StaticData.playerId, Event.EventClass.SEND_OFFER, offer, (int) (2000*game.getMultiplier()));
	}

}
