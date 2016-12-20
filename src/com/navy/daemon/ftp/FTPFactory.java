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
 * ����ftp��װftp������Դ�أ�������Զ�̺ͱ����ļ���������
 * @author mup
 *
 */
public class FTPFactory {
	/**
	 * ��Դ��Ĭ�ϵȴ�ʱ�� tomeout/MILLISECONDS
	 */
	public static final int defualt_timeout = 100;
	/**
	 * ftp������Դ��Ĭ�ϳ���
	 */
	public static final int defualt_length = 10;
	/**
	 * ���ftpʵ����keyΪ�������������������÷���������keyΪ��Ӧ�������µ�����ftp������󼯺�
	 */
	private static ConcurrentHashMap<String, FTPFactory> pools = new ConcurrentHashMap<String, FTPFactory>();
	/**
	 * Ĭ��ftp����
	 */
	private static Client default_ftp =  new Client();
	
	private Client client =  new Client();
	
	private String serviceName = "default";
	
	private AtomicInteger createdsize = new AtomicInteger(0);
	
	private ArrayBlockingQueue<Client> used;
	
	private ArrayBlockingQueue<Client> aviliable;
	
	/**
	 * ftp������Դ��Ĭ�ϳ���
	 */
	private int length;
	
	private long timeout;
	
	private FTPFactory(){
	}
	
	/**
	 * ��ȡ�ͻ��˶���
	 * @return
	 * @throws InterruptedException
	 * @throws IllegalStateException ����Դ����ʱ�׳����쳣
	 */
	public Client getClient() throws InterruptedException, IllegalStateException{
		Client ftpc = null;
		
		ftpc = aviliable.poll(timeout, TimeUnit.MILLISECONDS);
		//�޿��еĳ�ʼ��һ��ftp����
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
				//ftp��Դ���������׳�IllegalStateException�쳣
				throw new IllegalStateException("ftp pool is pool full,call FTPUtil's method logout() to release the source");
			}
		}
		
		return ftpc;
	}
	
	/**
	 * ��ȡĬ��ftp �ͻ��˷������
	 * @return ����Ĭ��ip���˿ڵ�ftp
	 * @throws InterruptedException
	 * @throws IllegalStateException ����Դ����ʱ�׳����쳣
	 */
	public static FTPFactory newInstance() {
		FTPFactory fc = null;
		//��С���̻߳�������
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
	 * ��ָ��IP�û�������
	 * @param serviceName �Զ���ftp���ӷ�����
	 * @param ip �����ip
	 * @param port ����˶˿�
	 * @param account �˺�
	 * @param pwd ����
	 * @return
	 * @throws NullPointerException ������ȫ�׳��쳣
	 */
	public static FTPFactory newInstance(String serviceName, String ip, int port, 
			String account, String pwd) throws NullPointerException{
		
		if(null == serviceName || null ==ip || port <= 0 || null == account || null == pwd){
			 throw new NullPointerException("parameter inaviliable");
		}
		
		FTPFactory fc = null;
		//��С���̻߳�������
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
		//��С���̻߳�������
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
	 * �ͷ�serviceNameָ��������ftp������Դ
	 * @param serviceName ���ӷ�����
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
	 * �ͷ�����ftp��Դ��
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
		 * �����IP
		 */
		private String ip = "192.168.3.133";
		/**
		 * ����˶˿�, Ĭ��21
		 */
		private int port = 21;
		/**
		 * ftpʵ��״̬ 
		 * <br/>0:����
		 * <br/>1:ʹ��
		 */
		private AtomicInteger stat = new AtomicInteger(0);
		/**
		 * ftpʵ��������
		 */
		private String serviceName = "default";
		/**
		 * ��¼�û���
		 */
		private String account = "";
		/**
		 * ��¼����
		 */
		private String pwd = "";
		
		public Client(){
			// �����ͻ���  
			super();
		}
		
		public Client(String serviceName, String ip, int port, String account, String pwd){
			// �����ͻ���  
			super();
	        this.serviceName = serviceName;
	        this.ip = ip;
	        this.port = port;
	        this.account = account;
	        this.pwd = pwd;
	       
		}
		
		/**
		 * �������Ӳ���½
		 * @throws IllegalStateException
		 * @throws IOException
		 * @throws FTPIllegalReplyException
		 * @throws FTPException
		 */
		public void login() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException{
	        // ��ָ���˿ڣ���ʹ��Ĭ�϶˿�21  
	        connect(ip, port);
	        // �û���¼  
	        login(account, pwd);
	        // ��ӡ��ַ��Ϣ  
	        System.out.println("client logon:"+ip+":"+port+"/"+account);
		}
		
		/**
		 * �˳���¼���Ͽ�����
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
		 * �����IP
		 */
		public String getIp() {
			return ip;
		}
		/**
		 * ����˶˿�, Ĭ��21
		 */
		public int getPort() {
			return port;
		}
		/**
		 * ftpʵ��״̬ 
		 * <br/>0:����
		 * <br/>1:ʹ��
		 */
		public int getStat() {
			return stat.get();
		}
		/**
		 * ftp���ӷ�����
		 */
		public String getServiceName() {
			return serviceName;
		}
		/**
		 * ��¼�û���
		 */
		public String getAccount() {
			return account;
		}
		/**
		 * ��¼����
		 */
		public String getPwd() {
			return pwd;
		}
	}
}
