package com.miiitv.game.server.gui;

public interface RankListener {

	public void join(String facebookID, String facebookName, int win, int lose);
	public void matchAnswer(String fbId, int answer);
	public void selectAnswerer(String fdID);
}
