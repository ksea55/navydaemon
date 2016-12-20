package com.navy.daemon.action.report;

import java.util.concurrent.TimeUnit;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.navy.daemon.action.IReporter;

/**
 * 报告（或定时器）服务管理接口
 * @author mup
 *
 */
@WebService(endpointInterface = "com.navy.daemon.action.report.IBaseRepoter",
		targetNamespace="http://com.navy.daemon/Report/",
		portName="ReportServicePort",
		serviceName="ReportService"
)
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface IBaseRepoter extends IReporter {
	 /**
	  * 上报停止方法
	  * @param reportId 报告唯一id（默认是报告类的类全路径）
	  * @return
	  */
	@WebMethod(operationName="stopTheReport")
	@WebResult(name = "isstop")
	public boolean stopTheReport(String reportId);
	
	/**
	  * 上报启动方法
	  * @param reportId 报告唯一id（默认是报告类的类全路径）
	  * @return
	  */
	@WebMethod(operationName="startTheRuner")
	@WebResult(name = "isstart")
	public boolean startTheRuner(String reportId);
	
	/**
	 * 为某个报告设置定时运行规则（需重启定时器后生效）
	 * @param reporterId 报告唯一id（默认是报告类的类全路径）
	 * @param initialDelay 初始延时时间
	 * @param period 定时执行间隔时间
	 * @param unit TimeUnit类型时间单位
	 */
	@WebMethod(operationName="setQuartzCron")
	public void setQuartzCron(String reporterId, long initialDelay, long period, TimeUnit unit);
}
