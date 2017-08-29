package com.jypec.ebc.mq;

/**
 * @author Daniel
 * class to store constants used by the MQ-CODER
 */
public class MQConstants {
	/** Marker that signals the start of a two-byte code */
	public static final int BYTE_MARKER = 0xff;
	/** Mark the end of a MQ_CODER segment */
	public static final int BYTE_END_OF_MQ_CODER = 0xff;
	/** Default interval with which A starts */
	public static final int DEFAULT_INTERVAL = 0x8000;
	/** Number of spacer bits used when coding. Default is 3. Less leaves less codes for markers available */
	public static final int SPACER_BITS = 3;
	
	//CODER ONLY CONSTANTS
	/** Mask for the most significant byte of the C register */
	public static final int C_MSBS_MASK = 0x1fe0000 << SPACER_BITS;
	/** Mask for the partial most significant byte of the C register */
	public static final int C_PART_MASK = 0xff0000 << SPACER_BITS;
	/** Mask for the carry bit of C */
	public static final int C_CARRY_MASK = 0x1000000 << SPACER_BITS;
	/** Shift that applied to C leaves the MSBs at the rightmost place */
	public static final int C_MSBS_SHIFT = 17 + SPACER_BITS;
	/** Shift that applied to C leaves the Partial MSBs at the rightmost place */
	public static final int C_PART_SHIFT = 16 + SPACER_BITS;
	/** Shift that applied to C leaves the carry bit at the rightmost place */
	public static final int C_CARRY_SHIFT = 24 + SPACER_BITS;
	
	//DECODER ONLY CONSTANTS
	/** Starting value of the countdown register for the decoder */
	public static final int COUNTDOWN_INIT = 9 + SPACER_BITS;
	/** Mask for the active part of the C register */
	public static final int C_ACTIVE_MASK = 0xffff00;
	/** Mask for the shift needed for the active C register */
	public static final int C_ACTIVE_SHIFT = 8;
	/** Start of the interval of markers left unused by the spacing bits selected */
	public static final int SPECIAL_CODE_START_INTERVAL = 0x80 + (0x1 << (7 - SPACER_BITS));
}
