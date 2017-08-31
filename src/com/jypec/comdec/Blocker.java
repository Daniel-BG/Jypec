package com.jypec.comdec;

import java.util.ArrayList;

import com.jypec.ebc.SubBand;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.img.HyperspectralBand;
import com.jypec.util.BitTwiddling;
import com.jypec.util.Stepper;

/**
 * @author Daniel
 * Class that partitions a Band into Blocks for coding
 */
public class Blocker extends ArrayList<CodingBlock> {

	private static final long serialVersionUID = -9028934315574770801L;
	
	private int waveletSteps;
	private int expectedBlockDim;
	private int maxBlockDim;
	private int maxBlockSize;

	/**
	 * Partitions the given band into blocks. A block is a square in the image, with certain restrictions
	 * imposed on its size, with all of its samples belonging to the same SubBand, for easier coding
	 * @param hb the band that is to be blocked
	 * @param waveletSteps the number of steps of subdivision the wavelet did. It is assumed that the LL
	 * subBand is on the top-left-most position of the band (towards 0,0)
	 * @param expectedBlockDim maximum block size (in samples) (has to be a power of 2)
	 * @param maxBlockDim maximum block dimensions (in samples) (has to be >= than expectedBlockDim)
	 */
	public Blocker(HyperspectralBand hb, int waveletSteps, int expectedBlockDim, int maxBlockDim) {
		this.waveletSteps = waveletSteps;
		this.expectedBlockDim = expectedBlockDim;
		this.maxBlockSize = this.expectedBlockDim * this.expectedBlockDim;
		this.maxBlockDim = maxBlockDim;
		//ensure maxblocksize is an even power of two
		if (!BitTwiddling.powerOfTwo(this.expectedBlockDim)) {
			throw new IllegalArgumentException("Expected dimensions for the block must be a power of two");
		}
		
		this.block(hb);
	}
	
	/**
	 * Block the given band into the given list. The order of the blocks is deterministic so that
	 * the same method can be used when coding and decoding
	 */
	private void block(HyperspectralBand hb) {
		int col = 0, row = 0;
		int[] cols = Stepper.getStepSizes(hb.getColumns(), this.waveletSteps);
		int[] rows = Stepper.getStepSizes(hb.getRows(), this.waveletSteps);
		for (int i = this.waveletSteps; i >= 0; i--) {
			if (i == this.waveletSteps) {
				this.blockSameSubBandRegion(hb, SubBand.LL, row, col, rows[i], cols[i]);
			} else {
				this.blockSameSubBandRegion(hb, SubBand.HL, 0, col, row, cols[i] - col);
				this.blockSameSubBandRegion(hb, SubBand.LH, row, 0, rows[i] - row, col);
				this.blockSameSubBandRegion(hb, SubBand.HH, row, col, rows[i] - row, cols[i] - col);
			}
			col = cols[i];
			row = rows[i];
		}
	}
	
	/**
	 * Subdivides a region of all the same SubBand type into blocks, from top left to bottom right
	 * @param hb
	 * @param sb
	 * @param strow
	 * @param stcol
	 * @param rows
	 * @param cols
	 */
	private void blockSameSubBandRegion(HyperspectralBand hb, SubBand sb, int strow, int stcol, int rows, int cols) {
		//corner case: regions are of zero size
		if (rows == 0 || cols == 0) {
			return;
		}
		
		//case 1: our region is smaller than the expected block. Make all the region the same block
		if (rows < this.expectedBlockDim && cols < this.expectedBlockDim) {
			this.add(new CodingBlock(hb, rows, cols, strow, stcol, hb.getDataType().getBitDepth(), sb));
		}
		//case 2: our region is smaller only in the vertical direction
		else if (rows < this.expectedBlockDim) {
			int maxLength = Math.min(this.maxBlockSize / rows, this.maxBlockDim);
			int realLength = Math.min(maxLength, cols);
			this.add(new CodingBlock(hb, rows, realLength, strow, stcol, hb.getDataType().getBitDepth(), sb));
			//case 2.1: the total size is smaller than the max size
			if (realLength < cols) {
				this.blockSameSubBandRegion(hb, sb, strow, stcol + realLength, rows, cols - realLength);
			}
		}
		//case 3: our region is smaller only in the horizontal direction
		else if (cols < this.expectedBlockDim) {
			int maxLength = Math.min(this.maxBlockSize / cols, this.maxBlockDim);
			int realLength = Math.min(maxLength, rows);
			this.add(new CodingBlock(hb, realLength, cols, strow, stcol, hb.getDataType().getBitDepth(), sb));
			//case 2.1: the total size is smaller than the max size
			if (realLength < rows) {
				this.blockSameSubBandRegion(hb, sb, strow + realLength, stcol, rows - realLength, cols);
			}
		}
		//case 4: our region is bigger than the expected block, so we add it fully and subdivide the region
		else {
			this.add(new CodingBlock(hb, this.expectedBlockDim, this.expectedBlockDim, strow, stcol, hb.getDataType().getBitDepth(), sb));
			this.blockSameSubBandRegion(hb, sb, strow, stcol + this.expectedBlockDim, this.expectedBlockDim, cols - this.expectedBlockDim);
			this.blockSameSubBandRegion(hb, sb, strow + this.expectedBlockDim, stcol, rows - this.expectedBlockDim, this.expectedBlockDim);
			this.blockSameSubBandRegion(hb, sb, strow + this.expectedBlockDim, stcol + this.expectedBlockDim, rows - this.expectedBlockDim, cols - this.expectedBlockDim);
		}
	}
	
}
