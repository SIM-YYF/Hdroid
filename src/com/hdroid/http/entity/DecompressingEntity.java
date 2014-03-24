package com.hdroid.http.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import com.hdroid.http.callback.RequestCallBackHandler;


/**
 * @Title DecompressingEntity.java
 * @Package com.ykdl.common.http.entities
 * @Description
 * @date 2014-3-7 下午2:01:27

 */
public abstract class DecompressingEntity extends HttpEntityWrapper implements UploadEntity {
	
	/** 压缩的数据长度 **/
	private long uncompressedLength;
	/** 解压的数据长度 **/
	private long compressedLength = 0;
	private InputStream content;
	private RequestCallBackHandler callBackHandler = null;
	
	
	public DecompressingEntity(HttpEntity wrapped) {
		super(wrapped);
		this.uncompressedLength = wrapped.getContentLength();
	}
	
    @Override
    public void setCallBackHandler(RequestCallBackHandler callBackHandler) {
        this.callBackHandler = callBackHandler;
    }
	
	abstract InputStream decorate(final InputStream wrapped) throws IOException;
	
	@Override
	public InputStream getContent() throws IOException {
		if(wrappedEntity.isStreaming()){
			if(content == null){
				content = getDecompressingStream();
			}
			
		}else{
			content = getDecompressingStream();
		}
		
		return content;
	}
	
	private InputStream getDecompressingStream() throws IOException {
		InputStream in = null;
        try {
            in = wrappedEntity.getContent();
            return decorate(in);
        } catch (IOException ex) {
        	if (in != null) {
                try {
                    in.close();
                } catch (Throwable e) {
                }
            }
            throw ex;
        }
	}

	@Override
	public long getContentLength() {
		return -1;
	}
	
	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		if(null == outstream){
			throw new IllegalArgumentException("----- 输出流为空 -----");
		}
		InputStream is = null;
		try{
			is = getContent();
			byte[] tmp = new byte[4096];
			int len;
			while((len = is.read(tmp)) != -1){
				outstream.write(tmp, 0, len);
				compressedLength += len;
				if (callBackHandler != null) {
                    if (!callBackHandler.updateProgress(uncompressedLength, compressedLength, false)) {
                        throw new InterruptedIOException("----- 解压输出流出现异常停止 -----");
                    }
                }
			}
			outstream.flush();
			
            if (callBackHandler != null) {
                callBackHandler.updateProgress(uncompressedLength, compressedLength, true);
            }
            
		}finally {
			if (is != null) {
                try {
                    is.close();
                } catch (Throwable e) {
                }
            }
        }
	}
	
}
