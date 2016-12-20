
/**
 * com.cetca.util.security
 * SHAI.java
 * 
 * 2012-7-3-上午11:37:23
 * 2012-版权所有
 * 
 */

package com.navy.daemon.security;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <b>项目名称 </b>aim-w<br>
 * <b>类名称 </b>com.cetca.util.security.SHAI<br>
 * <b>类描述 </b>TODO(描述类的职责)<br>
 * <b>创建时间 </b>2012-7-3-上午11:37:23<br>
 * <b>@author </b>mupan<br>
 * <b>@Copyright </b>2012-
 * 
 */

public class SHAI{
	public static byte[] encrypt(byte[] obj){
		 byte[] data=null;
		MessageDigest sha;
		try {
			sha = MessageDigest.getInstance("SHA");
			sha.update(obj);
			data=sha.digest();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	/**
	 * 
	 * <b>方法描述：</b>TODO(shai加密，返回base64编码字符)<br>
	 * @param msg
	 * @return
	 */
	public static String doEncrypt(String msg){
		return new String(Base64.encode(encrypt(msg.getBytes())));
	}
	
	/**
	 * 
	 * <b>方法描述：</b>TODO(shai加密，返回加密后16进制值编码字符)<br>
	 * @param msg
	 * @return not successful then renturn null,all HEXString are UpperCase
	 */
	public static String[] doEncrypt_HexString(String msg){
		String[] HEXResult=null;
		if(null!=msg){
			HEXResult=new String[5];
			byte[] DigestByte=encrypt(msg.getBytes());
			//System.out.println(com.cetca.util.NumberSwap.byteArrayToHexString(DigestByte));
			if(null!=DigestByte){
				//System.out.println("DigestByte length="+DigestByte.length);
				byte[] INT=new byte[4];
				
				ByteArrayInputStream bin=new ByteArrayInputStream(DigestByte);
				DataInputStream din=new DataInputStream(bin);
				for(int i=0;i<5;i++){
					try {
						if(din.read(INT, 0, 4)<1){
							return null;
						}
						int ik = NumberSwap.byteArrayToInt(INT, 0);
						//System.out.print(ik);
						HEXResult[i]=Integer.toHexString(ik);
						HEXResult[i]=HEXResult[i].toUpperCase();
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
					
				}
			}
		}
		return HEXResult;
	}
	
	public static String to_HexString(byte[] digestb){
		String[] HEXResult = null;
		String result = "";
		//System.out.println(com.cetca.util.NumberSwap.byteArrayToHexString(DigestByte));
		if(null!=digestb){
			HEXResult=new String[5];
			//System.out.println("DigestByte length="+DigestByte.length);
			byte[] INT=new byte[4];
			
			ByteArrayInputStream bin=new ByteArrayInputStream(digestb);
			DataInputStream din=new DataInputStream(bin);
			for(int i=0;i<5;i++){
				try {
					if(din.read(INT, 0, 4)<1){
						return null;
					}
					int ik = NumberSwap.byteArrayToInt(INT, 0);
					//System.out.print(ik);
					HEXResult[i]=Integer.toHexString(ik);
					HEXResult[i]=HEXResult[i].toUpperCase();
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				
			}
		}
		if(null != HEXResult){
			for(String t : HEXResult){
				result = result + t.toLowerCase();
			}
		}
		
		return result;
	}
	
	public static String doEncrypt_HexString_0(String msg){
		String[] r = SHAI.doEncrypt_HexString(msg);
		String result = "";
		for(String t : r){
			result = result + t.toLowerCase();
		}
		return result;
	}
	
	public static void main(String[] args){
		/**String msg1 = "ascdssdfsfdsfdsfdscfwecedde";
		String msg2 = "111111";
		String msg3 = msg1+msg2;
		
		byte[] obj1 = msg1.getBytes();
		byte[] obj2 = msg2.getBytes();
		byte[] obj3 = msg3.getBytes();
		
		byte[] data = null;
		byte[] data2 = null;
		MessageDigest sha = null;
		System.out.println();
		try {
			sha = MessageDigest.getInstance("SHA");
			sha.update(obj1);
			sha.update(obj2);
			data=sha.digest();
			msg1 = to_HexString(data);
			
			sha.reset();
			sha.update(obj3);
			data2=sha.digest();
			msg2 = to_HexString(data2);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("msg1:"+msg1);
		System.out.println("msg2:"+msg2);
		System.out.println(msg2.equals(msg1));
		*/
		
		/*SimpleDateFormat simp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",java.util.Locale.ENGLISH);
		SimpleDateFormat simps = new SimpleDateFormat("dd-MMM-yy hh.mm.ss aaa",Locale.ENGLISH);
		try {
			System.out.println(simp.format(simps.parse("11-NOV-15 12.26.36 PM")));
			//simp.parse("16-十一月-15 06.00.30 下午");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//Timestamp s = Timestamp.valueOf("11-NOV-15 12.26.36 PM");
		String msg = "/com.navy.daemon/appctrl/AppContrlService";
		System.out.println(SHAI.doEncrypt_HexString_0(msg));
		
	}
	
	
}
