package puyopuyo.think.util;

import java.util.ArrayList;

import puyopuyo.think.data.BlockData;
import puyopuyo.think.data.MoveData;

/**
 * 最も連鎖数が多いところを選ぶ
 * @author hkoba
 *
 */
public class CheckMostRensa implements CheckFilter {
	/**
	 * 最小連鎖数
	 */
	private int minRensa;

	public CheckMostRensa() {
		minRensa = 0;
	}
	/**
	 * コンストラクタ
	 * @param min 選択する最小連鎖数を指定する
	 */
	public CheckMostRensa(int min) {
		minRensa = min;
	}

	public int checkScore(BlockData blkdt, MoveData mvdt) {
		ArrayList<int[]> ret = new ArrayList<int[]>();
		BlockData nxt = blkdt.putMoveData(mvdt, ret);
		if (ret.size() < minRensa) {
			return IGNORE_DATA;
		}
		// 連鎖が多くて消える数も一番多いところを得る
		return ret.size()*100+nxt.getBlockNum(BlockData.BLK_SPACE);
	}

}
