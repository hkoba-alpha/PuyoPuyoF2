package puyopuyo.think;

import java.util.ArrayList;

import puyopuyo.think.data.BlockData;
import puyopuyo.think.data.EnvData;
import puyopuyo.think.data.MoveData;
import puyopuyo.think.util.CheckAryData;
import puyopuyo.think.util.CheckAryFilter;

public class TyokoRensaThink extends AbstractPuyoThink {

	private final int MIN_RENSA = 9;

	private int feverNum = -1;

	private int nowRensa;

	private MoveData mvPuyo;

	private ArrayList<int[]> fadeList;

	private MoveData[] mvDtAry;

	private CheckAryData chkAryDt;

	private int limitY;

	private int rensa;

	/**
	 * 連鎖数が増えるところを探すフィルタ
	 * 
	 * @author t-yokotani
	 * 
	 */
	class MostChain implements CheckAryFilter {
		private int minRensa = 0;

		private boolean firstRensa;

		private int goodIndex = -1;

		public MostChain(int minRensa) {
			this.minRensa = minRensa;
		}

		public int checkScore(MoveData[] mvDtAry, BlockData preBlkDt,
				BlockData postBlkDt, ArrayList<int[]> fadeList, int index) {
			if (postBlkDt.getBlock(2, limitY) != BlockData.BLK_SPACE
					|| postBlkDt.getBlock(3, limitY) != BlockData.BLK_SPACE) {
				return IGNORE_DATA;
			}

			if (goodIndex >= 0) {
				if (index <= goodIndex) {
					goodIndex = -1;
				}
			}

			int ret = 0;
			int rensaNum = fadeList.size();

			if (index == 0) {
				if (rensaNum > 0) {
					firstRensa = true;
				} else {
					firstRensa = false;
				}
			}

			int spaceNum = postBlkDt.getBlockNum(BlockData.BLK_SPACE);

			if (spaceNum == 14 * 6) {
				if (goodIndex < 0) {
					goodIndex = index;
				}
				return 100000;
			}

			if ((minRensa > 2 && rensaNum > minRensa) || rensaNum >= rensa) {
				if (goodIndex < 0) {
					goodIndex = index;
				}
				ret += rensaNum * rensaNum * 1000;
			} else {
				ret += rensaNum * rensaNum * 500;
			}

			if (index == mvDtAry.length - 1 && firstRensa) {
				if (goodIndex < 0) {
					return IGNORE_DATA;
				}
			}

			if (rensaNum > nowRensa) {
				nowRensa = rensaNum;
			}

			int[] numAry = preBlkDt.checkChainNum(mvDtAry[index]);
			for (int num : numAry) {
				if (num < 4) {
					ret += num * 200;
				}
			}

			ret += Math.abs(mvDtAry[index].getX() - 2);

			if (rensaNum > 0) {
				ret += 10;
			}

			return ret;
		}
	}

	/**
	 * 連鎖数が大きいところを探すフィルタ
	 * 
	 * @author t-yokotani
	 * 
	 */
	class MostRensa implements CheckAryFilter {
		private int minRensa = 0;

		private int limitNum;

		private int goodIndex = -1;

		public MostRensa(int minRensa, int limitNum) {
			this.minRensa = minRensa;
			this.limitNum = limitNum;
		}

		public int checkScore(MoveData[] mvDtAry, BlockData preBlkDt,
				BlockData postBlkDt, ArrayList<int[]> fadeList, int index) {
			if (postBlkDt.getBlock(2, limitY) != BlockData.BLK_SPACE
					|| postBlkDt.getBlock(3, limitY) != BlockData.BLK_SPACE) {
				return IGNORE_DATA;
			}

			if (goodIndex >= 0) {
				if (index <= goodIndex) {
					goodIndex = -1;
				}
			}

			int ret = 0;

			int rensaNum = fadeList.size();

			int spaceNum = postBlkDt.getBlockNum(BlockData.BLK_SPACE);

			if (spaceNum == 14 * 6) {
				if (goodIndex < 0) {
					goodIndex = index;
				}
				return 100000;
			}

			if (rensaNum >= minRensa) {
				if (goodIndex < 0) {
					goodIndex = index;
				}
				ret += rensaNum * rensaNum * 1000;
			} else {
				ret += rensaNum * rensaNum * 500;
			}

			if (index == mvDtAry.length - 1 || index == limitNum - 1) {
				if (goodIndex < 0) {
					return IGNORE_DATA;
				}
			}

			int[] numAry = preBlkDt.checkChainNum(mvDtAry[index]);
			for (int num : numAry) {
				if (num < 4) {
					ret += num * 200;
				}
			}

			ret += Math.abs(mvDtAry[index].getX() - 2);

			if (rensaNum > 0) {
				ret += 10;
			}

			return ret;
		}

	}

