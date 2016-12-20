package com.navy.daemon.action.report.appreport;


import java.util.Date;

import com.navy.daemon.BootStart;
import com.navy.daemon.action.ActionContext;
import com.navy.daemon.action.cmd.appctrl.APPCtrlThread;
import com.navy.daemon.entity.ClusterConfig;
import com.navy.daemon.util.MonitorInfoBean;
import com.navy.daemon.util.SystemInfo;
/**
 * 应用软件监控线程，监控某个应用的cpu占用、进程信息、进程内存占用
 *<li> 一个线程监控一个应用软件</li>
 * @author mup
 */
public class AppStatMonitorThread implements Runnable{
	public ClusterConfig app;
	private ActionContext serviceContext;
	private MonitorInfoBean nodeinfo;
	private APPCtrlThread appctrl;
	public AppStatMonitorThread(ActionContext serviceContext, ClusterConfig app, MonitorInfoBean nodeinfo){
		this.serviceContext = serviceContext;
		this.app = app;
		this.nodeinfo = nodeinfo;
		appctrl = new APPCtrlThread(null, app);
	}
	
	@Override
	public void run(){
		Date d = new Date();
		System.out.println("AppStatMonitorThread start..........."+d.getTime());
		if(null == app) return;
		//更新进程信息
		app.getProcessCountv().set(0);
		SystemInfo.getAppProcessInfo(app, nodeinfo);
		app.setProcessCount(app.getProcessCountv().get());
		serviceContext.report(app);
		//System.out.println("AppStatMonitorThread getProcessCount:"+app.getProcessCount());
		//没有对应进程，可能是异常终止了
		if(app.getProcessCountv().get() < 1){
			System.out.println(app.getProcessName()+" is stoped ....");
			//判断当前应用终止是否是用户相关请求导致，即通过判断是否有请求停止的标识来确定
			if(BootStart.checkStop(app.getProcessName(), false, false)){
				System.out.println(app.getProcessName()+" is duamp ....");
				app.setAction(ClusterConfig.START);
				try {
					appctrl.call();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("AppStatMonitorThread end..........."+d.getTime());
	}
	
}
