package com.navy.daemon.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;



//import java.lang.management.*

public class CallBat {
	public static void main(String args[]) {
		//cmd.exe /C start  java -jar navydaemon.jar
		//E:\工具\服务器\PLSQL Developer\bin\plsqldev.exe
		
		//callCmd("tasklist /NH /fo csv /fi \"imagename eq eclipse.exe\" ", null);
		
		//callCmd("TASKLIST /NH /FO CSV /FI \"PID EQ 5952\"", null);
		
		//String pid ="6524";
		//callCmd("TASKLIST /NH /FO CSV /FI \"PID EQ " + pid + "\"", null);
		
		//callCmd("start \"\" \"F:\\Work\\workspace3\\navydaemon\\installfolder\\plsqldev.exe\\startup.bat\"", null);
		
		callCmd("F:\\Work\\workspace3\\navydaemon\\installfolder\\plsqldev.exe\\uninstall.bat", null);
		//callCmd("F:\\Work\\workspace3\\navydaemon\\installfolder\\plsqldev.exe\\install.bat", null);
		
		System.out.println("excute success");
	}

	public static String[] callCmd(String locationCmd, String[] params) {
		String[] result = new String[2];
		BufferedReader reader = null;
		Process pcs = null;
		try {
			File shell= new File(locationCmd);
			StringBuffer buf = new StringBuffer();
			//赋权限
			if(shell.isFile()){
				/*String softPackage = shell.getParent();
				if(softPackage.endsWith(""+File.separatorChar)){
					buf.append("chmod -R 777 ").append(softPackage).append("*");
				}else{
					buf.append("chmod -R 777 ").append(softPackage)
					.append(File.separator).append("*");
				}
				
				pcs = Runtime.getRuntime().exec(buf.toString());
				pcs.waitFor();*/
			}else if(shell.isDirectory()){
				result[0] = "-10";
				result[0] = "not a shell file or shell script";
				return result;
			}
			
			
			//执行脚本
			buf.delete(0, buf.length());
			buf.append(locationCmd);
			//添加参数
			if(null != params){
				for(String p : params){
					buf.append(" ").append(p);
				}
			}
			/*if(shell.isFile()){
				System.out.println(shell.getName()+" ---"+shell.getParent());
				Runtime.getRuntime().exec("cmd.exe /C start \""+shell.getName()+"\"", null, new File(shell.getParent()) );
			}*/
			
			pcs = Runtime.getRuntime().exec(buf.toString());
			buf.delete(0, buf.length());
			buf = null;
			//读取返回结果
			reader = new BufferedReader(new InputStreamReader(pcs.getInputStream()));
			String line = null;
			int count = 2;
			while (null != (line = reader.readLine() ) ) {
				if(line.startsWith("code=") || line.startsWith("code=")){
					result[0] = line.replace("code=", "");
					System.out.println("code:"+result[0]);
					count--;
				}else if(line.startsWith("msg=") || line.startsWith("msg=")){
					result[1] = line.replace("msg=", "");
					System.out.println("msg:"+result[1]);
					count--;
				}else{
					 System.out.println("line="+line);
				}
				if(0 >= count) break;
			}
			System.out.println("result:"+count);
			reader.close();
			//pcs.waitFor();
			pcs.destroy();
			System.out.println("done");
		} catch (Exception e) {
			result[0] = "-10";
			result[1] = e.getMessage();
			e.printStackTrace();
		}finally{
			try{reader.close();}catch(Exception e){}
		}
		return result;
	}
}
