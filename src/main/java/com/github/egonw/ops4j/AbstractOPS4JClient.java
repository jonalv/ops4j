package com.github.egonw.ops4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class AbstractOPS4JClient {

	protected String server;
	protected String appID;
	protected String appKey;

	protected String runRequest(String server, Map<String, String> params, Object... objects)
	throws ClientProtocolException, IOException, HttpException {
		DefaultHttpClient httpclient = new DefaultHttpClient();

		params.put("app_id", appID);
		params.put("app_key", appKey);
		params.put("_format", "ttl"); // the default
		String requestUrl = createRequest(server, params, objects);
		System.out.println("Call: " + requestUrl);
		HttpGet httppost = new HttpGet(requestUrl); 

		HttpResponse response = httpclient.execute(httppost);
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode != 200) throw new HttpException(statusLine.getReasonPhrase());

		HttpEntity responseEntity = response.getEntity();
		InputStream in = responseEntity.getContent();
		StringWriter writer = new StringWriter();
		IOUtils.copy(in, writer, "UTF-8");
		in.close();
		return writer.toString();
	}
	
	private String createRequest(String server, Map<String, String> params, Object... objects)
	throws UnsupportedEncodingException {
		StringBuffer requestURI = new StringBuffer();
		for (int i=0; i<objects.length; i++) {
			Object obj = objects[i];
			if (obj instanceof ResponseFormat) {
				params.put("_format", ((ResponseFormat)obj).getOPSCode());
			}
		}
		if (!params.isEmpty()) {
			requestURI.append(server).append('?');
			boolean beyondFirst = false;
			for (String key : params.keySet()) {
				if (beyondFirst) requestURI.append('&');
				requestURI.append(key).append('=').append(URLEncoder.encode(params.get(key), "UTF-8"));
				beyondFirst = true;
			}
		}
		return requestURI.toString();
	}

}
