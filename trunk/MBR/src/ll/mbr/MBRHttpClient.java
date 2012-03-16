package ll.mbr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class MBRHttpClient {
	private static final String TAG = "HttpClient";

	public static StudentDetail doRequest(String url,
			HashMap<String, ? extends Object> paramMap) {
		Log.v(TAG, "deRequest");

		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		Iterator iterator = paramMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry entry = (Entry) iterator.next();
			param.add(new BasicNameValuePair((String) entry.getKey(),(String) entry.getValue()));
		}

		String line = null;
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(param));
			HttpResponse httpResponse = httpClient.execute(httpPost);
			line = readResponse(httpResponse);
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG,"Could not establish a HTTP connection to the server or could not get a response properly from the server.",e);
			e.printStackTrace();
		}

		StudentDetail sd;
		if(line == null){
			sd = new StudentDetail("serveroffline");
		}else{
			sd = new StudentDetail("fail");
			try {
				JSONObject jsonObject = new JSONObject(line);
				String loginState = jsonObject.getString("loginState");

				if (loginState.equals("fail")) {
					sd.setLoginState(loginState);
//					return sd;
				} else {
					sd.setLoginState("valid");
//					sd.setIsFound(jsonObject.getString("notFound"));
					sd.setId(jsonObject.getString("studentId"));
					sd.setFirstname(jsonObject.getString("firstname"));
					sd.setSurname(jsonObject.getString("surname"));
					sd.setGender(jsonObject.getString("gender"));
					sd.setDateOfBirth(jsonObject.getString("dateOfBirth"));
					sd.setCourse(jsonObject.getString("course"));
					sd.setCourseCode(jsonObject.getString("courseCode"));
//					return sd;
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sd;
		// return line.trim();
		// return new StudentDetail();
	}

	public static String readResponse(HttpResponse httpResponse) {
		String line = null;

		try {
			InputStream content = httpResponse.getEntity().getContent();
			BufferedReader in = new BufferedReader(new InputStreamReader(content));
			line = in.readLine();
			in.close();

		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return line;

	}
}
