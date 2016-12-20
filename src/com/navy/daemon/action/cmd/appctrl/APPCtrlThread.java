package com.navy.daemon.action.cmd.appctrl;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Callable;

import com.navy.daemon.BootStart;
import com.navy.daemon.action.ActionContext;
import com.navy.daemon.conf.SynConfig;
import com.navy.daemon.entity.ClusterConfig;
import com.navy.daemon.ftp.FTPFactory.Client;
import com.navy.daemon.util.CallBat;
import com.navy.daemon.util.CallShell;
import com.navy.daemon.util.Compress;
import com.navy.daemon.util.StringUtils;
import com.navy.daemon.util.SystemInfo;
/**
 * ��������߳���
 * @author mup
 *
 */
public class APPCtrlThread implements Callable<HashMap<String, String>>{
	private ActionContext serviceContext;
	private ClusterConfig app;
	public APPCtrlThread(ActionContext serviceContext, ClusterConfig app){
		this.serviceContext = serviceContext;
		this.app = app;
	}
	
	@Override
	public HashMap<String, String> call() throws Exception {
		if(ClusterConfig.INSTALL.equals(app.getAction()) ){
			install();
		}else if(ClusterConfig.UNINSTALL.equals(app.getAction()) ){
			//��������������ز�����ֹͣ�����쳣ֹͣ
			BootStart.checkStop(app.getProcessName(), false, true);
			unInstall();
			//ȥ�����
			BootStart.checkStop(app.getProcessName(), true, false);
		}else if(ClusterConfig.START.equals(app.getAction()) ){
			if(start(null)){
				System.out.println("start sucess checkStop remark.....");
				//ȥ�����
				BootStart.checkStop(app.getProcessName(), true, false);
			}
		}else if(ClusterConfig.STOP.equals(app.getAction()) ){
			//��������������ز�����ֹͣ�����쳣ֹͣ
			BootStart.checkStop(app.getProcessName(), false, true);
			stop(null);
		}else if(ClusterConfig.UPDATE.equals(app.getAction()) ){
			//��������������ز�����ֹͣ�����쳣ֹͣ
			BootStart.checkStop(app.getProcessName(), false, true);
			update();
			//ȥ�����
			BootStart.checkStop(app.getProcessName(), true, false);
		}
		return null;
	}
	
