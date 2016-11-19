package puyopuyo.think;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import puyopuyo.play.PuyoData;
import puyopuyo.think.data.BlockData;
import puyopuyo.think.data.EnvData;
import puyopuyo.think.data.MoveData;
import puyopuyo.think.util.CheckData;
import puyopuyo.think.util.CheckFilter;

public class HkobaThink5 extends AbstractPuyoThink {

	/**
	 * 敵の状態
	 * @author hkoba
	 *
	 */
	class EneState {
		/**
		 * 消えている時の連鎖数
		 */
		int fadeRestCount;

		/**
		 * 消えているときの最大数
		 */
		int fadeMaxCount;

		/**
		 * 最後のぷよの数
		 */
		int lastPuyoCount;

		/**
		 * 今までの最大連鎖数
		 */
		int maxRensa;

		/**
		 * 現在連鎖数
		 */
		int nowRensa;

		/**
		 * 落ちてくる予定のおじゃまぷよ
		 */
		int planOjama;

		/**
		 * フィーバーの連鎖数
		 */
		int feverRensa;

		void checkPuyo(BlockData blk, EnvData env) {
			int num = 6 * 14 - blk.getBlockNum(BlockData.BLK_SPACE);
			if (num == lastPuyoCount) {
				return;
			}
			lastPuyoCount = num;
			// 消えているかをチェックする
			ArrayList<int[]> lst = new ArrayList<int[]>();
			blk.putPuyoData(new int[0], new int[0], lst);
			if (lst.size() > 0) {
				// 消えている
				if (fadeRestCount == 0) {
					// 消え始めた
					nowRensa = maxRensa = 0;
					fadeMaxCount = lst.size();
				}
				fadeRestCount = lst.size();
				planOjama = getPlanOjama(fadeMaxCount - fadeRestCount, lst, env.getFeverRestTime() >= 0);
				for (int oja: env.getOjamaPuyo()) {
					planOjama -= oja;
				}
				if (planOjama < 0) {
					planOjama = 0;
				}
				System.out.println("消えている:" + fadeRestCount);
				feverRensa = 0;
				return;
			}
			fadeRestCount = 0;
			planOjama = 0;
			// 2つ同じ色を縦においてチェックする
			int[] rensanum = new int[6];
			int[] ojanum = new int[6];
			nowRensa = checkRensaNum(blk, rensanum, ojanum);
			if (nowRensa > maxRensa) {
				// 最大連鎖数に設定
				maxRensa = nowRensa;
				if (env.getFeverRestTime() >= 0) {
					feverRensa = maxRensa;
				}
			}
			System.out.println("連鎖数："+nowRensa + "/" + maxRensa);
		}
	}

	/**
	 * 移動可能データを作るためのチェックフィルタ
	 * @author hkoba
	 *
	 */
	static class MakeMoveData implements CheckFilter {
		HashMap<Integer, MoveData> posMap = new HashMap<Integer, MoveData>();

		public int checkScore(BlockData blkdt, MoveData mvdt) {
			Point[] ptlst = mvdt.getPoint();
			int[] cldt = mvdt.getPuyoColor();
			int mx = 6;
			int my = 14;
			int[] miny = new int[6];
			for (int i = 0; i < 6; i++) {
				miny[i] = 15;
			}
			for (Point pt: ptlst) {
				if (miny[pt.x] > pt.y) {
					miny[pt.x] = pt.y;
				}
				if (pt.x < mx) {
					mx = pt.x;
				}
				if (pt.y < my) {
					my = pt.y;
				}
			}
			int flag = mx << 16;
			for (int i = 0; i < ptlst.length; i++) {
				int ax = ptlst[i].x - mx;
				int ay = ptlst[i].y - miny[ptlst[i].x];
				flag |= (cldt[i]<<(ay * 8 + ax * 4));
			}
			posMap.put(flag, mvdt.clone());
			return 0;
		}

		ArrayList<MoveData> getResult() {
			return new ArrayList<MoveData>(posMap.values());
		}
	}