	/**
	 * 連続で消せるところを探すフィルタ
	 * 
	 * @author t-yokotani
	 * 
	 */
	class RenzokuRensa implements CheckAryFilter {
		private int minRensa = 0;

		private int goodIndex = -1;

		private int[] rensaAry;

		public RenzokuRensa(int minRensa, int limitNum) {
			this.minRensa = minRensa;
			this.rensaAry = new int[limitNum];
		}

		public int checkScore(MoveData[] mvDtAry, BlockData preBlkDt,
				BlockData postBlkDt, ArrayList<int[]> fadeList, int index) {
			if (postBlkDt.getBlock(2, limitY) != BlockData.BLK_SPACE
					|| postBlkDt.getBlock(3, limitY) != BlockData.BLK_SPACE) {
				return IGNORE_DATA;
			}

			if (goodIndex >= 0) {
				if (index <= goodIndex) {
					goodIndex = -1;
				}
			}

			int ret = 0;

			int rensaNum = fadeList.size();

			if (goodIndex < 0) {
				if (rensaNum <= 0) {
					return IGNORE_DATA;
				}

				if (index < rensaAry.length) {
					int sumNum = 0;

					rensaAry[index] = rensaNum;
					for (int i = 0; i <= index; i++) {
						sumNum += rensaAry[i];
					}

					if (sumNum >= minRensa) {
						goodIndex = index;
					}
				}

			}

			ret += rensaNum * rensaNum * 1000;

			int[] numAry = preBlkDt.checkChainNum(mvDtAry[index]);
			for (int num : numAry) {
				if (num < 4) {
					ret += num * 200;
				}
			}

			ret += Math.abs(mvDtAry[index].getX() - 2);

			if (rensaNum > 0) {
				ret += 10;
			}

			return ret;
		}

	}

	/**
	 * フィーバー時のフィルタ
	 * 
	 * @author t-yokotani
	 * 
	 */
	class Fever implements CheckAryFilter {
		private int minRensa;

		private int limitNum;

		private int feverIndex = -1;

		public Fever(int minRensa, int limitNum) {
			this.minRensa = minRensa;
			this.limitNum = limitNum;
		}

		public int checkScore(MoveData[] mvDtAry, BlockData preBlkDt,
				BlockData postBlkDt, ArrayList<int[]> fadeList, int index) {
			if (feverIndex >= 0) {
				if (index > feverIndex) {
					return 0;
				} else {
					feverIndex = -1;
				}
			}

			if (postBlkDt.getBlock(2, limitY) != BlockData.BLK_SPACE
					|| postBlkDt.getBlock(3, limitY) != BlockData.BLK_SPACE) {
				return IGNORE_DATA;
			}

			int ret = 0;
			int spaceNum = postBlkDt.getBlockNum(BlockData.BLK_SPACE);

			if (spaceNum == 14 * 6) {
				feverIndex = index;
				return 100000;
			}

			int rensaNum = fadeList.size();

			if (rensaNum > 0) {
				if (rensaNum < minRensa) {
					return IGNORE_DATA;
				} else {
					feverIndex = index;
					ret += rensaNum * rensaNum * 1000;
					ret += spaceNum * 5;
				}
			}

			if (index == mvDtAry.length - 1 || index == limitNum - 1) {
				if (feverIndex < 0) {
					return IGNORE_DATA;
				}
			}

			return ret;
		}
	}

	/**
	 * 連鎖しないフィルタ
	 * 
	 * @author t-yokotani
	 * 
	 */
	class MinRensa implements CheckAryFilter {