	private void install(){
		//���������
		StringBuffer buf = new StringBuffer();
		buf.append(BootStart.getBoot().getDownloadTemp().getAbsolutePath())
			.append(File.separator).append(app.getProcessName()).append(".zip");
		
		File localFile = new File(buf.toString());
		buf.delete(0, buf.length());
		
		Client ftpclient = null;
		try {
			String ftppath = app.getFtpPath();
			if(!app.getFtpPath().endsWith("/") && !app.getFtpFileName().startsWith("/")) 
				ftppath = ftppath + "/";
			
			ftppath = ftppath + app.getFtpFileName();
			System.out.println("download file:"+ftppath);
			
			ftpclient = BootStart.getBoot().getFc().getClient();
			ftpclient.login();
			ftpclient.download(ftppath, localFile);
			System.out.println("finished download file:"+ftppath);
			
			
			app.setCode(11);
			app.setMes("��������سɹ�");
			if(null != serviceContext) serviceContext.report(app);
		} catch (Exception e) {
			//״̬�ϱ�-���������
			app.setCode(-11);
			app.setMes("���������ʧ��("+e.getMessage()+")");
			if(null != serviceContext) serviceContext.report(app);
			e.printStackTrace();
			return;
		}finally{
			try{ ftpclient.logout(); }catch(Exception e){}
		}
		
		//��ѹ���������Ŀ��Ŀ¼
		File installfolder = new File(BootStart.getBoot().getInstallfolder()+File.separator+app.getProcessName()+File.separator);
		if(!installfolder.exists()) installfolder.mkdirs();
		if(localFile.exists() 
				&& (localFile.getName().endsWith(".zip") 
						|| localFile.getName().endsWith(".ZIP")) ){
			try {
				Compress.unzip(localFile.getAbsolutePath(), installfolder.getAbsolutePath(), false);
				app.setActionPath(installfolder.getAbsolutePath());
				//ɾ������Ŀ¼��ʱ�ļ�
				//FileOperate.deleteGeneralFile(localFile.getAbsolutePath());
				localFile = null;
				
				app.setCode(12);
				app.setMes("�������ѹ�ɹ�");
				if(null != serviceContext) serviceContext.report(app);
			} catch (Exception e) {
				//���״̬�ϱ�
				app.setCode(-12);
				app.setMes("�������ѹʧ��("+e.getMessage()+")");
				if(null != serviceContext) serviceContext.report(app);
				e.printStackTrace();
				return;
			}
		}
		
		
		//ִ�а�װ�ű�����ȡ��װ�ű�ִ�н��
		String[] re = null;
		if(SystemInfo.isUnix()){
			File installshell = new File(installfolder.getAbsolutePath()+File.separator+"install.sh");
			re = CallShell.callShell(installshell.getAbsolutePath(), null);
		}else{
			File installshell = new File(installfolder.getAbsolutePath()+File.separator+"install.bat");
			re = CallBat.callCmd(installshell.getAbsolutePath(), null);
		}
		if(null != re){
			if("0".equals(re[0])){
				SynConfig.saveApp(app);
				app.setCode(13);
				if(StringUtils.isNotEmpty(re[1])) 
					app.setMes("�����װ�ɹ�("+re[1]+")");
				else app.setMes("�����װ�ɹ�");
				if(null != serviceContext) serviceContext.report(app);
			}else{
				app.setCode(-13);
				if(StringUtils.isNotEmpty(re[1])) 
					app.setMes("�����װʧ��("+re[1]+")");
				else app.setMes("�����װʧ��");
				if(null != serviceContext) serviceContext.report(app);
				return;
			}
		}
		
		//�������
		start(installfolder);
	}
	
	private void unInstall(){
		//��ȡ�����װĿ¼��������Ϣ
		File installfolder = new File(BootStart.getBoot().getInstallfolder()+File.separator+app.getProcessName());
		
		//Ӧ�������������ʱ���ȹرյ�ǰӦ��
		if(!SystemInfo.isStop(app.getProcessName())){
			stop(installfolder);
		}
		
		//ִ��ж�ؽű�
		String[] re = null;
		if(SystemInfo.isStop(app.getProcessName())){
			if(SystemInfo.isUnix()){
				File installshell = new File(installfolder.getAbsolutePath()+File.separator+"uninstall.sh");
				re = CallShell.callShell(installshell.getAbsolutePath(), null);
			}else{
				File installshell = new File(installfolder.getAbsolutePath()+File.separator+"uninstall.bat");
				System.out.println("uninstall:"+installshell.getAbsolutePath());
				re = CallBat.callCmd(installshell.getAbsolutePath(), null);
			}
			
			if(null != re){
				if("0".equals(re[0])){
					app.setCode(0);
					if(StringUtils.isNotEmpty(re[1])) 
						app.setMes("���ж�سɹ�("+re[1]+")");
					else app.setMes("���ж�سɹ�");
					if(null != serviceContext) serviceContext.report(app);
				}else{
					app.setCode(-23);
					if(StringUtils.isNotEmpty(re[1])) 
						app.setMes("���ж��ʧ��("+re[1]+")");
					else app.setMes("���ж��ʧ��");
					if(null != serviceContext) serviceContext.report(app);
					return;
				}
			}
		}
	}
	
	private void update(){
		//ж��
		unInstall();
		//��װ
		install();
	}
	
