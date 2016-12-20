package com.navy.daemon.test;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import com.navy.daemon.entity.ClusterConfig;

public class TestWS {
	private static String IP = "192.168.3.151";
	private static int port = 6552;

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		/*try {
			URL wsdlUrl = new URL("http://"+IP+":"+port+"/com.navy.daemon/app/AppContrlService?wsdl");
	        Service s = Service.create(wsdlUrl, new QName("http://com.navy.daemon/app/","AppContrlServiceImplService"));
	        AppContrlService appc = s.getPort(new QName("http://com.navy.daemon/app/","AppContrlServiceImplPort"), AppContrlService.class);
	        ClusterConfig app = new ClusterConfig();
	        app.setProcessName("test");
	        app.setAction("install");
	        appc.excuteCMD(app);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		URL wsdlUrl = new URL("http://"+IP+":"+port+"/com.navy.daemon/app/AppContrlService!getd");
		System.out.println(wsdlUrl.toString());
		String urlstr = wsdlUrl.toString();
		String methodname = null;
		String wsdl = null;
		int methodindx = urlstr.lastIndexOf("!");
		if( methodindx > -1){
			wsdl = urlstr.substring(0, methodindx);
			methodname = urlstr.substring(methodindx+1, urlstr.length());
		}
		System.out.println(methodindx);
		System.out.println(wsdl);
		System.out.println(methodname);
		
	}

}
