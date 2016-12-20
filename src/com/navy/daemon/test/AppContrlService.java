package com.navy.daemon.test;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.navy.daemon.entity.ClusterConfig;

@WebService(targetNamespace="http://com.navy.daemon/app/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface AppContrlService {

	/**
	 * 接收mes数据通知
	 * @param app 应用信息及应用控制命令
	 */
	@WebMethod
	@WebResult(name = "oack")
	public String excuteCMD(@WebParam(name = "app")ClusterConfig app);
}
