package com.navy.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.AbstractAction;

public abstract class Action {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {  
            //��һ��  ���ó��ļ���ʽ  
            File file = new File("D:\\jarload\\test.txt");  
            BufferedReader in = new BufferedReader(new FileReader(file));  
            String s = new String();  
            while ((s = in.readLine()) != null) {  
            	
                URL url = new URL(s);  
                s = null;  
                
                URLClassLoader myClassLoader = new URLClassLoader(new URL[] { url }, Thread.currentThread()  
                        .getContextClassLoader());  
                Class<? extends AbstractAction> myClass = (Class<? extends AbstractAction>) myClassLoader.loadClass("com.java.jarloader.TestAction");  
                AbstractAction action = (AbstractAction) myClass.newInstance();  
               // String str = action.action();  
                //System.out.println(str);  
                  
                //�ڶ���  
                URL url1 = new URL("file:D:/jarload/test.jar");  
                URLClassLoader myClassLoader1 = new URLClassLoader(new URL[] { url1 }, Thread.currentThread()  
                        .getContextClassLoader());  
                Class<?> myClass1 = myClassLoader1.loadClass("com.java.jarloader.TestAction");  
                AbstractAction action1 = (AbstractAction) myClass1.newInstance();  
               // String str1 = action1.action();  
               // System.out.println(str1);  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        } 

	}

}
