package com.navy.daemon;

import java.io.File;

import it.sauronsoftware.ftp4j.FTPFile;

import com.navy.daemon.ftp.FTPFactory;
import com.navy.daemon.ftp.FTPFactory.Client;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		ftp://192.168.3.234:2121/
			账号admin
			密码admin
		*/
		FTPFactory fc = FTPFactory.newInstance("test", "192.168.3.234", 2121, "admin", "admin");
		Client c = null;
		try {
			c = fc.getClient();
			c.login();
			System.out.println(c.currentDirectory());
			//c.createDirectory("daemon");
			//c.changeDirectory("/daemon");
			//c.deleteFile("/daemon/plsqldev.zip");
			//c.changeDirectory("/daemon");
			//c.upload(new File("F:\\navy_aca\\plsqldev.zip"));
			FTPFile[] fs = c.list("*.zip");
			for(FTPFile ftpf : fs){
				System.out.println(ftpf.toString());
			}
			
			//c.download("/daemon/plsqldev.zip", new File("F:\\Work\\服务总线\\daemon\\download\\plsqldev.zip") );
			System.out.println("finished:...");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try { c.logout(); } catch (Exception e) { e.printStackTrace(); } 
		}
	}

}