	/**
	 * 移動状態をチェックする
	 * @param blk
	 * @param puyo
	 * @return
	 */
	static ArrayList<MoveData> getAllMove(BlockData blk, MoveData puyo) {
		CheckData chk = new CheckData(blk, puyo);
		MakeMoveData mkdt = new MakeMoveData();
		chk.selectData(mkdt);
		return mkdt.getResult();
	}

	/**
	 * 移動チェック用で判定する
	 * @author hkoba
	 *
	 */
	interface MoveCheckFilter {
		int checkMove(BlockData blk, ArrayList<int[]> rensa);
	}

	/**
	 * 移動チェック用のぷよデータ
	 * @author hkoba
	 *
	 */
	class CheckMovePuyo {
		ArrayList<MoveData> moveList;

		CheckMovePuyo(BlockData blk, MoveData puyo) {
			moveList = getAllMove(blk, puyo);
		}

		int checkData(BlockData blk, MoveCheckFilter filter) {
			int retnum = 0;
			for (MoveData mvdt: moveList) {
				ArrayList<int[]> lst = new ArrayList<int[]>();
				BlockData nxt = blk.putMoveData(mvdt, lst);
				int num = filter.checkMove(nxt, lst);
				if (num > retnum) {
					retnum = num;
				}
			}
			return retnum;
		}
	}

	/**
	 * おじゃまぷよを仮想的に落とす
	 * @param blk
	 * @param oja
	 * @return
	 */
	static BlockData dropOjama(BlockData blk, int[] oja) {
		int num = oja[0];
		if (num > 30) {
			num = 30;
		}
		if (num == 0) {
			return blk;
		}
		oja[0] -= num;
		int[] drop = new int[((num + 5) / 6) * 6];
		int[] xp = new int[drop.length];
		for (int i = 0; i < xp.length; i++) {
			drop[i] = BlockData.BLK_OJAMA;
			xp[i] = i % 6;
		}
		return blk.putPuyoData(xp, drop, null);
	}

	static int[] normalBonus = {
		8,16,22,34,67,111,167,223,280,350,421
	};
	static int[] feverBonus = {
		6,12,15,20,33, 55, 83,111,167,195,201,239,279,307,335
	};

	/**
	 * 連鎖数に応じた予定されているおじゃまぷよ
	 * @param rensa
	 * @param fevflag
	 * @return
	 */
	int getPlanOjama(int rensa, boolean fevflag) {
		ArrayList<int[]> lst = new ArrayList<int[]>();
		for (int i = 0; i < rensa; i++) {
			lst.add(new int[]{4});
		}
		return getPlanOjama(0, lst, fevflag);
	}

	/**
	 * 連鎖数に応じた予定されているおじゃまぷよ
	 * @param stnum 連鎖済みの数
	 * @param rensalst
	 * @param fevflag
	 * @return
	 */
	int getPlanOjama(int stnum, ArrayList<int[]> rensalst, boolean fevflag) {
		int[] rensaBai = fevflag ? feverBonus: normalBonus;
		int score = 0;
		for (int i = 0; i < rensalst.size(); i++) {
			int num = stnum + i;
			if (num >= rensaBai.length) {
				num = rensaBai.length - 1;
			}
			int[] dt = rensalst.get(i);
			int sc = dt.length * 3 - 3;
			sc += rensaBai[num];
			int punum = 0;
			for (int j = 0; j < dt.length; j++) {
				punum += dt[j];
				sc += (dt[j] - 4);
			}
			if (sc == 0) {
				sc = 1;
			}
			score += (punum * sc);
		}
		return (score + 11) / 12;
	}

	/**
	 * フィーバーの連鎖が終わった後の残り時間
	 * @param fevtm
	 * @param rensa
	 * @return
	 */
	static int afterFadeFeverTime(int fevtm, int rensa) {
		fevtm -= (rensa * 0.9);
		if (fevtm <= 0) {
			fevtm = 0;
		}
		else {
			if (rensa > 2) {
				fevtm += (rensa - 2) * 5;
			}
		}
		return fevtm;
	}

	/**
	 * 数回に分けてチェックする
	 * @author hkoba
	 *
	 */
	class DelayMoveCheck implements MoveCheckFilter {
		/**
		 * 1ターンでチェックできる最大数
		 */
		int maxCheckNum;

		ArrayList<MoveData> moveList;
		int[] resultData;
		MoveData resultObj;

