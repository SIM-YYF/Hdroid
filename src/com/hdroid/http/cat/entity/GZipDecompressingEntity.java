package com.hdroid.http.cat.entity;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;

/**
 * @Title GZipDecompressingEntity.java
 * @Package com.ykdl.common.http.entitie
 * @Description
 * @date 2014-3-7 下午6:45:09

 */
public class GZipDecompressingEntity extends DecompressingEntity {

	public GZipDecompressingEntity(final HttpEntity wrapped) {
		super(wrapped);
	}

	@Override
	InputStream decorate(InputStream wrapped) throws IOException {
		return new GZIPInputStream(wrapped); /** 解压缩输入流 **/
	}

}
