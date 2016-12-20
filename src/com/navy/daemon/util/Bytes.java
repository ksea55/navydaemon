package com.navy.daemon.util;

/** *//** 
 * byte������. 
 * @author mupan
 */  
public class Bytes {  
    /** *//** 
     * ����String.subString�Ժ��ִ���������⣨��һ��������Ϊһ���ֽ�)������� 
     * �������ֵ��ַ���ʱ�����������ֵ������£� 
     * @param src Ҫ��ȡ���ַ��� 
     * @param start_idx ��ʼ���꣨����������) 
     * @param end_idx   ��ֹ���꣨���������꣩ 
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
    	String line = "E:       Local Fixed Disk  E:        3          NTFS        132605816832  367001595904  �����빤��  ";
    	System.out.println(substring(line, head.indexOf("VolumeName"), line.getBytes().length-1 ) );
		
	}
} 