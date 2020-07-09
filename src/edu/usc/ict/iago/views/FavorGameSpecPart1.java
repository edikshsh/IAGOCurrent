package edu.usc.ict.iago.views;

import java.util.HashMap;
import edu.usc.ict.iago.utils.GameSpec;

class FavorGameSpecPart1 extends GameSpec{
	
	
	
	FavorGameSpecPart1(boolean privileged) 
	{

		//VH preferences
		
		HashMap<String, Integer> simpleVHPoints = new HashMap<String, Integer>();
		HashMap<String, Integer> simplePlayerPoints = new HashMap<String, Integer>();
		
		simpleVHPoints.put(getIssuePluralNames()[0], 4);
		simpleVHPoints.put(getIssuePluralNames()[1], 3);
		simpleVHPoints.put(getIssuePluralNames()[2], 2);
		simpleVHPoints.put(getIssuePluralNames()[3], 8);
		
		simplePlayerPoints.put(getIssuePluralNames()[0], 2);
		simplePlayerPoints.put(getIssuePluralNames()[1], 3);
		simplePlayerPoints.put(getIssuePluralNames()[2], 4);
		simplePlayerPoints.put(getIssuePluralNames()[3], 1);
		
		//Privileged calls -- cannot be called outside package.
		setSimpleOpponentPoints(simpleVHPoints);
		setSimpleUserPoints(simplePlayerPoints);	
		setUserBATNA(12);
		setOpponentBATNA(12);	
		if(privileged)
			enablePrivilege();
		
		
		
		setIndexMenu(this.buildMenu());
	}

	@Override
	public int getNumIssues() {
		return 4;
	}

	@Override
	public int[] getIssueQuants() {
		return new int[] {5,5,5,5};
	}

	@Override
	public String[] getIssuePluralNames() {
		return new String[] {"lamps", "paintings", "boxes of records", "cuckoo clocks"};
	}

	@Override
	public String[] getIssueNames() {
		return new String[] {"lamp", "painting", "box of records", "cuckoo clock"};
	}

	
	@Override
	public boolean isAdvancedPoints() {
		return false;
	}
	

	@Override
	public int getTotalTime() {
		return 420;
	}
	
	@Override
	public String getTargetEmail()
	{
		return "mell@ict.usc.edu";
	}
	

	@Override
	public String getEndgameMessage() {
		return "The game has ended!  Be prepared, a new game will start with the same opponent soon!";
	}
	
	@Override
	public String getNewgameMessage() {
		return "Time need to negotiate a for some antiques!  Click \"View Payoffs\" to view these again. You now get 4 points for each box of records, "
				+ " 3 points for each painting, 2 points for each lamp, and <strong>only 1 point for each cuckoo clock.</strong>  You have an alternative deal for 12 points!";
	}

	@Override
	public boolean showOpponentScoreOnEnd() {
		return false;
	}

	@Override
	public boolean showNegotiationTimer() {
		return true;
	}

	@Override
	public String getStudyName() {
		return "FavorPart1";
	}
	
	@Override
	public String getSurvey() {
		return "favorSurvey1.jsp";
	}

}
