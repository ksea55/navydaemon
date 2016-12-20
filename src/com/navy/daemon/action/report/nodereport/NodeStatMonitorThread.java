package com.navy.daemon.action.report.nodereport;


import java.util.Date;

import com.navy.daemon.action.ActionContext;
import com.navy.daemon.util.MonitorInfoBean;
import com.navy.daemon.util.SystemInfo;
/**
 * 应用管控代理驻留服务器的cpu占用、进程信息、进程内存占用
 *<li> 可采用多个不同的线程监控同一个服务器的不同信息</li>
 * @author mup
 */
public class NodeStatMonitorThread implements Runnable{
	private MonitorInfoBean nodeSysInfo;
	
	private ActionContext serviceContext;
	
	public NodeStatMonitorThread(ActionContext serviceContext,MonitorInfoBean nodeSysInfo){
		this.serviceContext = serviceContext;
		this.nodeSysInfo = nodeSysInfo;
	}
	
	@Override
	public void run(){
		Date d = new Date();
		System.out.println("start..........."+d.getTime());
		if(null == nodeSysInfo) return;
		//更新进程信息
		SystemInfo.getNodeSysInfo(nodeSysInfo);
		
		serviceContext.report(nodeSysInfo);
		System.out.println("end..........."+d.getTime());
	}
	
}
