
/**
 * com.cetca.util
 * NumberSwap.java
 * 
 * 2012-7-26-����11:21:36
 * 2012-��Ȩ����
 * 
 */

package com.navy.daemon.security;


/**
 * <b>��Ŀ���� </b>cetcautil<br>
 * <b>������ </b>com.cetca.util.NumberSwap<br>
 * <b>������ </b>TODO(����ת��)<br>
 * <b>����ʱ�� </b>2012-7-26-����11:21:36<br>
 * <b>@author </b>mupan<br>
 * <b>@Copyright </b>2012-
 * 
 */

public class NumberSwap {
	/**
	 * 
	 * <b>����������</b>TODO(������ת��Ϊ4�ֽ�)<br>
	 * @param integer
	 * @return
	 */
	public static byte[] intToByteArray(final int integer) {
		int byteNum = (40 -Integer.numberOfLeadingZeros (integer < 0 ? ~integer : integer))/ 8;
		byte[] byteArray = new byte[4];

		for (int n = 0; n < byteNum; n++)
		byteArray[3 - n] = (byte) (integer>>> (n * 8));

		return (byteArray);
	}
	/**
	 * 
	 * <b>����������</b>TODO(��4�ֽ�װ��Ϊ����)<br>
	 * @param b
	 * @param offset
	 * @return
	 */
	public static int byteArrayToInt(byte[] b, int offset) {
       int value= 0;
       for (int i = 0; i < 4; i++) {
           int shift= (4 - 1 - i) * 8;
           value +=(b[i + offset] & 0x000000FF) << shift;
       }
       return value;
	}
	
	
	 public static String byteToHexString(byte paramByte)
	  {
	    char[] arrayOfChar1 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	    char[] arrayOfChar2 = new char[2];
	    arrayOfChar2[0] = arrayOfChar1[(paramByte >>> 4 & 0xF)];
	    arrayOfChar2[1] = arrayOfChar1[(paramByte & 0xF)];
	    return new String(arrayOfChar2);
	  }
	 
	 public static String byteArrayToHexString(byte[] paramArrayOfByte)
	  {
	    StringBuilder localStringBuilder = new StringBuilder();
	    for (int i = 0; i < paramArrayOfByte.length; i++)
	    {
	      localStringBuilder.append(byteToHexString(paramArrayOfByte[i]));
	    }
	    
	    return localStringBuilder.toString();
	  }

}
