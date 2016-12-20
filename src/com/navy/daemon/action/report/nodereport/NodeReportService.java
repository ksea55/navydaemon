package com.navy.daemon.action.report.nodereport;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.navy.daemon.action.IReporter;
import com.navy.daemon.util.MonitorInfoBean;
@WebService(endpointInterface = "com.navy.daemon.action.report.nodereport.NodeReportService",
		targetNamespace="http://com.navy.daemon/nodeReport/",
		portName="NodeReportServicePort",
		serviceName="NodeReportService"
)
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface NodeReportService extends IReporter{
	/**
	 * 查询应用管控代理驻留服务器的状态
	 * @return 
	 */
	@WebMethod(operationName="getNodeStat")
	@WebResult(name = "NodeStat")
	public MonitorInfoBean getNodeStat();
}
