

package com.hdroid.http.response.result;

import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;

import com.hdroid.http.AsynHttpClient;
import com.hdroid.http.util.IOUtils;


import java.io.*;
import java.util.Locale;

public class ResponseStream extends InputStream {
	
	private AsynHttpClient client;
    private HttpResponse baseResponse;
    private InputStream baseStream;

    private String charset;

    private String requestUrl;
    private String requestMethod;
    private long expiry;

    public ResponseStream(HttpResponse baseResponse, String requestUrl, long expiry) throws IOException {
        this(baseResponse, HTTP.UTF_8, requestUrl, expiry);
    }

    public ResponseStream(HttpResponse baseResponse, String charset, String requestUrl, long expiry) throws IOException {
        if (baseResponse == null) {
            throw new IllegalArgumentException("baseResponse may not be null");
        }

        this.baseResponse = baseResponse;
        this.baseStream = baseResponse.getEntity().getContent();
        this.charset = charset;
        this.requestUrl = requestUrl;
        this.expiry = expiry;
    }

    private String _directResult;

    public ResponseStream(String result) throws IOException {
        if (result == null) {
            throw new IllegalArgumentException("result may not be null");
        }

        _directResult = result;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    /*package*/ void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public InputStream getBaseStream() {
        return baseStream;
    }

    public HttpResponse getBaseResponse() {
        return baseResponse;
    }

    public int getStatusCode() {
        if (_directResult != null) return 200;
        return baseResponse.getStatusLine().getStatusCode();
    }

    public Locale getLocale() {
        if (_directResult != null) return Locale.getDefault();
        return baseResponse.getLocale();
    }

    public String getReasonPhrase() {
        if (_directResult != null) return "";
        return baseResponse.getStatusLine().getReasonPhrase();
    }

    public String readString() throws IOException {
        if (_directResult != null) return _directResult;
        if (baseStream == null) return null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(baseStream, charset));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            _directResult = sb.toString();
            //缓存机制目前暂不开发
//            if (requestUrl != null && client.sHttpCache.isEnabled(requestMethod)) {
//            	client.sHttpCache.put(requestUrl, _directResult, expiry);
//            }
            return _directResult;
        } finally {
            IOUtils.closeQuietly(baseStream);
        }
    }

    public void readFile(String savePath) throws IOException {
        if (_directResult != null) return;
        if (baseStream == null) return;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(savePath);
            BufferedInputStream ins = new BufferedInputStream(baseStream);
            byte[] buffer = new byte[4096];
            int len = 0;
            while ((len = ins.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(baseStream);
        }
    }

    @Override
    public int read() throws IOException {
        if (baseStream == null) return -1;
        return baseStream.read();
    }

    @Override
    public int available() throws IOException {
        if (baseStream == null) return 0;
        return baseStream.available();
    }

    @Override
    public void close() throws IOException {
        if (baseStream == null) return;
        baseStream.close();
    }

    @Override
    public void mark(int readLimit) {
        if (baseStream == null) return;
        baseStream.mark(readLimit);
    }

    @Override
    public boolean markSupported() {
        if (baseStream == null) return false;
        return baseStream.markSupported();
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        if (baseStream == null) return -1;
        return baseStream.read(buffer);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (baseStream == null) return -1;
        return baseStream.read(buffer, offset, length);
    }

    @Override
    public synchronized void reset() throws IOException {
        if (baseStream == null) return;
        baseStream.reset();
    }

    @Override
    public long skip(long byteCount) throws IOException {
        if (baseStream == null) return 0;
        return baseStream.skip(byteCount);
    }
}
