package com.miiitv.game.server.gui;

public interface RankListener {

	public void join(String facebookID, String facebookName, String win, String lose);
	public void matchAnswer(String fbId, String questionId, String answer);

}
