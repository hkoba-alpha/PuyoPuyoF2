package puyopuyo.think.util;

import puyopuyo.think.data.BlockData;
import puyopuyo.think.data.MoveData;

/**
 * 最もぷよが消えるところを探す
 * 消えるものがなければ null
 * @author hkoba
 *
 */
public class CheckMostDelete implements CheckFilter {

	public int checkScore(BlockData blkdt, MoveData mvdt) {
		int n1 = blkdt.getBlockNum(BlockData.BLK_SPACE);
		BlockData ret = blkdt.putMoveData(mvdt, null);
		int n2 = ret.getBlockNum(BlockData.BLK_SPACE);
		if (n2 < n1) {
			// 消えるものがない
			return IGNORE_DATA;
		}
		return n2;
	}

}
