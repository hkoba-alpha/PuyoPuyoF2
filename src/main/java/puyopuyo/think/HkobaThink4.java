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
import puyopuyo.think.util.CheckMostDelete;

public class HkobaThink4 extends AbstractPuyoThink {

	enum EnemyState {
		MoveMode,
		FadeMode,
	}
	EnemyState eneState = EnemyState.MoveMode;
	/**
	 * 敵が連鎖を始めた時の数
	 */
	int eneRensaNum;
	/**
	 * 敵が連鎖を始めた時に通常モードだったかのフラグ
	 * 通常モードで開始した者は、フィーバーモード時は無視するため
	 */
	boolean rensaNormalModeFlag;

	/**
	 * 敵がフィーバーモードかどうかのフラグ
	 */
	boolean eneFeverModeFlag;

	/**
	 * 敵の最後のTOPYリスト
	 */
	int eneLastTopY;

	/**
	 * 落ちてきそうな敵のおじゃまぷよの数
	 */
	int enePlanOjama;
	/**
	 * 敵の連鎖最大種数を覚える
	 */
	int eneTaneNum = -1;
	/**
	 * 敵の連鎖の残り回数
	 */
	int eneLeftRensa = 0;

	/**
	 * 移動済みフラグ
	 */
	boolean movedFlag;

	/**
	 * 死にそうなモードでのチェック
	 */
	boolean feverTryFlag;

	/**
	 * 初めの方なので、とりあえずいろいろつなげる
	 */
	boolean chainTryFlag;

	/**
	 * 標準的な連鎖倍率
	 */
	static int[] rensaBai = {0, 8, 16, 22, 34, 67, 11, 167, 223, 280, 350, 421, 480, 560, 615, 670};