		int checkNum;
		BlockData blockData;
		int tmpCheckCount;
		int[] ojamaNum;

		/**
		 * 予定以上の連鎖をした時の追加するスコア
		 */
		int[] rensaScore;

		MoveData[] nextPuyo;
		/**
		 * 全消しを有効にするかどうかのフラグ
		 */
		boolean zenkesiFlag;

		DelayMoveCheck(BlockData blk, MoveData mvdt, EnvData env, int maxnum) {
			moveList = getAllMove(blk, mvdt);
			blockData = blk;
			maxCheckNum = maxnum;
			resultData = new int[moveList.size()];
			checkNum = 0;
			ojamaNum = env.getOjamaPuyo().clone();
			if (eneState.fadeRestCount < 2) {
				// 敵の連鎖がすぐに終わりそう
				ojamaNum[1] += eneState.planOjama;
				ojamaNum[0] += ojamaNum[1];
				ojamaNum[1] = 0;
			}
			nextPuyo = env.getNextPuyo().clone();
			rensaScore = new int[20];
			for (int i = 14; i < rensaScore.length; i++) {
				rensaScore[i] = 0x20000;
			}
			// つみあがってきたときは、残り少なくても連鎖する
			int[] topy = blk.getBlockY();
			// フィーバーの残り時間によって先読みを変える
			if (env.getFeverRestTime() >= 0) {
				for (int i = 0; i < rensaScore.length; i++) {
					if (i < feverTryRensa - 1) {
						rensaScore[i] -= 0x400;
					}
					else {
						rensaScore[i] += 0x100;
					}
				}
				if (env.getFeverRestTime() < 10) {
					// 1秒未満
					nextPuyo[0] = null;
				}
				if (env.getFeverRestTime() < 20) {
					// 2秒未満
					nextPuyo[1] = null;
				}
				else {
					// 残り時間で調整
					int rest = afterFadeFeverTime(env.getFeverRestTime(), feverTryRensa);
					if (rest < 0) {
						// もう消してもダメ
						// ちょっと多めに消したい
						System.out.println("フィーバー終わり");
						for (int i = feverTryRensa; i < 14; i++) {
							rensaScore[i] -= 0x100;
						}
					}
					else {
						// 残り時間はある
						if (afterFadeFeverTime(rest - 20, feverTryRensa + 1) > 0) {
							// 2回以上消せる
						}
						else {
							// 残り1回
							int tm = 19;
							while (afterFadeFeverTime(env.getFeverRestTime(), tm) == 0) {
								tm--;
							}
							System.out.println("ぎりぎり消せるよ！！ rest="+tm);
							// なるべくギリギリだったら消せる
							for (int i = feverTryRensa - 1; i <= tm; i++) {
								rensaScore[i] += 0x200;
							}
						}
					}
				}
			}
			else {
				// トライする連鎖数
				int rest = 7 - env.getFeverStockCount();
				if (ojamaNum[0] + ojamaNum[1] >= rest * rest) {
					// フィーバーに入れるだけのおじゃまぷよがある
					for (int i = rest; i < rensaScore.length; i++) {
						rensaScore[i] += 0x400;
					}
				}
				else {
					int mx = eneState.maxRensa * 3 / 2;
					int mx2 = eneState.nowRensa + 4;
					if (mx < 5) {
						mx = 5;
					}
					// 通常モード
					if (eneState.fadeMaxCount > 0) {
						// 敵が消している
						if (ojamaNum[0] > 0) {
							mx = mx2 = eneState.fadeMaxCount;
						}
						else {
							mx = mx2 = eneState.fadeMaxCount + 2;
						}
						if (mx < 5) {
							mx = mx2 = 5;
						}
					}
					for (int i = mx; i < rensaScore.length; i++) {
						rensaScore[i] += 0x100;
					}
					for (int i = mx2; i < rensaScore.length; i++) {
						rensaScore[i] += 0x80;
					}
				}
				if (blk.getBlockNum(BlockData.BLK_SPACE) < 6 * 5) {
					System.out.println("ピンチなので連鎖数を抑える");
					for (int i = 1; i < rensaScore.length; i++) {
						rensaScore[i] += 0x400;
					}
				}
			}
		}

