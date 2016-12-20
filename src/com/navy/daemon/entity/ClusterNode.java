package com.navy.daemon.entity;



/**
 * ��Ⱥ�ڵ�
 */

public class ClusterNode {
    /**
     * ID
     */
    private String id;


    /**
     * CPU����
     */
    private String totalCpuNum;

    /**
     * Ӳ���ܼ�
     */
    private Double totalHardDiskInfo;

    /**
     * �ڴ��ܼ�
     */
    private Double totalMemoryInfo;


    /**
     * �ڵ�IP
     */
    private String ip;
    /**
     * �ڵ���
     */
    private String nodeName;
    /**
     * cpuʹ����
     */
    private Double cpuUsage;
    /**
     * ʹ�����ڴ�
     */
    private Double memoryInuse;

    /**
     * ʹ����Ӳ��
     */
    private Double hardDiskInuse;
    ;
    /**
     * �ڴ�ʹ����
     */
    private Double memoryUsage;
    /**
     * �ڵ�״̬
     */
    private String nodeState;
    /**
     * �Ƿ�װ��������
     * 0:δ��װ
     * 1:�Ѱ�װ
     */
    private int busIn;
    /**
     * �Ƿ�װ��������
     * 0:δ��װ
     * 1:�Ѱ�װ
     */
    private int dataIn;


    /**
     * �ڵ��·���������
     */
    private int busNum;

    /**
     * �ڵ�������������
     */
    private int dataNum;


    public ClusterNode() {

    }

    public ClusterNode(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getMemoryInuse() {
        return totalMemoryInfo*memoryUsage/100;
    }

    public void setMemoryInuse(Double memoryInuse) {
        this.memoryInuse = memoryInuse;
    }

    public String getIp() {
        return ip;
    }


    public String getNodeName() {
        return nodeName;
    }



    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }
    public String getTotalCpuNum() {
        return totalCpuNum;
    }

    public Double getCpuUsage() {
        return cpuUsage;
    }

    public Double getHardDiskInuse() {
        return hardDiskInuse;
    }

    public String getNodeState() {
        return nodeState;
    }

    public int getBusNum() {
        return busNum;
    }

    public Double getTotalHardDiskInfo() {
        return totalHardDiskInfo;
    }

    public Double getTotalMemoryInfo() {
        return totalMemoryInfo;
    }
    public int getBusIn() {
        return busIn;
    }

    public void setBusIn(int busIn) {
        this.busIn = busIn;
    }
    public int getDataIn() {
        return dataIn;
    }

    public void setDataIn(int dataIn) {
        this.dataIn = dataIn;
    }
    
    public int getDataNum() {
        return dataNum;
    }

    public void setDataNum(int dataNum) {
        this.dataNum = dataNum;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }



    public void setNodeState(String nodeState) {
       this.nodeState = nodeState;
    }

    public void setBusNum(int busNum) {
        this.busNum = busNum;
    }


    public void setTotalHardDiskInfo(Double totalHardDiskInfo) {
        this.totalHardDiskInfo = totalHardDiskInfo;
    }

    public void setTotalMemoryInfo(Double totalMemoryInfo) {
        this.totalMemoryInfo = totalMemoryInfo;
    }

    public void setTotalCpuNum(String totalCpuNum) {
        this.totalCpuNum = totalCpuNum;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }
    public void setHardDiskInuse(Double hardDiskInuse) {
        this.hardDiskInuse = hardDiskInuse;
    }
}
