package edu.usc.ict.iago.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import edu.usc.ict.iago.utils.Event;
import edu.usc.ict.iago.utils.GameSpec;
import edu.usc.ict.iago.utils.History;
import edu.usc.ict.iago.utils.MathUtils;
import edu.usc.ict.iago.utils.Offer;
import edu.usc.ict.iago.utils.Preference;
import edu.usc.ict.iago.utils.ServletUtils;
import edu.usc.ict.iago.utils.Preference.Relation;

class AgentUtilsExtension 
{
	private IAGOCoreVH agent;
	public GameSpec game;
	private ArrayList<ArrayList<Integer>> orderings = new ArrayList<ArrayList<Integer>>();
	private int[][] permutations;
	private LinkedList<Preference> preferences = new LinkedList<Preference>();
	public int adversaryBATNA = -1;
	public int myPresentedBATNA = -1;
	public final double LIE_THRESHOLD = 0.6; 	
	protected ArrayList<Boolean>  previouslyOffered = new ArrayList<Boolean>();
	public boolean competitive = false;
	public int myRow;
	public int freeRow = 1;
	public int adversaryRow;
	private boolean isFixedPie = false;
	
	public static int[][] itemsMat;
	int realIndex=0;
	int prefIndex=1;
	int valueIndex=2;
	int quantIndex=3;
	
	enum SortOrder{
		ASCENDING,
		DESCENDING
	}
	
	public void initItemValuesMat() {
		itemsMat = new int[game.getNumIssues()][4];
		var values = game.getSimplePoints(this.agent.getID());
		var prefs = getMyOrdering();
		String[] pluralNames = game.getIssuePluralNames();
		for (int i=0; i<game.getNumIssues(); i++) {
			itemsMat[i][realIndex] = i;
			itemsMat[i][prefIndex] = prefs.get(i);
			itemsMat[i][valueIndex] = game.getSimplePoints(this.agent.getID()).get(pluralNames[i]);
			itemsMat[i][quantIndex] = game.getIssueQuants()[i];
		}
		
		sortItemsMat(realIndex, SortOrder.ASCENDING);
		sortItemsMat(valueIndex, SortOrder.DESCENDING);
	}
	
	// sorts the itemsMat in a descending order, by column
	public int[][] sortItemsMat(int col, SortOrder sortOrder)
	{
		int[] tempRow = new int[itemsMat[0].length];
		for (int i=0; i<game.getNumIssues(); i++) {
			for (int j=0; j<itemsMat[0].length; j++) {
				if (sortOrder == SortOrder.DESCENDING) {
					if (itemsMat[i][col] > itemsMat[j][col]) {
						tempRow = itemsMat[i];
						itemsMat[i] = itemsMat[j];
						itemsMat[j] = tempRow;
					}
				} else {
					if (itemsMat[i][col] < itemsMat[j][col]) {
						tempRow = itemsMat[i];
						itemsMat[i] = itemsMat[j];
						itemsMat[j] = tempRow;
					}
				}
			}
		}
		return itemsMat;
	}
	/**
	 * Constructor for the AUE.
	 * @param core The VH associated with this instance of AUE.
	 */
	AgentUtilsExtension(IAGOCoreVH core){
		this.agent = core;
		
		if (this.agent.getID() == History.USER_ID) 
		{
			myRow = 2;
			adversaryRow = 0;
		} else if (this.agent.getID() == History.OPPONENT_ID) 
		{
			myRow = 0;
			adversaryRow = 2;
		}
	}

	/**
	 * Constructor for the AUE.
	 * @param core The VH associated with this instance of AUE.
	 */
	AgentUtilsExtension(IAGOCoreVHOld core){
//		this.agent = core;
		
		if (this.agent.getID() == History.USER_ID) 
		{
			myRow = 2;
			adversaryRow = 0;
		} else if (this.agent.getID() == History.OPPONENT_ID) 
		{
			myRow = 0;
			adversaryRow = 2;
		}
	
	}
	/**
	 * Configures initial parameters for the given game.
	 * @param game the game being played.
	 */
	protected void configureGame(GameSpec game)
	{
		this.game = game;
		permutations = MathUtils.getPermutations(game.getNumIssues(), 1);//offset by 1, so we will be 1-indexed
		for (int i = 0; i < game.getNumIssues(); i++)
			previouslyOffered.add(false);
	}
	
