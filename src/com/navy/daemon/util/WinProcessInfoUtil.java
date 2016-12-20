package com.navy.daemon.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
  
 
/**
 * windowsϵͳ������Ϣ
 * @author mup
 *
 */
public class WinProcessInfoUtil {
    /**
     * ͨ����ȡ��ǰ����������pidName����ȡ�������pid  
     * @return
     * @throws Exception
     */
    public static String getCurrentPid() throws Exception {  
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();  
        String pidName = runtime.getName();// 5296@dell-PC  
        String pid = pidName.substring(0, pidName.indexOf("@"));  
        return pid;  
    }  
  
    /**
     * ͨ��Pid��ȡPidName  
     * @param pid
     * @return
     * @throws Exception
     */
    public static String getPidNameByPid(String pid) throws Exception {  
        String pidName = null;  
        InputStream is = null;  
        InputStreamReader ir = null;  
        BufferedReader br = null;  
        String line = null;  
        String[] array = (String[]) null;  
        try {  
            Process p = Runtime.getRuntime().exec("TASKLIST /NH /FO CSV /FI \"PID EQ " + pid + "\"");  
            is = p.getInputStream(); // "javaw.exe","3856","Console","1","72,292  
                                        // K"����������л�ȡ��Ӧ��PidName  
            ir = new InputStreamReader(is);  
            br = new BufferedReader(ir);  
            while ((line = br.readLine()) != null) {  
                if (line.indexOf(pid) != -1) {  
                    array = line.split(",");  
                    line = array[0].replaceAll("\"", "");  
                    line = line.replaceAll(".exe", "");// ����pidName��׺Ϊexe����EXE  
                    line = line.replaceAll(".exe".toUpperCase(), "");  
                    pidName = line;  
                }  
            }  
        } catch (IOException localIOException) {  
            throw new Exception("��ȡ�������Ƴ���");  
        } finally {  
            if (br != null) {  
                br.close();  
            }  
            if (ir != null) {  
                ir.close();  
            }  
            if (is != null) {  
                is.close();  
            }  
        }  
        return pidName;  
    }  
  
    /**
     * ����Pid��ȡ��ǰ���̵�CPU  
     * @param pid
     * @return
     * @throws Exception
     */
    public static String getCPUByPID(String pid) throws Exception {  
        if (pid == null) {  
            return null;  
        }  
        InputStream is = null;  
        InputStreamReader ir = null;  
        BufferedReader br = null;  
        String line = null;  
        String[] array = (String[]) null;  
        try {  
            Process p = Runtime.getRuntime().exec("TASKLIST /NH /FO CSV /FI \"PID EQ " + pid + "\"");  
            is = p.getInputStream();  
            ir = new InputStreamReader(is);  
            br = new BufferedReader(ir);  
            while ((line = br.readLine()) != null) {  
                if (!"".equals(line)) {  
                	System.out.println("pid:"+pid+",line1:"+line);
                    array = line.split("\",\"");  
                    line = array[3].replaceAll("\"", "");
                    System.out.println("pid:"+pid+",line2:"+line);
                    return line;  
                }  
            }  
        } catch (Exception localException) {  
            throw new Exception("��ȡ����CPU��Ϣ����");  
        } finally {  
            if (br != null) {  
                br.close();  
            }  
            if (ir != null) {  
                ir.close();  
            }  
            if (is != null) {  
                is.close();  
            }  
        }  
        if (br != null) {  
            br.close();  
        }  
        if (ir != null) {  
            ir.close();  
        }  
        if (is != null) {  
            is.close();  
        }  
        return null;  
    }  
  
    /**
     * ����Pid��ȡ��ǰ���̵�memory  
     * @param pid
     * @return
     * @throws Exception
     */
    public static String getMemByPID(String pid) throws Exception {  
        if (pid == null) {  
            return null;  
        }  
        InputStream is = null;  
        InputStreamReader ir = null;  
        BufferedReader br = null;  
        String line = null;  
        String[] array = (String[]) null;  
        try {  
            Process p = Runtime.getRuntime().exec(" TASKLIST /NH /FO CSV /FI \"PID EQ " + pid + "\"");  
            is = p.getInputStream();  
            ir = new InputStreamReader(is);  
            br = new BufferedReader(ir);  
            while ((line = br.readLine()) != null) {  
                if (!"".equals(line)) {  
                    array = line.split("\",\"");  
                    line = array[4].replaceAll("\"", "");  
                    return line;  
                }  
            }  
        } catch (IOException localIOException) {  
            throw new Exception("��ȡ�����ڴ���Ϣ����");  
        } finally {  
            if (br != null) {  
                br.close();  
            }  
            if (ir != null) {  
                ir.close();  
            }  
            if (is != null) {  
                is.close();  
            }  
        }  
        if (br != null) {  
            br.close();  
        }  
        if (ir != null) {  
            ir.close();  
        }  
        if (is != null) {  
            is.close();  
        }  
        return null;  
    }  
  