		public int checkMove(BlockData blk, ArrayList<int[]> rensa) {
			tmpCheckCount++;
			int[] bak = ojamaNum.clone();
			if (rensa.size() == 0) {
				blk = dropOjama(blk, ojamaNum);
			}
			int ret = calcScore(blk, rensa);
			ojamaNum = bak;
			return ret;
		}

		/**
		 * オーバライドできる
		 * @param blk
		 * @param rensa
		 * @return
		 */
		protected int calcScore(BlockData blk, ArrayList<int[]> rensa) {
			if (zenkesiFlag && blk.getBlockNum(BlockData.BLK_SPACE) == 6 * 14) {
				return 0x20000|rensa.size();
			}
			// スコアをチェックする
			if (blk.getBlock(2, 2) != BlockData.BLK_SPACE || blk.getBlock(3, 2) != BlockData.BLK_SPACE) {
				// 死亡
				return -9999;
			}
			if (feverTryRensa > 0) {
				// フィーバー中
				if (rensa.size() > 0) {
					// 消したスコア
					return ((rensa.size()<<8)|blk.getBlockNum(BlockData.BLK_SPACE)) + rensaScore[rensa.size()];
				}
			}
			int[] rensanum = new int[6];
			int[] ojanum = new int[6];
			int num = checkRensaNum(blk, rensanum, ojanum);
			num <<= 8;
			for (int i = 0; i < rensanum.length; i++) {
				num += rensanum[i];
			}
			return num;
		}

		/**
		 * 一度チェックして応答データを返す
		 * まだチェック中の場合は null を返す
		 * @return
		 */
		MoveData getResult() {
			if (resultObj != null) {
				return resultObj;
			}
			tmpCheckCount = 0;
			while (checkNum < resultData.length) {
				// 
				ArrayList<int[]> rensa = new ArrayList<int[]>();
				BlockData nxt = blockData.putMoveData(moveList.get(checkNum), rensa);
				int[] bak = ojamaNum.clone();
				if (rensa.size() == 0) {
					nxt = dropOjama(nxt, ojamaNum);
				}
				// 死亡と全けしチェック
				if (nxt.getBlock(2, 2) > 0 || nxt.getBlock(3, 2) > 0) {
					// 死亡した
					resultData[checkNum] = -9999;
					if ((nxt.getBlock(2, 2) % BlockData.BLK_OJAMA) == 0 && (nxt.getBlock(3, 2) % BlockData.BLK_OJAMA) == 0) {
						// 両方ともおじゃまぷよ
						// おじゃまがない場合をチェックする
					}
				}
				else if (nxt.getBlockNum(BlockData.BLK_SPACE) == 14 * 6) {
					// 全消し
					resultData[checkNum] = 0x20000|rensa.size();
				}
				else {
					// チェックする
					int sc = 0;
					if (feverTryRensa > 0) {
						// フィーバー中
						if (rensa.size() > 0) {
							// 消えてしまう
							sc = ((rensa.size()<<8)|nxt.getBlockNum(BlockData.BLK_SPACE)) + rensaScore[rensa.size()];
						}
					}
					else {
						// 通常モード
						// 敵が貯めている連鎖数を大きく超えていれば落とす
						if (rensaScore[rensa.size()] != 0) {
							sc = ((rensa.size()<<8)|nxt.getBlockNum(BlockData.BLK_SPACE)) + rensaScore[rensa.size()];
						}
					}
					// 次のぷよ
					if (sc == 0 && nextPuyo[0] != null) {
						// おじゃまぷよの微調整
						if (eneState.nowRensa < rensa.size() + 2) {
							// 次のおじゃまも落とす対象
							ojamaNum[0] += ojamaNum[1];
						}
						zenkesiFlag = true;
						CheckMovePuyo chkmv = new CheckMovePuyo(nxt, nextPuyo[0]);
						sc = chkmv.checkData(nxt, this);
						if (sc < 0) {
							// 死亡
						}
						else if (sc < 0x20000 && nextPuyo[1] != null) {
							// 3つ目もチェック
							zenkesiFlag = false;
							chkmv = new CheckMovePuyo(nxt, nextPuyo[1]);
							int newsc = chkmv.checkData(nxt, this);
							if (newsc > sc) {
								sc = newsc;
							}
						}
					}
					resultData[checkNum] = sc;
				}
				ojamaNum = bak;
				checkNum++;
				if (tmpCheckCount >= maxCheckNum) {
					// チェック数を超えたので終了
					break;
				}
			}
			if (checkNum >= resultData.length) {
				// 確定した
				int maxnum = -10000;
				for (int num: resultData) {
					if (num > maxnum) {
						maxnum = num;
					}
				}
				ArrayList<MoveData> tmplst = new ArrayList<MoveData>();
				for (int i = 0; i < resultData.length; i++) {
					if (maxnum == resultData[i]) {
						tmplst.add(moveList.get(i));
					}
				}
				resultObj = tmplst.get((int)(Math.random() * tmplst.size()));
				System.out.println("結果="+Integer.toString(maxnum, 16));
			}
			return resultObj;
		}
	}

