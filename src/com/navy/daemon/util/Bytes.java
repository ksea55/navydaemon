package com.navy.daemon.util;

/** *//** 
 * byte操作类. 
 * @author mupan
 */  
public class Bytes {  
    /** *//** 
     * 由于String.subString对汉字处理存在问题（把一个汉字视为一个字节)，因此在 
     * 包含汉字的字符串时存在隐患，现调整如下： 
     * @param src 要截取的字符串 
     * @param start_idx 开始坐标（包括该坐标) 
     * @param end_idx   截止坐标（包括该坐标） 
     * @return 
     */  
    public static String substring(String src, int start_idx, int end_idx){  
    	//return src.substring(start_idx, end_idx+1);
    	
    	byte[] b =  null;
		b = src.getBytes();
		
        String tgt = "";
        int len = 0 ;
        byte[] tt = new byte[end_idx - start_idx+1];
        //System.out.println(start_idx+","+end_idx+"\n"+src);
        for(int i=start_idx; i<=end_idx; i++){  
            //tgt +=(char)b[i];
            tt[len] = b[i];
            len++;
        }
        tgt = new String(tt);
        //System.out.println("Bytes.substring:......."+tgt+"=="+bk+"--"+len+","+tt.length);
        return tgt;
    }
    
    public static void main(String[] args) throws Exception{
    	String head = "Caption  Description       DeviceID  DriveType  FileSystem  FreeSpace     Size          VolumeName  ";
    	String line = "E:       Local Fixed Disk  E:        3          NTFS        132605816832  367001595904  资料与工具  ";
    	System.out.println(substring(line, head.indexOf("VolumeName"), line.getBytes().length-1 ) );
		
	}
} 