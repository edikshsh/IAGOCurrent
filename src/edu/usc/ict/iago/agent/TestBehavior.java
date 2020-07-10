package edu.usc.ict.iago.agent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

import edu.usc.ict.iago.utils.BehaviorPolicy;
import edu.usc.ict.iago.utils.GameSpec;
import edu.usc.ict.iago.utils.History;
import edu.usc.ict.iago.utils.Offer;

public class TestBehavior extends IAGOCoreBehavior implements BehaviorPolicy {
		
	private AgentUtilsExtension utils;
	private GameSpec game;	
	private Offer allocated;
	private LedgerBehavior lb = LedgerBehavior.NONE;
	private int adverseEvents = 0;
	private Map<String , Integer> itemPoints;
	
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");  
	public void debug(String funcName) {
		System.out.println(dtf.format(LocalDateTime.now()) + " - " + "Behavior " + funcName);
	}
	
	public void setItemPoints(Map<String , Integer> itemPoints) {
		this.itemPoints = itemPoints;
	}
	
	
	public enum LedgerBehavior
	{
		FAIR,
		LIMITED,
		BETRAYING,
		NONE;
	}
	
	public TestBehavior (LedgerBehavior lb)
	{
		super();
		this.lb = lb;
	}
		
	@Override
	protected void setUtils(AgentUtilsExtension utils)
	{
		debug("setUtils()");
		this.utils = utils;
		
		this.game = this.utils.getSpec();
		allocated = new Offer(game.getNumIssues());
		for(int i = 0; i < game.getNumIssues(); i++)
		{
			int[] init = {0, game.getIssueQuants()[i], 0};
			allocated.setItem(i, init);
		}
	}
	
	@Override
	protected void updateAllocated (Offer update)
	{
		debug("updateAllocated()");
		allocated = update;
	}
	
	@Override
	protected void updateAdverseEvents (int change)
	{
		debug("updateAdverseEvents()");
		adverseEvents = Math.max(0, adverseEvents + change);
	}
	
	
	@Override
	protected Offer getAllocated ()
	{
		debug("getAllocated()");
		return allocated;
	}
	
	@Override
	protected Offer getConceded ()
	{
		debug("getConceded()");
		return allocated;
	}
	
	@Override
	protected Offer getFinalOffer(History history)
	{
		debug("getFinalOffer()");
		Offer propose = new Offer(game.getNumIssues());
		int totalFree = 0;
		do 
		{
			totalFree = 0;
			for(int issue = 0; issue < game.getNumIssues(); issue++)
			{
				totalFree += allocated.getItem(issue)[1]; // adds up middle row of board, calculate unclaimed items
			}
			propose = getNextOffer(history);
			updateAllocated(propose);
		} while(totalFree > 0); // Continue calling getNextOffer while there are still items left unclaimed
		return propose;
	}
	

	
	public void moveResource(Offer o, int resourceNum, int from, int to) {
		int[] currResourceDistribution = o.getItem(resourceNum);
		currResourceDistribution[from] -= 1;
		currResourceDistribution[to] += 1;

		o.setItem(resourceNum, currResourceDistribution);
		
	}
	
	
	@Override
	public Offer getCounterOffer(Offer o) {
		
		Offer counterOffer = new Offer(game.getNumIssues());
		counterOffer.setOffer(o);

		int playerFavoriteResource = utils.getPlayerFavoriteFreeResourceInOffer(counterOffer);
		int agentFavoriteResource = utils.getAgentFavoriteFreeResourceInOffer(counterOffer);

		while(playerFavoriteResource != agentFavoriteResource) {
			System.out.print("Counter offer = ");
			System.out.println(counterOffer.toString());
			System.out.println("Giving player resource " + playerFavoriteResource + " and giving agent resource " + agentFavoriteResource);
			moveResource(counterOffer, playerFavoriteResource, this.utils.adversaryRow, this.utils.myRow);
			moveResource(counterOffer, agentFavoriteResource, this.utils.myRow, this.utils.adversaryRow);
			playerFavoriteResource = utils.getPlayerFavoriteFreeResourceInOffer(counterOffer);
			agentFavoriteResource = utils.getAgentFavoriteFreeResourceInOffer(counterOffer);
		}
		
		debug("getCounterOffer() done"); 
		return counterOffer;
	}
	
	public ArrayList<Integer> getAgentResourceValues() {
		var playerResourceValuesMap = game.getSimplePoints(StaticData.playerId);
		ArrayList<Integer> playerResourceValues = new ArrayList<>();
		playerResourceValuesMap.forEach((name,value) -> playerResourceValues.add(value));
		debug("getPlayerResourceValues()");
		playerResourceValuesMap.forEach((name,value) -> System.out.println("Resource " + name + " is worth " + value));
		return playerResourceValues;
	}
	
	
	
