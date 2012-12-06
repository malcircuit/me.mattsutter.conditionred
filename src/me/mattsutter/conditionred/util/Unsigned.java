package me.mattsutter.conditionred.util;

import java.nio.ByteBuffer;

public class Unsigned{
	public static final short BYTE_MASK = 0xFF;
		
	public static void putUnsignedByte(ByteBuffer byte_buf, short value, int index){
		byte_buf.put(index, (byte) (value & BYTE_MASK));
	}
	
	public static void putUnsignedBytes(ByteBuffer byte_buf, short[] values){
		for (int i = 0; i < values.length; i++)
			byte_buf.put(i, (byte) (values[i] & BYTE_MASK));
	}

	public static short getUnsignedByte(ByteBuffer byte_buf, int index){
		return (short) (byte_buf.get(index) & BYTE_MASK);
	}
	
	public static short getUnsignedByte(byte[] array, int index){
		return getUnsignedByte(array[index]);
	}
	
	public static void putUnsignedByte(byte[] array, short value, int index){
		if (value > 0)
			assignUnsignedByte(array[index], value);
//			array[index] = (byte)(value & BYTE_MASK);
		else
			array[index] = 0;
	}
	
	public static void putUnsignedByte(byte[] array, int value, int index){
		if (value > 0)
//			assignUnsignedByte(array[index], value);
			array[index] = (byte)(value & BYTE_MASK);
		else
			array[index] = 0;
	}
	
	public static short getUnsignedByte(byte var){
		return (short)(var & BYTE_MASK);
	}
	
	public static void assignUnsignedByte(byte var, short value){
		var = (byte)(value & BYTE_MASK);
	}
	
	public static void assignUnsignedByte(byte var, int value){
		var = (byte)(value & BYTE_MASK);
	}
}

