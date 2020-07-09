package edu.usc.ict.iago.agent;

import java.util.ArrayList;
import java.util.Collections;

import edu.usc.ict.iago.utils.GameSpec;

public class StaticData {
	
	static GameSpec game;
	static int playerId;
	
	static ArrayList<ArrayList<Integer>> agentResourceValuesHistory; // a list of games, which is by itself a list of resource values, sorted desc
	// agentResourceValuesHistory.get(gameNum).get(0) for best resource
	public StaticData(GameSpec game, int playerId) {
		StaticData.game = game;
		StaticData.playerId = playerId;
	}
	
	
	public static void newRound() {
		var simplePoints = game.getSimplePoints(playerId);
		ArrayList<Integer> agentResourceValues = new ArrayList<>();
		simplePoints.forEach((name,value) -> agentResourceValues.add(value));
		Collections.sort(agentResourceValues);
		Collections.reverse(agentResourceValues);

		if (agentResourceValuesHistory == null) 
			agentResourceValuesHistory = new ArrayList<ArrayList<Integer>>();
		agentResourceValuesHistory.add(agentResourceValues);
	}
	

}
