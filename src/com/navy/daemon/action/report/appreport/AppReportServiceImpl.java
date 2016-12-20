package com.navy.daemon.action.report.appreport;

import java.util.ArrayList;
import java.util.Iterator;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.navy.daemon.BootStart;
import com.navy.daemon.action.report.AbstarctReporter;
import com.navy.daemon.conf.SynConfig;
import com.navy.daemon.entity.ClusterConfig;


@WebService(endpointInterface = "com.navy.daemon.action.report.appreport.AppReportService",
		targetNamespace="http://com.navy.daemon/appReport/",
		portName="AppReportServicePort",
		serviceName="AppReportService"
)
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class AppReportServiceImpl extends AbstarctReporter implements AppReportService {
	
	/**
	 * app状态监控线程对象
	 */
	private static ArrayList<AppStatMonitorThread> appsThreads = new ArrayList<AppStatMonitorThread>();
	/**
	 * 已创建 app状态监控线程的应用
	 */
	private static ArrayList<ClusterConfig> appStarted = new ArrayList<ClusterConfig>();
	
	@Override
	public ClusterConfig getAppStat(String appName) {
		System.out.println("appStatReport:"+appName);
		ClusterConfig app = null;
		try{
			app = SynConfig.apps.get(appName);
			new AppStatMonitorThread(this.getContext(), 
					app, 
					BootStart.getBoot().nodeSysInfo).run();
		}catch(Exception e){
			e.printStackTrace();
		}
		return app;
	}

	@Override
	public ArrayList<AppStatMonitorThread> getTasks() {
		try{
			Iterator<ClusterConfig> apps = SynConfig.apps.values().iterator();
			ClusterConfig app = null;
			while(apps.hasNext()){
				app = apps.next();
				if(appStarted.contains(app)){
					continue;
				}else{
					AppStatMonitorThread appt = new AppStatMonitorThread(
							this.getContext(), 
							app, 
							BootStart.getBoot().nodeSysInfo);
					appsThreads.add(appt);
					appStarted.add(app);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
		}
		return appsThreads;
	}
	
	public static void main(String[] args){
		try {
			AppReportServiceImpl m = new AppReportServiceImpl();
			ClusterConfig app = new ClusterConfig();
			app.setFtpPath("/test/");
			app.setFtpFileName("plsqldev.exe2.zip");
			app.setProcessName("plsqldev.exe");
			app.setAppName("plsqldev.exe");
			
			SynConfig.apps.put(app.getProcessName(), app);
			
			m.startRuner();
			System.out.println("start1..........");
			
			Thread.sleep(20000);
			
			m.stop();
			System.out.println("stop1..........");
			
			
			m.startRuner();
			System.out.println("start..........");
			
			Thread.sleep(100000);
			m.stop();
			System.out.println("stop....");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
