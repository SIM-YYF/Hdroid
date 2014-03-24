package com.hdroid.http.response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.BufferedHttpEntity;

import android.annotation.SuppressLint;
import com.hdroid.http.response.result.ResponseInfo;
import com.hdroid.http.util.OtherUtils;



/** 
 * @description: 
 * @time：2014-3-10 下午12:15:54 
 */

public class HttpResponseHandler extends BaseHttpResponseHandler {
	
	public HttpResponseHandler( ){
		super();
	}
	

	
	@SuppressLint("UseValueOf")
	public void sendResponseMessage(HttpResponse response){
		//TODO 构建消息并发送
		StatusLine status = response.getStatusLine();
		StringBuilder responseBody = new StringBuilder();
		try
		{
			HttpEntity entity = null;
			HttpEntity temp = response.getEntity();
	        long current = 0;
	        long total = 0;
			if (temp != null)
			{
				isUploading = false;
				entity = new BufferedHttpEntity(temp);
				total = entity.getContentLength();
				InputStream inputStream = null;
		        
	            inputStream = entity.getContent();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, this.responseCharset));
	            String line = "";
	            while ((line = reader.readLine()) != null) {
	            	responseBody.append(line);
	                current += OtherUtils.sizeOfString(line, responseCharset);
	                sendProgressMessage(PROGRESS_MESSAGE, new Object[]{new Long(total), new Long(current), new Boolean(isUploading)});
	            }
	            sendProgressMessage(PROGRESS_MESSAGE, new Object[]{new Long(total), new Long(total), new Boolean(isUploading)});
			}
		} catch (IOException e)
		{
			sendFailureMessage(e,  null);
		}

		if (status.getStatusCode() >= 300)
		{
			sendFailureMessage(new HttpResponseException(status.getStatusCode(), status.getReasonPhrase()), new StringBuilder(" net error"));
		} else {
			sendSuccessMessage(response, responseBody);
			
		}
	}
	
	

}