	/**
	 * フィーバー突入用に消し続けるチェック用
	 * @author kobayah
	 *
	 */
	class FeverWaitFilter implements CheckFilter {
		/**
		 * 残りの消すべき連鎖数
		 */
		int restCount;

		/**
		 * 消すべき対象のおじゃまぷよ
		 */
		int ojamaNum;

		MoveData nextPuyo;

		FeverWaitFilter(EnvData env) {
			restCount = 7 - env.getFeverStockCount();
			// TEST
			if (restCount < 2) {
				System.out.println("ピンチの数："+restCount);
			}
			ojamaNum = env.getOjamaPuyo()[0] + env.getOjamaPuyo()[1];
			nextPuyo = env.getNextPuyo()[0];
		}
		FeverWaitFilter(int oja, int rest) {
			ojamaNum = oja;
			restCount = rest;
			nextPuyo = null;
		}

		public int checkScore(BlockData blkdt, MoveData mvdt) {
			ArrayList<int[]> rensa = new ArrayList<int[]>();
			BlockData nxt = blkdt.putMoveData(mvdt, rensa);
			int[] ojamanum = new int[]{ojamaNum, 0};
			int rensa1 = rensa.size();
			if (rensa1 == 0) {
				nxt = dropOjama(nxt, ojamanum);
			}
			if (nxt.getBlock(2, 2) != BlockData.BLK_SPACE || nxt.getBlock(3, 2) != BlockData.BLK_SPACE) {
				// 死亡
				return -9999;
			}
			if (rensa1 >= restCount) {
				// OK
				return 0x2000|(rensa1<<4)|rensa1;
			}
			else if (nxt.getBlockNum(BlockData.BLK_SPACE) == 6 * 14) {
				// 全消し
				return 0x1000|(rensa1<<4)|rensa1;
			}
			if (nextPuyo != null) {
				// 次のぷよをチェックする
				CheckData chkdt = new CheckData(nxt, nextPuyo);
				chkdt.selectData(new FeverWaitFilter(ojamanum[0], restCount - rensa1));
				int rensa2 = chkdt.getMaxValue();
				if (rensa2 > 0) {
					if ((rensa1 & 0xf) > (rensa2 & 0xf)) {
						// 連鎖１が大きい
						return (rensa2 & 0xfff0) + rensa1;
					}
					else {
						// 連鎖２が大きい
						return rensa2 + (rensa1 & 0xff0);
					}
				}
			}
			return (rensa1<<4)|rensa1;
		}
		
	}

	private EneState eneState = new EneState();

	private DelayMoveCheck moveCheck;

	/**
	 * フィーバーでチャレンジする連鎖数
	 */
	int feverTryRensa;

