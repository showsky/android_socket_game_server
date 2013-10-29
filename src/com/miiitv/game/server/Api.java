package com.miiitv.game.server;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.miiicasa.game.bean.Account;
import com.miiicasa.game.bean.Account.Rank;
import com.miiitv.game.network.Network;
import com.miiitv.game.network.NetworkException;
import com.miiitv.game.network.NetworkException.TYPE;

public class Api {

	private final static String	TAG					= "Api";
	private final static String	STATUS				= "status";
	private final static String	OK					= "ok";
	private final static String	DATA				= "data";
	private final static String	FAIL				= "fail";
	private final static String	API					= "/api/";
	private final static String	API_GET_QUESTION	= API + "get_question";
	private final static String	API_SYNC_USER		= API + "sync_user";
	private final static String	API_BANK			= API + "get_rank";
	private final static String	FB_GRAPH			= "https://graph.facebook.com/%s/picture??type=large&redirect=false";
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
			result = verifyStatus(response).getJSONObject("data");
			return result;
		} catch (NetworkException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean syncUser(String facebookID, String facebookToken) {
		boolean flag = false;
		ArrayList<NameValuePair> values = new ArrayList<NameValuePair>(2);
		values.add(new BasicNameValuePair("facebook_id", facebookID));
		values.add(new BasicNameValuePair("facebook_token", facebookToken));
		try {
			String response = Network.getInstance().post(API_SYNC_USER, values);
			verifyStatus(response);
			flag = true;
		} catch (NetworkException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return flag;
	}

	public Rank getRank(String facebookID) {
		Rank rank = null;
		ArrayList<NameValuePair> values = new ArrayList<NameValuePair>(1);
		values.add(new BasicNameValuePair("facebook_id", facebookID));
		try {
			String response = Network.getInstance().post(API_BANK, values);
			verifyStatus(response);
			JSONObject json = new JSONObject(response);
			JSONObject jsonData = json.getJSONObject(DATA);
			rank = new Account.Rank();
			rank.win = (jsonData.isNull(Account.WIN) ? 0 : jsonData.getInt(Account.WIN));
			rank.lost = (jsonData.isNull(Account.LOSE)) ? 0 : jsonData.getInt(Account.LOSE);
			rank.score = (jsonData.isNull(Account.SCORE)) ? 0 : jsonData.getInt(Account.SCORE);
		} catch (NetworkException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return rank;
	}

	public String getAvatar(String facebookID) {
		String result = null;
		JSONObject object;
		ArrayList<NameValuePair> values = new ArrayList<NameValuePair>(1);
		values.add(new BasicNameValuePair("facebook_id", facebookID));
		try {
			String response = Network.getInstance().get(String.format(FB_GRAPH, facebookID), values);
			object = verifyStatus(response);
			if (object.has("data")) {
				result = object.getJSONObject("data").getString("url");
			}

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
