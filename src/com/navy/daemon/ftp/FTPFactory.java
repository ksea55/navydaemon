package com.navy.daemon.ftp;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * 基于ftp分装ftp连接资源池，及其他远程和本地文件操作方法
 * @author mup
 *
 */
public class FTPFactory {
	/**
	 * 资源池默认等待时间 tomeout/MILLISECONDS
	 */
	public static final int defualt_timeout = 100;
	/**
	 * ftp连接资源池默认长度
	 */
	public static final int defualt_length = 10;
	/**
	 * 存放ftp实例，key为服务名（快速链接配置服务名），key为对应服务名下的所有ftp服务对象集合
	 */
	private static ConcurrentHashMap<String, FTPFactory> pools = new ConcurrentHashMap<String, FTPFactory>();
	/**
	 * 默认ftp配置
	 */
	private static Client default_ftp =  new Client();
	
	private Client client =  new Client();
	
	private String serviceName = "default";
	
	private AtomicInteger createdsize = new AtomicInteger(0);
	
	private ArrayBlockingQueue<Client> used;
	
	private ArrayBlockingQueue<Client> aviliable;
	
	/**
	 * ftp连接资源池默认长度
	 */
	private int length;
	
	private long timeout;
	
	private FTPFactory(){
	}
	
	/**
	 * 获取客户端对象
	 * @return
	 * @throws InterruptedException
	 * @throws IllegalStateException 当资源已满时抛出该异常
	 */
	public Client getClient() throws InterruptedException, IllegalStateException{
		Client ftpc = null;
		
		ftpc = aviliable.poll(timeout, TimeUnit.MILLISECONDS);
		//无空闲的初始化一个ftp服务
		if( null == ftpc ){
			if(createdsize.incrementAndGet() <= length){
				ftpc = new Client();
				
				ftpc.serviceName = client.serviceName;
				ftpc.ip = client.ip;
				ftpc.port = client.port;
				ftpc.account = client.account;
				ftpc.pwd = client.pwd;
				ftpc.fc = this;
				used.offer(ftpc);
			}else{
				//ftp资源池已满，抛出IllegalStateException异常
				throw new IllegalStateException("ftp pool is pool full,call FTPUtil's method logout() to release the source");
			}
		}
		
		return ftpc;
	}
	
	/**
	 * 获取默认ftp 客户端服务对象
	 * @return 返回默认ip、端口的ftp
	 * @throws InterruptedException
	 * @throws IllegalStateException 当资源已满时抛出该异常
	 */
	public static FTPFactory newInstance() {
		FTPFactory fc = null;
		//缩小多线程互斥粒度
		synchronized (default_ftp.serviceName) {
			fc = pools.get(default_ftp.serviceName);
			if(null == fc){
				fc = new FTPFactory();
				fc.serviceName = default_ftp.serviceName;
				fc.aviliable = new ArrayBlockingQueue<Client>(defualt_length);
				fc.used = new ArrayBlockingQueue<Client>(defualt_length);
				fc.length = defualt_length;
				fc.timeout = defualt_timeout;
				
				default_ftp.fc = fc;
				fc.aviliable.offer(default_ftp);
				pools.put(default_ftp.serviceName, fc);
			}
		}
		return fc;
	}
	
	/**
	 * 按指定IP用户名创建
	 * @param serviceName 自定义ftp连接服务名
	 * @param ip 服务端ip
	 * @param port 服务端端口
	 * @param account 账号
	 * @param pwd 密码
	 * @return
	 * @throws NullPointerException 参数不全抛出异常
	 */
	public static FTPFactory newInstance(String serviceName, String ip, int port, 
			String account, String pwd) throws NullPointerException{
		
		if(null == serviceName || null ==ip || port <= 0 || null == account || null == pwd){
			 throw new NullPointerException("parameter inaviliable");
		}
		
		FTPFactory fc = null;
		//缩小多线程互斥粒度
		synchronized (serviceName) {
			fc = pools.get(serviceName);
			if(null == fc){
				fc = new FTPFactory();
				fc.serviceName = serviceName;
				fc.aviliable = new ArrayBlockingQueue<Client>(defualt_length);
				fc.used = new ArrayBlockingQueue<Client>(defualt_length);
				
				fc.client.serviceName = serviceName;
				fc.client.ip = ip;
				fc.client.port = port;
				fc.client.account = account;
				fc.client.pwd = pwd;
				fc.timeout = defualt_timeout;
				fc.length = defualt_length;
				
				fc.client.fc = fc;
				fc.aviliable.offer(fc.client);
				pools.put(serviceName, fc);
			}
		}
		
		return fc;
	}
	
