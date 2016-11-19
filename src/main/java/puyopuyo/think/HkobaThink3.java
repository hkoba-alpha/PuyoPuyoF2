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

public class HkobaThink3 extends AbstractPuyoThink {
	/**
	 * チェックの残りカウント
	 */
	static int checkRestCount;

	HashMap<Integer, int[]> moveList = new HashMap<Integer, int[]>();
	HashMap<Integer, Integer> resultMap = new HashMap<Integer, Integer>();
	MoveData fromData;

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
				if (mv[0] == -2 && mv[1] == -1 && mvdt.getPoint().length < 4) {
					System.out.println("check error");
					System.out.println(fromData);
					System.out.println(mvdt);
				}
			}
			return IGNORE_DATA;
		}
	}

	static int checkScoreNest(BlockData blkdt, MoveData mvdt, MoveData mv2, int ojanum, int minRensa, int maxRensa, boolean zenkesiflg) {
		ArrayList<int[]> rensa = new ArrayList<int[]>();
		BlockData blk = blkdt.putMoveData(mvdt, rensa);
		if (rensa.size() == 0 && ojanum > 0) {
			// おじゃまぷよを仮想的に降らす
			int numsz = (ojanum + 5)/6;
			int[] xp = new int[numsz*6];
			int[] oja = new int[numsz*6];
			for (int i = 0; i < xp.length; i++) {
				xp[i] = i%6;
				oja[i] = 6;
			}
			blk = blk.putPuyoData(xp, oja, null);
		}
		checkRestCount++;
		if (blk.getBlock(2, 2) > 0 || blk.getBlock(3, 2) > 0) {
			// 論外
			//System.out.println("論外！！！！");
			return CheckFilter.IGNORE_DATA;
		}
		int retscore = (minRensa+1)<<8;
		int[] chain = blkdt.checkChainNum(mvdt);
		for (int i = 0; i < chain.length; i++) {
			if (chain[i] < 4 && chain[i] > 0) {
				retscore += chain[i]*2;
			}
		}
		// 死亡直前だと少しマイナス
		if (blk.getBlock(2, 3) > 0) {
			retscore -= 16;
		}
		if (blk.getBlock(3, 3) > 0) {
			retscore -= 16;
		}
		// 全消しが優先
		if (zenkesiflg && blk.getBlockNum(BlockData.BLK_SPACE) == 14*6) {
			//System.out.println("全消し：min="+minRensa+",max="+maxRensa);
			return 0x7fff0000+rensa.size();
		}
		// 連鎖がＭＡＸを超えたらよい
		if (rensa.size() >= maxRensa) {
			return 0x6fff0000+(rensa.size()<<8)+blk.getBlockNum(BlockData.BLK_SPACE);
		}
		// 連鎖がＭＩＮを超えたらダメ
		if (rensa.size() > minRensa) {
			return (rensa.size()<<8)+blk.getBlockNum(BlockData.BLK_SPACE);
		}
		else if (mv2 == null && rensa.size() > 0) {
			// 連鎖数未満であれば、無駄に消さないようにする
			for (int[] dt: rensa) {
				int num = 0;
				for (int i = 0; i < dt.length; i++) {
					num += dt[i];
				}
				retscore -= (num-4);
			}
		}
		if (mv2 == null) {
			// スコア計算
			ArrayList<int[]> tane = getTaneData(blk);
			int[] sc = getTaneScore(tane);
			return sc[1]+retscore;
		}
		CheckData chkdt = new CheckData(blk, mv2);
		MoveData nxt = chkdt.selectData(new NextCheckFilter(minRensa, maxRensa, ojanum, zenkesiflg));
		if (nxt != null) {
			return chkdt.getMaxValue();
		}
		return CheckFilter.IGNORE_DATA;
	}
	static class NextCheckFilter implements CheckFilter {
		int minRensa;
		int maxRensa;
		int ojamaNum;
		boolean zenkesiFlag;

		NextCheckFilter(int min, int max, int oja, boolean zenkesi) {
			minRensa = min;
			maxRensa = max;
			ojamaNum = oja;
			zenkesiFlag = zenkesi;
		}

		public int checkScore(BlockData blkdt, MoveData mvdt) {
			return checkScoreNest(blkdt, mvdt, null, ojamaNum, minRensa, maxRensa, zenkesiFlag);
		}
	}
	/**
	 * なるべく低いところへ置く
	 * @author hkoba
	 */
	static class MyCheckFilter implements CheckFilter {
		public int checkScore(BlockData blkdt, MoveData mvdt) {
			int ret = 0;
			for (Point pt: mvdt.getPoint()) {
				ret += pt.y;
				if (pt.x == 2 || pt.x == 3) {
					ret--;
				}
			}
			return ret;
		}
	}

	private int checkMaxRensa;
	private int checkMinRensa;
	private int checkOjamaNum;
	private MoveData checkNext2;
	private MoveData checkNext3;
	private int feverFirstRensa = -1;

	@Override
	public void waitEvent(PuyoData puyo, BlockData meBlk, EnvData meEnv, BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {
		super.waitEvent(puyo, meBlk, meEnv, eneBlk, enePuyo, eneEnv);
		fromData = null;
	}

	@Override
	protected void think(BlockData meBlk, MoveData mePuyo, EnvData meEnv,
			BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {

		MoveData[] meNext = meEnv.getNextPuyo();
		int[] meOja = meEnv.getOjamaPuyo();
		int feverRest = meEnv.getFeverRestTime();
		if (feverRest < 0) {
			feverFirstRensa = -1;
		}

		if (fromData == null) {
			// 初期化
			fromData = mePuyo;
			moveList.clear();
			resultMap.clear();
			CheckData chkdt = new CheckData(meBlk, mePuyo);
			chkdt.selectData(new MoveFilter());
			//System.out.println("======== START ========");
			int minrensa = 3;
			int maxrensa = 7;
			if (eneEnv.getFeverRestTime() >= 0) {
				// 敵がフィーバーモード
				maxrensa = 5;
				minrensa = 4;
			}
			MoveData mv2 = meNext[0];
			MoveData mv3 = meNext[1];
			int ojamaNum = meOja[0];
			// たまっているおじゃまぷよが多ければ少ない連鎖でOK
			maxrensa -= (meBlk.getBlockNum(BlockData.BLK_OJAMA)/6);
			if (feverRest >= 0) {
				// フィーバーモード
				int[] sc = getTaneScore(getTaneData(meBlk));
				if (feverFirstRensa < 0) {
					feverFirstRensa = sc[0];
				}
				maxrensa = sc[0];
				minrensa = 0;
				if (meEnv.getFeverRestTime() < 15) {
					// 残り時間が1.5秒をきったら1つ少なくてもよい
					// フィーバーの最初の連鎖数より1少なくてよい
					maxrensa = feverFirstRensa - 1;
					mv3 = null;
				}
			}
			if (meOja[1] > 0) {
				// 予告ぷよがある
				ArrayList<int[]> rensa = new ArrayList<int[]>();
				eneBlk.putPuyoData(new int[0], new int[0], rensa);
				if (rensa.size() <= 2) {
					// ちょっと多めに足す
					ojamaNum += (int)(meOja[1]*1.5);
					// 連鎖数を減らす
					maxrensa--;
				}
			}
			if (ojamaNum >= 6) {
				// ちょっとやばめ
				maxrensa--;
				mv3 = null;
				if (ojamaNum >= 18) {
					maxrensa--;
					mv2 = null;
				}
				if (feverRest < 0) {
					// 通常時、フィーバーになるなら早めに消す
					int newrensa = 7 - meEnv.getFeverStockCount();
					if (newrensa < maxrensa) {
						maxrensa = newrensa;
					}
				}
			}
			// もし通常時で
			if (feverRest < 0 && meBlk.getBlockNum(BlockData.BLK_SPACE) < 7*6) {
				// 残り半分以下になったとき、連鎖数が増えていかなければすぐに消す
				maxrensa = 1;
				mv3 = null;
				//mv2 = mv3 = null;
				if (feverRest < 6) {
					mv2 = null;
				}
			}
			// 左右に移動できなければ連鎖数を引く
			if (meBlk.getBlock(1, 2) > 0) {
				maxrensa--;
			}
			if (meBlk.getBlock(4, 2) > 0) {
				maxrensa--;
			}
			if (maxrensa < 1) {
				maxrensa = 1;
			}
			checkMaxRensa = maxrensa;
			checkMinRensa = minrensa;
			checkOjamaNum = ojamaNum;
			checkNext2 = mv2;
			checkNext3 = mv3;
		}
		/*
		if (ojamaNum >= 6) {
			System.out.println("やばい！！");
			maxrensa = 1;
			mv2 = mv3 = null;
		}
		*/

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
				System.out.println("ERROR!!!->"+Integer.toString(key, 16));
				System.out.println(meBlk);
				System.out.println(mePuyo);
				System.out.println("turn="+mv[1]+", move="+mv[0]);
				System.out.println(pu);
				for (int key2: moveList.keySet()) {
					System.out.println("["+Integer.toString(key2, 16)+"]="+moveList.get(key2)[0]+","+moveList.get(key2)[1]);
				}
				continue;
			}
			int sc1 = checkScoreNest(meBlk, pu, checkNext2, checkOjamaNum, checkMinRensa, checkMaxRensa, true);
			resultMap.put(key, sc1);
			if (checkNext3 != null) {
				int sc2 = checkScoreNest(meBlk, pu, checkNext3, checkOjamaNum, checkMinRensa, checkMaxRensa, false);
				if (sc1 == CheckFilter.IGNORE_DATA || (sc2 != CheckFilter.IGNORE_DATA && sc2 > sc1)) {
					resultMap.put(key, sc2);
				}
			}
			if (checkRestCount > 100) {
				break;
			}
		}
		if (resultMap.size() < moveList.size()) {
			// 続きがある
			return;
		}

		int maxscore = -1000;
		int[] mvnext = null;
		for (int key: resultMap.keySet()) {
			int res = resultMap.get(key);
			if (res != CheckFilter.IGNORE_DATA && res > maxscore) {
				maxscore = res;
				mvnext = moveList.get(key);
			}
		}
		//System.out.println("MAX="+Integer.toString(maxscore, 16));
		if (mvnext != null) {
			super.moveRight(mvnext[0]);
			super.turnRight(mvnext[1]);
			if (feverRest >= 0) {
				// フィーバーモード
				MoveData pu = mePuyo.clone();
				if (mvnext[1] < 0) {
					pu.turnLeft(-mvnext[1]);
				}
				else {
					pu.turnRight(mvnext[1]);
				}
				pu.addX(mvnext[0], meBlk);
				ArrayList<int[]> rensa = new ArrayList<int[]>();
				meBlk.putMoveData(pu, rensa);
				if (rensa.size() > 0) {
					// 消える
					feverFirstRensa = -1;
				}
			}
			/*
			System.out.println("-------move---------");
			System.out.println(meBlk);
			System.out.print(mePuyo);
			System.out.println(" move="+mvnext[0]+",turn="+mvnext[1]);
			MoveData pu = mePuyo.clone();
			if (mvnext[1] < 0) {
				pu.turnLeft(-mvnext[1]);
			}
			else {
				pu.turnRight(mvnext[1]);
			}
			pu.addX(mvnext[0], null);
			int score = checkScoreNest(meBlk, pu, null, ojamaNum, minrensa, maxrensa);
			System.out.println("SCORE="+Integer.toString(score,16));
			*/
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
				nextpuyo = chkdt.selectData(new MyCheckFilter());
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
	 * 連鎖の種を得る
	 * @param blk
	 * @return 配列:[0]=x,[1]=Y,[2]=連鎖数
	 */
	private static ArrayList<int[]> getTaneData(BlockData blk) {
		int[] topy = blk.getBlockY();
		ArrayList<int[]> ret = new ArrayList<int[]>();
		int stx = 0;
		int edx = 6;
		if (topy[1] < 3) {
			// 詰まっていたらチェック対象外
			stx = 1;
		}
		if (topy[4] < 3) {
			edx = 5;
		}
		// 候補一覧を作成する
		for (int x = stx; x < edx; x++) {
			int ly = 0;
			int ry = 0;
			if (x > 0) {
				ly = topy[x-1]-1;
			}
			if (x < 5) {
				ry = topy[x+1]-1;
			}
			int sty = topy[x];
			int edy = Math.max(ly, ry);
			if (edy < sty) {
				edy = sty;
			}
			if (sty < 2) {
				sty = 2;
			}
			if (sty < 14) {
				// 同じ色が縦に連続していたら最初の1つだけを対象にする
				int lastch = 0;
				for (int y = sty; y <= edy; y++) {
					int ch = blk.getBlock(x, y);
					if (ch != lastch && ch != 6) {
						ret.add(new int[]{x, y, 0});
					}
					lastch = ch;
				}
			}
		}
		// ぷよを1つづつ置いてチェックする
		ArrayList<int[]> rensaList = new ArrayList<int[]>();
		for (int[] dt: ret) {
			// 上、右、左の優先順位で行う
			int x = dt[0];
			int y = dt[1];
			BlockData chkblk = null;
			int puyocl = blk.getBlock(x, y);
			if (y == topy[x]) {
				// 上に置く
				chkblk = blk.putPuyoData(x, puyocl, rensaList);
			}
			else if (x < 5 && y < topy[x+1]) {
				// 右に置く
				// 余分なおじゃまぷよで埋める
				int[] cl = new int[topy[x+1]-y];
				int[] xlst = new int[cl.length];
				for (int i = 0; i < cl.length; i++) {
					cl[i] = 6;
					xlst[i] = x+1;
				}
				cl[cl.length - 1] = puyocl;
				chkblk = blk.putPuyoData(xlst, cl, rensaList);
			}
			else if (x > 0 && y < topy[x-1]) {
				// 左に置く
				// 余分なおじゃまぷよで埋める
				int[] cl = new int[topy[x-1]-y];
				int[] xlst = new int[cl.length];
				for (int i = 0; i < cl.length; i++) {
					cl[i] = 6;
					xlst[i] = x-1;
				}
				cl[cl.length - 1] = puyocl;
				chkblk = blk.putPuyoData(xlst, cl, rensaList);
			}
			if (chkblk != null) {
				dt[2] = rensaList.size();
			}
		}
		return ret;
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

	@Override
	protected String getThinkName() {
		return "小林思考ルーチン上級";
	}

}
