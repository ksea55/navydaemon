package com.navy.daemon.util;
/** *//** 
 * ��ȡϵͳ��Ϣ��ҵ���߼���ӿ�. 
 * @author mupan 
 */  
public interface IMonitorService {  
    /** *//** 
     * ��õ�ǰ�ļ�ض���. 
     * @param infoBean ������״̬��Ϣ
     * ������Ϊnull�����ӿ�ʵ�ַ������ᴴ���¶���ֱ��ͨ����������״ֵ̬������
     * @return ���ع���õļ�ض��� 
     * @throws Exception 
     * @author amgkaka 
     * Creation date: 2008-4-25 - ����10:45:08 
     */  
    public MonitorInfoBean getMonitorInfoBean(MonitorInfoBean infoBean) throws Exception;  
  
}  