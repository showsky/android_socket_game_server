package com.miiitv.game.server;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.miiicasa.game.bean.Player;
import com.miiitv.game.network.Network;
import com.miiitv.game.network.NetworkException;
import com.miiitv.game.network.NetworkException.TYPE;
import com.miiitv.game.server.config.Config;

public class Api {

	private final static String	TAG					= "Api";
	private final static String	STATUS				= "status";
	private final static String	URL					= "url";
	private final static String	DATA				= "data";
	private final static String	OK					= "ok";
	private final static String	FAIL				= "fail";
	private final static String	QUESTION_ID			= "question_id";
	private final static String	USERS				= "users";
	private final static String	API					= Config.API_URL + "/api/";
	private final static String	API_GET_QUESTION	= API + "get_question";
	private final static String	API_SYNC_RANK		= API + "sync_rank";
	private final static String	FB_GRAPH			= "http://graph.facebook.com/%s/picture?type=large&redirect=false";
	private static Api			instance			= null;

	private Api() {
	}

	public static Api getInstance() {
		if (instance == null)
			instance = new Api();
		return instance;
	}

	public JSONObject getQuestion() {
		JSONObject result = null;
		try {
			String response = Network.getInstance().post(API_GET_QUESTION, new ArrayList<NameValuePair>());
			result = verifyStatus(response).getJSONObject(DATA);
			return result;
		} catch (NetworkException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	public String getAvatar(String facebookID) {
		String result = null;
		JSONObject object;
		try {
			String response = Network.getInstance().get(String.format(FB_GRAPH, facebookID), null);
			if (response == null) {
				throw new NetworkException(TYPE.NETWORK_ERROR);
			}
			object = new JSONObject(response);
			if (object.has(DATA)) {
				result = object.getJSONObject(DATA).getString(URL);
			}

		} catch (NetworkException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean syncRank(Collection<Player> players, String questionID) {
		boolean result = false;
		ArrayList<NameValuePair> values;
		JSONArray array = new JSONArray(players);
		values = new ArrayList<NameValuePair>(2);
		values.add(new BasicNameValuePair(QUESTION_ID, questionID));
		values.add(new BasicNameValuePair(USERS, array.toString()));
		String response;
		try {
			response = Network.getInstance().post(API_SYNC_RANK, values);
			verifyStatus(response);
			result = true;
		} catch (NetworkException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	private JSONObject verifyStatus(String response) throws NetworkException, JSONException {
		Logger.d(TAG, "Response: ", response);
		JSONObject json = null;
		if (response == null) {
			throw new NetworkException(TYPE.NETWORK_ERROR);
		} else {
			json = new JSONObject(response);
			String status = json.getString(STATUS);
			if (!OK.equals(status))
				throw new NetworkException(TYPE.API_FAIL);
		}
		return json;
	}
}
