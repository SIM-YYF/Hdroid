package com.hdroid.http.response;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectTimeoutException;

import com.hdroid.http.exception.FileAlreadyExistException;
import com.hdroid.http.util.IOUtils;

import android.annotation.SuppressLint;
import android.util.Log;

public class FileHttpResponseHandler extends HttpResponseHandler {
	/**超时时间数**/
	public final static int TIME_OUT = 30000;
	private final static int BUFFER_SIZE = 1024 * 8;
	private static final String TAG = "FileHttpResponseHandler";
	private static final String TEMP_SUFFIX = ".download";//下载的临时文件
	private File file;
	private File tempFile;
	private File baseDirFile;//
	private RandomAccessFile outputStream; //随机访问输出流
	private long downloadSize;//正在下载的大小
	private long previousFileSize;//上次下载的大小
	private long totalSize;//总大小
	private long networkSpeed;
	private long previousTime;
	private long totalTime;
	private boolean interrupt = false;//当前下载线程运行状态，是否中断
	private boolean timerInterrupt = false;//定时器
	private String url;
	private Timer timer = new Timer();
	private static final int TIMERSLEEPTIME = 200;
	
	public FileHttpResponseHandler(String url, String rootFile, String fileName)
	{
		super();
		
		this.url = url;
		this.baseDirFile = new File(rootFile); //文件目录
		this.file = new File(rootFile, fileName);//文件
		this.tempFile = new File(rootFile, fileName + TEMP_SUFFIX);
		init();
	}
	public FileHttpResponseHandler(String rootFile, String fileName)
	{
		super();
		
		this.baseDirFile = new File(rootFile);
		this.file = new File(rootFile, fileName);
		this.tempFile = new File(rootFile, fileName + TEMP_SUFFIX);
		init();
	}
	public FileHttpResponseHandler(String filePath)
	{
		super();
		
		this.file = new File(filePath);
		this.baseDirFile = new File(this.file.getParent());
		this.tempFile = new File(filePath + TEMP_SUFFIX);
		init();
	}
	
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public File getTempFile()
	{
		return tempFile;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public boolean isInterrupt()
	{
		return interrupt;
	}
	public void setInterrupt(boolean interrupt)
	{
		this.interrupt = interrupt;
	}
	public long getDownloadSize()
	{
		return downloadSize;
	}

	public long getTotalSize()
	{
		return totalSize;
	}
	public void setPreviousFileSize(long previousFileSize)
	{
		this.previousFileSize = previousFileSize;
	}
	public double getDownloadSpeed()
	{
		return this.networkSpeed;
	}
	public long getTotalTime()
	{
		return this.totalTime;
	}
	
	/**
	 * 文件目录不存在创建文件目录
	 */
	private void init() {
		if (!this.baseDirFile.exists())
		{
			this.baseDirFile.mkdirs();
		}
	}
	
	/**
	 * 计时管理器，用来更新下载进度
	 */
	private void startTimer(){
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				while(!timerInterrupt){
					sendProgressMessage(PROGRESS_MESSAGE, new Object[]{new Long(totalSize), new Long(getDownloadSize()), new Boolean(isUploading)});
				}
			}
		}, 0, 1000);
	}
	
	/**
	 * 停止中断下载任务
	 */
	@SuppressWarnings("unused")
	private void stopTimer()
	{
		timerInterrupt = true;
	}
	
	@SuppressWarnings("unused")
	private class ProgressReportingRandomAccessFile extends RandomAccessFile
	{
		private int progress = 0;
		public ProgressReportingRandomAccessFile(File file, String mode)throws FileNotFoundException
		{
			super(file, mode);
		}
		@Override
		public void write(byte[] buffer, int offset, int count) throws IOException
		{
			super.write(buffer, offset, count);
			progress += count;
			totalTime = System.currentTimeMillis() - previousTime;
			downloadSize = progress + previousFileSize;
			if (totalTime > 0)
			{
				networkSpeed = (long) ((progress / totalTime)/1.024);//计算流量
			}
		}
	}
	
	@Override
	@SuppressLint("UseValueOf")
	public void sendResponseMessage(HttpResponse response) {
		Throwable  error = null;
		long  result = -1;
		try{
			long contentLenght = response.getEntity().getContentLength();
			if (contentLenght == -1)contentLenght = response.getEntity().getContent().available();
			//总大小
			totalSize = contentLenght + previousFileSize;
			Log.v(TAG, "-------**********----------totalSize-------*************--------: " + totalSize);
			//存在的文件大小与总大小相同时，抛出文件下载完成已存在异常
			if(file.exists() && totalSize == file.length())throw new FileAlreadyExistException("--------exception-------- output file already exists.");
			//上次下载的大小
			if(tempFile.exists()){
				previousFileSize = tempFile.length();
				Log.v(TAG, "-------**********----------previousFileSize-------*************--------: " + previousFileSize);
			}
			outputStream = new ProgressReportingRandomAccessFile(tempFile, "rw");
			InputStream input = response.getEntity().getContent();
			startTimer();
			int bytesCopied = copy(input, outputStream);
			if ((previousFileSize + bytesCopied) != totalSize && totalSize != -1 && !interrupt)
			{
				throw new IOException("-------**********----------:Download fail download file complete:-------*************-------- " + bytesCopied + " != " + totalSize);
			}
			result = bytesCopied;
		}catch(FileNotFoundException e){
			sendFailureMessage(e, new StringBuilder("file not found exception"));
			error = e;
		}catch(IllegalStateException e){
			error = e;
		}catch(FileAlreadyExistException e){
			error = e;
		}catch(IOException e){
			error = e;
		}
		
		//停止计时器及更新进度条
		stopTimer();
		//确保计时器已关闭
		try
		{
			Thread.sleep(TIMERSLEEPTIME);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		if (result == -1 || interrupt || error != null)
		{
			if (error != null)
			{
				Log.v(TAG, "Download failed." + error.getMessage());
				if (error instanceof FileAlreadyExistException)
					sendSuccessMessage(response, new StringBuilder("file download success"));
				 else
					sendFailureMessage(error, new StringBuilder("file download fail exception"));
			}
			return;
		}
		
		tempFile.renameTo(file);
		sendSuccessMessage(response, new StringBuilder("file download success"));
	}
	private int copy(InputStream input, RandomAccessFile out)throws IOException {
		if (input == null || out == null) return -1;
		byte[] buffer = new byte[BUFFER_SIZE];
		BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
		int count = 0, n = 0;
		try{
			out.seek(out.length());
			previousTime = System.currentTimeMillis();
			while (!interrupt)
			{
				n = in.read(buffer, 0, BUFFER_SIZE);
				if (n == -1)break;
				
				out.write(buffer, 0, n);
				count += n;
				compBlockTime();//根据网络流量监控状态，下载是否超时
			}
		}finally{
			IOUtils.closeQuietly(in); //这里可能无法关闭输入流
			IOUtils.closeQuietly(out);
		}
		return count;
	}
	private long compBlockTime()throws ConnectTimeoutException {
		long errorBlockTimePreviousTime = -1, expireTime = 0;
		if (networkSpeed == 0)
		{
			if (errorBlockTimePreviousTime > 0)
			{
				expireTime = System.currentTimeMillis()- errorBlockTimePreviousTime;
				if (expireTime > TIME_OUT)
				{
					throw new ConnectTimeoutException("connection time out.");
				}
			} else {
				errorBlockTimePreviousTime = System.currentTimeMillis();
			}
		} else {
			expireTime = 0;
			errorBlockTimePreviousTime = -1;
		}
		return errorBlockTimePreviousTime;
	}

}