		public int checkScore(MoveData[] mvDtAry, BlockData preBlkDt,
				BlockData postBlkDt, ArrayList<int[]> fadeList, int index) {
			if (postBlkDt.getBlock(2, limitY) != BlockData.BLK_SPACE
					|| postBlkDt.getBlock(3, limitY) != BlockData.BLK_SPACE) {
				return IGNORE_DATA;
			}

			int ret = 0;
			int rensaNum = fadeList.size();

			if (index == 0) {
				if (rensaNum <= 0) {
					return IGNORE_DATA;
				}
				ret -= rensaNum * 1000;
			} else {
				ret += rensaNum * rensaNum * 1000;
			}

			int[] numAry = preBlkDt.checkChainNum(mvDtAry[index]);
			for (int num : numAry) {
				if (num < 4) {
					ret += num * 200;
				}
			}

			ret += Math.abs(mvDtAry[index].getX() - 2);

			if (rensaNum > 0) {
				ret += 10;
			}

			return ret;
		}

	}

	/**
	 * フィーバー時に消せない場合のフィルタ
	 * 
	 * @author t-yokotani
	 * 
	 */
	class NotFever implements CheckAryFilter {

		private int feverNum = 0;

		public NotFever(int num) {
			this.feverNum = num;
		}

		public int checkScore(MoveData[] mvDtAry, BlockData preBlkDt,
				BlockData postBlkDt, ArrayList<int[]> fadeList, int index) {
			if (postBlkDt.getBlock(2, limitY) != BlockData.BLK_SPACE
					|| postBlkDt.getBlock(3, limitY) != BlockData.BLK_SPACE) {
				return IGNORE_DATA;
			}

			int ret = 0;

			boolean fever = false;

			if (fadeList.size() > 0) {
				// 消えたらだめ
				return IGNORE_DATA;
			} else {
				if (countFeverNum(postBlkDt, mvDtAry[index].getColorNum()) >= feverNum) {
					fever = true;
				}
			}

			if (!fever) {
				return IGNORE_DATA;
			}

			int[] numAry = preBlkDt.checkChainNum(mvDtAry[index]);
			for (int num : numAry) {
				if (num < 4) {
					ret += num * 200;
				}
			}

			return ret;
		}
	}

	@Override
	protected String getThinkName() {
		return "横谷大連鎖中辛";
	}

