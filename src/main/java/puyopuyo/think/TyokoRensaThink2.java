package puyopuyo.think;

import java.util.ArrayList;

import puyopuyo.play.PuyoData;
import puyopuyo.think.data.BlockData;
import puyopuyo.think.data.EnvData;
import puyopuyo.think.data.MoveData;
import puyopuyo.think.util.CheckAryData;
import puyopuyo.think.util.CheckAryFilter;

public class TyokoRensaThink2 extends AbstractPuyoThink {

	private final int MIN_RENSA = 9;

	private int feverNum = -1;

	private int nowRensa;

	private MoveData mvPuyo;

	private ArrayList<int[]> fadeList = new ArrayList<int[]>();

	private CheckAryData chkAryDt;

	private int limitY = 2;

	private int rensa;

	private boolean eneMoveFlag = false;

	private int eneRensaNum = 0;

	private int eneNowRensaNum = 0;

	private int eneTopY = 0;

	private int eneSendOjama = 0;

	private boolean eneFeverStarting = false;

	private boolean eneRensaMyFever = false;

	private ArrayList<int[]> eneFadeList = new ArrayList<int[]>();

	private int nowOjama = 0;

	private int nextOjama = 0;

	private boolean toFever = false;

	private boolean waitOjama = false;

	private boolean waitFeverStart = false;

	private boolean waitFeverEnd = false;

	private int nextRoute = 0;

	private PuyoData puyoData;

	private boolean firstCheck = true;

	private int firstBestScore;

	private MoveData firstBestPuyo;

	private int checkRoute = -1;

	private MostChain mostChainFilter = new MostChain();

	private MostRensa mostRensaFilter = new MostRensa();

	private RenzokuRensa renzokuRensaFilter = new RenzokuRensa();

	private MinRensa minRensaFilter = new MinRensa();

	private Fever feverFilter = new Fever();

	private NotFever notFeverFilter = new NotFever();

	/**
	 * 連鎖数が増えるところを探すフィルタ
	 * 
	 * @author t-yokotani
	 * 
	 */
	class MostChain implements CheckAryFilter {
		private int minRensa;

		private boolean firstRensa;

		private int goodIndex;

		public void init(int minRensa) {
			this.minRensa = minRensa;
			this.firstRensa = false;
			this.goodIndex = -1;
		}