	public static FTPFactory newInstance(String serviceName, String ip, int port, 
			String account, String pwd, int sourcesize, int waittimeforsource) throws NullPointerException{
		
		if(null == serviceName || null ==ip || port <= 0 || null == account || null == pwd){
			 throw new NullPointerException("parameter inaviliable");
		}
		
		FTPFactory fc = null;
		//缩小多线程互斥粒度
		synchronized (serviceName) {
			fc = pools.get(serviceName);
			if(null == fc){
				fc = new FTPFactory();
				fc.serviceName = serviceName;
				fc.aviliable = new ArrayBlockingQueue<Client>(sourcesize);
				fc.used = new ArrayBlockingQueue<Client>(sourcesize);
				
				fc.client.serviceName = serviceName;
				fc.client.ip = ip;
				fc.client.port = port;
				fc.client.account = account;
				fc.client.pwd = pwd;
				fc.timeout = waittimeforsource;
				fc.length = sourcesize;
				
				fc.client.fc = fc;
				fc.aviliable.offer(fc.client);
				pools.put(serviceName, fc);
			}
		}
		return fc;
	}
	
	/**
	 * 释放serviceName指定的所有ftp连接资源
	 * @param serviceName 连接服务名
	 */
	public static void releaseAll(String serviceName){
		synchronized (serviceName) {
			FTPFactory fc = pools.get(serviceName);
			fc.aviliable.clear();
			fc.used.clear();
			pools.remove(serviceName);
		}
	}
	
	/**
	 * 释放所有ftp资源池
	 */
	public static synchronized void releaseAll(){
		Iterator<FTPFactory> its = pools.values().iterator();
		while(its.hasNext()){
			FTPFactory fc = its.next();
			fc.aviliable.clear();
			fc.used.clear();
		}
		pools.clear();
	}
	
	public static class Client extends FTPClient{
		private FTPFactory fc;
		
		/**
		 * 服务端IP
		 */
		private String ip = "192.168.3.133";
		/**
		 * 服务端端口, 默认21
		 */
		private int port = 21;
		/**
		 * ftp实例状态 
		 * <br/>0:闲置
		 * <br/>1:使用
		 */
		private AtomicInteger stat = new AtomicInteger(0);
		/**
		 * ftp实例服务名
		 */
		private String serviceName = "default";
		/**
		 * 登录用户名
		 */
		private String account = "";
		/**
		 * 登录密码
		 */
		private String pwd = "";
		
		public Client(){
			// 创建客户端  
			super();
		}
		
		public Client(String serviceName, String ip, int port, String account, String pwd){
			// 创建客户端  
			super();
	        this.serviceName = serviceName;
	        this.ip = ip;
	        this.port = port;
	        this.account = account;
	        this.pwd = pwd;
	       
		}
		
		/**
		 * 建立连接并登陆
		 * @throws IllegalStateException
		 * @throws IOException
		 * @throws FTPIllegalReplyException
		 * @throws FTPException
		 */
		public void login() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException{
	        // 不指定端口，则使用默认端口21  
	        connect(ip, port);
	        // 用户登录  
	        login(account, pwd);
	        // 打印地址信息  
	        System.out.println("client logon:"+ip+":"+port+"/"+account);
		}
		
		/**
		 * 退出登录并断开连接
		 * @throws IllegalStateException
		 * @throws IOException
		 * @throws FTPIllegalReplyException
		 * @throws FTPException
		 */
		public void logout() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException{
			super.logout();
			disconnect(true);
			fc.used.remove(this);
			fc.aviliable.offer(this);
		}
		/**
		 * 服务端IP
		 */
		public String getIp() {
			return ip;
		}
		/**
		 * 服务端端口, 默认21
		 */
		public int getPort() {
			return port;
		}
		/**
		 * ftp实例状态 
		 * <br/>0:闲置
		 * <br/>1:使用
		 */
		public int getStat() {
			return stat.get();
		}
		/**
		 * ftp连接服务名
		 */
		public String getServiceName() {
			return serviceName;
		}
		/**
		 * 登录用户名
		 */
		public String getAccount() {
			return account;
		}
		/**
		 * 登录密码
		 */
		public String getPwd() {
			return pwd;
		}
	}
}