	private boolean start(File installfolder){
		boolean flag = false;
		if(null == installfolder){
			installfolder = new File(BootStart.getBoot().getInstallfolder()+File.separator+app.getProcessName());
		}
		//ִ�������ű�����ȡ�����ű�ִ�н��
		String[] re = null;
		if(SystemInfo.isUnix()){
			File installshell = new File(installfolder.getAbsolutePath()+File.separator+"startup.sh");
			re = CallShell.callShell(installshell.getAbsolutePath(), null);
		}else{
			File installshell = new File(installfolder.getAbsolutePath()+File.separator+"startup.bat");
			re = CallBat.callCmd(installshell.getAbsolutePath(), null);
		}
		//���½�����Ϣ
		SystemInfo.getAppProcessInfo(app, BootStart.getBoot().nodeSysInfo);
		if(!SystemInfo.isStop(app.getProcessName())){
			if(null != re){
				if("0".equals(re[0])){
					flag = true;
					app.setCode(0);
					if(StringUtils.isNotEmpty(re[1])) 
						app.setMes("��������ɹ�("+re[1]+")");
					else app.setMes("��������ɹ�");
				}else{
					app.setCode(-24);
					if(StringUtils.isNotEmpty(re[1])) 
						app.setMes("�������ʧ��("+re[1]+")");
					else app.setMes("�������ʧ��");
				}
			}else{
				app.setCode(-24);
				app.setMes("��������ɹ����������ű�δ��������");
			}
		}else{
			if(null != re){
				if("0".equals(re[0])){
					if(null == installfolder){
						installfolder = new File(BootStart.getBoot().getInstallfolder()+File.separator+app.getProcessName());
					}
					app.setCode(-24);
					app.setMes("��������ű�ִ�гɹ�����δ�ҵ���Ӧ������Ϣ�������������������");
				}else{
					app.setCode(-24);
					if(StringUtils.isNotEmpty(re[1])) 
						app.setMes("�������ʧ��("+re[1]+")");
					else app.setMes("�������ʧ��");
				}
			}else{
				app.setMes("�������ʧ��");
				app.setMes("�������ʧ��");
			}
		}
		
		if(null != serviceContext) serviceContext.report(app);
		
		//����ʧ�ܣ�ִ�йرսű���������Ч����
		if(!flag){
			System.out.println("app ["+app.getProcessName()+"]start erro:"+app.getCode()+","+app.getMes());
			//��������������ز�����ֹͣ�����쳣ֹͣ
			//BootStart.checkStop(app.getProcessName(), false, true);
			if(SystemInfo.isUnix()){
				File installshell = new File(installfolder.getAbsolutePath()+File.separator+"stop.sh");
				CallShell.callShell(installshell.getAbsolutePath(), null);
			}else{
				File installshell = new File(installfolder.getAbsolutePath()+File.separator+"stop.bat");
				CallBat.callCmd(installshell.getAbsolutePath(), null);
			}
		}
		
		return flag;
	}
	
	private void stop(File installfolder){
		if(null == installfolder){
			installfolder = new File(BootStart.getBoot().getInstallfolder()+File.separator+app.getProcessName());
		}
		//ִ�йرսű�����ȡ�رսű�ִ�н��
		String[] re = null;
		if(SystemInfo.isUnix()){
			File installshell = new File(installfolder.getAbsolutePath()+File.separator+"stop.sh");
			re = CallShell.callShell(installshell.getAbsolutePath(), null);
		}else{
			File installshell = new File(installfolder.getAbsolutePath()+File.separator+"stop.bat");
			re = CallBat.callCmd(installshell.getAbsolutePath(), null);
		}
		if(null != re){
			if("0".equals(re[0])){
				app.setCode(0);
				if(StringUtils.isNotEmpty(re[1])) 
					app.setMes("���ֹͣ�ɹ�("+re[1]+")");
				else app.setMes("���ֹͣ�ɹ�");
			}else{
				app.setCode(-31);
				if(StringUtils.isNotEmpty(re[1])) 
					app.setMes("���ֹͣʧ��("+re[1]+")");
				else app.setMes("���ֹͣʧ��");
			}
		}else{
			app.setCode(-31);
			app.setMes("���ֹͣʧ��");
		}
		
		if(null != serviceContext) serviceContext.report(app);
	}
	
	
}
