package org.brain2.test.oauth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JsonUtil {

	public static JSONObject parseResponse(String json) {
		try {
			//			System.out.println("json: "+json);
			JSONObject jsonObject = new JSONObject(json);
			JSONObject response = jsonObject.getJSONObject("response");
			//			System.out.print("response: ");
			if(response != null){
				return response;
			}
			return new JSONObject("{}");
		} catch (JSONException e) {
		}
		return null;
	}

	public static JSONObject parseResponseResult(JSONObject response){
		try {
			JSONObject result = response.getJSONObject("result");
			return result;
		} catch (JSONException e) {
		}
		return null;
	}

	public static JSONArray parseResponseArrayResult(JSONObject response){
		try {
			JSONArray result = response.getJSONArray("result");
			return result;
		} catch (JSONException e) {
		}
		return null;
	}


}
