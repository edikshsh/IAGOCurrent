package edu.usc.ict.iago.agent;

import java.util.ArrayList;

import edu.usc.ict.iago.utils.Preference;
import edu.usc.ict.iago.utils.Preference.Relation;

public class PreferenceHelper {

	public static boolean missingInfo(Preference pref) {
		Relation r = pref.getRelation();
		ArrayList<ArrayList<Integer>> toRemove = new ArrayList<ArrayList<Integer>>();
		
		switch (r) {
		case BEST:
			return pref.getIssue1() == -1;
		case WORST:
			return pref.getIssue1() == -1;
		case GREATER_THAN:
			return pref.getIssue1() == -1 || pref.getIssue2() == -1;
		case LESS_THAN:
			return pref.getIssue1() == -1 || pref.getIssue2() == -1;
		case EQUAL:
			return pref.getIssue1() == -1 || pref.getIssue2() == -1;
		}
		return true;
	
	}
}
