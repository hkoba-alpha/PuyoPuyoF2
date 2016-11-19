package puyopuyo.think;

import java.util.ArrayList;

import puyopuyo.think.data.BlockData;
import puyopuyo.think.data.EnvData;
import puyopuyo.think.data.MoveData;
import puyopuyo.think.util.CheckData;
import puyopuyo.think.util.CheckFilter;
import puyopuyo.think.util.CheckMostRensa;

public class HkobaThink2 extends AbstractPuyoThink {

	/**
	 * 無駄にぷよを消さずに連鎖できるところを探す
	 * @author hkoba
	 *
	 */
	static class CheckSmallRensa implements CheckFilter {
		public int checkScore(BlockData blkdt, MoveData mvdt) {
			ArrayList<int[]> rensa = new ArrayList<int[]>();
			blkdt.putMoveData(mvdt, rensa);
			if (rensa.size() == 0) {
				return IGNORE_DATA;
			}
			int ret = 1000;
			for (int[] dt: rensa) {
				int num = 0;
				for (int n: dt) {
					num += n;
				}
				ret -= (num-4);
			}
			return ret;
		}
	}
	static class CheckMostTane implements CheckFilter {
		static int[] limitY = {7, 8, 10, 10, 8, 7};
		private boolean rensaNgFlag;

		/**
		 * コンストラクタ
		 * @param rensang 消すのがＮＧならtrue
		 */
		CheckMostTane(boolean rensang) {
			rensaNgFlag = rensang;
		}

		public int checkScore(BlockData blkdt, MoveData mvdt) {
			ArrayList<int[]> rensa = new ArrayList<int[]>();
			BlockData blk = blkdt.putMoveData(mvdt, rensa);
			if (rensaNgFlag && rensa.size() > 0) {
				return IGNORE_DATA;
			}
			// もしゲームオーバーなら論外
			if (blk.getBlock(2, 2) > 0 || blk.getBlock(3, 2) > 0) {
				return -1000;
			}
			int[] ret = getTaneScore(getTaneData(blk));
			// 場所によってスコアから引く
			int[] topy = blk.getBlockY();
			for (int x = 0; x < 6; x++) {
				if (topy[x] < limitY[x]) {
					ret[1] -= (limitY[x] - topy[x]);
				}
			}
			if (topy[1] < 3 || topy[4] < 3) {
				// 左右に移動できない
				ret[1] -= 100;
			}
			int[] chain = blkdt.checkChainNum(mvdt);
			for (int i = 0; i < chain.length; i++) {
				if (chain[i] < 4 && chain[i] > 1) {
					ret[1] += chain[i]*2;
				}
			}
			return ret[1];
		}
	}
	static class CheckZenkesi implements CheckFilter {

		public int checkScore(BlockData blkdt, MoveData mvdt) {
			ArrayList<int[]> rensa = new ArrayList<int[]>();
			BlockData blk = blkdt.putMoveData(mvdt, rensa);
			if (blk.getBlockNum(BlockData.BLK_SPACE) == 14*6) {
				// 全消し
				return rensa.size();
			}
			return IGNORE_DATA;
		}

	}

	@Override
	protected void think(BlockData meBlk, MoveData mePuyo, EnvData meEnv,
			BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {
		int[] meOja = meEnv.getOjamaPuyo();
		int feverRest = meEnv.getFeverRestTime();

		// 連鎖の種情報
		ArrayList<int[]> tanedt = getTaneData(meBlk);
		int[] tanescore = getTaneScore(tanedt);
		//System.out.println("現在の最大連鎖："+tanescore[0]+" ,種のスコア："+(tanescore[1]&0xffff));
		CheckData checkdt = new CheckData(meBlk, mePuyo);
		MoveData nextpuyo = null;
		int[] topy = meBlk.getBlockY();
		int ojamanum = meOja[0];
		if (meOja[1] > 0) {
			// 予告ぷよが飛んできている
			ArrayList<int[]> enelst = new ArrayList<int[]>();
			eneBlk.putPuyoData(new int[0], new int[0], enelst);
			if (enelst.size() <= 2) {
				// もう連鎖が終わる
				ojamanum += meOja[1];
			}
		}

		if (feverRest >= 0) {
			// フィーバー中
			int limit = tanescore[0];
			/*
			if (limit < 3) {
				// Error
				System.out.println(meBlk);
				for (int[] dt: tanedt) {
					System.out.println("("+dt[0]+","+dt[1]+")="+dt[2]);
				}
			}
			*/
			if (ojamanum >= 5) {
				// とにかく置く
				limit = 1;
				//System.out.println("フィーバーでやばいのでとにかく消す");
			}
			else if (feverRest < 30) {
				// 3秒以内なら少し少なくても消す
				limit--;
			}
			nextpuyo = checkdt.selectData(new CheckMostRensa(limit));
			if (nextpuyo == null) {
				//System.out.println("フィーバーで消せない！！！");
				nextpuyo = checkdt.selectData(new CheckMostTane(true));
				if (nextpuyo == null) {
					// 消さずには置けない・・・
					// とにかく消す
					//System.out.println("フィーバーでもう消すしかない！！！");
					nextpuyo = checkdt.selectData(new CheckMostRensa(1));
				}
			}
		}
		else {
			// 全消し優先
			nextpuyo = checkdt.selectData(new CheckZenkesi());
			if (nextpuyo == null) {
				if (ojamanum >= 6) {
					// おじゃまぷよがいるので細かく消す
					nextpuyo = checkdt.selectData(new CheckSmallRensa());
				}
				else {
					// 7連鎖以上
					int limit = 7;
					if (topy[2] < 5 || topy[3] < 5 || meOja[0]+meOja[1] >= 6) {
						// とにかく消す
						limit = 1;
					}
					else {
						// 平均値で求める？
						int num = 0;
						for (int x = 0; x < 6; x++) {
							num += topy[x];
						}
						num /= 6;
						if (num < 5) {
							// ちょっとずつ消す
							limit = 2;
						}
						else if (num < 6) {
							// ちょっとずつ消す
							limit = 2;
						}
						else if (num < 8) {
							limit = 3;
						}
						else if (num < 10) {
							limit = 4;
						}
					}
					//System.out.println("消す予定："+limit);
					nextpuyo = checkdt.selectData(new CheckMostRensa(limit));
				}
			}
		}
		//
		if (nextpuyo == null) {
			// 種スコアの一番多くなるところを選ぶ
			nextpuyo = checkdt.selectData(new CheckMostTane(false));
		}
		if (nextpuyo != null) {
			int[] diff = mePuyo.getDiff(nextpuyo);
			super.moveRight(diff[0]);
			super.turnRight(diff[1]);
		}
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
		// 候補一覧を作成する
		for (int x = 0; x < 6; x++) {
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

	@Override
	protected String getThinkName() {
		return "小林思考ルーチン中級";
	}

}
