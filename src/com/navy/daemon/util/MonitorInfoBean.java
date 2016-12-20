package com.navy.daemon.util;

import java.util.List;

/**
 * ������Ϣ��JavaBean��. 
 * @author  mup
 */  
public class MonitorInfoBean {  
    /**��ʹ���ڴ�*/
    public long totalMemory;
      
    /**ʣ���ڴ�*/
    public long freeMemory;
      
    /**����ʹ���ڴ�*/
    public long maxMemory;
      
    /**����ϵͳ*/
    public String osName;
      
    /**
     * �ܵ������ڴ�
     */  
    public long totalMemorySize;
      
    /**
     * ʣ��������ڴ�
     **/  
    public long freePhysicalMemorySize;
      
    /** 
     * ��ʹ�õ������ڴ�
     */
    public long usedMemory;
      
    /**�߳�����*/
    public int totalThread;
      
    /**cpuʹ����*/
    public double cpuRatio;
    
    /**
     * פ������ڵ�IP
     */
    public String IP;
    
    /**
     * פ������������
     */
    public String hostName;
    /**
     * cpu��
     */
    public int avliaProcessCount;
    /**
     * �����ܴ�С
     */
    public double diskTotalSize;
    /**
     * ���̿��ÿռ�
     */
    public double diskTotalFreeSize;
    /**
     * ������
     */
    public int avliaLogicDiskCount;
    /**
     * ������Ϣ
     */
    public List<LogicDiskInfo> logicDiskInfos;
} 