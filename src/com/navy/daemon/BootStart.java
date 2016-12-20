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
 * 应用管控代理启动类
 * @author mup
 *
 */
public class BootStart {
	/**
	 * 默认环境配置文件
	 */
	public static final String evnsetpath = "com/navy/daemon/conf/default_daemonevn.properties";
	/**
	 * 默认sqllite 数据库文件
	 */
	public static final String databasepath = "com/navy/daemon/conf/aca.db";
	
	/**
	 * 1:代理端程序
	 * 0：管控中心
	 */
	private int isagent = 1;
	
	/**
	 * 应用控制端口
	 */
	private int appctrl_port = 6551;
	/**
	 * 代理配置和代理插件更新服务端口
	 */
	private int configset_port = 6552;
	/**
	 * 采集器控制服务端口
	 */
	private int monictrl_port = 6553;
	
	/**
	 * 应用控制端口
	 */
	private int appreport_port = 6551;
	/**
	 * 代理配置和代理插件更新服务端口
	 */
	private int nodereport_port = 6552;
	/**
	 * 采集器控制服务端口
	 */
	private int eventreport_port = 6553;
	
	private String runtimePath;
	/**
	 * 管控代理工作更目录
	 */
	private File root;
	/**
	 * 应用管控代理的配置目录（sqllite数据库、环境配置文件）
	 */
	private File conf;
	
	/**
	 * 管控代理更新软件包临时目录
	 */
	private File updatTemp;
	/**
	 * 管控代理软件安装包、插件包下载临时目录
	 */
	private File downloadTemp;
	/**
	 * 管控代理应用解压安装目录
	 */
	private File installfolder;
	/**
	 * 管控代理插件包存放目录
	 */
	private File pluginlib;
	/**
	 * 当前代理IP
	 */
	private String agentIP;
	/**
	 * 管控中心IP
	 */
	private String serverIP;
	/**
	 * WS插件发布协议头
	 */
	private String agentPluginWS_Url_Head;
	
	private static BootStart boot;
	private AtomicInteger status = new AtomicInteger(-1);
	
	private static DataSource data_source;
	/**
	 * 管控中心ftp连接仓库
	 */
	private FTPFactory fc;
	/**
	 * 受管应用信息
	 */
	public static ConcurrentHashMap<String, ClusterConfig> 
	apps = new ConcurrentHashMap<String, ClusterConfig>();
	
	/**
	 * 已请求停止的app
	 */
	public static ConcurrentHashMap<String, Byte> 
	requestStopedAPPS = new ConcurrentHashMap<String, Byte>();
	
	/**
	 * 代理驻留服务器系统信息实体
	 */
	public MonitorInfoBean nodeSysInfo = new MonitorInfoBean();
	
	/**
	 * 代理驻留服务器系统信息实体
	 */
	public Object webContext;
	
	/**
	 * 报告定时器（如采集器等继承AbstarctReporter实现IReporter接口的定时报告任务）
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
	 * 创建工作目录
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
	 * 拷贝初始化配置文件
	 */
	private void initConf(){
		//判断配置目录中是否存在对应配置文件，没有则充软件jar包中拷贝默认配置
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
	 * 初始化Action
	 */
	private void initAction(){
		SynConfig.initActionSet();
	}
	
	/**
	 * 加载受管应用信息
	 */
	private void loadApp(){
		SynConfig.loadApp();
	}
	/**
	 * 初始化，并启动定时报告任务
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
	 * 初始化配置
	 */
	private void init() {
		if (status.compareAndSet(-1, 0)) {
			//创建工作目录
			createFolders();
			
			//拷贝初始化配置
			initConf();
			
			//加载受管应用信息
			loadApp();
			
			//若无配置，采用本机默认网卡IP
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
			//初始化Action,启动服务
			initAction();
			
			//初始化，并启动定时报告任务
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
	 * 比较是否已发起了停止应用请求，并设置已请求停止标记
	 * @param appProcessName
	 * @param isstoped 判断是否已标记请求停止（预期）
	 * @param isStopSet 在预期相同时，根据isStopSet设置，true设置应用停止请求标识，或false清除标记
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
		System.out.println("启动是否为代理？(y/n)");
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
						System.out.print("退出？(exit)");
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
	 * 获取数据库连接
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
	 * 管控代理工作更目录
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
	 * 管控代理更新软件包临时目录
	 */
	public File getUpdatTemp() {
		return updatTemp;
	}
	/**
	 * 管控代理软件安装包、插件包下载临时目录
	 */
	public File getDownloadTemp() {
		return downloadTemp;
	}
	/**
	 * 管控代理应用解压安装目录
	 */
	public File getInstallfolder() {
		return installfolder;
	}
	/**
	 * 管控代理插件包存放目录
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
