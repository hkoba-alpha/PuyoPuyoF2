package puyopuyo.think.util;

import java.util.ArrayList;

import puyopuyo.think.data.BlockData;
import puyopuyo.think.data.MoveData;

/**
 * すべての移動パターンについてチェックするユーティリティ
 * @author hkoba
 *
 */
public class CheckData {
	protected BlockData blkData;
	protected MoveData moveData;
	protected int maxValue;

	/**
	 * コンストラクタ
	 * @param blkdt
	 * @param mvdt
	 */
	public CheckData(BlockData blkdt, MoveData mvdt) {
		blkData = blkdt;
		moveData = mvdt;
	}

	/**
	 * 最もスコアの高いところをチェック
	 * @param fl チェック用フィルタ
	 * @return 最も大きいものをリストで返す
	 */
	public ArrayList<MoveData> listData(CheckFilter fl) {
		return listData(fl, false);
	}

	/**
	 * 最もスコアの高いところまたは低いところををチェック
	 * @param fl     チェック用フィルタ
	 * @param revflg true:最も小さいものを選ぶ, false:最も大きいものを選ぶ
	 * @return 最も大きいものをリストで返す
	 */
	public ArrayList<MoveData> listData(CheckFilter fl, boolean revflg) {
		ArrayList<MoveData> ret = new ArrayList<MoveData>();
		int loopnum = 4;
		if (moveData.isBigPuyo()) {
			loopnum = moveData.getColorNum();
		}
		int maxscore = 0;
		boolean firstflg = true;
		for (int i = 0; i < loopnum; i++) {
			MoveData tmpdt = moveData.clone();
			tmpdt.turnRight(i);
			tmpdt.addX(-6, blkData);
			if (!tmpdt.setX(tmpdt.getX(), blkData)) {
				// もし移動できなければ
				if (!tmpdt.setX(tmpdt.getX() + 1, blkData)) {
					// 全然回転できない
					continue;
				}
			}
			for (int x = 0; x < 6; x++) {
				int sc = fl.checkScore(blkData, tmpdt);
				if (sc != CheckFilter.IGNORE_DATA) {
					if (firstflg) {
						maxscore = sc;
						ret.add(tmpdt.clone());
						firstflg = false;
					}
					else if (revflg) {
						// 小さい方を優先
						if (sc < maxscore) {
							ret.clear();
							ret.add(tmpdt.clone());
							maxscore = sc;
						}
						else if (sc == maxscore) {
							ret.add(tmpdt.clone());
						}
					}
					else {
						if (sc > maxscore) {
							ret.clear();
							ret.add(tmpdt.clone());
							maxscore = sc;
						}
						else if (sc == maxscore) {
							ret.add(tmpdt.clone());
						}
					}
				}
				if (tmpdt.addX(1, blkData) != 1) {
					// 移動できなくなったため終了
					break;
				}
			}
		}
		maxValue = maxscore;
		return ret;
	}

	/**
	 * フィルタ条件に合うスコアが最大のものをランダムで返す
	 * @param fl フィルタ
	 * @return 選択されたデータ、対象がなければ null
	 */
	public MoveData selectData(CheckFilter fl) {
		return selectData(fl, false);
	}
	/**
	 * フィルタ条件に合うスコアが最大のものをランダムで返す
	 * @param fl フィルタ
	 * @param revflg true:最小の値を選ぶ
	 * @return 選択されたデータ、対象がなければ null
	 * @param fl
	 */
	public MoveData selectData(CheckFilter fl, boolean revflg) {
		ArrayList<MoveData> ret = listData(fl, revflg);
		if (ret.size() > 0) {
			int n = (int)(Math.random()*ret.size());
			return ret.get(n);
		}
		return null;
	}

	/**
	 * 直前のチェックでの最大値を返す
	 * @return 最大値
	 */
	public int getMaxValue() {
		return maxValue;
	}
}