	@Override
	protected void think(BlockData meBlk, MoveData mePuyo, EnvData meEnv,
			BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {

		fadeList = new ArrayList<int[]>();
		mvPuyo = null;

		int spaceNum = meBlk.getBlockNum(BlockData.BLK_SPACE);
		// System.out.println("★★spaceNum: " + spaceNum);

		if (spaceNum < 40) {
			rensa = MIN_RENSA - 2;
		} else if (spaceNum < 45) {
			rensa = MIN_RENSA - 1;
		} else {
			rensa = MIN_RENSA;
		}

		eneBlk.putPuyoData(new int[0], new int[0], fadeList);
		int eneRensa = fadeList.size();
		// System.out.println("★★eneRensa: " + eneRensa);

		mvDtAry = new MoveData[meEnv.getNextPuyo().length + 1];
		mvDtAry[0] = mePuyo;
		for (int i = 1; i < mvDtAry.length; i++) {
			mvDtAry[i] = meEnv.getNextPuyo()[i - 1];
		}

		chkAryDt = new CheckAryData(meBlk, mvDtAry);
		int[] ojama = meEnv.getOjamaPuyo();

		limitY = 2;

		if (meEnv.getFeverRestTime() >= 0) {
			if (ojama[0] > 6 || spaceNum < 20 || (ojama[1] > 6 && eneRensa < 2)) {
				// 消したほうがよさそう
				// System.out.println("★★フィーバーあきらめる");
				mvPuyo = chkAryDt.selectData(new Fever(1, 1), 1);
			} else if ((ojama[1] > 3 && eneRensa < 3) || spaceNum < 25) {
				if (feverNum == -1) {
					// 最大連鎖数を求める
					feverNum = countFeverNum(meBlk, mePuyo.getColorNum());

					// System.out.println("★★feverNum: " + feverNum);
				}

				mvPuyo = chkAryDt.selectData(new Fever(feverNum - 1, 2), 2);
			} else {
				if (feverNum == -1) {
					// 最大連鎖数を求める
					feverNum = countFeverNum(meBlk, mePuyo.getColorNum());

					// System.out.println("★★feverNum: " + feverNum);
				}

				mvPuyo = chkAryDt.selectData(new Fever(feverNum, meEnv
						.getFeverRestTime() / 10 + 1));

				if (mvPuyo == null) {
					// 邪魔にならないところに落とす
					// System.out.println("★★邪魔にならないところに落とす");
					mvPuyo = chkAryDt.selectData(new NotFever(feverNum), 1);
				}

				if (mvPuyo == null) {
					// System.out.println("★★邪魔にならないところがない");
					mvPuyo = chkAryDt.selectData(new MostRensa(0, 2), 2);
				}
			}

			if (mvPuyo != null) {
				meBlk.putMoveData(mvPuyo, fadeList);
				if (!fadeList.isEmpty()) {
					feverNum = -1;
				}
			} else {
				// System.out.println("★★いいのがない");
				feverNum = -1;
				limitY = 2;
				mvPuyo = chkAryDt.selectData(new MostRensa(0, 1), 2);
			}
		} else {
			int[] blkY = meBlk.getBlockY();
			boolean blkWarn = false;

			if (blkY[2] <= 3 || blkY[3] <= 3) {
				blkWarn = true;
			}

			if ((ojama[0] > 6 * 4) || (ojama[1] > 6 * 4) && eneRensa < 3) {
				// System.out.println("★★フィーバー狙う");
				nowRensa = 0;
				mvPuyo = chkAryDt.selectData(new RenzokuRensa(7 - meEnv
						.getFeverStockCount(), 3));
				if (mvPuyo == null) {
					// System.out.println("★★狙えない");
					mvPuyo = chkAryDt.selectData(new MostRensa(1, 1), 2);
				}
			} else if (spaceNum < 30 || ojama[0] > 6
					|| (ojama[1] > 6 && eneRensa < 2)) {
				// System.out.println("★★すぐ消す");
				mvPuyo = chkAryDt.selectData(new MostRensa(1, 1));
				nowRensa = 0;
			} else if ((blkWarn && ojama[0] > 0)
					|| (blkWarn && ojama[1] > 0 && eneRensa < 2)) {
				// System.out.println("★★相殺する");
				mvPuyo = chkAryDt.selectData(new MinRensa());
				nowRensa = 0;
			} else if (spaceNum < 35 || (ojama[1] > 6 && eneRensa < 4)) {
				// if (ojama[0] > 0) {
				// limitY = 3;
				// }
				// System.out.println("★★少しあとに消す");
				mvPuyo = chkAryDt.selectData(new MostRensa(nowRensa - 1, 2));

				if (mvPuyo != null) {
					meBlk.putMoveData(mvPuyo, fadeList);
					if (fadeList.size() >= nowRensa - 1) {
						nowRensa = 0;
					}
				} else {
					limitY = 2;
					mvPuyo = chkAryDt.selectData(new MostRensa(1, 2), 2);
					nowRensa = 0;
				}
			} else {
				// if (ojama[0] > 0) {
				// limitY = 3;
				// }
				mvPuyo = chkAryDt.selectData(new MostChain(nowRensa));

				if (mvPuyo != null) {
					meBlk.putMoveData(mvPuyo, fadeList);
					if (fadeList.size() >= nowRensa) {
						nowRensa = 0;
					}
				} else {
					// System.out.println("★★少しだけ消す");
					mvPuyo = chkAryDt.selectData(new MinRensa(), 2);
					nowRensa = 0;
				}
			}

			if (mvPuyo == null) {
				// System.out.println("★★いいのがない");
				nowRensa = 0;
				limitY = 2;
				mvPuyo = chkAryDt.selectData(new MostRensa(0, 1), 2);
			}
		}

		if (mvPuyo == null) {
			// System.out.println("★★どうしようもない");
			mvPuyo = mePuyo;
		}

		// System.out.println("★★nowRensa: " + nowRensa);
		// System.out.println();

		int[] mv = mePuyo.getDiff(mvPuyo);
		moveRight(mv[0]);
		turnRight(mv[1]);
		moveDrop(14);
	}

	private int countFeverNum(BlockData blk, int colorNum) {
		ArrayList<int[]> fadeList = new ArrayList<int[]>();
		int feverNum = 0;
		for (int color = 1; color <= colorNum; color++) {
			for (int x = 0; x < 6; x++) {
				blk.putPuyoData(x, color, fadeList);
				if (fadeList.size() > feverNum) {
					feverNum = fadeList.size();
				}
			}
		}
		return feverNum;
	}
}
