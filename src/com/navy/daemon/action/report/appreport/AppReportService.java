package com.navy.daemon.action.report.appreport;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.navy.daemon.action.IReporter;
import com.navy.daemon.entity.ClusterConfig;
/**
 * 受管应用软件状态采集、上报
 * @author mup
 *
 */
@WebService(endpointInterface = "com.navy.daemon.action.report.appreport.AppReportService",
		targetNamespace="http://com.navy.daemon/appReport/",
		portName="AppReportServicePort",
		serviceName="AppReportService"
)
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface AppReportService extends IReporter{
	/**
	 * 接口服务发布路径
	 */
	public final String wsdl = "http://IP:6553/com.navy.daemon/appReport/AppReportService";
	/**
	 * 查询app状态
	 * @param appName 应用名称 
	 * @return 应用信息(进程，应用状态)
	 */
	@WebMethod(operationName="getAppStat")
	@WebResult(name = "appStat")
	public ClusterConfig getAppStat(String appName);
}
