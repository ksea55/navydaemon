package com.navy.daemon.action.cmd.confset;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.navy.daemon.action.AbstractAction;

public class ConfSetServiceImpl  extends AbstractAction implements ConfSetService{
	/**
	 * 线程池，用于执行生产者线程
	 */
	protected ExecutorService pool;
	/**
	 * 生产者线程执行器
	 */
	protected ExecutorCompletionService<Object> completionService;
	
	@Override
	public HashMap<String, String> configSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, String> publishPluginServer(String pluginname, String pluginImplclasspath, 
			String jarFtpPath, int option, String wsdlLocation, 
			String report_wsdlLocation, String report_method) {
		
		HashMap<String, String> oack = new HashMap<String, String>();
		oack.put("code", "200");
		oack.put("msg", "recived request");
		try{
			completionService.submit(new PluginUpdate(this.getContext(), pluginname,
					pluginImplclasspath, jarFtpPath, option, wsdlLocation, 
					report_wsdlLocation, report_method));
		}catch(Exception e){
			e.printStackTrace();
			oack.put("code", "500");
			oack.put("msg", e.getMessage());
			
		}
		return oack;
	}
	
	@Override
	public HashMap<String, String> publishPluginClient(String wsdlLocation, String nameSpace,
			String pluginName, String portName, String serviceName, String endpointInterface,
			 String jarFtpPath, int option) {
		
		HashMap<String, String> oack = new HashMap<String, String>();
		oack.put("code", "200");
		oack.put("msg", "recived request");
		try{
			//启动处理线程
			completionService.submit(new PluginUpdatClient(this.getContext(), wsdlLocation, nameSpace,
					pluginName, portName, serviceName, endpointInterface, jarFtpPath, option));
		}catch(Exception e){
			e.printStackTrace();
			oack.put("code", "500");
			oack.put("msg", e.getMessage());
			
		}
		return oack;
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
		completionService = new ExecutorCompletionService(pool);
	}

	@Override
	public void close() {
		pool.shutdown();
	}

	
}
