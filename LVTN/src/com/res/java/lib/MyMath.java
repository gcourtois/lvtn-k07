package com.res.java.lib;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.res.java.lib.exceptions.OverflowException;

public class MyMath {
	public BigDecimal add(BigDecimal op1, BigDecimal op2) {
		return op1.add(op2);
	}
	public byte[] add(byte[] op1, byte[] op2) {
		return new byte[1];
	}
	public BigDecimal subtract(BigDecimal op1, BigDecimal op2) {
		return op1.subtract(op2);
	}
	public byte[] subtract(byte[] op1, byte[] op2) {
		return new byte[1];
	}
	public BigDecimal multiply(BigDecimal op1, BigDecimal op2) {
		return op1.multiply(op2);
	}
	
	
	public static void main(String[] args) {
		BaseClass test = new BaseClass(1);
//		byte[] testInput = {0x01,0x45,0x12,0x3D};
//		BigDecimal testBig = test.convertBCDToBigDec(testInput, 2);
//		System.out.println(test.convertBCDToBigDec(testInput, 2).toString());
////		System.out.println(test.convertBCDToInt(testInput));
////		byte[] testInput = {(byte) 0x0f,(byte) 0xff, (byte) 0xfb, 0x2e};
////		System.out.println(test.convertBytesToInt(testInput, false));
//		byte[] testInput2 = {(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte)0x31,(byte) 0xD4};
//		System.out.println(test.convertDisplayToLong(testInput2, true));
//		long testInt = 123456789123456789L;
//		System.out.println(test.printByteArray(test.convertLongToBCD(testInt, true)));
//		//BigDecimal testBig = new BigDecimal(123123123.1);
//		//testBig.setScale(1);
//		System.out.println(test.printByteArray(test.convertBigDecimalToBCD(testBig, true)));
//		//System.out.println(Integer.toHexString(-123));
//		int testIntDisplay = -123;
//		System.out.println(test.printByteArray(test.convertIntToDisplay(testIntDisplay, false)));
//		System.out.println(test.printByteArray(test.convertLongToDisplay(test.convertDisplayToLong(testInput2, true),true)));
//		byte[] testString = {0x31, 0x32, (byte) 0x61};
//		String stringCOBOL = test.convertDisplayToString(testString, 0, testString.length);
//		System.out.println(stringCOBOL);
//		byte[] newString = new byte[1000];
//		test.convertStringToDisplay("á", newString, 0);
		Long a = Long.valueOf("-1234");
	}
	
};


