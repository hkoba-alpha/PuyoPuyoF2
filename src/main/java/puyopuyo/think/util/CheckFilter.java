package puyopuyo.think.util;

import puyopuyo.think.data.BlockData;
import puyopuyo.think.data.MoveData;

public interface CheckFilter {
	/**
	 * フィルタで選択対象外のデータとする場合の値
	 */
	public static final int IGNORE_DATA = Integer.MIN_VALUE;

	/**
	 * チェックした結果を返す
	 * @param blkdt ブロックデータ
	 * @param mvdt  移動ぷよ
	 * @return 戻り値が一番大きいものが選択対象となる
	 */
	public int checkScore(BlockData blkdt, MoveData mvdt);
}
