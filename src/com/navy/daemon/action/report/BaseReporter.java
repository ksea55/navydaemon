package com.navy.daemon.action.report;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.navy.daemon.action.report.AbstarctReporter;
import com.navy.daemon.action.report.IBaseRepoter;

/**
 * ���棨��ʱ�����������й�����
 * @author mup
 *
 */
@WebService(endpointInterface = "com.navy.daemon.action.report.IBaseRepoter",
		targetNamespace="http://com.navy.daemon/Report/",
		portName="ReportServicePort",
		serviceName="ReportService"
)
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class BaseReporter extends AbstarctReporter implements IBaseRepoter{
	
	@Override
	public boolean stopTheReport(String reportId) {
		return AbstarctReporter.stopTheReport_0(reportId);
	}
	
	@Override
	public boolean startTheRuner(String reportId) {
		return AbstarctReporter.startTheRuner_0(reportId);
	}

	@Override
	public void setQuartzCron(String reporterId, long initialDelay,
			long period, TimeUnit unit) {
		AbstarctReporter.setQuartzCron_0(reporterId, initialDelay, period, unit);
	}
	
	/**
	 * δʵ��ֱ�ӷ���null����Ӧͨ���˻�ȡ��ͨ������ı�������ȡ
	 */
	@Deprecated
	@Override
	public ArrayList<? extends Runnable> getTasks() {
		return null;
	}
	
}
