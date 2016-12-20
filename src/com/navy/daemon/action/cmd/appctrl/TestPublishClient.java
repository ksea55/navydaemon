package com.navy.daemon.action.cmd.appctrl;

import com.navy.daemon.BootStart;
import com.navy.daemon.action.AbstractAction;
import com.navy.daemon.action.report.appreport.AppReportServiceImpl;
import com.navy.daemon.entity.ClusterConfig;

public class TestPublishClient implements Runnable{
	private String testname;
	private static AppContrlService s;
	
	@Override
	public void run() {
		/*
		if(null == s){
			BootStart boot = BootStart.getBoot();
			boot.setIsagent(true);
			boot.start();
			String wsdlLocation= BootStart.getBoot().getAgentPluginWS_Url_Head()+":"+BootStart.getBoot().getMonictrl_port()+"/com.navy.daemon/appctrl/AppContrlService";
			if(1==boot.getIsagent()){
				ActionContext context = new ActionContext();
				context.wsdlLocation = wsdlLocation;
				context.nameSpace = "http://com.navy.daemon/appctrl/";
				context.serviceName="AppContrlService";
				context.portName="AppContrlServicePort";
				context.endpointInterface="com.navy.daemon.action.cmd.appctrl.AppContrlService";
				WSClientFactory.publishClient(context);
			}
			
			System.out.println("test client:"+wsdlLocation);
			try {
				s = (AppContrlService)WSClientFactory.getWsClient(wsdlLocation);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}*/
		
		BootStart.startListen();
		ClusterConfig app = new ClusterConfig();
		app.setFtpPath("/test/");
		app.setFtpFileName("plsqldev.exe.zip");
		app.setProcessName("plsqldev.exe");
		
		app.setAction(ClusterConfig.UPDATE);
    	Object result = null;
    	
    	try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("stopReport...................");
		AbstractAction.stopReport("192.168.3.156", 6553, AppReportServiceImpl.class.getName());
		
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		AbstractAction.startReport("192.168.3.156", 6553, AppReportServiceImpl.class.getName());
		System.out.println("startReport...................");
		/*try {
			result = ActionInvocationHandler.invoke(new URL("http://192.168.3.156:6553/com.navy.daemon/Report/ReportService?stopTheReport"), AppReportServiceImpl.class.getName());
			System.out.println("callAction.................."+result.toString());
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (Throwable e1) {
			e1.printStackTrace();
		}*/
    	//result = s.excuteCMD(conf);
    	/*System.out.println("jws call.................."+result.toString());*/
	}
	
	private TestPublishClient(String testname){
		this.testname = testname;
	}
	
	public static void main(String[] args) {
    	Thread on  = null;
    	for(int i =0 ; i < 1; i++){
    		on = new Thread(new TestPublishClient(""+i));
    		on.start();
    	}
    }
}
