package com.navy.daemon.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import com.navy.daemon.entity.ClusterConfig;
import com.navy.daemon.entity.ProcessInfo;
import com.sun.management.OperatingSystemMXBean;

/** *//** 
 * ��ȡϵͳ��Ϣ��ҵ���߼�ʵ����. 
 * @author amg * @version 1.0 Creation date: 2008-3-11 - ����10:06:06 
 */  
public class WinMonitorServiceImpl implements IMonitorService {  
    //�������ó�Щ����ֹ�������д˴�ϵͳ���ʱ��cpuռ���ʣ��Ͳ�׼��  
    private static final int CPUTIME = 5000;
  
    private static final int PERCENT = 100;
  
    private static final int FAULTLENGTH = 10;
  
    /** *//** 
     * ��õ�ǰ�ļ�ض���. 
     * @return ���ع���õļ�ض��� 
     * @throws Exception 
     * @author amg     * Creation date: 2008-4-25 - ����10:45:08 
     */  
    public MonitorInfoBean getMonitorInfoBean(MonitorInfoBean infoBean) throws Exception {  
        int kb = 1024;  
          
        // ��ʹ���ڴ�  
        long totalMemory = Runtime.getRuntime().totalMemory() / kb;  
        // ʣ���ڴ�  
        long freeMemory = Runtime.getRuntime().freeMemory() / kb;  
        // ����ʹ���ڴ�  
        long maxMemory = Runtime.getRuntime().maxMemory() / kb;  
  
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory  
                .getOperatingSystemMXBean();  
  
        // ����ϵͳ  
        String osName = System.getProperty("os.name");  
        // �ܵ������ڴ�  
        long totalMemorySize = osmxb.getTotalPhysicalMemorySize() / kb;  
        // ʣ��������ڴ�  
        long freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize() / kb;  
        // ��ʹ�õ������ڴ�  
        long usedMemory = (osmxb.getTotalPhysicalMemorySize() - osmxb  
                .getFreePhysicalMemorySize())  
                / kb;  
        //ϵͳ���еĽ�����
        int avaliableprocs = osmxb.getAvailableProcessors();
        System.out.println(osmxb.getArch());
        
        String hostName = getSystemHostName();
        
        // ����߳�����  
        ThreadGroup parentThread;  
        for (parentThread = Thread.currentThread().getThreadGroup(); parentThread  
                .getParent() != null; parentThread = parentThread.getParent())  
            ;  
        int totalThread = parentThread.activeCount();  
  
        double cpuRatio = 0;  
        if (osName.toLowerCase().startsWith("windows")) {  
            cpuRatio = this.getCpuRatio(null);  
        }  
          
        // ���췵�ض���  
        if(null == infoBean) infoBean = new MonitorInfoBean();  
        infoBean.freeMemory = freeMemory;  
        infoBean.freePhysicalMemorySize = freePhysicalMemorySize;  
        infoBean.maxMemory = maxMemory;  
        infoBean.osName = osName;  
        infoBean.totalMemory = totalMemory;  
        infoBean.totalMemorySize = totalMemorySize;  
        infoBean.totalThread = totalThread;  
        infoBean.usedMemory = usedMemory;
        infoBean.cpuRatio = cpuRatio; 
        infoBean.avliaProcessCount = avaliableprocs;
        infoBean.hostName = hostName;
        
        //��ȡ������Ϣ
        infoBean.logicDiskInfos = readSystemLogicDiskInfo(infoBean);
        
        return infoBean;  
    }  
  
