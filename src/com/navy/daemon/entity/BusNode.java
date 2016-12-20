package com.navy.daemon.entity;
/**
 * @author ������
 * @date 2016/10/14 0014
 * <p/>
 * �������߽ڵ�
 */
public class BusNode {
    private String id;
    /**
     * ��Ⱥ�ڵ�
     */
    private ClusterNode clusterNode;
    /**
     * IP
     */
    private String ip;
    /**
     *�˿�
     */
    private String port;
    /**
     *����װ·��
     */
    private String serverPath;
    /**
     *״̬
     */
    private String status;
    /**
     * ���̺�
     */
    private String process;
    /**
     *���ط������
     */
    private Integer serverAppCount;
    public BusNode() {
    }
    public BusNode(String id) {
        this.id = id;
    }

    public String getId() {
        return createId(this.ip,this.port);
    }

    public ClusterNode getClusterNode() {
        return clusterNode;
    }
    public String getIp() {
        return ip;
    }
    public String getPort() {
        return port;
    }

    public String getServerPath() {
        return serverPath;
    }

    public String getStatus() {
        return status;
    }

    public String getProcess() {
        return process;
    }
    public Integer getServerAppCount() {
        return serverAppCount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setClusterNode(ClusterNode clusterNode) {
        this.clusterNode = clusterNode;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public void setServerAppCount(Integer serverAppCount) {
        this.serverAppCount = serverAppCount;
    }


    public static String createId(String ip,String port)  {
        return ip + "@" + port;
    }

}
