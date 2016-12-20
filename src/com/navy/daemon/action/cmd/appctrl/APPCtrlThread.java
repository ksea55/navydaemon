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
 * 软件控制线程类
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
			//标记软件是请求相关操作的停止而非异常停止
			BootStart.checkStop(app.getProcessName(), false, true);
			unInstall();
			//去掉标记
			BootStart.checkStop(app.getProcessName(), true, false);
		}else if(ClusterConfig.START.equals(app.getAction()) ){
			if(start(null)){
				System.out.println("start sucess checkStop remark.....");
				//去掉标记
				BootStart.checkStop(app.getProcessName(), true, false);
			}
		}else if(ClusterConfig.STOP.equals(app.getAction()) ){
			//标记软件是请求相关操作的停止而非异常停止
			BootStart.checkStop(app.getProcessName(), false, true);
			stop(null);
		}else if(ClusterConfig.UPDATE.equals(app.getAction()) ){
			//标记软件是请求相关操作的停止而非异常停止
			BootStart.checkStop(app.getProcessName(), false, true);
			update();
			//去掉标记
			BootStart.checkStop(app.getProcessName(), true, false);
		}
		return null;
	}
	
	private void install(){
		//下载软件包
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
			app.setMes("软件包下载成功");
			if(null != serviceContext) serviceContext.report(app);
		} catch (Exception e) {
			//状态上报-插件包下载
			app.setCode(-11);
			app.setMes("软件包下载失败("+e.getMessage()+")");
			if(null != serviceContext) serviceContext.report(app);
			e.printStackTrace();
			return;
		}finally{
			try{ ftpclient.logout(); }catch(Exception e){}
		}
		
		//解压到软件包到目标目录
		File installfolder = new File(BootStart.getBoot().getInstallfolder()+File.separator+app.getProcessName()+File.separator);
		if(!installfolder.exists()) installfolder.mkdirs();
		if(localFile.exists() 
				&& (localFile.getName().endsWith(".zip") 
						|| localFile.getName().endsWith(".ZIP")) ){
			try {
				Compress.unzip(localFile.getAbsolutePath(), installfolder.getAbsolutePath(), false);
				app.setActionPath(installfolder.getAbsolutePath());
				//删除下载目录临时文件
				//FileOperate.deleteGeneralFile(localFile.getAbsolutePath());
				localFile = null;
				
				app.setCode(12);
				app.setMes("软件包解压成功");
				if(null != serviceContext) serviceContext.report(app);
			} catch (Exception e) {
				//添加状态上报
				app.setCode(-12);
				app.setMes("软件包解压失败("+e.getMessage()+")");
				if(null != serviceContext) serviceContext.report(app);
				e.printStackTrace();
				return;
			}
		}
		
		
		//执行安装脚本，获取安装脚本执行结果
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
					app.setMes("软件安装成功("+re[1]+")");
				else app.setMes("软件安装成功");
				if(null != serviceContext) serviceContext.report(app);
			}else{
				app.setCode(-13);
				if(StringUtils.isNotEmpty(re[1])) 
					app.setMes("软件安装失败("+re[1]+")");
				else app.setMes("软件安装失败");
				if(null != serviceContext) serviceContext.report(app);
				return;
			}
		}
		
		//启动软件
		start(installfolder);
	}
	
	private void unInstall(){
		//获取软件安装目录、进程信息
		File installfolder = new File(BootStart.getBoot().getInstallfolder()+File.separator+app.getProcessName());
		
		//应用软件正在运行时，先关闭当前应用
		if(!SystemInfo.isStop(app.getProcessName())){
			stop(installfolder);
		}
		
		//执行卸载脚本
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
						app.setMes("软件卸载成功("+re[1]+")");
					else app.setMes("软件卸载成功");
					if(null != serviceContext) serviceContext.report(app);
				}else{
					app.setCode(-23);
					if(StringUtils.isNotEmpty(re[1])) 
						app.setMes("软件卸载失败("+re[1]+")");
					else app.setMes("软件卸载失败");
					if(null != serviceContext) serviceContext.report(app);
					return;
				}
			}
		}
	}
	
	private void update(){
		//卸载
		unInstall();
		//安装
		install();
	}
	
	private boolean start(File installfolder){
		boolean flag = false;
		if(null == installfolder){
			installfolder = new File(BootStart.getBoot().getInstallfolder()+File.separator+app.getProcessName());
		}
		//执行启动脚本，获取启动脚本执行结果
		String[] re = null;
		if(SystemInfo.isUnix()){
			File installshell = new File(installfolder.getAbsolutePath()+File.separator+"startup.sh");
			re = CallShell.callShell(installshell.getAbsolutePath(), null);
		}else{
			File installshell = new File(installfolder.getAbsolutePath()+File.separator+"startup.bat");
			re = CallBat.callCmd(installshell.getAbsolutePath(), null);
		}
		//更新进程信息
		SystemInfo.getAppProcessInfo(app, BootStart.getBoot().nodeSysInfo);
		if(!SystemInfo.isStop(app.getProcessName())){
			if(null != re){
				if("0".equals(re[0])){
					flag = true;
					app.setCode(0);
					if(StringUtils.isNotEmpty(re[1])) 
						app.setMes("软件启动成功("+re[1]+")");
					else app.setMes("软件启动成功");
				}else{
					app.setCode(-24);
					if(StringUtils.isNotEmpty(re[1])) 
						app.setMes("软件启动失败("+re[1]+")");
					else app.setMes("软件启动失败");
				}
			}else{
				app.setCode(-24);
				app.setMes("软件启动成功，但启动脚本未正常结束");
			}
		}else{
			if(null != re){
				if("0".equals(re[0])){
					if(null == installfolder){
						installfolder = new File(BootStart.getBoot().getInstallfolder()+File.separator+app.getProcessName());
					}
					app.setCode(-24);
					app.setMes("软件启动脚本执行成功，但未找到对应进程信息，可能主进程名填报错误");
				}else{
					app.setCode(-24);
					if(StringUtils.isNotEmpty(re[1])) 
						app.setMes("软件启动失败("+re[1]+")");
					else app.setMes("软件启动失败");
				}
			}else{
				app.setMes("软件启动失败");
				app.setMes("软件启动失败");
			}
		}
		
		if(null != serviceContext) serviceContext.report(app);
		
		//启动失败，执行关闭脚本，避免无效进程
		if(!flag){
			System.out.println("app ["+app.getProcessName()+"]start erro:"+app.getCode()+","+app.getMes());
			//标记软件是请求相关操作的停止而非异常停止
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
		//执行关闭脚本，获取关闭脚本执行结果
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
					app.setMes("软件停止成功("+re[1]+")");
				else app.setMes("软件停止成功");
			}else{
				app.setCode(-31);
				if(StringUtils.isNotEmpty(re[1])) 
					app.setMes("软件停止失败("+re[1]+")");
				else app.setMes("软件停止失败");
			}
		}else{
			app.setCode(-31);
			app.setMes("软件停止失败");
		}
		
		if(null != serviceContext) serviceContext.report(app);
	}
	
	
}
