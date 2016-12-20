package com.navy.daemon.entity;


import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * �ڵ��ļ���
 */
public class ClusterConfig {
    /**
     * ���²���
     */
    public final  static String UPDATE="update";
    /**
     * ��װ����
     */
    public final  static String INSTALL="install";
    /**
     * ж�ز���
     */
    public final  static String UNINSTALL ="uninstall";
    /**
     * ��������
     */
    public final  static String START="start";
    /**
     * ֹͣ����
     */
    public final  static String STOP="stop";
    /**
     * �������
     */
    private String appName;
    /**
     * ����ID
     */
    private String id;
    /**
     * Ӧ������ip
     */
    private String ip;
    /**
     * �˿�
     */
    private String port;
    /**
     * ���̺�
     */
    private Integer processNo;
    /**
     * ������
     */
    private String processName;
    /**
     * �������
     */
    private String  type;
    /**
     * �ļ���ftp�ϵ�·��
     */
    private String  ftpPath;
    /**
     * ftp�ϵ��ļ���
     */
    private String ftpFileName;
    /**
     * ִ�еĲ���(ȡֵ���ޱ��ඨ��Ĳ�������)
     */
    private String action;
    /**
     * ����ʱ��
     */
    private Date  actionDate;
    /**
     * ִ���ļ���·��
     */
    private String actionPath;
    /**
     * �����
     * 0:ʧ��
     * 1:�ɹ�
     */
    private Integer code;
    /**
     * ������Ϣ
     */
    private List<ProcessInfo> processInfoList;
    /**
     * ��ǰȡ���Ľ�����
     */
    private volatile int processCount;
    
    private AtomicInteger processCountv = new AtomicInteger(1);
    
    /**
     * �����Ϣ
     */
    private String mes;
    /**
     * ����ID
     */
    public String getId() {
        return id;
    }
    /**
     * ftp�ϵ��ļ���
     */
    public String getFtpFileName() {
        return ftpFileName;
    }
    /**
     * ���̺�
     */
    public void setProcessNo(Integer processNo) {
        this.processNo = processNo;
    }
    /**
     * �����
     * 0:ʧ��
     * 1:�ɹ�
     */
    public Integer getCode() {
        return code;
    }
    /**
     * �����
     * 0:ʧ��
     * 1:�ɹ�
     */
    public void setCode(Integer code) {
        this.code = code;
    }
    /**
     * �����Ϣ
     */
    public String getMes() {
        return mes;
    }
    /**
     * �����Ϣ
     */
    public void setMes(String mes) {
        this.mes = mes;
    }
    /**
     * ftp�ϵ��ļ���
     */
    public void setFtpFileName(String ftpFileName) {
        this.ftpFileName = ftpFileName;
    }
    /**
     * ִ���ļ���·��
     */
    public String getActionPath() {
        return actionPath;
    }
    /**
     * ������
     */
    public String getProcessName() {
        return processName;
    }
    /**
     * ������
     */
    public void setProcessName(String processName) {
        this.processName = processName;
    }
    /**
     * ִ���ļ���·��
     */
    public void setActionPath(String actionPath) {
        this.actionPath = actionPath;
    }
    /**
     * ����ID
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * ����ʱ��
     */
    public Date getActionDate() {
        return actionDate;
    }
    /**
     * ����ʱ��
     */
    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }
    /**
     * ִ�еĲ���(ȡֵ���ޱ��ඨ��Ĳ�������)
     */
    public String getAction() {
        return action;
    }
    /**
     * ִ�еĲ���(ȡֵ���ޱ��ඨ��Ĳ�������)
     */
    public void setAction(String action) {
        this.action = action;
    }
    /**
     * �������
     */
    public String getType() {
        return type;
    }
    /**
     * �������
     */
    public void setType(String type) {
        this.type = type;
    }
    /**
     * �ļ���ftp�ϵ�·��
     */
    public String getFtpPath() {
        return ftpPath;
    }
    /**
     * �ļ���ftp�ϵ�·��
     */
    public void setFtpPath(String ftpPath) {
        this.ftpPath = ftpPath;
    }
    /**
     * Ӧ������ip
     */
    public String getIp() {
        return ip;
    }
    /**
     * Ӧ������ip
     */
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    /**
     * �˿�
     */
    public String getPort() {
        return port;
    }
    
    /**
     * �˿�
     */
    public void setPort(String port) {
        this.port = port;
    }
    
    /**
     * ������Ϣ
     */
	public List<ProcessInfo> getProcessInfoList() {
		return processInfoList;
	}
	/**
     * ������Ϣ
     */
	public void setProcessInfoList(List<ProcessInfo> processInfoList) {
		this.processInfoList = processInfoList;
	}
	
	/**
     * ���̺�
     */
	public Integer getProcessNo() {
		return processNo;
	}
	/**
     * �������
     */
	public String getAppName() {
		return appName;
	}
	/**
     * �������
     */
	public void setAppName(String appName) {
		this.appName = appName;
	}
	/**
     * ��ǰȡ���Ľ�����
     */
	public int getProcessCount() {
		return processCount;
	}
	/**
     * ��ǰȡ���Ľ�����
     */
	public void setProcessCount(int processCount) {
		this.processCount = processCount;
	}
	public AtomicInteger getProcessCountv() {
		return processCountv;
	}
	public void setProcessCountv(AtomicInteger processCountv) {
		this.processCountv = processCountv;
	}
	
	
}
