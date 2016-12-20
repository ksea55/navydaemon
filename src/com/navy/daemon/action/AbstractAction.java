package com.navy.daemon.action;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import com.navy.daemon.action.report.IBaseRepoter;


/**
 * �¼���������,����ͳһ������ʽ�����ע����Ϣ
 * <p>
 * <li>�ṩ�¼������������url���÷������callAction</li>
 * <li>�ṩ�����������ӿڻ�ȡ����getService</li>
 * <li>�ṩ��ǰ���������Ϣ��ѯ�ӿ�ActionContext</li>
 * </p>
 * @author mup
 *
 */
public abstract class AbstractAction extends JWS implements IAction{
	/**
	 * ����������������
	 */
	protected ActionContext context;
	
	/**
	 * ��ȡ����ʵ��
	 * @param namespace
	 * @return ����ʵ����������ӿڷ�ʽ����
	 */
	public static final <T extends IAction> T getService(URL wsdlurl){
		T action = null;
		try {
			action = WSClientFactory.<T>getWsClient(wsdlurl.toString());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return action;
	}
	
	/**
	 * �ͷſͻ��˶���
	 */
	public final void closeClient(){
		WSClientFactory.close(this);
	}
	
	/**
	 * �رշ���
	 */
	public final void closeServer(){
		WSServerFactory.destroy(this.getWsdlLocation());
	}
	
	/**
	 * 
	 * @param <T>
	 * @param request ����url����ʽhttp://IP+PORT/��Ŀ����/ģ����/ģ���������!������
	 * @param param 
	 * @return
	 */
	public static final <T> T callAction(URL request, Object...param) throws IllegalArgumentException{
		T result = null;
		request.toString();
		String urlstr = request.toString();
		String methodname = null;
		String wsdl = null;
		int methodindx = urlstr.lastIndexOf("!");
		if( methodindx > -1 && methodindx < urlstr.length()-1){
			wsdl = urlstr.substring(0, methodindx)+"?wsdl";
			methodname = urlstr.substring(methodindx+1, urlstr.length());
			//����jws�ͻ���Զ�̵���
			try {
				IAction action = WSClientFactory.getWsClient(wsdl);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}else{
			throw new IllegalArgumentException("request url not found the request method, the url must match to 'http://IP+PORT/��Ŀ����/ģ����/ģ���������!������?����'");
		}
		System.out.println(wsdl);
		System.out.println(methodname);
		return result;
	}
	/**
	 * �û�����ѡ��Ϊ�ò���������ע�ᱨ��
	 */
	public void addActionListener(Class<IReporter> reporter_clz){
		
	}
	/**
	 * ��ȡ�������
	 * @return
	 */
	public IReporter getIReporter(){
		return context.getReporter();
	}
	/**
	 * ״̬�ϱ�
	 * ����ǰ����ע����reportʱ�÷���������Ч
	 * @param reports �������ݶ���
	 */
	public void report(Object...reports){
		context.report(reports);
	}

	public ActionContext getContext() {
		return context;
	}
	
	/**
	 * ���ñ���������ʽ
	 * ���ö�Ӧ���涨ʱִ�й��򣬸ù���ֻ�ڹܿش��������ڼ���Ч���ܿش������������ָ�ΪĬ�Ϲ���
	 * @param ip �������ڷ�����ip
	 * @param port �������ڷ����������˿�
	 * @param reporterId ������񷢲��ӿ�ʵ���ࣨͬclass.getName()��
	 * @param initialDelay ��ʼ��ʱʱ��
	 * @param period ��ʱִ�м��ʱ��
	 * @param unit TimeUnit����ʱ�䵥λ
	 * @return
	 */
	public static boolean resetReport(String ip, int port, String reporterId, 
			long initialDelay, long period, TimeUnit unit){
		
		boolean flag = false;
		
		IBaseRepoter repotermanage = getreportmanage_server(ip, port);
		if(null == repotermanage) return false;
		
		try{
			//�ȹرձ��涨ʱ��
			flag = repotermanage.stopTheReport(reporterId);
			//���ñ��涨ʱ���й���
			if(flag) repotermanage.setQuartzCron(reporterId, initialDelay, period, unit);
			//�������涨ʱ��
			if(flag) flag = repotermanage.startTheRuner(reporterId);
		}catch(Exception e){
			e.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * ����ָ���ı���
	 * @param ip �������ڷ�����ip
	 * @param port �������ڷ����������˿�
	 * @param reporterId ������񷢲��ӿ�ʵ���ࣨͬclass.getName()��
	 * @return
	 */
	public static boolean startReport(String ip, int port, String reporterId){
		boolean flag = false;
		
		IBaseRepoter repotermanage = getreportmanage_server(ip, port);
		if(null == repotermanage) return false;
		
		try{
			flag = repotermanage.startTheRuner(reporterId);
		}catch(Exception e){
			e.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * ָֹͣ���ı���
	 * @param ip �������ڷ�����ip
	 * @param port �������ڷ����������˿�
	 * @param reporterId ������񷢲��ӿ�ʵ���ࣨͬclass.getName()��
	 * @return
	 */
	public static boolean stopReport(String ip, int port, String reporterId){
		boolean flag = false;
		
		IBaseRepoter repotermanage = getreportmanage_server(ip, port);
		if(null == repotermanage) return false;
		
		try{
			flag = repotermanage.stopTheReport(reporterId);
		}catch(Exception e){
			e.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * ��ȡ�������������
	 * @param ip �������ڷ�����ip
	 * @param port �������ڷ����������˿�
	 * @return
	 */
	public static IBaseRepoter getreportmanage_server(String ip, int port){
		IBaseRepoter repotermanage = null;
		
		StringBuffer reportmanage_server = new StringBuffer("http://");
		reportmanage_server.append(ip).append(":");
		if(port <= 0){
			reportmanage_server.append("6553");
		}else{
			reportmanage_server.append(port);
		}
		reportmanage_server.append("/com.navy.daemon/Report/ReportService");
		System.out.println("reportmanage_server:"+reportmanage_server);
		try{
			repotermanage = WSClientFactory.<IBaseRepoter>getWsClient(reportmanage_server.toString() );
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return repotermanage;
	}
	
	public static boolean getRemoteConfSetService(boolean isPublish2Agent){
		return false;
	}
}