		public int checkScore(MoveData[] mvDtAry, BlockData preBlkDt,
				BlockData postBlkDt, ArrayList<int[]> fadeList, int index) {
			int[] blkY = postBlkDt.getBlockY();
			if (blkY[2] <= limitY || blkY[3] <= limitY) {
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
				return 6 * 6 * 1000;
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

			ret += blkY[2] + blkY[3];

			if (rensaNum > 0) {
				ret += 50;
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
		private int minRensa;

		private int limitNum;

		private int goodIndex;

		public void init(int minRensa, int limitNum) {
			this.minRensa = minRensa;
			this.limitNum = limitNum;
			this.goodIndex = -1;
		}

		public int checkScore(MoveData[] mvDtAry, BlockData preBlkDt,
				BlockData postBlkDt, ArrayList<int[]> fadeList, int index) {
			int[] blkY = postBlkDt.getBlockY();
			if (blkY[2] <= limitY || blkY[3] <= limitY) {
				return IGNORE_DATA;
			}

			if (goodIndex >= 0) {
				if (index <= goodIndex) {
					goodIndex = -1;
				}
			}

			int ret = 0;

			int rensaNum = fadeList.size();

			if (rensaNum > nowRensa) {
				nowRensa = rensaNum;
			}

			int spaceNum = postBlkDt.getBlockNum(BlockData.BLK_SPACE);

			if (spaceNum == 14 * 6) {
				if (goodIndex < 0) {
					goodIndex = index;
				}
				return 6 * 6 * 1000;
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

			ret += blkY[2] + blkY[3];

			if (rensaNum > 0) {
				ret += 50;
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
		private int minRensa;

		private int goodIndex;

		private int[] rensaAry;

		public void init(int minRensa, int limitNum) {
			this.minRensa = minRensa;
			this.goodIndex = -1;
			this.rensaAry = new int[limitNum];
		}

		public int checkScore(MoveData[] mvDtAry, BlockData preBlkDt,
				BlockData postBlkDt, ArrayList<int[]> fadeList, int index) {
			int[] blkY = postBlkDt.getBlockY();
			if (blkY[2] <= limitY || blkY[3] <= limitY) {
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

			ret += blkY[2] + blkY[3];

			if (rensaNum > 0) {
				ret += 50;
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
			int[] blkY = postBlkDt.getBlockY();
			if (blkY[2] <= limitY || blkY[3] <= limitY) {
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

			ret += blkY[2] + blkY[3];

			if (rensaNum > 0) {
				ret += 50;
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

		private int feverIndex;

		public void init(int minRensa, int limitNum) {
			this.minRensa = minRensa;
			this.limitNum = limitNum;
			this.feverIndex = -1;
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
	 * フィーバー時に消せない場合のフィルタ
	 * 
	 * @author t-yokotani
	 * 
	 */
	class NotFever implements CheckAryFilter {

		private int feverNum;

		public void init(int num) {
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
		return "横谷大連鎖辛口2";
	}

	@Override
	protected void think(BlockData meBlk, MoveData mePuyo, EnvData meEnv,
			BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {

		nowOjama = meEnv.getOjamaPuyo()[0];
		nextOjama = meEnv.getOjamaPuyo()[1];

		if (waitOjama || waitFeverStart || waitFeverEnd) {
			if (waitOjama && !checkWaitOjama()) {
				waitOjama = false;
			}
			if (waitFeverStart && !checkWaitFeverStart()) {
				waitFeverStart = false;
			}
			if (waitFeverEnd && !checkWaitFeverEnd(eneEnv)) {
				waitFeverEnd = false;
			}
			if (!(waitOjama || waitFeverStart || waitFeverEnd)) {
				moveDrop(14);
			}
			return;
		}

		int spaceNum = meBlk.getBlockNum(BlockData.BLK_SPACE);
		// System.out.println("★★route: " + route);

		if (spaceNum < 40) {
			rensa = MIN_RENSA - 2;
		} else if (spaceNum < 45) {
			rensa = MIN_RENSA - 1;
		} else {
			rensa = MIN_RENSA;
		}

		chkAryDt = new CheckAryData(meBlk, mePuyo, meEnv.getNextPuyo()[0],
				meEnv.getNextPuyo()[1]);

		if (meEnv.getFeverRestTime() >= 0) {
			nextRoute = 0;
			if (feverThink(meBlk, mePuyo, meEnv, eneEnv, spaceNum)) {
				return;
			}
		} else {
			if (normalThink(meBlk, mePuyo, meEnv, eneEnv, spaceNum)) {
				return;
			}
		}

		if (mvPuyo == null) {
			// System.out.println("★★どうしようもない");
			mostRensaFilter.init(0, 1);
			mvPuyo = chkAryDt.selectData(mostRensaFilter, 2);
		}

		if (mvPuyo == null) {
			mvPuyo = mePuyo;
		}

		int[] mv = mePuyo.getDiff(mvPuyo);
		moveRight(mv[0]);
		turnRight(mv[1]);

		if (!(waitOjama || waitFeverStart || waitFeverEnd)) {
			moveDrop(14);
		}

		mvPuyo = null;
		// System.out.println("★★eneFeverStart: " + eneFeverStart);
		// System.out.println("★★spaceNum: " + spaceNum);
		// System.out.println("★★nowRensa: " + nowRensa);
		// System.out.println("★★eneRensaNum: " + eneRensaNum);
		// System.out.println("★★eneNowRensaNum: " + eneNowRensaNum);
		// System.out.println("★★eneSendOjama: " + eneSendOjama);
		// System.out.println("★★nowOjama: " + nowOjama);
		// System.out.println("★★nextOjama: " + nextOjama);
		// System.out.println();
	}

	private boolean normalThink(BlockData meBlk, MoveData mePuyo,
			EnvData meEnv, EnvData eneEnv, int spaceNum) {
		int nextSumOjama = nextOjama + eneSendOjama;
		// System.out.println("★★route: " + route);
		// System.out.println("★★firstCheck: " + firstCheck);

		switch (nextRoute) {
		case 0:
			int[] blkY = meBlk.getBlockY();
			boolean blkWarn = false;

			if (blkY[2] <= 3 || blkY[3] <= 3) {
				blkWarn = true;
			}

			if (nowOjama + nextSumOjama <= 6 * 2) {
				toFever = false;
			}

			if (toFever) {
				// System.out.println("★★フィーバー狙う");
				if (checkRoute != 0) {
					firstCheck = true;
				}
				if (firstCheck) {
					checkRoute = 0;
					renzokuRensaFilter.init(7 - meEnv.getFeverStockCount(), 3);
				}
				if (selectData(renzokuRensaFilter)) {
					return true;
				}

				if (mvPuyo != null && checkWaitOjama()) {
					// System.out.println("★★おじゃま待つ");
					waitOjama = true;
				}
			} else if (spaceNum < 30 || nowOjama > 6
					|| (nextSumOjama > 6 && eneNowRensaNum < 2)) {
				// System.out.println("★★すぐ消す");
				if (checkRoute != 1) {
					firstCheck = true;
				}
				if (firstCheck) {
					checkRoute = 1;
					mostRensaFilter.init(1, 1);
				}
				if (selectData(mostRensaFilter)) {
					return true;
				}
				nowRensa = 0;

				if (mvPuyo != null) {
					meBlk.putMoveData(mvPuyo, fadeList);
					nextRoute = 1;
					return true;
				}
			} else if ((blkWarn && nowOjama > 0)
					|| (blkWarn && nextSumOjama > 0 && eneNowRensaNum < 2)) {
				// System.out.println("★★相殺する");
				if (checkRoute != 2) {
					firstCheck = true;
				}
				checkRoute = 2;
				if (selectData(minRensaFilter)) {
					return true;
				}
				nowRensa = 0;
			} else if (spaceNum < 35
					|| (nextSumOjama > 6 && eneNowRensaNum < 3)) {
				// System.out.println("★★少しあとに消す");
				if (checkRoute != 3) {
					firstCheck = true;
				}
				if (firstCheck) {
					checkRoute = 3;
					mostRensaFilter.init(nowRensa - 1, 2);
				}
				if (selectData(mostRensaFilter)) {
					return true;
				}

				if (mvPuyo != null) {
					meBlk.putMoveData(mvPuyo, fadeList);
					if (fadeList.size() >= nowRensa - 1) {
						nowRensa = 0;
						nextRoute = 1;
						return true;
					}
				} else {
					// System.out.println("★★少しあとに消せない");
					nowRensa = 0;
					nextRoute = 2;
					return true;
				}
			} else {
				// System.out.println("★★連鎖増やす");
				if (checkRoute != 4) {
					firstCheck = true;
				}
				if (firstCheck) {
					checkRoute = 4;
					mostChainFilter.init(nowRensa);
				}
				if (selectData(mostChainFilter)) {
					return true;
				}

				if (mvPuyo != null) {
					meBlk.putMoveData(mvPuyo, fadeList);
					if (fadeList.size() >= nowRensa) {
						nowRensa = 0;
						nextRoute = 1;
						return true;
					}
				} else {
					// System.out.println("★★少しだけ消す");
					nowRensa = 0;
					nextRoute = 3;
					return true;
				}
			}
			break;

		case 1:
			if (firstCheck) {
				if (checkAfterPut(meEnv, nowOjama + nextSumOjama, fadeList)) {
					return true;
				}
			} else {
				checkAfterPut(meEnv, nowOjama + nextSumOjama, fadeList);
				toFever = true;
				if (mvPuyo != null && checkWaitOjama()) {
					// System.out.println("★★おじゃま待つ");
					waitOjama = true;
				}
			}
			break;

		case 2:
			if (checkRoute != 5) {
				firstCheck = true;
			}
			if (firstCheck) {
				checkRoute = 5;
				mostRensaFilter.init(1, 2);
			}
			if (selectData(mostRensaFilter)) {
				return true;
			}

			if (mvPuyo != null) {
				meBlk.putMoveData(mvPuyo, fadeList);
				nextRoute = 1;
				return true;
			}
			break;

		case 3:
			if (checkRoute != 6) {
				firstCheck = true;
			}
			checkRoute = 6;
			if (selectData(minRensaFilter)) {
				return true;
			}
			break;
		}

		if (mvPuyo == null) {
			// System.out.println("★★いいのがない");
			mostRensaFilter.init(1, 1);
			mvPuyo = chkAryDt.selectData(mostRensaFilter, 2);
			nowRensa = 0;
		}

		if (mvPuyo != null) {
			meBlk.putMoveData(mvPuyo, fadeList);
			if (fadeList.size() > 0) {
				int[] sendOjamaAry = puyoData.getSendOjamaList(fadeList, 1,
						false, true);
				int sosaiNum = 0;
				int ojama = nowOjama + nextOjama;
				for (int sendOjama : sendOjamaAry) {
					if (ojama > 0) {
						sosaiNum++;
					}
					ojama -= sendOjama;
				}

				if (meEnv.getFeverStockCount() + sosaiNum >= 7) {
					// System.out.println("★★フィーバー突入");
					if (checkWaitFeverEnd(eneEnv)) {
						// System.out.println("★★フィーバー終了待つ");
						waitFeverEnd = true;
					}
				}
			}
		}

		nextRoute = 0;
		firstCheck = true;
		return false;
	}

	private boolean checkAfterPut(EnvData meEnv, int ojama,
			ArrayList<int[]> fadeList) {
		int leftOjama = ojama - countSendOjama(meEnv, fadeList, 1);
		// System.out.println("★★leftOjama: " + leftOjama);

		if (leftOjama > 6 * 2) {
			// System.out.println("★★フィーバー狙う");
			if (checkRoute != 7) {
				firstCheck = true;
			}
			if (firstCheck) {
				checkRoute = 7;
				renzokuRensaFilter.init(7 - meEnv.getFeverStockCount(), 3);
			}
			selectData(renzokuRensaFilter);
			return true;
		} else if (leftOjama <= 0 && fadeList.size() > 2) {
			if (checkWaitFeverStart()) {
				// System.out.println("★★フィーバー開始待つ");
				waitFeverStart = true;
			}
		}
		return false;
	}

	private boolean checkWaitOjama() {
		return (nowOjama + nextOjama) <= 0;
	}

	private boolean checkWaitFeverStart() {
		return eneFeverStarting;
	}

	private boolean checkWaitFeverEnd(EnvData eneEnv) {
		int restTime = eneEnv.getFeverRestTime();
		return restTime >= 0 && restTime <= 30
				&& (restTime - eneNowRensaNum * 10) > 0;
	}

	private boolean feverThink(BlockData meBlk, MoveData mePuyo, EnvData meEnv,
			EnvData eneEnv, int spaceNum) {
		toFever = false;
		int nextSumOjama = nextOjama;
		if (eneRensaMyFever) {
			nextSumOjama += eneSendOjama;
		}

		if (spaceNum < 20
				|| nowOjama > 6
				|| (nextSumOjama > 6 && eneNowRensaNum < 2)
				|| (meEnv.getOjamaPuyo()[2] > 6 * 2 && meEnv.getFeverRestTime() < 10)) {
			// 消したほうがよさそう
			// System.out.println("★★フィーバーあきらめる");
			feverFilter.init(1, 1);
			mvPuyo = chkAryDt.selectData(feverFilter, 1);
		} else if (spaceNum < 25
				|| (nextSumOjama > 6 && eneNowRensaNum < 3)
				|| (meEnv.getOjamaPuyo()[2] > 6 * 2 && meEnv.getFeverRestTime() < 20)) {
			// System.out.println("★★フィーバー少しあとに消す");
			if (feverNum == -1) {
				// 最大連鎖数を求める
				feverNum = countFeverNum(meBlk, mePuyo.getColorNum());

				// System.out.println("★★feverNum: " + feverNum);
			}
			feverFilter.init(feverNum - 1, 2);
			mvPuyo = chkAryDt.selectData(feverFilter, 2);
		} else {
			// System.out.println("★★フィーバーする");
			if (feverNum == -1) {
				// 最大連鎖数を求める
				feverNum = countFeverNum(meBlk, mePuyo.getColorNum());

				// System.out.println("★★feverNum: " + feverNum);
			}

			if (checkRoute != 8) {
				firstCheck = true;
			}
			if (firstCheck) {
				checkRoute = 8;
				feverFilter.init(feverNum, meEnv.getFeverRestTime() / 10 + 1);
			}
			if (selectData(feverFilter)) {
				return true;
			}

			if (mvPuyo == null) {
				// 邪魔にならないところに落とす
				// System.out.println("★★邪魔にならないところに落とす");
				notFeverFilter.init(feverNum);
				mvPuyo = chkAryDt.selectData(notFeverFilter, 1);
			}

			if (mvPuyo == null) {
				// System.out.println("★★邪魔にならないところがない");
				mostRensaFilter.init(0, 2);
				mvPuyo = chkAryDt.selectData(mostRensaFilter, 2);
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
			mostRensaFilter.init(1, 1);
			mvPuyo = chkAryDt.selectData(mostRensaFilter, 2);
		}
		return false;
	}

	private boolean selectData(CheckAryFilter fl) {
		if (firstCheck) {
			firstBestPuyo = chkAryDt.selectData(fl, 3, 0, 2);
			firstBestScore = chkAryDt.getMaxValue();
			firstCheck = false;
			return true;
		} else {
			mvPuyo = chkAryDt.selectData(fl, 3, 1, 2);
			if (chkAryDt.getMaxValue() < firstBestScore) {
				mvPuyo = firstBestPuyo;
			}
			firstCheck = true;
			return false;
		}
	}

	public void waitEvent(PuyoData puyo, BlockData meBlk, EnvData meEnv,
			BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {
		puyoData = puyo;
		waitOjama = false;
		waitFeverStart = false;
		waitFeverEnd = false;
		nextRoute = 0;
		firstCheck = true;
		checkRoute = -1;
		eneRensaCheck(eneBlk, enePuyo, eneEnv, meEnv);
		super.waitEvent(puyo, meBlk, meEnv, eneBlk, enePuyo, eneEnv);
	}

	public void nextMoveEvent(PuyoData puyo, BlockData meBlk, MoveData mePuyo,
			EnvData meEnv, BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {
		puyoData = puyo;
		eneRensaCheck(eneBlk, enePuyo, eneEnv, meEnv);
		super
				.nextMoveEvent(puyo, meBlk, mePuyo, meEnv, eneBlk, enePuyo,
						eneEnv);
	}

	private void eneRensaCheck(BlockData eneBlk, MoveData enePuyo,
			EnvData eneEnv, EnvData meEnv) {
		if (enePuyo == null) {
			if (eneMoveFlag) {
				eneMoveFlag = false;
				eneBlk.putPuyoData(new int[0], new int[0], eneFadeList);
				eneRensaNum = eneFadeList.size();
				eneNowRensaNum = eneRensaNum;

				if (eneRensaNum > 0) {
					countEneSendOjama(eneEnv, eneFadeList);

					eneRensaMyFever = (meEnv.getFeverRestTime() >= 0);

					eneTopY = getTopY(eneBlk);
				}
			} else {
				if (eneRensaNum > 0) {
					int nowTopY = getTopY(eneBlk);

					if (eneTopY != nowTopY) {
						eneTopY = nowTopY;
						eneFadeList.remove(0);
						eneNowRensaNum--;

						countEneSendOjama(eneEnv, eneFadeList);

						if (eneNowRensaNum == 0) {
							eneRensaNum = 0;
						}
					}
				}
			}
		} else {
			eneMoveFlag = true;
			eneFeverStarting = false;
		}
	}

	private void countEneSendOjama(EnvData eneEnv, ArrayList<int[]> fadeList) {
		int eneOjama = 0;
		for (int ojamaNum : eneEnv.getOjamaPuyo()) {
			eneOjama += ojamaNum;
		}

		int[] sendOjamaAry = puyoData.getSendOjamaList(fadeList, eneRensaNum
				- eneNowRensaNum + 1, eneEnv.getFeverRestTime() >= 0, false);
		int sosaiNum = 0;
		for (int sendOjama : sendOjamaAry) {
			if (eneOjama > 0) {
				sosaiNum++;
			}
			eneOjama -= sendOjama;
		}

		if (eneEnv.getFeverRestTime() < 0) {
			eneFeverStarting = (eneEnv.getFeverStockCount() + sosaiNum) >= 7;
		}

		eneSendOjama = -eneOjama;
	}

	private int countSendOjama(EnvData env, ArrayList<int[]> fadeList,
			int startRensaNum) {
		int[] sendOjamaAry = puyoData.getSendOjamaList(fadeList, startRensaNum,
				env.getFeverRestTime() >= 0, true);
		int sendOjamaNum = 0;
		for (int ojamaNum : sendOjamaAry) {
			sendOjamaNum += ojamaNum;
		}
		return sendOjamaNum;
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

	private int getTopY(BlockData blk) {
		int rtn = 0;
		for (int topY : blk.getBlockY()) {
			rtn |= topY;
			rtn <<= 4;
		}
		return rtn;
	}

}
