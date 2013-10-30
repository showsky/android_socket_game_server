package com.miiicasa.game.bean;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Player implements Serializable {
	private final static long	serialVersionUID	= 1L;
	private final static String	FACEBOOK_ID			= "facebook_id";
	private final static String	FACEBOOK_NAME		= "facebook_name";
	private final static String	FACEBOOK_AVATAR		= "facebook_avatar";
	private final static String	WIN					= "win";
	private final static String	LOSE				= "lose";
	private final static String	SCORE				= "score";
	private final static String	RANK				= "rank";
	public String				facebookID			= null;
	public String				facebookName		= null;
	public String				facebookAvatar		= null;
	public int					win					= 0;
	public int					lose				= 0;
	public int					score				= 0;
	public int					rank				= 0;

	public Player() {
	}

	public String toJSONString() {
		JSONObject playerObject = null;
		try {
			playerObject = new JSONObject();
			playerObject.put(FACEBOOK_ID, facebookID);
			playerObject.put(FACEBOOK_NAME, facebookName);
			playerObject.put(FACEBOOK_AVATAR, facebookAvatar);
			playerObject.put(WIN, win);
			playerObject.put(LOSE, lose);
			playerObject.put(SCORE, score);
			playerObject.put(RANK, rank);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return playerObject.toString();
	}

	@Override
	public String toString() {
		return toJSONString();
	}
}