	/**
	 * 連鎖数をチェックする
	 * @param blk ブロック
	 * @param rensanum int[5]を渡す
	 * @param ojanum   int[5]を渡す
	 * @return 連鎖数
	 */
	static int checkRensaNum(BlockData blk, int[] rensanum, int[] ojanum) {
		ArrayList<int[]> lst = new ArrayList<int[]>();
		int[] topy = blk.getBlockY();
		for (int i = 0; i < 5; i++) {
			rensanum[i] = ojanum[i] = 0;
		}
		int stx = 0;
		int edx = 6;
		if (blk.getBlock(1, 2) != BlockData.BLK_SPACE) {
			stx = 2;
		}
		if (blk.getBlock(4, 2) != BlockData.BLK_SPACE) {
			edx = 4;
		}
		for (int x = stx; x < edx; x++) {
			int[] putx = new int[]{x, x};
			int yy = topy[x];
			if (yy <= 2) {
				continue;
			}
			else if (yy == 3) {
				// 1つしかおけない
				putx = new int[]{x};
			}
			// 一つ上をチェック
			for (int cl = 1; cl <= 5; cl++) {
				if (cl == blk.getBlock(x, yy)
						|| cl == blk.getBlock(x - 1, yy - 1)
						|| cl == blk.getBlock(x + 1, yy - 1)) {
					// OK
					lst.clear();
					blk.putPuyoData(putx, new int[]{cl, 0}, lst);
					if (lst.size() > 0) {
						// 消える
						if (rensanum[cl - 1] < lst.size()) {
							rensanum[cl - 1] = lst.size();
						}
					}
				}
			}
			// 2つ上をチェック
			if (yy > 3) {
				for (int cl = 1; cl <= 5; cl++) {
					if (cl == blk.getBlock(x - 1, yy - 2)
						|| cl == blk.getBlock(x + 1, yy - 2)) {
						// OK
						lst.clear();
						blk.putPuyoData(putx, new int[]{BlockData.BLK_OJAMA, cl}, lst);
						if (lst.size() > 0) {
							// 消える
							if (rensanum[cl - 1] < lst.size()) {
								rensanum[cl - 1] = lst.size();
							}
						}
					}
				}
			}
		}
		// 連鎖チェック終了
		int rensa = 0;
		for (int cl = 1; cl <= 5; cl++) {
			if (rensa < rensanum[cl - 1]) {
				rensa = rensanum[cl - 1];
			}
		}
		return rensa;
	}

	@Override
	protected String getThinkName() {
		return "小林思考ルーチン極上";
	}

	@Override
	public void waitEvent(PuyoData puyo, BlockData meBlk, EnvData meEnv, BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {
		super.waitEvent(puyo, meBlk, meEnv, eneBlk, enePuyo, eneEnv);
		eneState.checkPuyo(eneBlk, eneEnv);
		moveCheck = null;
	}

	@Override
	protected void think(BlockData meBlk, MoveData mePuyo, EnvData meEnv,
			BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {
		if (meEnv.getFeverRestTime() >= 0) {
			// フィーバー中
			if (feverTryRensa == 0) {
				// チェックする
				feverTryRensa = checkRensaNum(meBlk, new int[6], new int[6]);
			}
		}
		MoveData nxt = null;
		eneState.checkPuyo(eneBlk, eneEnv);
		// ピンチのチェック
		if (meEnv.getFeverRestTime() < 0) {
			// 通常モード
			int ojanum = meEnv.getOjamaPuyo()[0];
			if (eneState.nowRensa < 2) {
				ojanum += meEnv.getOjamaPuyo()[1];
			}
			if (ojanum > 20 || meBlk.getBlockNum(BlockData.BLK_SPACE) - ojanum <= 3 * 6) {
				// 大ぴーんち
				System.out.println("ぴんち！！");
				CheckData chkdt = new CheckData(meBlk, mePuyo);
				nxt = chkdt.selectData(new FeverWaitFilter(meEnv));
			}
		}
		if (nxt == null) {
			// 通常のチェック
			if (moveCheck == null) {
				moveCheck = new DelayMoveCheck(meBlk, mePuyo, meEnv, 80);
			}
			nxt = moveCheck.getResult();
		}
		if (nxt != null) {
			int[] mv = mePuyo.getDiff(nxt);
			super.moveRight(mv[0]);
			super.turnRight(mv[1]);
			super.moveDrop(14);
			// フィーバー中の消えるかチェック
			if (meEnv.getFeverRestTime() >= 0) {
				ArrayList<int[]> rensa = new ArrayList<int[]>();
				meBlk.putMoveData(nxt, rensa);
				if (rensa.size() > 0) {
					feverTryRensa = 0;
				}
			}
		}
	}

}
