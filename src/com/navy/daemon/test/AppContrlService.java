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
	 * ����mes����֪ͨ
	 * @param app Ӧ����Ϣ��Ӧ�ÿ�������
	 */
	@WebMethod
	@WebResult(name = "oack")
	public String excuteCMD(@WebParam(name = "app")ClusterConfig app);
}