	/**
	 * Sets the agent belief for when multiple opponent orderings are equally likely.  When true, it assumes it has the same preferences.  When false, it does not.
	 * @param fixedpie true if fixed pie belief is in effect, false otherwise (if method is not called, defaults to false)
	 */
	protected void setAgentBelief(boolean fixedpie)
	{
		isFixedPie = fixedpie;
	}
	
	/**
	 * returns the GameSpec being used in the current game.
	 * @return the GameSpec currently being played.
	 */
	protected GameSpec getSpec()
	{
		return game;
	}
	
	/**
	 * Adds the given preference to the list of preferences.
	 * @param p the preference to add
	 */
	protected void addPref (Preference p)
	{
		preferences.add(p);
	}
	
	/**
	 * Removes the 0th element in the preferences queue.
	 * @return the preference removed, or throws IndexOutOfBoundException 
	 */
	protected Preference dequeuePref()
	{
		return preferences.remove(0);
	}
	
	/**
	 * Returns the value of an offer with respect to the caller. 
	 * @param o the offer
	 * @return the total value (how many points the agent will get)
	 */
	protected int myActualOfferValue(Offer o) 
	{
		int ans = 0;
		for (int num = 0; num < game.getNumIssues(); num++)
			ans += o.getItem(num)[myRow] * game.getSimplePoints(agent.getID()).get(game.getIssuePluralNames()[num]);
		return ans;
	}
	
	/**
	 * Returns the value of an offer lost to adversary. 
	 * @param o the offer
	 * @return the total value (how many points the agent will lose)
	 */
	protected int pointsLostInOffer(Offer o) 
	{
		int ans = 0;
		for (int num = 0; num < game.getNumIssues(); num++)
			ans += o.getItem(num)[adversaryRow] * game.getSimplePoints(agent.getID()).get(game.getIssuePluralNames()[num]);
		return ans;
	}
	
	/**
	 * Returns the VH value of an ordering (of preferences?).
	 * @param o the ordering
	 * @return the total value
	 */
	protected int myActualOrderValue(ArrayList<Integer> o) 
	{
		int ans = 0;
		for (int num = 0; num < game.getNumIssues(); num++)
			ans += o.get(num) * game.getSimplePoints(agent.getID()).get(game.getIssuePluralNames()[num]);
		return ans;
	}
	
	/**
	 * Check to see if the offer is a full offer.
	 * @param o the offer
	 * @return is full offer
	 */
	protected boolean isFullOffer(Offer o)
	{
		boolean ans = true;
		for (int num = 0; num < game.getNumIssues(); num++)
			if(o.getItem(num)[1] > 0)
				ans = false;
		return ans;
	}

	/**
	 * Returns the normalized ordering of VH preferences (e.g., a point value of {3, 7, 2} would return {2, 1, 3}), with 1 being the highest
	 * @return an ArrayList of preferences
	 */
	protected ArrayList<Integer> getMyOrdering() 
	{
		int rating = 1;
		ArrayList<Integer> ans = new ArrayList<Integer>(game.getNumIssues());
		ArrayList<Integer> sortedIndices = new ArrayList<Integer>(game.getNumIssues());
		for(int init = 0; init < game.getNumIssues(); init++)
			ans.add(0);
		
		for (int i = 0; i < game.getNumIssues(); i++)
		{
			int max = 0;
			int value = 0;
			int index = 0;
			for (int j = 0; j < game.getNumIssues(); j++)
			{
				value = game.getSimplePoints(agent.getID()).get(game.getIssuePluralNames()[j]);
				if (value > max && !sortedIndices.contains(j))
				{
					max = value;
					index = j;
				}
			}
			sortedIndices.add(index);
			ans.set(index, rating);
			rating++;
		}
		return ans;
	}
	
