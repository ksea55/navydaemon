package com.navy.daemon.action.cmd.appctrl;



import com.navy.daemon.entity.ClusterConfig;
import com.navy.daemon.excuter.TaskListener;
/**
 * ���ùܿ�ƽ̨����webservice �ϱ����Ĺ���������ƵĽ���
 * @author mup
 *
 */
public class ContrlStat extends TaskListener<ClusterConfig>{
	@Override
	public void doListen(Object data) {
		ClusterConfig appinfo = null != data 
		&& data instanceof ClusterConfig ? (ClusterConfig) data : null;
		//���÷����webservice
		System.out.println("call center ws.....");
	}

	@Override
	public void setParams(Object... params) {
		
	}
}