	@Override
	public Offer getNextOffer(History history) 
	{	
		debug("getNextOffer()");
		
		//start from where we currently have accepted
		Offer propose = utils.copyOffer(allocated);
		
		// Assign ordering to the player based on perceived preferences. Ideally, they would be opposite the agent's (integrative)
		ArrayList<Integer> playerPref = utils.getMinimaxOrdering(); 
		ArrayList<Integer> vhPref = utils.getMyOrdering();
		
		// Array representing the middle of the board (undecided items)
		int[] free = new int[game.getNumIssues()];
		
		for(int issue = 0; issue < game.getNumIssues(); issue++)
		{
			free[issue] = allocated.getItem(issue)[1];
		}
	
		int userFave = -1;
		int opponentFave = -1;
		
//		for (int j = 0; j < game.getNumIssues(); j++)
//		{
//			System.out.println("item " + game.getIssuePluralNames()[j] + " is worth " + this.itemPoints.get(game.getIssuePluralNames()[j]));
//		}
		
		for (int i=0; i<StaticData.agentResourceValuesHistory.size(); i++) {
			System.out.print("Game " + i + " resource values are: ");
			for (int resourceValue : StaticData.agentResourceValuesHistory.get(i)) {
				System.out.print(resourceValue + ", ");
			}
			System.out.println();
		}
		
		// Find most valued issue for player and VH (of the issues that have undeclared items)
		opponentFave = utils.getAgentFavoriteFreeResourceInOffer(allocated);
		userFave = utils.getPlayerFavoriteFreeResourceInOffer(allocated);
		
		System.out.println("opponentFave = " + opponentFave);
		System.out.println("userFave = " + userFave);

		//is there ledger to work with?
		if(lb == LedgerBehavior.NONE) //this agent doesn't care
		{
			//nothing
		}
		else if (utils.getVerbalLedger() < 0) //we have favors to cash!
		{
			//we will naively cash them immediately regardless of game importance
			//take entire category
			utils.modifyOfferLedger(-1);
			propose.setItem(opponentFave, new int[] {allocated.getItem(opponentFave)[0] + free[opponentFave], 0, allocated.getItem(opponentFave)[2]});
			return propose;	
		}
		else if (utils.getVerbalLedger() > 0) //we have favors to return!
		{
			if (lb == LedgerBehavior.BETRAYING)//this agent doesn't care
			{
				//nothing, so continue
			}
			else if(lb == LedgerBehavior.FAIR)//this agent returns an entire column!
			{
				//return entire category
				utils.modifyOfferLedger(1);
				propose.setItem(userFave, new int[] {allocated.getItem(userFave)[0], 0, allocated.getItem(userFave)[2] + free[userFave]});
				return propose;
			}
			else //if (lb == LedgerBehavior.LIMITED)//this agent returns a single item.  woo hoo
			{
				//return single item
				utils.modifyOfferLedger(1);
				propose.setItem(userFave, new int[] {allocated.getItem(userFave)[0], free[userFave] - 1, allocated.getItem(userFave)[2] + 1});
				return propose;
			}
		}
		else //we have nothing special
		{
			//nothing, so continue
		}

		

		if (userFave == -1 && opponentFave == -1) // We already have a full offer (no undecided items), try something different
		{
			//just repeat and keep allocated
		}			
		else if(userFave == opponentFave)// Both agent and player want the same issue most
		{
			if(free[userFave] >= 2) // If there are more than two of that issue, propose an offer where the VH and player each get one more of that issue
				propose.setItem(userFave, new int[] {allocated.getItem(userFave)[0] + 1, free[userFave] - 2, allocated.getItem(userFave)[2] + 1});
			else // Otherwise just give the one item left to us, the agent
			{
				if (utils.adversaryRow == 0) {
					propose.setItem(userFave, new int[] {allocated.getItem(userFave)[0], free[userFave] - 1, allocated.getItem(userFave)[2] + 1});
				} else if (utils.adversaryRow == 2) {
					propose.setItem(userFave, new int[] {allocated.getItem(userFave)[0] + 1, free[userFave] - 1, allocated.getItem(userFave)[2]});
				}
			}
		}
		else // If the agent and player have different top picks
		{
			// Give both the VH and the player one more of the item they want most
			propose.setItem(userFave, new int[] {allocated.getItem(userFave)[0], free[userFave] - 1, allocated.getItem(userFave)[2] + 1});
			propose.setItem(opponentFave, new int[] {allocated.getItem(opponentFave)[0] + 1, free[opponentFave] - 1, allocated.getItem(opponentFave)[2]});
		}
		
		return propose;
	}

	@Override
	protected Offer getTimingOffer(History history) {
		debug("getTimingOffer()");
		return null;
	}

	@Override
	protected Offer getAcceptOfferFollowup(History history) {
		debug("getAcceptOfferFollowup()");
		return null;
	}
	
	@Override
	protected Offer getFirstOffer(History history) {
		debug("getFirstOffer()");
		return null;
	}

	@Override
	protected int getAcceptMargin() {
		debug("getAcceptMargin()");
		return Math.max(0, Math.min(game.getNumIssues(), adverseEvents));//basic decaying will, starts with fair
	}

	@Override
	protected Offer getRejectOfferFollowup(History history) {
		debug("getRejectOfferFollowup()");
		return null;
	}
	

}
