package puyopuyo.think.util;

import java.util.ArrayList;

import puyopuyo.think.data.BlockData;
import puyopuyo.think.data.MoveData;

/**
 * 複数のぷよの移動パターンについてチェックする
 * 
 * @author t-yokotani
 * 
 */
public class CheckAryData {

	protected BlockData blkData;

	protected MoveData[] mvDataAry;

	protected int maxScore;

	protected boolean addFlag;

	protected int bestPuyoNum;

	protected MoveData[] bestPuyoAry = new MoveData[30];

	protected ArrayList<int[]> fadeList = new ArrayList<int[]>();

	protected int[] loopNumAry;

	public CheckAryData(BlockData blkdt, MoveData... mvDtAry) {
		blkData = blkdt;
		mvDataAry = copyDataAry(mvDtAry);
		loopNumAry = new int[mvDataAry.length];
		for (int i = 0; i < mvDataAry.length; i++) {
			int loopNum = 4;

			if (mvDataAry[i].isBigPuyo()) {
				loopNum = mvDataAry[i].getColorNum();
			} else {
				int[] color = mvDataAry[i].getPuyoColor();
				if (color.length == 2 && color[0] == color[1]) {
					// １色の２個組の場合
					loopNum = 2;
				}
			}
			loopNumAry[i] = loopNum;
		}
	}

	public MoveData selectData(CheckAryFilter fl) {
		return selectData(fl, mvDataAry.length);
	}

	public MoveData selectData(CheckAryFilter fl, int dataNum) {
		return selectData(fl, dataNum, 0, 1);
	}

	public MoveData selectData(CheckAryFilter fl, int dataNum,
			int loopStartNum, int loopAddNum) {
		maxScore = CheckAryFilter.IGNORE_DATA;
		bestPuyoNum = 0;

		if (dataNum > mvDataAry.length) {
			dataNum = mvDataAry.length;
		}

		MoveData[] checkData = new MoveData[dataNum];

		for (int i = 0; i < checkData.length; i++) {
			checkData[i] = mvDataAry[i];
		}

		checkPuyo(fl, blkData, checkData, 0, 0, loopStartNum, loopAddNum);

		if (bestPuyoNum == 0) {
			return null;
		} else {
			int n = (int) (Math.random() * bestPuyoNum);
			return bestPuyoAry[n];
		}
	}

	public int getMaxValue() {
		return maxScore;
	}

	protected void checkPuyo(CheckAryFilter fl, BlockData blkdt,
			MoveData[] mvDtAry, int index, int score, int loopStartNum,
			int loopAddNum) {

		int tmpScore = 0;
		BlockData nextBlk;
		MoveData nowData = mvDtAry[index];

		nowData.turnRight(loopStartNum);

		for (int i = loopStartNum; i < loopNumAry[index]; i += loopAddNum) {
			nowData.addX(-6, blkdt);
			if ((nowData.setX(nowData.getX(), blkdt))
					|| (nowData.setX(nowData.getX() + 1, blkdt))) {
				for (int x = 0; x < 6; x++) {
					if (index == 0) {
						addFlag = false;
					}
					nextBlk = blkdt.putMoveData(nowData, fadeList);
					tmpScore = fl.checkScore(mvDtAry, blkdt, nextBlk, fadeList,
							index);

					if (tmpScore != CheckAryFilter.IGNORE_DATA) {
						tmpScore += score;
						if (index < mvDtAry.length - 1) {
							checkPuyo(fl, nextBlk, mvDtAry, index + 1,
									tmpScore, 0, 1);
						} else {
							if (tmpScore > maxScore) {
								maxScore = tmpScore;
								bestPuyoAry[0] = mvDtAry[0].clone();
								bestPuyoNum = 1;
								addFlag = true;
							} else if (tmpScore == maxScore && !addFlag) {
								bestPuyoAry[bestPuyoNum++] = mvDtAry[0].clone();
								addFlag = true;
							}
						}
					}

					if (nowData.addX(1, blkdt) != 1) {
						break;
					}
				}
			}

			nowData.turnRight(loopAddNum);
		}
	}

	private MoveData[] copyDataAry(MoveData[] src) {
		MoveData[] ret = new MoveData[src.length];
		for (int i = 0; i < src.length; i++) {
			ret[i] = src[i].clone();
		}
		return ret;
	}
}
