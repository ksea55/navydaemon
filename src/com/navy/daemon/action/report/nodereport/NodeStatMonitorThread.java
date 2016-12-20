package com.navy.daemon.action.report.nodereport;


import java.util.Date;

import com.navy.daemon.action.ActionContext;
import com.navy.daemon.util.MonitorInfoBean;
import com.navy.daemon.util.SystemInfo;
/**
 * Ӧ�ùܿش���פ����������cpuռ�á�������Ϣ�������ڴ�ռ��
 *<li> �ɲ��ö����ͬ���̼߳��ͬһ���������Ĳ�ͬ��Ϣ</li>
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
		//���½�����Ϣ
		SystemInfo.getNodeSysInfo(nodeSysInfo);
		
		serviceContext.report(nodeSysInfo);
		System.out.println("end..........."+d.getTime());
	}
	
}