    /** *//** 
     * ���CPUʹ����. 
     * @return ����cpuʹ���� 
     * @author mup 
     */  
    public double getCpuRatio(String processName) {  
        try {  
        	//where Caption=\"System Idle Process,"+processName+"\"
            String procCmd = System.getenv("windir")  
                    + "\\system32\\wbem\\wmic.exe process get Caption,CommandLine,"  
                    + "KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";  
            // ȡ������Ϣ  
            long[] c0 = readCpu(Runtime.getRuntime().exec(procCmd), processName);  
            Thread.sleep(CPUTIME);  
            long[] c1 = readCpu(Runtime.getRuntime().exec(procCmd), processName);  
            if (c0 != null && c1 != null) {  
                long idletime = c1[0] - c0[0];  
                long busytime = c1[1] - c0[1];  
                return Double.valueOf(  
                        PERCENT * (busytime) / (busytime + idletime))  
                        .doubleValue();  
            } else {  
                return 0.0;  
            }  
        } catch (Exception ex) {  
            ex.printStackTrace();  
            return 0.0;  
        }  
    }  
    
  
    /** *//** 
     * ��ȡCPU��Ϣ. 
     * @param proc 
     * @return 
     * @author mup
     */  
    public long[] readCpu(final Process proc, String ProcessName) {  
        long[] retn = new long[2];  
        try {  
            proc.getOutputStream().close();  
            InputStreamReader ir = new InputStreamReader(proc.getInputStream());  
            LineNumberReader input = new LineNumberReader(ir);  
            String line = input.readLine();  
            if (line == null || line.length() < FAULTLENGTH) {  
                return null;  
            }  
            int capidx = line.indexOf("Caption");  
            int cmdidx = line.indexOf("CommandLine");  
            int rocidx = line.indexOf("ReadOperationCount");  
            int umtidx = line.indexOf("UserModeTime");  
            int kmtidx = line.indexOf("KernelModeTime");  
            int wocidx = line.indexOf("WriteOperationCount");  
            long idletime = 0;  
            long kneltime = 0;  
            long usertime = 0;  
            while ((line = input.readLine()) != null) {  
                if (line.length() < wocidx) {  
                    continue;  
                }  
                // �ֶγ���˳��Caption,CommandLine,KernelModeTime,ReadOperationCount,  
                // ThreadCount,UserModeTime,WriteOperation  
                String caption = Bytes.substring(line, capidx, cmdidx - 1)  
                        .trim();  
                String cmd = Bytes.substring(line, cmdidx, kmtidx - 1).trim();  
                if (cmd.indexOf("wmic.exe") >= 0) {  
                    continue;  
                } 
                // log.info("line="+line);  
                if (caption.equals("System Idle Process")  
                        || caption.equals("System")) {
                    idletime += Long.valueOf(  
                            Bytes.substring(line, kmtidx, rocidx - 1).trim())  
                            .longValue();  
                    idletime += Long.valueOf(  
                            Bytes.substring(line, umtidx, wocidx - 1).trim())  
                            .longValue();  
                    continue;  
                } 
                //�в���ProcessName��֤����Ҫ��Ծ�����̽���ͳ��
                if(null != ProcessName && !ProcessName.equals(caption)) continue;
                /*System.out.println("line="+line);
                
                System.out.println(kmtidx+","+rocidx+","+umtidx+","+wocidx+";"
                		 +Bytes.substring(line, kmtidx, rocidx - 1).trim()
                		 +";"+Bytes.substring(line, umtidx, wocidx - 1).trim()
                );*/
                kneltime += Long.valueOf(  
                        Bytes.substring(line, kmtidx, rocidx - 1).trim())  
                        .longValue();  
                usertime += Long.valueOf(  
                        Bytes.substring(line, umtidx, wocidx - 1).trim())  
                        .longValue();  
            }  
            retn[0] = idletime;  
            retn[1] = kneltime + usertime;  
            return retn;  
        } catch (Exception ex) {  
            ex.printStackTrace();  
        } finally {  
            try {  
                proc.getInputStream().close();  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
        return null;  
    }  
      
    /** *//** 
     * ���CPUʹ����. 
     * @return ����cpuʹ���� 
     * @author mup 
     */  
    public void getProcessInfo(String ProcessName, String pid) {  
        try {  
        	//where Caption=\"System Idle Process,"+processName+"\"
            String procCmd = System.getenv("windir")  
                    + "\\system32\\wbem\\wmic.exe process ";
                    if(StringUtils.isNotBlank(ProcessName) || StringUtils.isNotBlank(pid)){
                    	procCmd = procCmd+"where ";
                    	boolean hasc = false;
                    	if(StringUtils.isNotBlank(ProcessName)){
                    		hasc = true;
                    		procCmd = procCmd+ "\"Name='chrome.exe'";
                    	}
                    		
                    	if(StringUtils.isNotBlank(pid)){
                    		if(hasc) procCmd = procCmd+ " and ";
                    		else procCmd = procCmd+ "\" ";
                    		procCmd = procCmd + "ProcessId='6328'";
                    	}
                    		
                    	procCmd = procCmd +"\"";
                    }
                    procCmd = procCmd + " get Caption,CommandLine,"  
                    + "KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";  
           
            Process pcs = Runtime.getRuntime().exec(procCmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(pcs.getInputStream()));
            String line = null;
            while((line = br.readLine()) !=null ){
            	if(StringUtils.isNotBlank(line))System.out.println("line="+line);
            }
        } catch (Exception ex) {  
            ex.printStackTrace();  
        }  
    }
    
    /**
     * ��ȡ��ǰϵͳcpu˲ʱ����ʱ��
     * @return
     */
    public static long readCpu_idletime() {  
        long idletime = 1;
        Process proc = null;
        try {
        	 String procCmd = System.getenv("windir")  
             + "\\system32\\wbem\\wmic.exe process where \"Name='System Idle Process'\" get Caption,CommandLine,"  
             + "KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";  
        	
        	proc = Runtime.getRuntime().exec(procCmd);
        	proc.getOutputStream().close();  
            InputStreamReader ir = new InputStreamReader(proc.getInputStream());  
            LineNumberReader input = new LineNumberReader(ir);  
            String line = input.readLine();  
            if (line == null || line.length() < FAULTLENGTH) {  
                return idletime;
            }  
            int capidx = line.indexOf("Caption");  
            int cmdidx = line.indexOf("CommandLine");  
            int rocidx = line.indexOf("ReadOperationCount");  
            int umtidx = line.indexOf("UserModeTime");  
            int kmtidx = line.indexOf("KernelModeTime");  
            int wocidx = line.indexOf("WriteOperationCount");  
            while ((line = input.readLine()) != null) { 
                if (line.length() < wocidx) {  
                    continue;  
                }  
                // �ֶγ���˳��Caption,CommandLine,KernelModeTime,ReadOperationCount,  
                // ThreadCount,UserModeTime,WriteOperation  
                String caption = Bytes.substring(line, capidx, cmdidx - 1)  
                        .trim();  
                String cmd = Bytes.substring(line, cmdidx, kmtidx - 1).trim();  
                if (cmd.indexOf("wmic.exe") >= 0) {  
                    continue;
                } 
                // log.info("line="+line);  
                if (caption.equals("System Idle Process")  
                        || caption.equals("System")) {
                    idletime += Long.valueOf(  
                            Bytes.substring(line, kmtidx, rocidx - 1).trim())  
                            .longValue();  
                    idletime += Long.valueOf(  
                            Bytes.substring(line, umtidx, wocidx - 1).trim())  
                            .longValue();  
                    continue;  
                } 
            }  
        } catch (Exception ex) {  
            ex.printStackTrace();  
        } finally {  
            try {  
                proc.getInputStream().close();  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
        return idletime;  
    }
    
    /**
     * ��ȡpid����cpu˲ʱռ��ʱ��
     * @param pid
     * @return
     */
    public static long readePIDCpu_busyTime(String pid) {
        Process proc = null;
        long busyTime = 0;
        try {
        	 String procCmd = System.getenv("windir")  
             + "\\system32\\wbem\\wmic.exe process where \"ProcessId='"+pid+"'\" get Caption,CommandLine,"  
             + "KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";  
        	
        	proc = Runtime.getRuntime().exec(procCmd);
        	
            proc.getOutputStream().close();
            InputStreamReader ir = new InputStreamReader(proc.getInputStream());  
            LineNumberReader input = new LineNumberReader(ir);  
            String line = input.readLine();
            if (line == null || line.length() < FAULTLENGTH) {  
                return busyTime;
            }  
            int cmdidx = line.indexOf("CommandLine");  
            int rocidx = line.indexOf("ReadOperationCount");  
            int umtidx = line.indexOf("UserModeTime");  
            int kmtidx = line.indexOf("KernelModeTime");  
            int wocidx = line.indexOf("WriteOperationCount");  
            long kneltime = 0;  
            long usertime = 0;
            while ((line = input.readLine()) != null) {  
                if (line.length() < wocidx) {  
                    continue;
                }  
                // �ֶγ���˳��Caption,CommandLine,KernelModeTime,ReadOperationCount,  
                // ThreadCount,UserModeTime,WriteOperation  
                String cmd = Bytes.substring(line, cmdidx, kmtidx - 1).trim();
                if (cmd.indexOf("wmic.exe") >= 0) {
                    continue;  
                } 
                 
                //�в���ProcessName��֤����Ҫ��Ծ�����̽���ͳ��
                //System.out.println("line="+line);
               /* System.out.println(kmtidx+","+rocidx+","+umtidx+","+wocidx+";"
                		 +Bytes.substring(line, kmtidx, rocidx - 1).trim()
                		 +";"+Bytes.substring(line, umtidx, wocidx - 1).trim()
                );*/
                
                kneltime = Long.valueOf(  
                        Bytes.substring(line, kmtidx, rocidx - 1).trim())  
                        .longValue();  
                usertime = Long.valueOf(  
                        Bytes.substring(line, umtidx, wocidx - 1).trim())  
                        .longValue();
                
                busyTime = kneltime + usertime;
            }  
        } catch (Exception ex) {  
            ex.printStackTrace();  
        } finally {  
            try {  
                proc.getInputStream().close();  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
        return busyTime;  
    }
    
    /**
     * ����processName��ȡ���н���cpu���ڴ�ռ�ü���������Ϣ
     * @param processName ��������һ��������������
     * 
     * @return �޷�����wendowsϵͳwmic����ʱ������null��
     * �������������appʱ��ʵ��ȡ�����ӽ�����Ϣ�� app.getProcessCount()����������
     * ������forѭ������List<ProcessInfo>ʱ�����ó���Ϊapp.getProcessCount()��
     * �磺
     * <br/>for(int i = 0; i < app.getProcessCount(); i++){}
     */
    public static List<ProcessInfo> readProcessNamePidsInfo(String processName, ClusterConfig app) {
    	System.out.println("readProcessNamePidsInfo null==app?"+(null == app));
    	List<ProcessInfo> pidsInfo = null;
    	if(null != app) {
    		pidsInfo = app.getProcessInfoList();
    	}
        Process proc = null;
        try {
        	 String procCmd = System.getenv("windir")  
             + "\\system32\\wbem\\wmic.exe process where \"Name='"+processName+"'\" get Caption,ParentProcessId,ProcessId,VirtualSize,WorkingSetSize";  
        	proc = Runtime.getRuntime().exec(procCmd);
        	
            proc.getOutputStream().close();  
            InputStreamReader ir = new InputStreamReader(proc.getInputStream());  
            LineNumberReader input = new LineNumberReader(ir);  
            String line = input.readLine();
            //System.out.println(line);
            if (line == null || line.length() < FAULTLENGTH) {  
                return null;
            }  
            int pid_idx = line.indexOf("ProcessId");
            int ppid_idx = line.indexOf("ParentProcessId");
            int wss_idx = line.indexOf("WorkingSetSize");
            int vss_idx = line.indexOf("VirtualSize");
            
            ArrayList<Long> busytimes = new ArrayList<Long>();
            ArrayList<Long> idletimes = new ArrayList<Long>();
            if(null == pidsInfo ) {
            	pidsInfo = new ArrayList<ProcessInfo>();
            	if(null != app) app.setProcessInfoList(pidsInfo);
            }
            ProcessInfo pidinfo = null;
            
            int processindx = 0;
            int createdLen = pidsInfo.size();
            while ((line = input.readLine()) != null) {
            	 if(StringUtils.isBlank(line)) continue;
                /*if (line.length() < wocidx) {  
                    continue;  
                }*/  
                // �ֶγ���˳��Caption    ParentProcessId  ProcessId  VirtualSize  WorkingSetSize
                //System.out.println("line="+line);
            	 //�����ã����ⷴ����������
            	if(createdLen > processindx){
            		pidinfo = pidsInfo.get(processindx);
            	}else{
            		pidinfo = new ProcessInfo();
            		pidsInfo.add(pidinfo);
            	}
            	processindx ++;
                pidinfo.ProcessName = processName;
                pidinfo.PProcessId = Long.valueOf(Bytes.substring(line, ppid_idx, pid_idx - 1) .trim() );
                pidinfo.ProcessId = Long.valueOf(Bytes.substring(line, pid_idx, vss_idx - 1) .trim() );
                pidinfo.memoryInuse = Double.valueOf(
                		Bytes.substring(line, wss_idx, (line.getBytes().length - 1)) .trim() 
                	) / 1024;
                pidinfo.VirtualSize = Double.valueOf(
                		Bytes.substring(line, vss_idx, wss_idx - 1) .trim() 
                	) / 1024;
                
                idletimes.add(readCpu_idletime());
                busytimes.add(readePIDCpu_busyTime(""+pidinfo.ProcessId));
            }
            
            //������ӽ���cpuռ����
            if(null != pidsInfo && processindx > 0){
            	//����ȡ�����ӽ�����
            	if(null != app) {
            		app.getProcessCountv().set(processindx);
                	System.out.println("readProcessNamePidsInfo processcount:"+app.getProcessCountv().get());
            	}
            	//˯��һ��ʱ���ȡcpu���к�ռ��ʱ��Ƭ
            	Thread.sleep(CPUTIME);
            	for(int i =0 ; i < processindx; i++){
            		pidinfo = pidsInfo.get(i);
            		long idletime = readCpu_idletime() - idletimes.get(i);
            		long busytime = readePIDCpu_busyTime(""+pidinfo.ProcessId) - busytimes.get(i);
            		//ǰ�����ε�ʱ��ʱ��Ƭ����ֽ���ռ��cpu�ٷֱ�
            		pidinfo.cpuUsage = Double.valueOf(
            				PERCENT * (busytime) / (busytime + idletime)
            			) .doubleValue();
            	}
            }
        } catch (Exception ex) {  
            ex.printStackTrace(); 
        } finally {  
            try {  
                proc.getInputStream().close();
            } catch (Exception e) {  
                e.printStackTrace();
            }
        }  
        return pidsInfo;  
    }
    
    /**
     * ��ȡwindowsϵͳ����Ϣ
     * @param infoBean ϵͳ״̬��Ϣ SystemMXBean jmx bean��
     *    Ϊnullʱ������һ���µļ��ϴ�ŷ���ϵͳ��Ϣ��
     *    ��Ϊnullʱ��������ϵͳ�ܵĴ��̿ռ䣬������jmxʵ���еļ��ϴ�ŷ�����Ϣ
     *    
     * @return ������Ϣ���޷�����wendowsϵͳwmic����ʱ������null��
     * �������������infoBeanʱ��ʵ��ȡ���ķ�������infoBean.AvliaLogicDiskCount����������
     * ������forѭ������infoBean.getLogicDiskInfos()ʱ�����ó���ΪinfoBean.AvliaLogicDiskCount��
     * �磺
     * <br/>for(int i = 0; i < infoBean.AvliaLogicDiskCount; i++){}
     */
    public static List<LogicDiskInfo> readSystemLogicDiskInfo(MonitorInfoBean infoBean) {
    	Process proc = null;
    	
    	MonitorInfoBean infoBeano = infoBean;
    	List<LogicDiskInfo> logicDiskInfos = null;
    	if(null != infoBeano){
    		infoBean.avliaLogicDiskCount = 0;
    		logicDiskInfos = infoBean.logicDiskInfos;
    	}
    	
    	if(null == logicDiskInfos) {
    		logicDiskInfos = new ArrayList<LogicDiskInfo>();
    	}
    	
        try {
        	/*
        	wmic LOGICALDISK get Caption,Description,DeviceID,DriveType,FileSystem,FreeSpace,Size,VolumeName
        	 */
        	 String procCmd = System.getenv("windir")  
             + "\\system32\\wbem\\wmic.exe logicaldisk get Caption, Description, DeviceID, DriveType, FileSystem, FreeSpace, Size, VolumeName";  
        	proc = Runtime.getRuntime().exec(procCmd);
        	
            proc.getOutputStream().close();  
            InputStreamReader ir = new InputStreamReader(proc.getInputStream());  
            LineNumberReader input = new LineNumberReader(ir);  
            String line = input.readLine();
            //System.out.println(line);
            if (line == null || line.length() < FAULTLENGTH) {  
                return null;
            }
            
            //��ȡ��ͷλ��
            int cap_idx = line.indexOf("Caption");
            int desc_idx = line.indexOf("Description");
            int did_idx = line.indexOf("DeviceID");
            int drt_idx = line.indexOf("DriveType");
            int fsys_idx = line.indexOf("FileSystem");
            int fsp_idx = line.indexOf("FreeSpace");
            int size_idx = line.indexOf("Size");
            int voln_idx = line.indexOf("VolumeName");
            
            
            int logicdiskindx = 0;
            //�����ã������ζ���new����������ȡ�Ѿ��������󳤶�
            int createdLen = logicDiskInfos.size();
            LogicDiskInfo diskoinfo = null;
            long diskTotalSize = 0;
            long diskTotalFreeSize = 0;
            while ((line = input.readLine()) != null) {
            	if(StringUtils.isBlank(line)) continue;
            	System.out.println(line);
            	
            	 //�����ã����ⷴ����������
            	if(createdLen > logicdiskindx){
            		diskoinfo = logicDiskInfos.get(logicdiskindx);
            	}else{
            		diskoinfo = new LogicDiskInfo();
            		logicDiskInfos.add(diskoinfo);
            	}
            	logicdiskindx ++;
            	diskoinfo.Caption = Bytes.substring(line, cap_idx, desc_idx - 1) .trim();
            	diskoinfo.Description = Bytes.substring(line, desc_idx, did_idx - 1) .trim();
            	diskoinfo.DeviceID = Bytes.substring(line, did_idx, drt_idx - 1) .trim();
            	diskoinfo.DriveType = Bytes.substring(line, drt_idx, fsys_idx - 1) .trim();
            	diskoinfo.FileSystem = Bytes.substring(line, fsys_idx, fsp_idx - 1) .trim();
            	
            	String freeSpace = Bytes.substring(line, fsp_idx, size_idx - 1) .trim();
            	if(StringUtils.isNotBlank(freeSpace)){
            		diskoinfo.FreeSpace = Double.valueOf(freeSpace) /1024/1024;
            		diskTotalFreeSize += diskoinfo.FreeSpace;
            	}else{
            		diskoinfo.FreeSpace = -1;
            	}
            	
            	String size = Bytes.substring(line, size_idx, voln_idx - 1) .trim();
            	if(StringUtils.isNotBlank(size)){
            		diskoinfo.Size = Double.valueOf(size) /1024/1024;
            		diskTotalSize += diskoinfo.Size;
            	}else{
            		diskoinfo.Size = -1;
            	}
            	
            	diskoinfo.VolumeName = Bytes.substring(line, voln_idx, (line.getBytes().length - 1) ) .trim();
            	
           	}
            
            if(null != infoBeano){
        		infoBean.avliaLogicDiskCount = logicdiskindx;
        		infoBean.diskTotalSize = diskTotalSize;
        		infoBean.diskTotalFreeSize = diskTotalFreeSize;
        	}
        } catch (Exception ex) {  
            ex.printStackTrace(); 
        } finally {  
            try {  
                proc.getInputStream().close();
            } catch (Exception e) {  
                e.printStackTrace();
            }
        }  
        return logicDiskInfos;  
    }
    
    /**
     * ��ȡ��ǰϵͳ������
     * @return δ�ɹ�����wmicʱ������null
     */
    public static String getSystemHostName() {
    	Process proc = null;
    	String hostName = null;
    	
        try {
        	/*
        	wmic LOGICALDISK get Caption,Description,DeviceID,DriveType,FileSystem,FreeSpace,Size,VolumeName
        	 */
        	 String procCmd = System.getenv("windir")  
             + "\\system32\\wbem\\wmic.exe computersystem get Name";  
        	proc = Runtime.getRuntime().exec(procCmd);
        	
            proc.getOutputStream().close();  
            InputStreamReader ir = new InputStreamReader(proc.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line = input.readLine();
            //System.out.println(line);
            if (line == null || line.length() < FAULTLENGTH) {
                return null;
            }
            
            //��ȡ��ͷλ��
            int name_idx = line.indexOf("Name");
            
            while ((line = input.readLine()) != null) {
            	if(StringUtils.isBlank(line)) continue;
            	hostName = Bytes.substring(line, name_idx, (line.getBytes().length -1) ) .trim();
           	}
            
        } catch (Exception ex) {  
            ex.printStackTrace(); 
        } finally {  
            try {  
                proc.getInputStream().close();
            } catch (Exception e) {  
                e.printStackTrace();
            }
        }  
        return hostName;  
    }
    
    
    /** *//** 
     * ���Է���. 
     * @param args 
     * @throws Exception 
     * @author mupan 
     */  
    public static void main(String[] args) throws Exception { 
    	/*WinMonitorServiceImpl d = new WinMonitorServiceImpl(); 
        IMonitorService service = new WinMonitorServiceImpl();  
        MonitorInfoBean monitorInfo = service.getMonitorInfoBean(null); 
        
        System.out.println("cpuռ����=" + monitorInfo.cpuRatio);  
          
        System.out.println("��ʹ���ڴ�=" + monitorInfo.totalMemory);  
        System.out.println("ʣ���ڴ�=" + monitorInfo.freeMemory);  
        System.out.println("����ʹ���ڴ�=" + monitorInfo.maxMemory);  
          
        System.out.println("����ϵͳ=" + monitorInfo.osName);  
        System.out.println("�ܵ������ڴ�=" + monitorInfo.totalMemorySize + "kb");  
        System.out.println("ʣ��������ڴ�=" + monitorInfo.freeMemory + "kb");  
        System.out.println("��ʹ�õ������ڴ�=" + monitorInfo.usedMemory + "kb");  
        System.out.println("�߳�����=" + monitorInfo.totalThread + "kb"); 
    	
    	d.getProcessInfo("plsqldev.exe", null);
    	
    	List<ProcessInfo> appprocesss = readProcessNamePidsInfo("plsqldev.exe", null);
    	 for(ProcessInfo prcess : appprocesss){
    		 System.out.println(prcess.ProcessName+","+prcess.ProcessId+","+prcess.PProcessId+","
    				 +prcess.cpuUsage+","+prcess.memoryInuse/1024+","+prcess.VirtualSize/1024);
    	 }
    	*/
    	MonitorInfoBean infoBean = new MonitorInfoBean();
    	List<LogicDiskInfo> logicDiskInfos = new ArrayList<LogicDiskInfo>();
    	infoBean.logicDiskInfos = logicDiskInfos;
    	for(int i =0 ; i < 10; i++) logicDiskInfos.add(new LogicDiskInfo());
    	infoBean.avliaLogicDiskCount = 0;
    	
    	logicDiskInfos = readSystemLogicDiskInfo(infoBean);
    	System.out.println("infoBean.getAvliaLogicDiskCount():"+infoBean.avliaLogicDiskCount);
    	System.out.println("logicDiskInfos.size():"+logicDiskInfos.size());
    	
    	System.out.println("DiskTotalSize:"+infoBean.diskTotalSize);
    	System.out.println("TotalFreeSize:"+infoBean.diskTotalFreeSize);
    	
    	for(int i = 0; i < infoBean.avliaLogicDiskCount; i++){
    		LogicDiskInfo b = logicDiskInfos.get(i);
    		System.out.println(
    				b.Caption
    				+","+b.Description
    				+","+b.DeviceID
    				+","+b.DriveType
    				+","+b.FileSystem
    				+","+b.FreeSpace
    				+","+b.Size
    				+","+b.VolumeName
    		);
    	}
    	
    	getSystemHostName();
    }
}  
