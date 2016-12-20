package com.navy.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import com.navy.daemon.action.IReporter;
import com.navy.daemon.conf.SynConfig;
import com.navy.daemon.db.ConnectionProxy;
import com.navy.daemon.entity.ClusterConfig;
import com.navy.daemon.ftp.FTPFactory;
import com.navy.daemon.util.FileOperate;
import com.navy.daemon.util.MonitorInfoBean;

/**
 * Ӧ�ùܿش���������
 * @author mup
 *
 */
public class BootStart {
	/**
	 * Ĭ�ϻ��������ļ�
	 */
	public static final String evnsetpath = "com/navy/daemon/conf/default_daemonevn.properties";
	/**
	 * Ĭ��sqllite ���ݿ��ļ�
	 */
	public static final String databasepath = "com/navy/daemon/conf/aca.db";
	
	/**
	 * 1:����˳���
	 * 0���ܿ�����
	 */
	private int isagent = 1;
	
	/**
	 * Ӧ�ÿ��ƶ˿�
	 */
	private int appctrl_port = 6551;
	/**
	 * �������úʹ��������·���˿�
	 */
	private int configset_port = 6552;
	/**
	 * �ɼ������Ʒ���˿�
	 */
	private int monictrl_port = 6553;
	
	/**
	 * Ӧ�ÿ��ƶ˿�
	 */
	private int appreport_port = 6551;
	/**
	 * �������úʹ��������·���˿�
	 */
	private int nodereport_port = 6552;
	/**
	 * �ɼ������Ʒ���˿�
	 */
	private int eventreport_port = 6553;
	
	private String runtimePath;
	/**
	 * �ܿش�������Ŀ¼
	 */
	private File root;
	/**
	 * Ӧ�ùܿش��������Ŀ¼��sqllite���ݿ⡢���������ļ���
	 */
	private File conf;
	
	/**
	 * �ܿش�������������ʱĿ¼
	 */
	private File updatTemp;
	/**
	 * �ܿش��������װ���������������ʱĿ¼
	 */
	private File downloadTemp;
	/**
	 * �ܿش���Ӧ�ý�ѹ��װĿ¼
	 */
	private File installfolder;
	/**
	 * �ܿش����������Ŀ¼
	 */
	private File pluginlib;
	/**
	 * ��ǰ����IP
	 */
	private String agentIP;
	/**
	 * �ܿ�����IP
	 */
	private String serverIP;
	/**
	 * WS�������Э��ͷ
	 */
	private String agentPluginWS_Url_Head;
	
	private static BootStart boot;
	private AtomicInteger status = new AtomicInteger(-1);
	
	private static DataSource data_source;
	/**
	 * �ܿ�����ftp���Ӳֿ�
	 */
	private FTPFactory fc;
	/**
	 * �ܹ�Ӧ����Ϣ
	 */
	public static ConcurrentHashMap<String, ClusterConfig> 
	apps = new ConcurrentHashMap<String, ClusterConfig>();
	
	/**
	 * ������ֹͣ��app
	 */
	public static ConcurrentHashMap<String, Byte> 
	requestStopedAPPS = new ConcurrentHashMap<String, Byte>();
	
	/**
	 * ����פ��������ϵͳ��Ϣʵ��
	 */
	public MonitorInfoBean nodeSysInfo = new MonitorInfoBean();
	
	/**
	 * ����פ��������ϵͳ��Ϣʵ��
	 */
	public Object webContext;
	
	/**
	 * ���涨ʱ������ɼ����ȼ̳�AbstarctReporterʵ��IReporter�ӿڵĶ�ʱ��������
	 */
	public static ConcurrentHashMap<String, IReporter> 
	rePorters = new ConcurrentHashMap<String, IReporter>();
	
	public static synchronized BootStart getBoot(){
		if(null == boot){
			boot = new BootStart();
		}
		return boot;
	}
		
