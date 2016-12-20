package com.navy.daemon.action.report;

import java.util.concurrent.TimeUnit;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.navy.daemon.action.IReporter;

/**
 * ���棨��ʱ�����������ӿ�
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
	  * �ϱ�ֹͣ����
	  * @param reportId ����Ψһid��Ĭ���Ǳ��������ȫ·����
	  * @return
	  */
	@WebMethod(operationName="stopTheReport")
	@WebResult(name = "isstop")
	public boolean stopTheReport(String reportId);
	
	/**
	  * �ϱ���������
	  * @param reportId ����Ψһid��Ĭ���Ǳ��������ȫ·����
	  * @return
	  */
	@WebMethod(operationName="startTheRuner")
	@WebResult(name = "isstart")
	public boolean startTheRuner(String reportId);
	
	/**
	 * Ϊĳ���������ö�ʱ���й�����������ʱ������Ч��
	 * @param reporterId ����Ψһid��Ĭ���Ǳ��������ȫ·����
	 * @param initialDelay ��ʼ��ʱʱ��
	 * @param period ��ʱִ�м��ʱ��
	 * @param unit TimeUnit����ʱ�䵥λ
	 */
	@WebMethod(operationName="setQuartzCron")
	public void setQuartzCron(String reporterId, long initialDelay, long period, TimeUnit unit);
}
