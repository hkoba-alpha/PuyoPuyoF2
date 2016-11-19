/**
 * 
 */
package puyopuyo.think;

import java.awt.Point;
import java.util.ArrayList;

import puyopuyo.think.data.BlockData;
import puyopuyo.think.data.EnvData;
import puyopuyo.think.data.MoveData;

/**
 * @author M
 * 
 */
public class ShimizuThink extends AbstractPuyoThink {

	static int[] limitY = { 0, 5, 6, 6, 5, 0 };

	/*
	 * (非 Javadoc)
	 * 
	 * @see puyopuyo.think.AbstractPuyoThink#getThinkName()
	 */
	@Override
	protected String getThinkName() {
		return "清水ルーチン";
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see puyopuyo.think.AbstractPuyoThink#think(puyopuyo.think.data.BlockData,
	 *      puyopuyo.think.data.MoveData, int[][], int,
	 *      puyopuyo.think.data.BlockData, puyopuyo.think.data.MoveData,
	 *      int[][], int) 思考ルーチン
	 * 
	 * パラメーター: meBlk 自分のブロック mePuyo 自分の移動中ぷよ meNext 自分の次のぷよ meOja
	 * 自分にたまっているおじゃまぷよ eneBlk 敵のブロック enePuyo 敵の移動中ぷよ。移動していなければ null eneNext
	 * 敵の次のぷよ eneOja 敵にたまっているおじゃまぷよ
	 */
	@Override
	protected void think(BlockData meBlk, MoveData mePuyo, EnvData meEnv,
			BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {
		int[] meOja = meEnv.getOjamaPuyo();
		int feverRest = meEnv.getFeverRestTime();

		// どのルートを通ったか判定する。
		int root = -1;

		int[] dy = meBlk.getBlockY();
		boolean fadeflg = false;
		MoveData retPuyo = null;

		// 全消しできるなら実行
		retPuyo = getAllClearData(meBlk, mePuyo.clone());
		if (retPuyo != null) {
			// System.out.println("全消しルート\n");
			int[] mv = mePuyo.getDiff(retPuyo);
			moveRight(mv[0]);
			turnRight(mv[1]);
			moveDrop(14);
			return;
		}
		// NEXTぷよのMoveData
		/*
		 * @param x 中心ぷよのX座標(固定で2) @param y 中心ぷよのY座標(固定で1) @param p 中心ぷよの色
		 * @param nx 隣のぷよのX座標(固定で2) @param ny 隣のぷよのY座標(固定で0) @param np 隣のぷよの色
		 * 
		 * 色は1〜5
		 */
		// MoveData nextPuyo = new MoveData(2, 1, meNext[0][0], 2, 0,
		// meNext[0][1]);
		// // NEXT2ぷよのMoveData
		// MoveData next2Puyo = new MoveData(2, 1, meNext[1][0], 2, 0,
		// meNext[1][1]);
		
		// フィーバールート 適当
		if (feverRest > 0) {
			int maxRensa = getMaxChainSeed(meBlk);
			retPuyo = getMaxChainData(meBlk, mePuyo.clone(), maxRensa);
			if (meOja[0] >= 4) {
				mostFade(meBlk, mePuyo.clone());
				return;
			}
			if (retPuyo == null) {
				retPuyo = getChainPlusData(meBlk, mePuyo.clone(), maxRensa);
			}
			if (retPuyo == null) {
				retPuyo = getMostChain(meBlk, mePuyo.clone());
			}
			if (retPuyo != null) {
				int[] mv = mePuyo.getDiff(retPuyo);
				moveRight(mv[0]);
				turnRight(mv[1]);
				moveDrop(14);
				return;
			}
		}
		
		for (int x = 0; x < 6; x++) {
			if (dy[x] < limitY[x]) {
				fadeflg = true;
				break;
			}
		}
		if (fadeflg || meOja[0] > 4 || meOja[1] > 4) {
			// System.out.println("緊急or相殺ルート");
			retPuyo = getMaxChainData(meBlk, mePuyo.clone(), 2);
			if (retPuyo != null) {
				int[] mv = mePuyo.getDiff(retPuyo);
				moveRight(mv[0]);
				turnRight(mv[1]);
				moveDrop(14);
				return;
			}
			mostFade(meBlk, mePuyo.clone());
			return;
		}

		int myBlank = meBlk.getBlockNum(BlockData.BLK_SPACE);
		int eneBlank = eneBlk.getBlockNum(BlockData.BLK_SPACE);
		int[] eneY = eneBlk.getBlockY();

		// 敵が死にそうなら2連鎖でとどめ
		if (eneBlank <= 30 || eneY[2] <= 2) {
			// System.out.println("とどめルート\n");
			retPuyo = getMaxChainData(meBlk, mePuyo.clone(), 2);
			if (retPuyo != null) {
				int[] mv = mePuyo.getDiff(retPuyo);
				moveRight(mv[0]);
				turnRight(mv[1]);
				moveDrop(14);
				return;
			}
		}

		if (myBlank >= 30) {
			// 4連鎖可能なら実行
			root = 1;
			retPuyo = getMaxChainData(meBlk, mePuyo.clone(), 4);
		}

		// 自分が死にそうなら2連鎖で妥協
		else if (myBlank < 30) {
			root = 1;
			retPuyo = getMaxChainData(meBlk, mePuyo.clone(), 2);
		}

		int nowMaxSeed = getMaxChainSeed(meBlk);
		// System.out.println("最大連鎖種:" + nowMaxSeed);

		// 連鎖をのばす
		if (retPuyo == null) {
			root = 3;
			retPuyo = getChainPlusData(meBlk, mePuyo.clone(), nowMaxSeed);
		}

		if (retPuyo == null) {
			root = 4;
			retPuyo = getMostChain(meBlk, mePuyo.clone());
			// System.out.println("getMostChain");
		}

		if (root == 1) {
			// System.out.println("連鎖実行ルート");
		} else if (root == 3) {
			// System.out.println("連鎖のばしルート");
		} else if (root == 4) {
			// System.out.println("くっつくとこ探す");
		}

		int[] mv = mePuyo.getDiff(retPuyo);
		moveRight(mv[0]);
		turnRight(mv[1]);
		moveDrop(14);
	}

	/**
	 * とにかく一番消えるところへ置く
	 */
	private void mostFade(BlockData meBlk, MoveData mePuyo) {
		MoveData nextpuyo = getMostFade(meBlk, mePuyo.clone());
		int[] mv = mePuyo.getDiff(nextpuyo);
		moveRight(mv[0]);
		turnRight(mv[1]);
		moveDrop(14);
	}

	/**
	 * 一番消えるところを探す
	 * 
	 * @param meBlk
	 * @param mePuyo
	 * @return
	 */
	private MoveData getMostFade(BlockData meBlk, MoveData mePuyo) {
		MoveData ret = null;
		int maxnum = 0;
		for (int turn = 0; turn < 3; turn++) {
			for (int x = 0; x < 6; x++) {
				mePuyo.setX(x, meBlk);
				BlockData blk = meBlk.putMoveData(mePuyo, null);
				int num = blk.getBlockNum(BlockData.BLK_SPACE);
				if (num > maxnum) {
					maxnum = num;
					ret = mePuyo.clone();
				}
			}
			mePuyo.turnLeft();
		}
		if (maxnum <= meBlk.getBlockNum(BlockData.BLK_SPACE)) {
			// どれも消えない
			return getMostChain(meBlk, mePuyo);
		}
		return ret;
	}
	
	/**
	 * 4つ未満で一番長くくっつくところを探す
	 * 
	 * @param meBlk
	 * @param mePuyo
	 * @return
	 */
	private MoveData getMostChain(BlockData meBlk, MoveData mePuyo) {
		MoveData ret = null;
		int maxnum = 0;

		int maxChainSeed = getMaxChainSeed(meBlk);

		// 真中は優先度を低くする。
		int[] checkX = { 2, 1, 0, 0, 1, 2 };
		
		int[] topy = null;
		ArrayList<MoveData> retlst = new ArrayList<MoveData>();
		for (int turn = 0; turn < 4; turn++) {
			for (int x = 0; x < 6; x++) {
				mePuyo.setX(x, meBlk);
				BlockData retBlk = meBlk.putMoveData(mePuyo, null);
				int nowMaxChainSeed = getMaxChainSeed(retBlk);
				// 連鎖が減っていないかチェック
				if (maxChainSeed > nowMaxChainSeed) {
					continue;
				}
				
				topy = retBlk.getBlockY();
				if (topy[2] <= 2 || topy [3] <= 2) {
					continue;
				}
				
				int[] chk = meBlk.checkChainNum(mePuyo);
				if (chk[0] < 4 && chk[1] < 4) {

					// くっつく長さ値？。
					int num = chk[0] * 10 + chk[1] * 10 + checkX[mePuyo.getX()];

					if (num == maxnum) {
						// くっつく長さがMAXなら候補値としてリストに追加しておく。
						retlst.add(mePuyo.clone());
					} else if (num > maxnum) {
						// くっつく長さが現MAXより大きい場合はいままでのリストをクリアして
						// 新しく現在のMoveDataを追加する。
						maxnum = num;
						retlst.clear();
						retlst.add(mePuyo.clone());
					}
				}
			}
			mePuyo.turnLeft();
		}
		if (retlst.size() == 0) {
			ret = mePuyo.clone();
		} else {
			// 候補値の中からランダムで選出する。
			int ix = (int) (Math.random() * retlst.size());
			ret = retlst.get(ix);
		}
		return ret;
	}

	/**
	 * 現在のブロックデータでの最大連鎖を実行するMoveDataを取得する。 期待値以下の場合はnullを返す。
	 * 
	 * @param meBlk
	 * @param mePuyo
	 * @param chainNum
	 *            期待する連鎖数
	 * @return
	 */
	public MoveData getMaxChainData(BlockData meBlk, MoveData mePuyo,
			int chainNum) {
		int maxChain = 0;
		int maxBlank = 0;
		MoveData ret = null;
		ArrayList<int[]> fadeList = new ArrayList<int[]>();
		for (int turn = 0; turn < 4; turn++) {
			for (int x = 0; x < 6; x++) {
				mePuyo.setX(x, meBlk);
				BlockData nowBlk = meBlk.putMoveData(mePuyo, fadeList);
				int nowChain = fadeList.size();
				int nowBlank = nowBlk.getBlockNum(BlockData.BLK_SPACE);

				if (nowChain > maxChain) {
					maxChain = nowChain;
					ret = mePuyo.clone();
				} else if (maxChain == nowChain && nowBlank > maxBlank) {
					maxBlank = nowBlank;
					ret = mePuyo.clone();
				}
			}
			mePuyo.turnRight();
		}

		if (maxChain >= chainNum) {
			return ret;
		}
		return null;
	}

	/**
	 * 現在のブロックの連鎖種の最大連鎖数を取得する。
	 * 
	 * @param meBlk
	 * @return
	 */
	private int getMaxChainSeed(BlockData meBlk) {
		int maxChain = 0;

		ArrayList<int[]> fadeList = new ArrayList<int[]>();

		// とりあえず4色のみで
		Point pnt = new Point(0, 0);
		ArrayList<Point> ptlst = new ArrayList<Point>();
		ArrayList<Integer> cl = new ArrayList<Integer>();
		for (int i = 1; i <= 5; i++) {
			ptlst.add(pnt);
			cl.add(i);
			MoveData mePuyo = new MoveData(ptlst, cl,1);
			for (int turn = 0; turn < 4; turn++) {
				for (int x = 0; x < 6; x++) {
					mePuyo.setX(x, meBlk);
					meBlk.putMoveData(mePuyo, fadeList);
					int nowChain = fadeList.size();
					if (nowChain > maxChain) {
						maxChain = nowChain;
					}
				}
				mePuyo.turnRight();
			}
			ptlst.clear();
			cl.clear();
		}

		return maxChain;
	}

	/**
	 * ぷよを置いた時に連鎖数が伸びるか判定し、最良のデータを返す。 連鎖が伸びない場合はnullを返す。
	 * 
	 * @param meBlk
	 * @param mePuyo
	 * @param nowChain
	 * @return
	 */
	private MoveData getChainPlusData(BlockData meBlk, MoveData mePuyo,
			int nowChain) {
		int maxChain = nowChain;
		int[] maxChainNum = { 0, 0 };
		MoveData ret = null;
		ArrayList<MoveData> retlst = new ArrayList<MoveData>();
		for (int turn = 0; turn < 4; turn++) {
			for (int x = 0; x < 6; x++) {
				mePuyo.setX(x, meBlk);
				BlockData nowBlk = meBlk.putMoveData(mePuyo, null);
				nowChain = getMaxChainSeed(nowBlk);
				int[] chainNum = meBlk.checkChainNum(mePuyo);

				if (chainNum[0] < 4 && chainNum[1] < 4) {
					if (maxChain < nowChain) {
						retlst.clear();
						maxChain = nowChain;
						maxChainNum[0] = chainNum[0];
						maxChainNum[1] = chainNum[1];
						retlst.add(mePuyo.clone());
					}
					if (retlst.size() != 0
							&& maxChain == nowChain
							&& maxChainNum[0] + maxChainNum[1] < chainNum[0]
									+ chainNum[1]) {
						retlst.clear();
						maxChainNum[0] = chainNum[0];
						maxChainNum[1] = chainNum[1];
						retlst.add(mePuyo.clone());
					} else if (retlst.size() != 0
							&& maxChain == nowChain
							&& maxChainNum[0] + maxChainNum[1] == chainNum[0]
									+ chainNum[1]) {
						retlst.add(mePuyo.clone());
					}
				}
			}
			mePuyo.turnLeft();
		}
		if (retlst.size() == 0) {
			ret = null;
		} else {
			// 候補値の中からランダムで選出する。
			int ix = (int) (Math.random() * retlst.size());
			ret = retlst.get(ix);
			// System.out.println("増加後連鎖種:" + maxChain + " list:" + retlst.size());
		}

		return ret;
	}

	/**
	 * 全消しできる場合MoveDataを返す。それ以外はnull。
	 * 
	 * @param meBlk
	 * @param mePuyo
	 * @return
	 */
	private MoveData getAllClearData(BlockData meBlk, MoveData mePuyo) {
		MoveData ret = null;
		ArrayList<int[]> chainList = new ArrayList<int[]>();
		int maxChain = 0;
		for (int turn = 0; turn < 4; turn++) {
			for (int x = 0; x < 6; x++) {
				mePuyo.setX(x, meBlk);
				BlockData blk = meBlk.putMoveData(mePuyo, chainList);
				int nowChain = chainList.size();
				if (blk.getBlockNum(BlockData.BLK_SPACE) == 84
						&& maxChain < nowChain) {
					ret = mePuyo.clone();
					maxChain = nowChain;
				}
			}
			mePuyo.turnLeft();
		}
		return ret;
	}
}
