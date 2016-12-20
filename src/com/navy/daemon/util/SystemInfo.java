package com.navy.daemon.util;

import java.io.File;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.navy.daemon.entity.ClusterConfig;
import com.navy.daemon.entity.ProcessInfo;

public class SystemInfo {
	//http://www.bitscn.com/java/tools/200810/152661.html
	private static WinMonitorServiceImpl winm = new WinMonitorServiceImpl();
	
	/**
	 * ����File.separator�жϵ�ǰϵͳ�Ƿ�ΪUNixϵͳ
	 * @return
	 */
	public static boolean isUnix(){
		return "/".equals(File.separator);
	}
	
	/**
	 * ͨ����ѯӦ�õĽ�������Ӧ�Ľ����б��жϵ�ǰӦ���Ƿ���ֹͣ
	 * @param processName
	 * @return
	 */
	public static boolean isStop(String processName){
		List<String> pids = null;
		if(SystemInfo.isUnix()){
			
		}else{
			try {
				pids = WinProcessInfoUtil.getPIDListByPidName(processName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//˵��Ӧ��û�й�
		return !(null != pids && pids.size() > 0);
	}
	
	/**
	 * ��ȡ���̵�cpuռ����/�ڴ桢�ӽ�����Ϣ
	 * @param processName Ӧ����������ϵͳ����ʾ�Ľ�����
	 * @return 
	 */
	public static double getAppProcessInfo(ClusterConfig app, MonitorInfoBean nodeinfo){
		double cpurdio = 0.0;
		List<ProcessInfo> ps = app.getProcessInfoList();
		if(isUnix()){
			System.out.println("getAppProcessInfo is Unix");
		}else{
			System.out.println("getAppProcessInfo is windows");
			cpurdio = winm.getCpuRatio(app.getProcessName());
			System.out.println("getAppProcessInfo cpurdio:"+cpurdio);
			System.out.println("getAppProcessInfo idle0:"+WinMonitorServiceImpl.readCpu_idletime());
			
			try {
				ps = WinMonitorServiceImpl.readProcessNamePidsInfo(app.getProcessName(), app);
				//System.out.println("app.getProcessCountv().get() > 0?"+(app.getProcessCountv().get() > 0));
				//System.out.println("null == ps?"+(null==ps)+" size,"+(null==ps || ps.size() < 1));
				if(null != ps && app.getProcessCountv().get() > 0){
					
					if(null == nodeinfo) nodeinfo = new MonitorInfoBean();
					try {
						nodeinfo = winm.getMonitorInfoBean(nodeinfo);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					if(null != nodeinfo){
						for(ProcessInfo proc : ps){
							proc.memoryUsage = Double.valueOf(
									100 * proc.memoryInuse/nodeinfo.totalMemorySize
			                	);
							
							System.out.println("systeminfo getAppProcessInfo:"+proc.ProcessName+","+proc.ProcessId+","+proc.PProcessId+","
				    				 +proc.cpuUsage+","+proc.memoryInuse/1024+","+proc.VirtualSize/1024+","+proc.memoryUsage);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return cpurdio;
	}
	
	/**
	 * ��ȡ�ܿ�ƽ̨����פ��������ϵͳ��Ϣ
	 * @param nodeSysInfo �ܿش���פ��������ϵͳ��Ϣ
	 * @return
	 */
	public static MonitorInfoBean getNodeSysInfo(MonitorInfoBean nodeSysInfo){
		if(isUnix()){
			System.out.println("is Unix");
		}else{
			try {
				return winm.getMonitorInfoBean(nodeSysInfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return nodeSysInfo;
	}
	
	public static void main(String[] args) throws Exception{
		ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(5);
		ClusterConfig app = new ClusterConfig();
		app.setProcessName("360se.exe");
		//"chrome.exe"
		
		
	}
	
	
}
