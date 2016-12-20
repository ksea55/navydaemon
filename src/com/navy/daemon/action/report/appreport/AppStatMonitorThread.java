package com.navy.daemon.action.report.appreport;


import java.util.Date;

import com.navy.daemon.BootStart;
import com.navy.daemon.action.ActionContext;
import com.navy.daemon.action.cmd.appctrl.APPCtrlThread;
import com.navy.daemon.entity.ClusterConfig;
import com.navy.daemon.util.MonitorInfoBean;
import com.navy.daemon.util.SystemInfo;
/**
 * Ӧ���������̣߳����ĳ��Ӧ�õ�cpuռ�á�������Ϣ�������ڴ�ռ��
 *<li> һ���̼߳��һ��Ӧ�����</li>
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
		//���½�����Ϣ
		app.getProcessCountv().set(0);
		SystemInfo.getAppProcessInfo(app, nodeinfo);
		app.setProcessCount(app.getProcessCountv().get());
		serviceContext.report(app);
		//System.out.println("AppStatMonitorThread getProcessCount:"+app.getProcessCount());
		//û�ж�Ӧ���̣��������쳣��ֹ��
		if(app.getProcessCountv().get() < 1){
			System.out.println(app.getProcessName()+" is stoped ....");
			//�жϵ�ǰӦ����ֹ�Ƿ����û���������£���ͨ���ж��Ƿ�������ֹͣ�ı�ʶ��ȷ��
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
