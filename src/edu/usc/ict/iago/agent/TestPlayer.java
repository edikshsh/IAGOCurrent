package edu.usc.ict.iago.agent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

import javax.websocket.Session;

import edu.usc.ict.iago.utils.Event;
import edu.usc.ict.iago.utils.GameSpec;


/**
 * @author mell
 * 
 */
public class TestPlayer extends IAGOCoreVH {

	/**
	 * @author mell
	 * Instantiates a new  VH.
	 *
	 * @param name: agent's name
	 * @param game: gamespec value
	 * @param session: the session
	 */
	
	static TestBehavior a;
	static int round = 0;
	public TestPlayer(String name, GameSpec game, Session session)
	{

		super("Test", game, session, a = new TestBehavior(TestBehavior.LedgerBehavior.FAIR), new TestExpression(), 
				new TestMessage(false, false, TestBehavior.LedgerBehavior.FAIR));	


		super.safeForMultiAgent = true;
		
	}
	
	
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");  
	public void debug(String funcName) {
		System.out.println(dtf.format(LocalDateTime.now()) + " - " + "Player " + funcName);
	}
	
	
	@Override
	public LinkedList<Event> getEventResponse(Event e){


		if (e.getType() == Event.EventClass.GAME_START) {
			onNewRoundStart();
		}

//		LinkedList<Event> resp = new LinkedList<Event>();
//		Event e0 = new Event(this.getID(), Event.EventClass.SEND_MESSAGE, Event.SubClass.NONE, newGameMessage, (int) (100*game.getMultiplier()));
//		resp.add(e0);

		debug("getEventResponse(): event = " + e.getType() + ", subtype = " + e.getSubClass() +
				", message = " + e.getMessage() + ", value = " + e.getValue() + ", preference = " + e.getPreference() + 
				", offer = " + e.getOffer());

		return super.getEventResponse(e);
	}
	
	// Called once when a new round starts
	public void onNewRoundStart() {
		round++;
		var simplePoints = game.getSimplePoints(this.getID());
		a.setItemPoints(simplePoints);
		
		
		new StaticData(game, this.getID());
		StaticData.newRound();
		
	}
	
	

	@Override
	public String getArtName() {
		return "Rens";
	}

	@Override
	public String agentDescription() {
			return "<h1>Opponent</h1><p>They are excited to begin negotiating!</p>";
	}
}