	/**
	 * Returns the expected value for this agent's adversary on an offer for a given ordering of preferences.
	 * @param o the offer
	 * @param ordering the ordering
	 * @return the total value
	 */
	protected int adversaryValue(Offer o, ArrayList<Integer> ordering) 
	{
		int ans = 0;
		for (int num = 0; num < game.getNumIssues(); num++)
			ans += o.getItem(num)[adversaryRow] * (game.getNumIssues() - ordering.get(num) + 1);
		return ans;
	}
	
	/**
	 * Returns the maximum possible value for an adversary on an offer for all current orderings of preferences.
	 * @param o the offer
	 * @return the total value
	 */
	protected int adversaryValueMax(Offer o)
	{
		int max = 0;
		for (ArrayList<Integer> order : orderings)
			max = Math.max(max,  adversaryValue(o, order));
		return max;
	}
	
	/**
	 * Returns the minimum possible value for an adversary on an offer for all current orderings of preferences.
	 * @param o the offer
	 * @return the total value
	 */
	protected int adversaryValueMin(Offer o)
	{
		int min = 0;
		for (ArrayList<Integer> order : orderings)
			min = Math.min(min,  adversaryValue(o, order));
		return min;
	}
	
	/***
	 * Finds the adversary's highest ranked item in the most ideal ordering
	 * @return index of the best item (for opposing agent)
	 */
	protected int findAdversaryIdealBest()
	{
		ArrayList<Integer> order = getMinimaxOrdering();
		for (int i = 0; i < order.size(); i++)
		{
			if(order.get(i) == 1)
				return i;
		}
		return -1;
	}

	/***
	 * Finds the adversary's second highest ranked item in the most ideal ordering
	 * @return index of the second best item (for opposing agent)
	 */
	protected int findAdversaryIdealSecondBest()
	{
		ArrayList<Integer> order = getMinimaxOrdering();
		for (int i = 0; i < order.size(); i++)
		{
			if(order.get(i) == 2)
				return i;
		}
		return -1;
	}

	/***
	 * Finds the adversary's lowest ranked item in the most ideal ordering
	 * @return index of the worst item (for opposing agent)
	 */
	protected int findAdversaryIdealWorst(GameSpec game)
	{
		ArrayList<Integer> order = getMinimaxOrdering();
		for (int i = 0; i < order.size(); i++)
		{
			if(order.get(i) == game.getNumIssues())
				return i;
		}
		return -1;
	}
	
	/**
	 * Returns the last event of type type, or null if nothing found.
	 * @param history the history to search
	 * @param type the type of EventClass to search for
	 * @return the event found, or null
	 */
	protected Event lastEvent(LinkedList<Event> history, Event.EventClass type)
	{
		for (int i = history.size() - 1; i > 0; i--)
		{
			if(history.get(i).getType() == type)
				return history.get(i);
		}
		return null;
	}
	
