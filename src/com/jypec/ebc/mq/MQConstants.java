package com.jypec.ebc.mq;

public class MQConstants {
	/** Marker that signals the start of a two-byte code */
	public static final int BYTE_MARKER = 0xff;
	/** Mark the end of a MQ_CODER segment */
	public static final int BYTE_END_OF_MQ_CODER = 0xa1;
}
