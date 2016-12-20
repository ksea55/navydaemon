package com.navy.daemon.action.cmd.appctrl;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.navy.daemon.action.AbstractAction;
import com.navy.daemon.entity.ClusterConfig;
import com.navy.daemon.entity.ProcessInfo;

 
@WebService(endpointInterface = "com.navy.daemon.action.cmd.appctrl.AppContrlService",
		targetNamespace="http://com.navy.daemon/appctrl/",
		portName="AppContrlServicePort",
		serviceName="AppContrlService"
)
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class AppContrlServiceImpl extends AbstractAction implements AppContrlService{
	/**
	 * 线程池，用于执行生产者线程
	 */
	protected ExecutorService pool;
	/**
	 * 生产者线程执行器
	 */
	protected ExecutorCompletionService<HashMap<String, String>> completionService;
	
	@Override
	public String excuteCMD(ClusterConfig app) {
		System.out.println("excuteCMD:"+app.getProcessName()+":"+app.getAction());
		if(null == app.getAction() || null == app.getProcessName()
			|| null == app.getFtpFileName() || null == app.getFtpPath() 
		){
			return "fail:the param (Action, ProcessName, FtpFileName, FtpPath) needed";
		}
		
		String result = "oack";
		try{
			completionService.submit(new APPCtrlThread(this.context, app));
		}catch(Exception e){
			result="fail start cmand:"+e.getMessage();
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public String testCmandReport(ClusterConfig app) {
		if(null == app){
			System.out.println("app info is null");
		}
		System.out.println(app.getProcessName()+","+app.getActionPath()+","+app.getCode()+","+app.getMes());
		List<ProcessInfo> ps = app.getProcessInfoList();
		if(null != ps && ps.size() > 0){
			for(ProcessInfo proc : ps){
				System.out.println(proc.ProcessName+","+proc.ProcessId+","+proc.PProcessId+","
	    				 +proc.cpuUsage+","+proc.memoryInuse/1024+","+proc.VirtualSize/1024+","+proc.memoryUsage);
			}
		}
		
		return "oack";
	}

	@Override
	public void destroy() {
		for(int i = 0; i < 2; i++){
			try {
				completionService.take().get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		try { pool.shutdownNow(); }catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init() {
		pool = Executors.newFixedThreadPool(2);
		completionService = new ExecutorCompletionService<HashMap<String, String>>(pool);
	}

	@Override
	public void close() {
		pool.shutdown();
	}

}