    /**
     * ����Pid�����̸ɵ�  
     * @param pid
     * @throws Exception
     */
    public static void killProcessByPid(String pid) throws Exception {  
        Runtime.getRuntime().exec("taskkill /F /PID " + pid);  
    }  
  
    /**
     * ����PidName�����̸ɵ�  
     * @param pidName
     * @throws Exception
     */
    public static void killProcessByPidName(String pidName) throws Exception {  
        Runtime.getRuntime().exec("taskkill /F /IM " + pidName + ".exe");  
    }  
  
    /**
     *  ����PidName��ȡ��ǰ��Pid��list����  
     * @param pidName
     * @return
     * @throws Exception
     */
    public static List<String> getPIDListByPidName(String pidName) throws Exception {  
        List<String> pidList = new ArrayList<String>();  
        BufferedReader reader = null;  
        String line = null;  
        String[] array = (String[]) null;  
        try {  
            String imageName = pidName;  //"tasklist /NH /fo csv /fi \"imagename eq svchost.exe\" "
            //"tasklist /NH /fo csv /fi \"imagename eq " + imageName + "\""
            Process p = Runtime.getRuntime().exec("tasklist /NH /fo csv /fi \"imagename eq "+imageName+"\" ");  
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = reader.readLine()) != null) {  
            	System.out.println("line="+line);
                if (line.indexOf(imageName) != -1) {  
                    array = line.split(",");  
                    line = array[1].replaceAll("\"", "");  
                    pidList.add(line);  
                }  
            }  
        } catch (IOException localIOException) {  
            throw new Exception("��ȡ����ID����");  
        } finally {  
            if (reader != null) {  
            	reader.close();  
            }  
        }  
        return pidList;  
    }  
  
    /**
     * ��ȡ��ǰϵͳ�����е�PidName  
     * @return
     * @throws Exception
     */
    public static Set<String> getCurrOsAllPidNameSet() throws Exception {  
        Set<String> pidNameSet = new HashSet<String>();  
        InputStream is = null;  
        InputStreamReader ir = null;  
        BufferedReader br = null;  
        String line = null;  
        String[] array = (String[]) null;  
        try {  
            Process p = Runtime.getRuntime().exec("TASKLIST /NH /FO CSV");  
            is = p.getInputStream();  
            ir = new InputStreamReader(is);  
            br = new BufferedReader(ir);  
            while ((line = br.readLine()) != null) {  
                array = line.split(",");  
                line = array[0].replaceAll("\"", "");  
                line = line.replaceAll(".exe", "");  
                line = line.replaceAll(".exe".toUpperCase(), "");  
                if (StringUtils.isNotBlank(line)) {  
                    pidNameSet.add(line);  
                }  
            }  
        } catch (IOException localIOException) {  
            throw new Exception("��ȡϵͳ���н���������");  
        } finally {  
            if (br != null) {  
                br.close();  
            }  
            if (ir != null) {  
                ir.close();  
            }  
            if (is != null) {  
                is.close();  
            }  
        }  
        return pidNameSet;  
    }  
  
    /**
     * �жϵ�ǰpid�Ƿ��˳����жϸ���pid��ѯ���ڴ��Ƿ�Ϊ��������  
     * @param pid
     * @return
     * @throws Exception
     */
    public static boolean isExitPid(String pid) throws Exception {  
        return getMemByPID(pid) != null;  
    }  
  
    /*
     * ������·�������в�����ƴ��  
     */
    public static String getCommandFormatStr(String proPath) {// ������˼·�Ϳ��Կ������ֲ���������  
        return getCommandFormatStr(proPath, null);  
    }  
  
    public static String getCommandFormatStr(String proPath, String runArgs) {  
        StringBuffer command = new StringBuffer();  
        command.append("\"");  
        command.append(proPath);  
        command.append("\"");  
        if (StringUtils.isNotBlank(runArgs)) {  
            command.append(" ").append(runArgs);  
        }  
        return command.toString();  
    }  
  
    /**
     * ִ������Ӧ�������о��˳�cmd  
     * @param cmdStr
     * @return
     */
    private static String getCommandByCmd(String cmdStr) {  
        StringBuffer command = new StringBuffer();  
        command.append("cmd /C ");  
        command.append(cmdStr);  
        return command.toString();  
    }  
  
    /*
     * ��ȡ��ǰ������Java_Home  
     */
    private static String getJavaHome() throws Exception {  
        String javaHome = System.getenv("JAVA_HOME");  
        javaHome = javaHome == null ? System.getProperty("java.home") : javaHome;  
        return javaHome;  
    }  
  
    /**
     * �˳�Java_Home  
     * @return
     * @throws Exception
     */
    public static boolean existJavaHome() throws Exception {// ������������ڲ�Ʒ�׶������ж��Ƿ����JAVA_HOME  
        InputStream is = null;  
        InputStreamReader ir = null;  
        BufferedReader br = null;  
        String line = null;  
        try {  
            Process p = Runtime.getRuntime().exec("cmd   /c   echo   %JAVA_HOME% ");  
            is = p.getInputStream();  
            ir = new InputStreamReader(is);  
            br = new BufferedReader(ir);  
            if ((line = br.readLine()) != null) {  
                if (line.indexOf("%JAVA_HOME%") != -1) {  
                    return false;  
                }  
                return true;  
            }  
        } catch (IOException localIOException) {  
            throw new Exception("��ȡJAVA_HOME��Ϣ����");  
        } finally {  
            if (br != null) {  
                br.close();  
            }  
            if (ir != null) {  
                ir.close();  
            }  
            if (is != null) {  
                is.close();  
            }  
        }  
        if (br != null) {  
            br.close();  
        }  
        if (ir != null) {  
            ir.close();  
        }  
        if (is != null) {  
            is.close();  
        }  
        return false;  
    }  
  
    /**
     * ͨ��cmd�򿪶�Ӧ���ļ�  
     * @param fileDir
     * @throws Exception
     */
    public static void openDir(String fileDir) throws Exception {// ��cmd��ִ��explorer  
        Runtime.getRuntime().exec("cmd /c start explorer " + fileDir);// explorer.exe��Windows�ĳ�������������ļ���Դ������  
    }  
  
    /**
     * ���ݵ�ǰ��Pid��ȡ��ǰ���̵Ķ˿�  
     * @param pid
     * @return
     * @throws Exception
     */
    public static Map<String, List<String>> getPortByPID(String pid) throws Exception {  
        if (pid == null) {  
            return null;  
        }  
        InputStream is = null;  
        InputStreamReader ir = null;  
        BufferedReader br = null;  
        String line = null;  
        String TCP_TYPE = "TCP";  
        String UDP_TYPE = "UDP";  
        String LISTENING_STATE_TYPE = "LISTENING";// ״ֵ̬  
        Map<String, List<String>> portMap = new HashMap<String, List<String>>();  
        List<String> tcpPortList = new ArrayList<String>();  
        List<String> udpPortList = new ArrayList<String>();  
        portMap.put(TCP_TYPE, tcpPortList);  
        portMap.put(UDP_TYPE, udpPortList);  
        String[] array = (String[]) null;  
        try {  
            Process p = Runtime.getRuntime().exec("netstat /ano");  
            is = p.getInputStream();  
            ir = new InputStreamReader(is);  
            br = new BufferedReader(ir);  
            do {  
                if (line.indexOf(pid) != -1) {  
                    line = line.replaceFirst("\\s+", "");  
                    if (line.indexOf(TCP_TYPE) != -1) {  
                        if (line.indexOf(LISTENING_STATE_TYPE) != -1) {  
                            array = line.split("\\s+");  
                            String port = array[1].split(":")[1];  
                            tcpPortList.add(port);  
                        }  
                    } else {  
                        array = line.split("\\s+");  
                        String port = array[1].split(":")[1];  
                        udpPortList.add(port);  
                    }  
                }  
                if ((line = br.readLine()) == null) {  
                    break;  
                }  
            } while (pid != null);  
        } catch (IOException localIOException) {  
            throw new Exception("��ȡ���̶˿���Ϣ����");  
        } finally {  
            if (br != null) {  
                br.close();  
            }  
            if (ir != null) {  
                ir.close();  
            }  
            if (is != null) {  
                is.close();  
            }  
        }  
        return portMap;  
    }  
  
    public static void main(String[] args) throws Exception {  
        System.out.println(getCurrentPid());  
        // System.out.print(getPortByPID("5116"));  
        System.out.println(getMemByPID("5116"));  
        System.out.println(getJavaHome());  
        System.out.println(existJavaHome());  
        String property = System.getProperty("java.home");  
        System.out.println(property);// E:\Programs\jdk\jre  
        Set<String> set = getCurrOsAllPidNameSet();  
        for (String string : set) {  
            System.out.println(string);  
        }  
    }  
} 