	/**
	 * Returns the last event of type type that was received, or null if nothing found.
	 * @param history the history to search
	 * @param type the type of EventClass to search for
	 * @return the event found, or null
	 */
	protected Event lastEventReceived(LinkedList<Event> history, Event.EventClass type)
	{
		for (int i = history.size() - 1; i > 0; i--)
		{
			if(history.get(i).getType() == type && history.get(i).getOwner() != agent.getID()) 
			{
				return history.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Returns the second to last event of type type, or null if nothing found.
	 * @param history the history to search
	 * @param type the type of EventClass to search for
	 * @return the event found, or null
	 */
	protected Event secondLastEvent(LinkedList<Event> history, Event.EventClass type)
	{
		boolean foundFirst = false;
		for (int i = history.size() - 1; i > 0; i--)
		{
			if(history.get(i).getType() == type && !foundFirst)
			{
				foundFirst = true;
				continue;
			}
			else if (history.get(i).getType() == type && foundFirst)
				return history.get(i);
		}
		return null;
	}
	
	/**
	 * Eliminates all invalid orderings by looking at preferences, the oldest ones first.
	 */
	public void reconcileContradictionsAll() {
		if(reconcileContradictions())
		{
			//we simply drop the oldest expressed preference until we are reconciled.  This is not the best method, as it may not be the the most efficient route.
			int overflowCount = 0;
			while(reconcileContradictions() && overflowCount < 5)
			{
				dequeuePref();
				overflowCount++;
			}
		}
	}
	
	/**
	 * Eliminates invalid orderings by looking at preferences, the oldest ones first.
	 * @return true if there are no valid orderings, false otherwise
	 */
	protected boolean reconcileContradictions()
	{
		orderings = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < permutations.length; i++)
		{
			ArrayList<Integer> a = new ArrayList<Integer>();
			for (int j = 0; j < permutations[i].length; j++)
			{
				a.add(permutations[i][j]);
			}
			orderings.add(a); 
		}
		
		for (Preference pref: preferences)
		{
			//ServletUtils.log(pref.toString(), ServletUtils.DebugLevels.DEBUG);
			Relation r = pref.getRelation();
			ArrayList<ArrayList<Integer>> toRemove = new ArrayList<ArrayList<Integer>>();
			if(r == Relation.BEST)
			{
				//kludge when vague information is supplied
				if (pref.getIssue1() == -1)//if information not filled in
					continue;
				for (int x = 0; x < orderings.size(); x++)
				{
					ArrayList<Integer> o = orderings.get(x);
					if(o.get(pref.getIssue1()) != 1)//if the ordering does not have the item as number 1 
						toRemove.add(o);
				}
			}
			else if(r == Relation.WORST)
			{
				//kludge when vague information is supplied
				if (pref.getIssue1() == -1)//if information not filled in
					continue;
				for (int x = 0; x < orderings.size(); x++)
				{
					ArrayList<Integer> o = orderings.get(x);
					if(o.get(pref.getIssue1()) != game.getNumIssues())//if the ordering does not have the item as the last place 
						toRemove.add(o);
				}
			}
			else if(r == Relation.GREATER_THAN)
			{
				//kludge when vague information is supplied
				if (pref.getIssue1() == -1 || pref.getIssue2() == -1)//if information not filled in
					continue;
				for (int x = 0; x < orderings.size(); x++)
				{
					ArrayList<Integer> o = orderings.get(x);
					if(o.get(pref.getIssue1()) > o.get(pref.getIssue2()))//if the ordering does not have the item greater
						toRemove.add(o);
				}
			}
			else if(r == Relation.LESS_THAN)
			{
				//kludge when vague information is supplied
				if (pref.getIssue1() == -1 || pref.getIssue2() == -1)//if information not filled in
					continue;
				for (int x = 0; x < orderings.size(); x++)
				{
					ArrayList<Integer> o = orderings.get(x);
					if(o.get(pref.getIssue1()) < o.get(pref.getIssue2()))//if the ordering does not have the item lesser 
						toRemove.add(o);
				}
			}
			else if(r == Relation.EQUAL)
			{
				//kludge when vague information is supplied
				if (pref.getIssue1() == -1 || pref.getIssue2() == -1)//if information not filled in
					continue;
				for (int x = 0; x < orderings.size(); x++)
				{
					ArrayList<Integer> o = orderings.get(x);
					if(Math.abs(o.get(pref.getIssue1()) - o.get(pref.getIssue2())) == 1)//if the ordering does not have the items adjacent 
						toRemove.add(o);
				}
			}
			
			for(ArrayList<Integer> al : toRemove)
				orderings.remove(al);
			
			//ServletUtils.log(orderings.toString(), ServletUtils.DebugLevels.DEBUG);
		}
		//ServletUtils.log(orderings.toString(), ServletUtils.DebugLevels.DEBUG);
		
		if(orderings.size() == 0)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Finds the BATNA of an agent, takes into consideration whether it's lying or not
	 * @param game
	 * @param lieThreshold the gauge of how much an agent exaggerates its BATNA
	 * @param liar whether or not the agent actually wants to lie 
	 * @return the agent's BATNA, either true or a lie.
	 */
	public int getLyingBATNA(GameSpec game, double lieThreshold, boolean liar)
	{	
		if (liar)
		{
			int totalPoints = getMaxPossiblePoints();
			return (int) (lieThreshold*totalPoints); 
		}
		return game.getBATNA(agent.getID());
	}
		
	/**
	 * Finds the ordering among possible orderings that is most/least different than the VH's ordering, based on the value of isFixedPie (set by separate method).
	 * @return the chosen ordering.
	 */
	protected ArrayList<Integer> getMinimaxOrdering()
	{
		boolean reversed = isFixedPie;
		int valueHeuristic = 0;
		int minimax = reversed ? Integer.MAX_VALUE : 0;
		ArrayList<Integer> ans = null;
		// Just in case this hasn't been run yet
		reconcileContradictions();
		for(ArrayList<Integer> order: orderings) // Goes through each order in orderings
		{
			valueHeuristic = 0;
			for (int i = 0; i < order.size(); i++) 
			{
				valueHeuristic += Math.abs(order.get(i) - getMyOrdering().get(i)); // Sum up the difference between agent ranking and player ranking of each issue 
			}
			if(reversed)
			{
				if (valueHeuristic < minimax) // The largest sum means the bigger difference between the orderings and thus the most integrative scenario
				{
					minimax = valueHeuristic;
					ans = order;
				}
			}
			else
			{
				if(valueHeuristic > minimax)
				{
					minimax = valueHeuristic;
					ans = order;
				}
			}
		}
		return ans;
	}
	
	/***
	 * Finds the maximum amount of total points to be had in a certain game (i.e., agent's score if they received every item).
	 * @return max possible points
	 */
	public int getMaxPossiblePoints() 
	{
		int[] quantities = game.getIssueQuants();
		int totalPoints = 0;
		for (int index = 0; index < game.getNumIssues(); index++)
		{
			String s = game.getIssuePluralNames()[index];
			totalPoints += quantities[index]*game.getSimplePoints(agent.getID()).get(s);
		}
		return totalPoints;
	}
	public int getPointsOfAllocatedItems(Offer o) {
		int maxValue = getMaxPossiblePoints();
		int[] quantities = game.getIssueQuants();
		for(int i = 0 ; i< game.getNumIssues(); i++) {
			String s = game.getIssuePluralNames()[i];
			maxValue -= o.getItem(i)[freeRow]*game.getSimplePoints(agent.getID()).get(s);
		}
		return maxValue;
	}
	/**
	 * determines if a pair of given BATNA's are in conflict, i.e. an offer doesn't exist to satisfy both BATNAs.
	 * @param agentBATNA		can be either the true BATNA, or the presented BATNA, depending on the circumstances
	 * @param adversaryBATNA	this should always be the stored value of the opponent's BATNA.
	 * @return true if there is a conflict, false otherwise.
	 */
	public boolean conflictBATNA(int agentBATNA, int adversaryBATNA) 
	{
		return agentBATNA + adversaryBATNA > getMaxPossiblePoints();
	}
	
	/**
	 * determines if a pair of given BATNA's are in conflict, i.e. an offer doesn't exist to satisfy both BATNAs.
	 * If the BATNAs conflict, it returns a decreased value for the passed in BATNA.
	 * @param vhPresentedBATNA			can be either the true BATNA, or the presented BATNA, depending on the circumstances
	 * @return an updated BATNA for the VH, possibly lower, if the opponent and VH BATNAs conflict. Always at least the actual VH BATNA.
	 */
	public int lowerBATNA(int vhPresentedBATNA) 
	{
		if (conflictBATNA(vhPresentedBATNA, adversaryBATNA))
		{
			// If agent is competitive but they are not getting more than 60% of the joint value
			if (vhPresentedBATNA / (vhPresentedBATNA + adversaryBATNA) < 0.6 && competitive)
			{
				int curBATNA = vhPresentedBATNA;
				double newBATNA =  Math.max(game.getBATNA(agent.getID()), curBATNA - game.getBATNA(agent.getID())); 
				// Return the max of their actual BATNA and their current BATNA decremented by their actual BATNA 
				return (int) newBATNA;
			}
			// Otherwise, return the max of the agent's actual BATNA and the max number of points they could possibly get if
			// they awarded the user their BATNA.
			return Math.max(getMaxPossiblePoints() - adversaryBATNA, game.getBATNA(agent.getID()));
		}
		else
			return vhPresentedBATNA;
	}

	/***
	 * Helper to easily return expected/predicted value of an offer for an agent's adversary
	 * @param o an offer to look at
	 * @return the predicted value of the offer to the adversary.
	 */
	protected int getAdversaryValue(Offer o) 
	{
		ServletUtils.log("Minimax ordering: " + getMinimaxOrdering() , ServletUtils.DebugLevels.DEBUG);
		return adversaryValue(o, getMinimaxOrdering());
	}

	/***
	 * Getter for the agent's ID (0 for user, 1 for computer agent)
	 * @return current agent's ID 
	 */
	public int getID() 
	{
		return agent.getID();
	}
	
	/***
	 * Finds the position on the board (1 through num issues) of an item with a particular ranking for current agent
	 * @param game
	 * @param num the ranking of the item within an ordering (i.e., 1 means best, 2 means second best, etc.)
	 * @return the index of the item
	 */
	int findMyItemIndex(GameSpec game, int num) 
	{
		ArrayList<Integer> order = getMyOrdering();
		for (int i = 0; i < order.size(); i++) 
		{
			if (order.get(i) == num) 
			{
				return i;
			}
		}
		return -1;
	}
	
	/***
	 * Finds the position on the board (1 through num issues) of an item with a particular ranking for the agent's adversary.
	 * @param game
	 * @param num the ranking of the item within an ordering (i.e., 1 means best, 2 means second best, etc.)
	 * @return the index of the item
	 */
	int findAdversaryItemIndex(GameSpec game, int num) 
	{
		for (ArrayList<Integer> order: orderings)
		{
			for (int i = 0; i < order.size(); i++)
			{
				if(order.get(i) == num)
					return i;
			}
		}
		return -1;
	}
	
	/**
	 * Finds the name of an item that's ranked a certain way by the agent.
	 * @param order The rank of the item.
	 * @param game The GameSpec being used.
	 * @return the name of the item the agent ranks according to the given order.
	 */
	String findMyItem(int order, GameSpec game)
	{
		if(order <= 0 || order > game.getNumIssues())
			throw new IndexOutOfBoundsException("Index out bounds on VH Preference!");
		Map<String, Integer> pref =  MathUtils.sortByValue(game.getSimplePoints(this.getID()));
		int count = 0;
		for (Map.Entry<String, Integer> s: pref.entrySet())
		{
			count++;
			if(count == order)
				return s.getKey();
		}
		return null;
	}
	
	/***
	 * Finds a random, accurate preference
	 * @return random preference
	 */
	Preference randomPref() 
	{
		int issue1 = (int)( Math.random()* (getSpec().getNumIssues()));
		int issue2 = (int)( Math.random()* (getSpec().getNumIssues()));

		if (issue1 == issue2)
			issue2 = (issue2 + 1) % getSpec().getNumIssues();
		return getSpec().getPreference(issue1, issue2, this.getID());
	}
	
	/**
	 * Determines if there is a "particularly" valuable item this time around.
	 * @return a yes or no answer to that question
	 */
	protected boolean isImportantGame()
	{
		
		double RATIO = 1.5;
		double calcRatio = ((double)game.getSimpleOpponentPoints().get(this.findMyItem(1, game)))/
				((double)game.getSimpleOpponentPoints().get(this.findMyItem(2, game)));
		ServletUtils.log("Calculated value ratio was: " + calcRatio, ServletUtils.DebugLevels.DEBUG);
		
		return calcRatio > RATIO;
	}
	
	/**
	 * Returns a simple int representing the internal "ledger" of favors done for the agent.  Can be negative.  Persists across games.
	 * @return the ledger
	 */
	protected int getLedger()
	{
		return this.agent.getLedger();
	}
	
	/**
	 * Returns a simple int representing the internal "ledger" of favors done for the agent, including all pending values.  Can be negative.  Does not persist across games.
	 * @return the ledger
	 */
	protected int getTotalLedger()
	{
		return this.agent.getTotalLedger();
	}
	
	/**
	 * Returns a simple int representing the potential "ledger" of favors verbally agreed to.  Can be negative.  Does not persist across games.
	 * @return the ledger
	 */
	protected int getVerbalLedger()
	{
		return this.agent.getVerbalLedger();
	}
	
	/**
	 * Allows you to modify the agent's internal "ledger" of favors done for it.  
	 * @param increment value (negative ok)
	 */
	protected void modifyVerbalLedger(int increment)
	{
		this.agent.modifyVerbalLedger(increment);
	}
	
	/**
	 * Allows you to modify the agent's internal "ledger" of favors done for it.  
	 * @param increment value (negative ok)
	 */
	protected void modifyOfferLedger(int increment)
	{
		this.agent.modifyOfferLedger(increment);
	}


	
	// Added functions:
	/**
	 * return a deep copy of an offer
	 * @param the offer to make a deep copy of
	 */
	protected Offer copyOffer(Offer offer)
	{
		Offer newOffer = new Offer(game.getNumIssues());
		newOffer.setOffer(offer);
		return newOffer;
	}
	
	
	/**
	 * Get the resources allocated to any of the players or those that are free
	 * @offer the offer from which to read the resources
	 * @allocatedTo the allocation (allocated to agent, player or free), 
	 * myRow = allocated to agent
	 * freeRow = not allocated
	 * adversaryRow  allocated to player
	 */
	protected int[] getResourcesFromOffer(Offer offer, int allocatedTo)
	{
		int[] resources = new int[game.getNumIssues()];
		
		for(int issue = 0; issue < game.getNumIssues(); issue++)
		{
			resources[issue] = offer.getItem(issue)[allocatedTo];
		}
		return resources;
	}
	
	public int getPlayerFavoriteFreeResourceInOffer(Offer o)
	{
		int[] freeResources = this.getResourcesFromOffer(o,this.freeRow);
		int favoriteResource = -1;
		var playerPref = this.getMinimaxOrdering(); 
		
		int max = game.getNumIssues() + 1;
		for(int i  = 0; i < game.getNumIssues(); i++) {
//			System.out.println("Issue number " + i + " for the player is number " + playerPref.get(i));
			if(freeResources[i] > 0 && playerPref.get(i) < max)
			{
				favoriteResource = i;
				max = playerPref.get(i);
			}
		}
		
		return favoriteResource;
	}

	public int getAgentFavoriteFreeResourceInOffer(Offer o)
	{
		int[] freeResources = this.getResourcesFromOffer(o,this.freeRow);
		int favoriteResource = -1;
		ArrayList<Integer> agentPref = this.getMyOrdering(); 
		
		int max = game.getNumIssues() + 1;
		for(int i  = 0; i < game.getNumIssues(); i++) {
//			System.out.println("Issue number " + i + " for the agent is number " + agentPref.get(i));
		
			if(freeResources[i] > 0 && agentPref.get(i) < max)
			{
				favoriteResource = i;
				max = agentPref.get(i);
			}
		
		}
		return favoriteResource;
	}
	
	public int[][] offerToMatrix(Offer o){
		int[][] mat = new int[3][game.getNumIssues()];
		for(int i  = 0; i < game.getNumIssues(); i++) {
			int[] item = o.getItem(i);
			for (int j=0; j<3; j++) {
				mat[j][i] = item[j];
			}
		}
		
		return mat;
	}
	
	public Offer matrixToOffer(int[][] offerMatrix) {
		Offer offer = new Offer(game.getNumIssues());
		for(int i=0; i < game.getNumIssues(); i++) {
			offer.setItem(i, new int[] {offerMatrix[0][i], offerMatrix[1][i], offerMatrix[2][i]});
		}
		return offer;
		
	}
	
	// Exchanges 1 item of the best resource for the player and agent
	// The agent gives the player 1 item of the best resource it has for the player, and the player does the same
	public Offer exchangeBestResources(Offer o) {
		int[][] offerMat = offerToMatrix(o);
		ArrayList<Integer> agentResourcePreferences = getMinimaxOrdering(); 
		ArrayList<Integer> playerResourcePreferences = getMyOrdering();
		

		
		int[] resourceImportance = new int[game.getNumIssues()];
		
		int max=-9;
		int min=9;
		int maxResource = -1;
		int minResource = -1;
		
		for (int i=0; i<game.getNumIssues(); i++) {
			resourceImportance[i] = playerResourcePreferences.get(i) - agentResourcePreferences.get(i);
			if (resourceImportance[i] > max && offerMat[adversaryRow][i] > 0) {
				max = resourceImportance[i];
				maxResource = offerMat[adversaryRow][i];
			}
		    if (resourceImportance[i] < min && offerMat[myRow][i] > 0) {
				min = resourceImportance[i];
				minResource = offerMat[myRow][i];
			}
		}
		
		offerMat[adversaryRow][minResource] += 1;
		offerMat[myRow][minResource] -= 1;

		offerMat[adversaryRow][maxResource] -= 1;
		offerMat[myRow][maxResource] += 1;
		
		return matrixToOffer(offerMat);
		
	}
	
	
	public boolean isOfferGood(Offer lastOffer, Offer o) {

		Offer allocated = lastOffer;//what we've already agreed on
//		Offer conceded = behavior.getConceded();//what the agent has agreed on internally
		
//		int playerDiff = (utils.adversaryValue(o, utils.getMinimaxOrdering()) - utils.adversaryValue(allocated, utils.getMinimaxOrdering()));
		int newOfferValue = myActualOfferValue(o);
		int oldOfferValue = myActualOfferValue(allocated);
		int totalResourceValueThisRound = getMaxPossiblePoints();
		int newOfferValueLost = pointsLostInOffer(o);
		int oldOfferValueLost = pointsLostInOffer(allocated);
		
		int minValue = getPointsOfAllocatedItems(allocated)/2;

		
//		var simplePoints = game.getSimplePoints(StaticData.playerId);
//		ArrayList<Integer> agentResourceValues = new ArrayList<>();
//		simplePoints.forEach((name,value) -> agentResourceValues.add(value));
//		Collections.sort(agentResourceValues);
//		Collections.reverse(agentResourceValues);
//		int agentBestResourceStackValue = agentResourceValues.get(0) * game.getIssueQuants()[];
		
		itemsMat = sortItemsMat(valueIndex, SortOrder.DESCENDING);
		int minIssues = itemsMat.length/2;
		int prefValue = 0;
		for(int i = 0; i< minIssues; i++) {
			prefValue +=  (o.getItem(itemsMat[i][realIndex])[myRow]+ o.getItem(itemsMat[i][realIndex])[adversaryRow]) * itemsMat[i][valueIndex];
		}
		prefValue = prefValue > minValue? prefValue: minValue;
		if(newOfferValue < prefValue) {
			return false;
		} else {
			if(newOfferValue < oldOfferValue) {
				return false;
			} else {
				return true;
			}
		}
//		float oldGainRatio = (oldOfferValue == 0 ? (float)0.5: oldOfferValue)  /(oldOfferValueLost == 0 ? (float)0.5: oldOfferValueLost);
//		float newGainRatio = (newOfferValue == 0 ? (float)0.5: newOfferValue)  /(newOfferValueLost == 0 ? (float)0.5: newOfferValueLost);
//		
//		System.out.println("Agent can receive a maximum of " + totalResourceValueThisRound + " points this round");
//		System.out.println("Last offer, agent received " + oldOfferValue + " points, and lost " + oldOfferValueLost + " points to adversary");
//		System.out.println("This offer, agent received " + newOfferValue + " points, and lost " + newOfferValueLost + " points to adversary");
//		System.out.println("Last offer gain ratio = " + oldGainRatio + ", new offer gain ratio = " + newGainRatio);
//
//		boolean isGainBetter = newGainRatio > oldGainRatio;
//		boolean isBetterThanBATNA = true;
//		if (isFullOffer(o)) {
//			isBetterThanBATNA = newOfferValue > myPresentedBATNA;
//		}
//		
//		boolean isOfferGood = isGainBetter && isBetterThanBATNA;
//		System.out.println("Offer is " + (isOfferGood ? "good" : "bad"));
//		return isOfferGood;
	}
	

}