	/**
	 * ��������Ŀ¼
	 */
	private void createFolders(){
		URL url = BootStart.class.getProtectionDomain().getCodeSource().getLocation();
		String filePath = null;
		try {
			filePath = URLDecoder.decode(url.getPath(), "utf-8");
			if(filePath.endsWith(".jar")){
				filePath = filePath.substring(0, filePath.lastIndexOf("/")+1);
			}
			
			File fi = new File(filePath);
			filePath = fi.getAbsolutePath();
			System.out.println("filePath3:"+filePath);
			runtimePath = filePath;
			
			root = fi.getParentFile();
			System.out.println("root:"+root.getAbsolutePath());
			
			
			conf = new File(root.getAbsolutePath()+File.separator+"conf");
			if(!conf.exists()) conf.mkdirs();
			System.out.println("conf:"+conf.getAbsolutePath());
			
			updatTemp = new File(root.getAbsolutePath()+File.separator+"updatTemp");
			if(!updatTemp.exists()) updatTemp.mkdirs();
			System.out.println("updatTemp:"+updatTemp.getAbsolutePath());
			
			downloadTemp = new File(root.getAbsolutePath()+File.separator+"downloadTemp");
			if(!downloadTemp.exists()) downloadTemp.mkdirs();
			System.out.println("downloadTemp:"+downloadTemp.getAbsolutePath());
			
			installfolder = new File(root.getAbsolutePath()+File.separator+"installfolder");
			if(!installfolder.exists()) installfolder.mkdirs();
			System.out.println("installfolder:"+installfolder.getAbsolutePath());
			
			pluginlib = new File(root.getAbsolutePath()+File.separator+"pluginlib");
			if(!pluginlib.exists()) pluginlib.mkdirs();
			System.out.println("pluginlib:"+pluginlib.getAbsolutePath());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		
	}
	/**
	 * ������ʼ�������ļ�
	 */
	private void initConf(){
		//�ж�����Ŀ¼���Ƿ���ڶ�Ӧ�����ļ���û��������jar���п���Ĭ������
		Properties pr = null;
		File evnset = new File(conf.getAbsolutePath()+File.separator+"default_daemonevn.properties");
		if(evnset.exists()){
			InputStream in;
			try {
				in = new FileInputStream(evnset);
				pr = new Properties();
				pr.load(in);
			} catch (Exception e) {
				pr = null;
				e.printStackTrace();
			}
		}else{
			File srcfile;
			try {
				System.out.println(BootStart.class.getClassLoader().getResource(evnsetpath).toString());
				FileOperate.copyFile(BootStart.class.getClassLoader().getResourceAsStream(evnsetpath), 
						evnset);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		File databasefile = new File(conf.getAbsolutePath()+File.separator+"aca.db");
		if(!databasefile.exists()){
			try {
				FileOperate.copyFile(BootStart.class.getClassLoader().getResourceAsStream(databasepath), 
						databasefile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(null == pr){
			pr = FileOperate.getProperties(evnsetpath);
		}
		this.agentIP = pr.getProperty("agentIP");
		this.appctrl_port = Integer.parseInt(pr.getProperty("appctrl_port"));
		this.configset_port = Integer.parseInt(pr.getProperty("configset_port"));
		this.serverIP = pr.getProperty("serverIP");
		
		this.appreport_port = Integer.parseInt(pr.getProperty("appreport_port"));
		this.nodereport_port = Integer.parseInt(pr.getProperty("nodereport_port"));
		this.eventreport_port = Integer.parseInt(pr.getProperty("eventreport_port"));
		
		System.out.println(pr.getProperty("appctrl_port"));
	}
	
	/**
	 * ��ʼ��Action
	 */
	private void initAction(){
		SynConfig.initActionSet();
	}
	
	/**
	 * �����ܹ�Ӧ����Ϣ
	 */
	private void loadApp(){
		SynConfig.loadApp();
	}
	/**
	 * ��ʼ������������ʱ��������
	 */
	private void startReportRunner(){
		Iterator<Entry<String, IReporter>> reports = BootStart.rePorters.entrySet().iterator();
		Entry<String, IReporter> reportentry = null;
		IReporter report = null;
		System.out.println("reporter size:"+BootStart.rePorters.size());
		while(reports.hasNext()){
			try{
				reportentry = reports.next();
				report = reportentry.getValue();
				report.startRuner();
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * ��ʼ������
	 */
	private void init() {
		if (status.compareAndSet(-1, 0)) {
			//��������Ŀ¼
			createFolders();
			
			//������ʼ������
			initConf();
			
			//�����ܹ�Ӧ����Ϣ
			loadApp();
			
			//�������ã����ñ���Ĭ������IP
			InetAddress address;
			try {
				address = InetAddress.getLocalHost();
				agentIP = address.getHostAddress();
				nodeSysInfo.IP = agentIP;
				//agentIP="192.168.3.3";
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}   
			agentPluginWS_Url_Head = "http://"+agentIP;
			System.out.println("agentPluginWS_Url_Head:"+agentPluginWS_Url_Head);
			fc = FTPFactory.newInstance("test", "192.168.3.238", 2121, "admin", "admin");
			//��ʼ��Action,��������
			initAction();
			
			//��ʼ������������ʱ��������
			startReportRunner();
		}
	}
	
	public void start() {
		init();
		try{
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * �Ƚ��Ƿ��ѷ�����ֹͣӦ�����󣬲�����������ֹͣ���
	 * @param appProcessName
	 * @param isstoped �ж��Ƿ��ѱ������ֹͣ��Ԥ�ڣ�
	 * @param isStopSet ��Ԥ����ͬʱ������isStopSet���ã�true����Ӧ��ֹͣ�����ʶ����false������
	 * @return
	 */
	public static boolean checkStop(String appProcessName, boolean isstoped, boolean isStopSet){
		boolean flag = false;
		synchronized (appProcessName) {
			if(isstoped == requestStopedAPPS.containsKey(appProcessName)){
				if(isStopSet) {
					requestStopedAPPS.put(appProcessName, (byte)0);
				}else{
					requestStopedAPPS.remove(appProcessName);
				}
				flag = true;
			}
		}
		return flag;
	}
	
	public static void startListen(){
		if(null != boot) return;
		final BufferedReader strin = new BufferedReader(new InputStreamReader(
				System.in));
		System.out.println("�����Ƿ�Ϊ����(y/n)");
		String isagentstr = null;
		boolean isagent = true;
		try {
			isagentstr = strin.readLine();
			System.out.println(isagentstr);
			isagent = "y".equalsIgnoreCase(isagentstr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		BootStart boot = BootStart.getBoot();
		boot.setIsagent(isagent);
		boot.start();
		Thread t = new Thread(){
			public void run(){
				try {
					while(true){
						System.out.print("�˳���(exit)");
						String isagentstr = strin.readLine();
						if("exit".equalsIgnoreCase(isagentstr)){
							System.exit(1);
						}
					}
				}catch(Exception e){}
			}
		};
		
		t.start();
	}
	
	public static void main(String[] args){
		startListen();
		System.out.println(".........................");
	}
	
	/**
	 * ��ȡ���ݿ�����
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Connection getConnection(){
		Connection con = null;
		try{
			if(null != data_source){
				con = data_source.getConnection();
			}else{
				con = ConnectionProxy.getConnection(conf);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return con;
	}
	
	/**
	 * �ܿش�������Ŀ¼
	 */
	public File getRoot() {
		return root;
	}

	public AtomicInteger getStatus() {
		return status;
	}

	public FTPFactory getFc() {
		return fc;
	}
	/**
	 * �ܿش�������������ʱĿ¼
	 */
	public File getUpdatTemp() {
		return updatTemp;
	}
	/**
	 * �ܿش��������װ���������������ʱĿ¼
	 */
	public File getDownloadTemp() {
		return downloadTemp;
	}
	/**
	 * �ܿش���Ӧ�ý�ѹ��װĿ¼
	 */
	public File getInstallfolder() {
		return installfolder;
	}
	/**
	 * �ܿش����������Ŀ¼
	 */
	public File getPluginlib() {
		return pluginlib;
	}
	
	public String getRuntimePath() {
		return runtimePath;
	}

	public String getAgentIP() {
		return agentIP;
	}

	public String getServerIP() {
		return serverIP;
	}

	public int getAppctrl_port() {
		return appctrl_port;
	}

	public int getConfigset_port() {
		return configset_port;
	}

	public int getMonictrl_port() {
		return monictrl_port;
	}

	public String getAgentPluginWS_Url_Head() {
		return agentPluginWS_Url_Head;
	}

	public File getConf() {
		return conf;
	}

	public static String getEvnsetpath() {
		return evnsetpath;
	}

	public int getIsagent() {
		return isagent;
	}

	public int getAppreport_port() {
		return appreport_port;
	}

	public int getNodereport_port() {
		return nodereport_port;
	}

	public int getEventreport_port() {
		return eventreport_port;
	}

	public void setData_source(DataSource dataSource) {
		data_source = dataSource;
	}

	public void setIsagent(boolean isagent) {
		this.isagent = isagent ? 1 : 0;
	}
}
