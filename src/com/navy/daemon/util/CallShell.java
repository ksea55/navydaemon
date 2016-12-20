package com.navy.daemon.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class CallShell {
	public static void main(String args[]) {
		File d = new File("sdsd");
		if(d.isFile()){
			System.out.println("kkk");
		}
	}
	
	/**
	 * @param args
	 */
	public static String[] callShell(String locationsh, String[] params) {
		String[] result = new String[2];
		BufferedReader reader = null;
		Process pcs = null;
		try{
			File shell= new File(locationsh);
			StringBuffer buf = new StringBuffer();
			if(shell.isFile()){
				String softPackage = shell.getParent();
				//赋权限
				
				if(softPackage.endsWith(""+File.separatorChar)){
					buf.append("chmod -R 777 ").append(softPackage).append("*");
				}else{
					buf.append("chmod -R 777 ").append(softPackage)
					.append(File.separator).append("*");
				}
				
				pcs = Runtime.getRuntime().exec(buf.toString());
				pcs.waitFor();
			}else if(shell.isDirectory()){
				result[0] = "-10";
				result[0] = "not a shell file or shell script";
				return result;
			}
			
			
			//执行脚本
			buf.delete(0, buf.length());
			buf.append("/bin/sh ").append(locationsh);
			//添加参数
			if(null != params){
				for(String p : params){
					buf.append(" ").append(p);
				}
			}
			
			pcs = Runtime.getRuntime().exec(buf.toString());
			pcs.waitFor();
			buf.delete(0, buf.length());
			buf = null;
			
			//读取返回结果
			reader = new BufferedReader(new InputStreamReader(pcs
					.getInputStream()));
			String line = new String();
			while ((line = reader.readLine()) != null) {
				if(line.startsWith("code=") || line.startsWith("code=")){
					result[0] = line.replace("code=", "");
				}
				if(line.startsWith("msg=") || line.startsWith("msg=")){
					result[1] = line.replace("msg=", "");
				}
			}
			
			pcs.waitFor();
			int ret = pcs.exitValue();
			System.out.println(ret);
			System.out.println("done");
		}catch (Exception e) {
			result[0] = "-10";
			result[1] = e.getMessage();
			e.printStackTrace();
		}finally{
			try{reader.close();}catch(Exception e){}
		}
		return result;
	}

}
