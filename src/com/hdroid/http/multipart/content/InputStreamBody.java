

package com.hdroid.http.multipart.content;



import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import com.hdroid.http.multipart.MIME;
import com.hdroid.http.util.IOUtils;


/**
 * @since 4.0
 */
public class InputStreamBody extends AbstractContentBody {

    private final InputStream in;
    private final String filename;
    private long length;

    public InputStreamBody(final InputStream in, long length, final String filename, final String mimeType) {
        super(mimeType);
        if (in == null) {
            throw new IllegalArgumentException("Input stream may not be null");
        }
        this.in = in;
        this.filename = filename;
        this.length = length;
    }

    public InputStreamBody(final InputStream in, long length, final String filename) {
        this(in, length, filename, "application/octet-stream");
    }

    public InputStreamBody(final InputStream in, long length) {
        this(in, length, "no_name", "application/octet-stream");
    }

    public InputStream getInputStream() {
        return this.in;
    }

    public void writeTo(final OutputStream out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        try {
            byte[] tmp = new byte[4096];
            int l;
            while ((l = this.in.read(tmp)) != -1) {
                out.write(tmp, 0, l);
                callBackInfo.pos += l;
                if (!callBackInfo.doCallBack(false)) {
                    throw new InterruptedIOException("stop");
                }
            }
            out.flush();
        } finally {
            IOUtils.closeQuietly(this.in);
        }
    }

    public String getTransferEncoding() {
        return MIME.ENC_BINARY;
    }

    public String getCharset() {
        return null;
    }

    public long getContentLength() {
        return this.length;
    }

    public String getFilename() {
        return this.filename;
    }

}
