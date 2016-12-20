package com.navy.daemon.action.report.nodereport;

import java.util.ArrayList;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.navy.daemon.BootStart;
import com.navy.daemon.action.report.AbstarctReporter;
import com.navy.daemon.util.MonitorInfoBean;

@WebService(endpointInterface = "com.navy.daemon.action.report.nodereport.NodeReportService",
		targetNamespace="http://com.navy.daemon/nodeReport/",
		portName="NodeReportServicePort",
		serviceName="NodeReportService"
)
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class NodeReportServiceImpl extends AbstarctReporter implements NodeReportService  {
	
	private static MonitorInfoBean nodeSysInfo = BootStart.getBoot().nodeSysInfo;
	private static ArrayList<NodeStatMonitorThread> nodeths;
	
	@Override
	public MonitorInfoBean getNodeStat() {
		System.out.println("get NODEStatReport request");
		try{
			new NodeStatMonitorThread(this.getContext(), nodeSysInfo).run();
		}catch(Exception e){
			e.printStackTrace();
		}
		return nodeSysInfo;
	}
	
	@Override
	public ArrayList<NodeStatMonitorThread> getTasks() {
		if(null == nodeths) {
			nodeths = new ArrayList<NodeStatMonitorThread>();
			nodeths.add(new NodeStatMonitorThread(this.getContext(), nodeSysInfo));
		}
		return nodeths;
	}
	
}