	/**
	 * なんとなく落ちてきそうなおじゃまぷよの数を計算する
	 * @param stnum
	 * @param rensalst
	 * @param fevflg
	 * @return
	 */
	static int getPlanOjamaNum(int stnum, ArrayList<int[]> rensalst, boolean fevflg) {
		if (rensalst.size() == 0) {
			return 0;
		}
		int score = 0;
		for (int i = 0; i < rensalst.size(); i++) {
			int num = stnum + i;
			if (num >= rensaBai.length) {
				num = rensaBai.length - 1;
			}
			int[] dt = rensalst.get(i);
			int sc = dt.length * 3 - 3;
			if (fevflg) {
				// フィーバー時は半分
				sc += (rensaBai[num] / 2);
			}
			else {
				sc += rensaBai[num];
			}
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
	 * 連鎖可能な種のリストを返す
	 * @param blkdt
	 * @param clnum
	 * @return int[4]のArrayList
	 *  [0]=x,[1]=y,[2]=連鎖数,[3]=色
	 */
	static ArrayList<int[]> getTaneData(BlockData blkdt, int clnum) {
		ArrayList<int[]> ret = new ArrayList<int[]>();
		int[] topy = blkdt.getBlockY();

		int stx = 0;	// チェック開始
		int edx = 6;	// チェック終了
		if (topy[1] < 3) {
			stx = 2;
		}
		if (topy[4] < 3) {
			edx = 4;
		}
		ArrayList<int[]> chklst = new ArrayList<int[]>();

		for (int x = stx; x < edx; x++) {
			for (int cl = 1; cl <= clnum; cl++) {
				int num1 = 0;
				int num2 = 0;
				if (topy[x] > 2) {
					boolean chkflg = false;
					if (blkdt.getBlock(x, topy[x]) == cl
							|| blkdt.getBlock(x - 1, topy[x] - 1) == cl
							|| blkdt.getBlock(x + 1, topy[x] - 1) == cl) {
						blkdt.putPuyoData(x, cl, chklst);
						num1 = chklst.size();
						chkflg = true;
					}
					if (blkdt.getBlock(x - 1, topy[x] - 2) == cl
							|| blkdt.getBlock(x + 1, topy[x] - 2) == cl) {
						blkdt.putPuyoData(new int[]{x, x}, new int[]{6, cl}, chklst);
						num2 = chklst.size();
						chkflg = true;
					}
					if (num1 > num2) {
						ret.add(new int[]{x, topy[x] - 1, num1, cl});
					}
					else if (num2 > 0) {
						ret.add(new int[]{x, topy[x] - 2, num2, cl});
					}
					else if (chkflg) {
						// 2つ同じ色を置いたときのチェック
						blkdt.putPuyoData(new int[]{x, x}, new int[]{cl, cl}, chklst);
						if (chklst.size() > 0) {
							ret.add(new int[]{x, topy[x] - 2, chklst.size(), cl});
						}
					}
				}
			}
		}

		return ret;
	}

	/**
	 * 敵の状態をチェックする
	 * @param eneBlk
	 * @param enePuyo
	 * @param eneEnv
	 * @param meEnv
	 */
	void enemyCheck(BlockData eneBlk, MoveData enePuyo, EnvData eneEnv, EnvData meEnv) {
		if (enePuyo != null) {
			eneState = EnemyState.MoveMode;
			eneRensaNum = 0;
			enePlanOjama = 0;
			eneLeftRensa = 0;
			if (!eneFeverModeFlag && eneEnv.getFeverRestTime() >= 0) {
				//System.out.println("敵がフィーバー突入！！！");
			}
			eneFeverModeFlag = (eneEnv.getFeverRestTime() >= 0);
		}
		else if (eneState == EnemyState.MoveMode) {
			// 移動が終わったので連鎖数をチェックする
			eneState = EnemyState.FadeMode;
			ArrayList<int[]> lst = new ArrayList<int[]>();
			eneBlk.putPuyoData(new int[0], new int[0], lst);
			eneRensaNum = eneLeftRensa = lst.size();
			enePlanOjama = getPlanOjamaNum(0, lst, eneEnv.getFeverRestTime() >= 0);
			eneTaneNum = -1;
			rensaNormalModeFlag = (meEnv.getFeverRestTime() < 0);
			//System.out.println("敵が終了。連鎖予定数="+eneRensaNum+", おじゃま予定="+enePlanOjama);
			eneLastTopY = 0;
			for (int y: eneBlk.getBlockY()) {
				eneLastTopY <<= 4;
				eneLastTopY |= y;
			}
		}
	}
	/**
	 * 自分の異動直前にチェックする
	 * @param eneBlk
	 * @param enePuyo
	 * @param eneEnv
	 * @param meEnv
	 */
	void enemyRensaCheck(BlockData eneBlk, MoveData enePuyo, EnvData eneEnv, EnvData meEnv) {
		if (eneState == EnemyState.FadeMode && eneLeftRensa > 0) {
			// 連鎖状態が変わっているかチェック
			int newy = 0;
			for (int y: eneBlk.getBlockY()) {
				newy <<= 4;
				newy |= y;
			}
			if (eneLastTopY != newy) {
				eneLastTopY = newy;
				// 連鎖チェック
				ArrayList<int[]> lst = new ArrayList<int[]>();
				eneBlk.putPuyoData(new int[0], new int[0], lst);
				enePlanOjama = getPlanOjamaNum(eneRensaNum - lst.size(), lst, eneEnv.getFeverRestTime() >= 0);
				eneLeftRensa = lst.size();
				//System.out.println("敵が連鎖中！！ 残り="+eneLeftRensa+ ",おじゃま予定="+enePlanOjama);
			}
		}
		if (eneTaneNum < 0) {
			// 種のチェック
			ArrayList<int[]> tanelst = getTaneData(eneBlk, 5);
			eneTaneNum = 0;
			for (int[] tane: tanelst) {
				if (tane[2] > eneTaneNum) {
					eneTaneNum = tane[2];
				}
			}
			//System.out.println("敵の連鎖の種の数="+eneTaneNum);
		}
	}

	/**
	 * もうやられそうなので、フィーバー突入のための悪あがき
	 * @author hkoba
	 *
	 */
	class FeverTryFilter implements CheckFilter {
		int zenkesiBit;
		int bitFlag;
		BlockData lastCheckData;	// 苦し紛れ…
		int lastRensaNum;	// 苦し紛れ
		int lastRestOjama;

		int leftFevNum;
		int ojamaNum;

		FeverTryFilter(int bit, int zenkesi, int fevnum, int oja) {
			bitFlag = bit;
			zenkesiBit = zenkesi;
			leftFevNum = fevnum;
			ojamaNum = oja;
		}

		public int checkScore(BlockData blkdt, MoveData mvdt) {
			ArrayList<int[]> rensa = new ArrayList<int[]>();
			BlockData newdt = blkdt.putMoveData(mvdt, rensa);
			lastCheckData = newdt;
			checkRestCount++;
			lastRensaNum = rensa.size();
			int ojama = ojamaNum;
			if (rensa.size() == 0 && ojama > 0) {
				// 仮想的におじゃまぷよを落とす
				int oja = ((ojama + 5) / 6) * 6;
				if (oja > 30) {
					oja = 30;
				}
				int[] xp = new int[oja];
				int[] pu = new int[oja];
				for (int i = 0; i < oja; i++) {
					xp[i] = i % 6;
					pu[i] = BlockData.BLK_OJAMA;
				}
				newdt = newdt.putPuyoData(xp, pu, null);
				ojama -= oja;
				if (ojama < 0) {
					ojama = 0;
				}
			}
			lastRestOjama = ojama;
			if (newdt.getBlock(2, 2) > 0 || newdt.getBlock(3, 2) > 0) {
				// 論外
				return IGNORE_DATA;
			}
			if (newdt.getBlockNum(0) == 6 * 14) {
				// 全消し
				return bitFlag | zenkesiBit | rensa.size();
			}
			if (rensa.size() > 0) {
				if (lastRensaNum >= leftFevNum) {
					// フィーバー突入できる
					// 全消しなみに良い
					return bitFlag | zenkesiBit | rensa.size();
				}
				return bitFlag + rensa.size();
			}
			return 0;
		}
	}

	/**
	 * 連鎖の種でチェックする
	 * @author hkoba
	 *
	 */
	class TaneCheckFilter implements CheckFilter {
		int minRensa;
		int maxRensa;
		int zenkesiBit;
		int ojamaNum;
		int ojamaNum2;
		MoveData nextPuyo2;
		MoveData nextPuyo3;

		TaneCheckFilter(int minr, int maxr, int zenkesi, int oja, int oja2, MoveData nxt2, MoveData nxt3) {
			minRensa = minr;
			maxRensa = maxr;
			// デフォルトは 0x40000
			zenkesiBit = zenkesi;
			ojamaNum = oja;
			ojamaNum2 = oja2;
			nextPuyo2 = nxt2;
			nextPuyo3 = nxt3;
		}

		public int checkScore(BlockData blkdt, MoveData mvdt) {
			ArrayList<int[]> rensa = new ArrayList<int[]>();
			BlockData newdt = blkdt.putMoveData(mvdt, rensa);
			int ojama = ojamaNum;
			if (rensa.size() == 0 && ojamaNum > 0) {
				// 仮想的におじゃまぷよを落とす
				int oja = ((ojama + 5) / 6) * 6;
				if (oja > 30) {
					oja = 30;
				}
				int[] xp = new int[oja];
				int[] pu = new int[oja];
				for (int i = 0; i < oja; i++) {
					xp[i] = i % 6;
					pu[i] = BlockData.BLK_OJAMA;
				}
				newdt = newdt.putPuyoData(xp, pu, null);
				ojama -= oja;
				if (ojama < 0) {
					ojama = 0;
				}
			}
			// ゲームオーバーチェック
			if (newdt.getBlock(2, 2) > 0 || newdt.getBlock(3, 2) > 0) {
				return IGNORE_DATA;
			}
			// スコア計算
			checkRestCount++;
			if (zenkesiBit > 0 && newdt.getBlockNum(BlockData.BLK_SPACE) == 6 * 14) {
				// 全消し
				return zenkesiBit + (rensa.size() << 8);
			}
			else if (rensa.size() >= maxRensa) {
				return 0x20000 + (rensa.size() << 8) + newdt.getBlockNum(BlockData.BLK_SPACE);
			}
			else if (rensa.size() > minRensa) {
				// 消してはいけない中途半端な数を消してしまった
				return (rensa.size() << 8) + newdt.getBlockNum(BlockData.BLK_SPACE);
			}
			int score = 0x10000;
			if (nextPuyo2 != null) {
				// 次のぷよをチェックする
				ojama += ojamaNum2;
				CheckData chkdt = new CheckData(newdt, nextPuyo2);
				if (chkdt.selectData(new TaneCheckFilter(minRensa, maxRensa, 0x80000, ojama, 0, null, null)) != null) {
					score = chkdt.getMaxValue();
					// 全消ししてない場合のみチェックする
					if ((score & 0x40000) == 0 && nextPuyo3 != null) {
						// 次の次もチェック
						chkdt = new CheckData(newdt, nextPuyo3);
						if (chkdt.selectData(new TaneCheckFilter(minRensa, maxRensa, 0x00000, ojama, 0, null, null)) != null) {
							int sc = chkdt.getMaxValue();
							if (sc > score) {
								score = sc;
							}
						}
					}
				}
				else {
					return IGNORE_DATA;
				}
			}
			else {
				// チェックする
				ArrayList<int[]> tane = getTaneData(newdt, mvdt.getColorNum());
				int maxn = 0;
				for (int[] val: tane) {
					if (val[2] > maxn) {
						maxn = val[2];
					}
					score += val[2];
				}
				score += (maxn << 8);
			}

			return score;
		}
		
	}

	@Override
	protected String getThinkName() {
		return "小林思考ルーチン特上";
	}

	@Override
	protected void think(BlockData meBlk, MoveData mePuyo, EnvData meEnv, BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {
		int feverRest = meEnv.getFeverRestTime();

		if (feverRest < 0) {
			feverFirstRensa = -1;
		}
		if (fromData == null) {
			// 初期化
			enemyRensaCheck(eneBlk, enePuyo, eneEnv, meEnv);
			movedFlag = false;
			fromData = mePuyo;
			moveList.clear();
			resultMap.clear();
			CheckData chkdt = new CheckData(meBlk, mePuyo);
			chkdt.selectData(new MoveFilter());
			//System.out.println(feverRest < 0 ? "通常モード思考ルーチン":"フィーバーモード思考ルーチン");

			feverTryFlag = false;
			chainTryFlag = false;
			int minrensa = 3;
			int maxrensa = 7;

			if (eneEnv.getFeverRestTime() >= 0 && feverRest < 0) {
				// 敵がフィーバーモードで自分が通常モード
				int[] eneoja = eneEnv.getOjamaPuyo();
				if (eneoja[2] > 15 && eneoja[0] == 0) {
					// 敵の頭上に微妙におじゃまがたまっている
					// 早めに落としてつぶすのもよい
					if (eneLeftRensa > 4) {
						// 敵が消している最中
						// 敵の消えるのに合わせておじゃまを送るのもよい
						if (enePlanOjama < eneoja[2] + 15) {
							maxrensa = 3;
						}
					}
					else {
						minrensa = eneTaneNum;
						if (minrensa > maxrensa) {
							minrensa = maxrensa;
						}
					}
				}
			}
			MoveData mv2 = meEnv.getNextPuyo()[0];
			MoveData mv3 = meEnv.getNextPuyo()[1];
			int ojamaNum = meEnv.getOjamaPuyo()[0];
			int ojamaNum2 = 0;
			int lookOjama = ojamaNum + meEnv.getOjamaPuyo()[1];
			if (eneLeftRensa < 2 || (eneLeftRensa <= 2 && feverRest < 0)) {
				// もう敵の連鎖は終わり寸前
				ojamaNum += meEnv.getOjamaPuyo()[1];
				// こっちに飛んでくる予定のおじゃまぷよも入れてしまう
				// ただし、フィーバーモードのバックアップへいくケースではない場合
				if (eneLeftRensa < 2 && feverRest < 0) {
					int enenum = enePlanOjama;
					int[] eneoja = eneEnv.getOjamaPuyo();
					for (int i = 0; i < eneoja.length; i++) {
						enenum -= eneoja[i];
					}
					if (enenum > 0) {
						ojamaNum += enenum;
					}
				}
			}
			else if (eneLeftRensa <= 2) {
				ojamaNum2 = meEnv.getOjamaPuyo()[1];
			}
			// たまっているおじゃまぷよが多ければ少ない連鎖でOK
			maxrensa -= (meBlk.getBlockNum(BlockData.BLK_OJAMA)/6);
			int[] sc = getTaneScore(getTaneData(meBlk, mePuyo.getColorNum()));

			if (meBlk.getBlockNum(0) > 6 * 9 && sc[0] < 5) {
				// 5連鎖以内ならなんとなくつなげる
				if (getChainScore(meBlk, 700) < 800) {
					chainTryFlag = true;
				}
			}

			if (feverRest >= 0) {
				chainTryFlag = false;
				// フィーバーモード
				if (feverFirstRensa < 0) {
					feverFirstRensa = sc[0];
				}
				maxrensa = feverFirstRensa;
				// 最大連鎖数より種が大きいか、残りブロックが少なければそれを連鎖数にする
				// おじゃまぷよがいっぱいあってもすぐに消す
				if (sc[0] > maxrensa || meBlk.getBlockNum(0) < 6 * 4 || meBlk.getBlockNum(BlockData.BLK_OJAMA) >= 12) {
					maxrensa = sc[0];
				}
				if (maxrensa > 4 && maxrensa < 7 && (6*14 - meBlk.getBlockNum(0)) < 7 * 5) {
					// フィーバーをちょっとがんばってみる
					// ただし、敵に返される可能性が少なければよい
					if (meEnv.getOjamaPuyo()[2] > 90 || eneTaneNum > 6) {
						if (lookOjama < 6 || eneLeftRensa > 5) {
							// 敵から飛んできていなければ
							maxrensa = 7;
						}
					}
				}
				else if (maxrensa > 14) {
					maxrensa = 14;
				}
				minrensa = 0;
				if (feverRest < 18) {
					// 残り時間が1.8秒をきったら1つ少なくてもよい
					// フィーバーの最初の連鎖数より1少なくてよい
					maxrensa = feverFirstRensa - 1;
					mv3 = null;
					if (feverRest < 6) {
						mv2 = null;
						if (lookOjama + meEnv.getOjamaPuyo()[2] >= 30) {
							// 消さずにフィーバーが終わったらやばい！！
							maxrensa = 1;
						}
					}
				}
				else if (feverRest >= mePuyo.getColorNum() * 10 && feverRest < maxrensa * 8) {
					// もう消してしまっても次のフィーバーにいけない
					if (ojamaNum == 0 && meEnv.getOjamaPuyo()[1] == 0) {
						// おじゃまぷよはいない
						// スペースがそれなりに空いているならがんばる
						if (meBlk.getBlockNum(0) >= 6 * 6) {
							maxrensa++;
						}
					}
				}
				else if (meEnv.getOjamaPuyo()[1] >= 12 && eneLeftRensa < 4) {
					// 予告ぷよがたまっていて、敵の連鎖も終わりそう
					// フィーバーの種がつぶされる可能性がある
					// さっさと消してしまう
					mv3 = null;
					if (eneLeftRensa < 3) {
						mv2 = null;
						if (eneLeftRensa < 2) {
							if (maxrensa > feverFirstRensa - 1) {
								maxrensa = feverFirstRensa - 1;
							}
						}
					}
				}
			}
			else {
				// 通常時
				if (eneTaneNum >= sc[0] && eneTaneNum > 4) {
					// ちょっと連鎖組むのが遅れてきている
					int[] eneoja = eneEnv.getOjamaPuyo();
					int enenum = 0;
					for (int i = 0; i < eneoja.length; i++) {
						enenum += eneoja[i];
					}
					if (maxrensa > 3 && enenum < 12) {
						// 敵におじゃまぷよがたまっていなければさっそく消す
						maxrensa = eneTaneNum + 1;
						if (maxrensa < 8) {
							maxrensa = 8;
						}
						minrensa /= 2;	// ここまで消してもよい
						//System.out.println("連鎖が遅れている。敵="+eneTaneNum+",自分="+sc[0]);
					}
				}
			}

			if (ojamaNum + ojamaNum2 >= 6) {
				// ちょっとやばめ
				chainTryFlag = false;
				if (feverRest < 0) {
					maxrensa--;
					mv3 = null;
					if (ojamaNum + ojamaNum2 >= 18) {
						maxrensa--;
						//mv2 = null;
					}
					// 通常時、フィーバーになるなら早めに消す
					int newrensa = 7 - meEnv.getFeverStockCount();
					if (newrensa < maxrensa) {
						maxrensa = newrensa;
					}
					if (ojamaNum + ojamaNum2 > 30 || meBlk.getBlockNum(0) - ojamaNum - ojamaNum2 < 25) {
						// かなりやばそう！！
						feverTryFlag = true;
						//System.out.println("フィーバー入るためのモード！！！");
						mv2 = meEnv.getNextPuyo()[0];
						mv3 = meEnv.getNextPuyo()[1];
					}
					// ちょっとずつ消してもよい
					minrensa = maxrensa;
				}
				else if (ojamaNum + ojamaNum2 >= 18) {
					// フィーバー時はもうちょっと我慢する
					if (maxrensa > feverFirstRensa - 1) {
						maxrensa = feverFirstRensa - 1;
					}
				}
			}
			// もし通常時で
			if (!feverTryFlag && feverRest < 0 && meBlk.getBlockNum(BlockData.BLK_SPACE) < 7*6) {
				// 残り半分以下になったとき、連鎖数が増えていかなければすぐに消す
				maxrensa = 1;
				mv3 = null;
			}
			// 左右に移動できなければ連鎖数を引く
			/*
			if (meBlk.getBlock(1, 2) > 0) {
				maxrensa--;
			}
			if (meBlk.getBlock(4, 2) > 0) {
				maxrensa--;
			}
			*/
			if (maxrensa < 1) {
				maxrensa = 1;
			}
			checkMaxRensa = maxrensa;
			checkMinRensa = minrensa;
			checkOjamaNum = ojamaNum;
			checkOjamaNum2 = ojamaNum2;
			checkNext2 = mv2;
			checkNext3 = mv3;
			if (chainTryFlag) {
				//System.out.println("なんとなくつなげるモード");
			}
		}
		else if (movedFlag) {
			enemyRensaCheck(eneBlk, enePuyo, eneEnv, meEnv);
		}

		checkRestCount = 0;
		for (int key: moveList.keySet()) {
			if (resultMap.containsKey(key)) {
				continue;
			}
			MoveData pu = mePuyo.clone();
			int[] mv = moveList.get(key);
			if (mv[1] < 0) {
				pu.turnLeft(-mv[1]);
			}
			else {
				pu.turnRight(mv[1]);
			}
			if (pu.addX(mv[0], meBlk) != mv[0]) {
				resultMap.put(key, CheckFilter.IGNORE_DATA);
				continue;
			}
			int limitNum = 80;
			if (feverTryFlag) {
				// フィーバーにトライするモード
				limitNum = 100;
				int fevnum = 7 - meEnv.getFeverStockCount();
				FeverTryFilter filt = new FeverTryFilter(0x8000, 0x2000, fevnum, checkOjamaNum);
				int ret = filt.checkScore(meBlk, pu);
				if (ret != CheckFilter.IGNORE_DATA) {
					// チェック対象
					if (filt.lastRensaNum >= fevnum) {
						// フィーバーに突入できる！！
						// 連鎖数は少なくてもよい
						ret = 0xffff - filt.lastRensaNum;
					}
					else {
						// 次のぷよ
						fevnum -= filt.lastRensaNum;
						CheckData chkdt = new CheckData(filt.lastCheckData, checkNext2);
						if (chkdt.selectData(new FeverTryFilter(0x1000, 0x4000, fevnum, filt.lastRestOjama + checkOjamaNum2)) != null) {
							// チェック
							ret += chkdt.getMaxValue();
							// さらに次のぷよ
							chkdt = new CheckData(filt.lastCheckData, checkNext3);
							if (chkdt.selectData(new FeverTryFilter(0x200, 0x100, fevnum, filt.lastRestOjama + checkOjamaNum2)) != null) {
								ret += chkdt.getMaxValue();
								// 追加可能
							}
							else {
								ret = CheckFilter.IGNORE_DATA;
							}
						}
						else {
							ret = CheckFilter.IGNORE_DATA;
						}
					}
				}
				resultMap.put(key, ret);
			}
			else if (chainTryFlag) {
				// チェックする
				limitNum = 120;
				BlockData newdt = meBlk.putMoveData(pu, null);
				if (newdt.getBlockNum(0) == 6 * 14) {
					// 全消し
					resultMap.put(key, 10000);
				}
				else {
					CheckData chkdt = new CheckData(newdt, checkNext2);
					chkdt.selectData(new CheckChainFilter(10000));
					int sc1 = chkdt.getMaxValue();
					if (sc1 == 10000) {
						// 全消し
						resultMap.put(key, 11000);
					}
					else {
						chkdt = new CheckData(newdt, checkNext3);
						chkdt.selectData(new CheckChainFilter(100));
						int sc2 = chkdt.getMaxValue();
						if (sc1 > sc2) {
							resultMap.put(key, sc1);
						}
						else {
							resultMap.put(key, sc2);
						}
					}
				}
			}
			else {
				TaneCheckFilter filt = new TaneCheckFilter(checkMinRensa, checkMaxRensa, 0x40000, checkOjamaNum, checkOjamaNum2, checkNext2, checkNext3);
				resultMap.put(key, filt.checkScore(meBlk, pu));
			}
			if (checkRestCount > limitNum) {
				break;
			}
		}
		if (resultMap.size() < moveList.size()) {
			// 続きがある
			return;
		}

		int maxscore = -9999;
		int[] mvnext = null;
		for (int key: resultMap.keySet()) {
			int res = resultMap.get(key);
			if (res != CheckFilter.IGNORE_DATA && res > maxscore) {
				maxscore = res;
				mvnext = moveList.get(key);
			}
		}
		//System.out.println("min="+checkMinRensa+",max="+checkMaxRensa+", checkOjamaNum="+checkOjamaNum);
		//System.out.println("MAX="+Integer.toString(maxscore, 16));
		if (mvnext != null) {
			MoveData pu = mePuyo.clone();
			if (!movedFlag) {
				super.moveRight(mvnext[0]);
				super.turnRight(mvnext[1]);
				movedFlag = true;
				if (mvnext[1] < 0) {
					pu.turnLeft(-mvnext[1]);
				}
				else {
					pu.turnRight(mvnext[1]);
				}
				pu.addX(mvnext[0], meBlk);
			}
			ArrayList<int[]> rensa = new ArrayList<int[]>();
			BlockData newdt = meBlk.putMoveData(pu, rensa);
			if (rensa.size() > 0) {
				//
				int[] topy = meBlk.getBlockY();
				int lastht = 99;
				for (Point pt: mePuyo.getPoint()) {
					int ht = topy[pt.x] - pt.y;
					if (ht < lastht) {
						lastht = ht;
					}
				}
				// 消える
				// ここでもしも敵からおじゃまぷよが新しく飛んでくる可能性があるか、
				// 敵がフィーバーモードへ突入予定なら少し待つ
				// 自分の上におじゃまぷよがないことが条件だが
				int myoja = 0;
				for (int oja: meEnv.getOjamaPuyo()) {
					myoja += oja;
				}
				if (feverRest >= 0) {
					// フィーバーモード
					feverFirstRensa = -1;
				}
				int eneoja = 0;
				for (int oja: eneEnv.getOjamaPuyo()) {
					eneoja += oja;
				}
				if (feverRest < 0 && rensa.size() + meEnv.getFeverStockCount() >= 7 && myoja >= rensa.size() * 4) {
					// 自分がフィーバーモードに入りそう！！
					// 敵の連鎖が最後っぽいならすぐに
					if (eneFeverModeFlag) {
						if (eneEnv.getFeverRestTime() <= eneLeftRensa * 8) {
							// フィーバーモードの最後の連鎖っぽいのでそのまま落とす
							//System.out.println("フィーバーモードの最後");
						}
						else if (eneEnv.getFeverRestTime() > lastht * 10 + rensa.size() * 10) {
							// のこりの高さの秒以上待ってもだめそうならそのまま落とす
						}
						else if (eneEnv.getFeverRestTime() < rensa.size() * 10 && eneLeftRensa == 0) {
							// 敵の連鎖が終わっていて、残り時間も自分の連鎖で終わりそうなら落とす
							//System.out.println("自分の連鎖で残り時間は終わりそう");
						}
						else {
							//System.out.println("敵のフィーバーモードが終わりそう");
							super.skipDrop(1);
							return;
						}
					}
				}
				if (myoja == 0) {
					if (enePlanOjama > 0 && feverRest < 0) {
						// 通常時で、相手からおじゃまぷよが飛んできそうな時
						if (enePlanOjama - eneoja >= 6) {
							// このくらい大きければ、で3連鎖以内程度なら
							// またあまりにも跳ね返りすぎで、こっちも連鎖が大きければ
							if ((rensa.size() > 1 && eneLeftRensa < lastht) || (enePlanOjama - eneoja > 50 && rensa.size() >= 5 && eneLeftRensa < rensa.size() + lastht)) {
								//System.out.println("通常時で、敵からおじゃまぷよが飛んでくる予定");
								super.skipDrop(1);
								return;
							}
						}
						// 敵がフィーバー中に消しているが、終わった直後におじゃまを送れる？
						if (eneoja - enePlanOjama > 0) {
							int enecheck = (eneLeftRensa * 10 + 12) / 10;
							// 敵のおじゃまぷよは返ってきそうにない
							if (enecheck == 0) {
								// 移動中
								// 敵の連鎖数は種にしてみる
								//enecheck = (eneTaneNum * 4 + 8) / 5;
							}
							if (eneFeverModeFlag && rensa.size() < enecheck && rensa.size() + lastht > enecheck && rensa.size() > 2) {
								// ちょっと待つ
								//System.out.println("敵がフィーバーで消しているのでちょっと待つ");
								super.skipDrop(1);
								return;
							}
						}
					}
					if (!eneFeverModeFlag && feverRest < 10) {
						// 敵がフィーバー突入で、自分のフィーバーが終わり直前か、通常モードのとき
						int fevcnt = eneEnv.getFeverStockCount();
						if (eneoja > 0) {
							fevcnt += eneLeftRensa;
						}
						if (fevcnt >= 7 && eneLeftRensa < lastht + rensa.size() && rensa.size() > 2) {
							// フィーバーへ突入予定
							// 突入してからおじゃまぷよを飛ばす
							//System.out.println("敵がフィーバーモードへ突入する予定!!");
							super.skipDrop(1);
							//super.moveDrop(14);
							return;
						}
					}
				}
			}
			else if (feverRest >= 0) {
				// フィーバーモードで消せない
				// もし置いた後おじゃまぷよが落ちてゲームオーバーになるならフィーバー終りまで待つ
				int ojay = meEnv.getOjamaPuyo()[0] / 6;
				if (ojay > 5) {
					ojay = 5;
				}
				if (ojay > 0) {
					int[] topy = newdt.getBlockY();
					if (topy[2] < ojay + 2 || topy[3] < ojay + 2) {
						// ゲームオーバーになる
						System.out.println("フィーバー中に死にそう");
						super.skipDrop(1);
						return;
					}
				}
			}
		}
		else {
			// もうどこにもおけない・・・
			CheckData chkdt = new CheckData(meBlk, mePuyo);
			// とにかく消えるところ
			MoveData nextpuyo = chkdt.selectData(new CheckMostDelete());
			if (nextpuyo == null) {
				// 消えるところはない
				//System.out.println("消えない！！！");
				// とにかく高さが一番低くなるところ
				nextpuyo = chkdt.selectData(new MostChainFilter());
			}
			if (nextpuyo != null) {
				int[] diff = mePuyo.getDiff(nextpuyo);
				super.moveRight(diff[0]);
				super.turnRight(diff[1]);
			}
		}
		fromData = null;
		super.moveDrop(14);
	}

	@Override
	public void waitEvent(PuyoData puyo, BlockData meBlk, EnvData meEnv, BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {
		enemyCheck(eneBlk, enePuyo, eneEnv, meEnv);
		super.waitEvent(puyo, meBlk, meEnv, eneBlk, enePuyo, eneEnv);
		fromData = null;
	}

	@Override
	public void nextMoveEvent(PuyoData puyo, BlockData meBlk, MoveData mePuyo, EnvData meEnv, BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {
		enemyCheck(eneBlk, enePuyo, eneEnv, meEnv);
		super.nextMoveEvent(puyo, meBlk, mePuyo, meEnv, eneBlk, enePuyo, eneEnv);
	}


	///////////////////////////////////////////////////////////
	// 上級と同じロジック
	///////////////////////////////////////////////////////////
	private int checkMaxRensa;
	private int checkMinRensa;
	private int checkOjamaNum;
	private int checkOjamaNum2;
	private MoveData checkNext2;
	private MoveData checkNext3;
	private int feverFirstRensa = -1;
	/**
	 * チェックの残りカウント
	 */
	static int checkRestCount;

	HashMap<Integer, int[]> moveList = new HashMap<Integer, int[]>();
	HashMap<Integer, Integer> resultMap = new HashMap<Integer, Integer>();
	MoveData fromData;

	/**
	 * もっともつながるところへ置く
	 * @author kobayah
	 *
	 */
	class MostChainFilter implements CheckFilter {
		public int checkScore(BlockData blkdt, MoveData mvdt) {
			BlockData newdt = blkdt.putMoveData(mvdt, null);
			if (newdt.getBlock(2, 2) > 0 || newdt.getBlock(3, 2) > 0) {
				// GAME OVER
				return IGNORE_DATA;
			}
			int[] dt = blkdt.checkChainNum(mvdt);
			int ret = 0;
			for (int i = 0; i < dt.length; i++) {
				ret += dt[i];
			}
			return ret;
		}
	}

	/**
	 * 移動できるところ一覧
	 * @author hkoba
	 *
	 */
	class MoveFilter implements CheckFilter {
		public int checkScore(BlockData blkdt, MoveData mvdt) {
			int key = getMoveKey(mvdt);
			if (!moveList.containsKey(key)) {
				int[] mv = fromData.getDiff(mvdt);
				moveList.put(key, mv);
			}
			return IGNORE_DATA;
		}
	}

	/**
	 * 移動データのキー情報
	 * @param dt
	 * @return
	 */
	private static int getMoveKey(MoveData dt) {
		Point[] pt = dt.getPoint();
		int[] cl = dt.getPuyoColor();
		int minx = 6;
		int miny = 14;
		for (int i = 0; i < pt.length; i++) {
			if (pt[i].x < minx) {
				minx = pt[i].x;
			}
			if (pt[i].y < miny) {
				miny = pt[i].y;
			}
		}
		int ret = minx << 12;
		for (int i = 0; i < pt.length; i++) {
			ret |= cl[i]<<((pt[i].x-minx)*6+(pt[i].y-miny)*3);
		}
		return ret;
	}

	/**
	 * 種データのスコアを得る
	 * @param tanedt
	 * @return [0]=最大連鎖数,[1]=スコア
	 */
	private static int[] getTaneScore(ArrayList<int[]> tanedt) {
		/*
		 * 最大連鎖数<<16
		 * 連鎖数合計
		 */
		int[] ret = new int[2];
		for (int[] dt: tanedt) {
			if (ret[0] < dt[2]) {
				ret[0] = dt[2];
			}
			ret[1] += dt[2];
		}
		ret[1] |= (ret[0]<<16);
		return ret;
	}

	/**
	 * 連鎖初期状態のチェック
	 * 3つつなぐのをとにかく多く作る
	 * @param blkdt
	 * @return
	 */
	static int getChainScore(BlockData blkdt, int zenkesi) {
		if (blkdt.getBlockNum(0) == 6 * 14) {
			return zenkesi;
		}
		int[] topy = blkdt.getBlockY();
		int[] chkflg = new int[6];
		int score = 0;
		for (int i = 0; i < 6; i++) {
			score += topy[i] / 2;
			chkflg[i] = 0xffff - ((2<<topy[i]) - 1);
			if (topy[i] < 3) {
				score -= 1000;
			}
			else if (topy[i] < 7 && (i == 2 || i == 3)) {
				score -= 100;
			}
		}
		for (int x = 0; x < 6; x++) {
			for (int y = 13; y >= 0; y--) {
				// 連結チェック
				if (y < topy[x]) {
					break;
				}
				if ((chkflg[x] & (1<<y)) == 0) {
					continue;
				}
				int ch = blkdt.getBlock(x, y);
				chkflg[x] &= ~(1<<y);
				if (ch == BlockData.BLK_OJAMA) {
					continue;
				}
				if (blkdt.getBlock(x, y - 1) == ch) {
					// 1つ上が同じ色
					chkflg[x] &= ~(1<<(y - 1));
					if (blkdt.getBlock(x, y - 2) == ch) {
						// さらに1つ上が同じ色
						// 縦に3連結
						chkflg[x] &= ~(1<<(y - 2));
						score += 70;
					}
					else if (blkdt.getBlock(x + 1, y) == ch) {
						// o
						// oo という形
						chkflg[x + 1] &= ~(1<<y);
						score += 90;
					}
					else if (blkdt.getBlock(x + 1, y - 1) == ch) {
						// oo
						// o  という形
						chkflg[x + 1] &= ~(1<<(y - 1));
						score += 90;
					}
					else {
						// 縦に2連結
						chkflg[x] &= ~(1<<(y - 1));
						score += 40;
					}
				}
				else if (blkdt.getBlock(x + 1, y) == ch) {
					// 右が同じ色
					chkflg[x + 1] &= ~(1<<y);
					if (blkdt.getBlock(x + 1, y - 1) == ch) {
						//  o
						// oo という形
						chkflg[x + 1] &= ~(1<<(y - 1));
						score += 90;
					}
					else if (blkdt.getBlock(x + 1, y + 1) == ch) {
						// oo
						//  o という形
						chkflg[x + 1] &= ~(1<<(y + 1));
						score += 80;
					}
					else if (blkdt.getBlock( x + 2, y) == ch) {
						// 横に3連結
						chkflg[x + 2] &= ~(1<<y);
						score += 100;
					}
					else {
						// 横に2連結
						score += 50;
					}
				}
			}
		}
		return score;
	}

	/**
	 * つながりをチェックする
	 * @author kobayah
	 *
	 */
	class CheckChainFilter implements CheckFilter {
		int zenkesiNum;

		CheckChainFilter(int zenkesi) {
			zenkesiNum = zenkesi;
		}

		public int checkScore(BlockData blkdt, MoveData mvdt) {
			BlockData newdt = blkdt.putMoveData(mvdt, null);
			return getChainScore(newdt, zenkesiNum);
		}
	}
}
