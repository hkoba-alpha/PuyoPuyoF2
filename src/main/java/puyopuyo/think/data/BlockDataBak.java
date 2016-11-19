package puyopuyo.think.data;

import java.awt.Point;
import java.util.ArrayList;

public class BlockDataBak {
	/**
	 * 横幅
	 */
	public static final int BLK_WIDTH = 6;
	/**
	 * 高さ
	 */
	public static final int BLK_HEIGHT = 14;

	/**
	 * 範囲外のブロックデータ
	 */
	public static final int BLK_OUTSIDE = -1;
	/**
	 * 何も置かれていない場所
	 */
	public static final int BLK_SPACE = 0;
	/**
	 * おじゃまぷよ
	 */
	public static final int BLK_OJAMA = 6;

	/**
	 * ブロックデータ
	 */
	private int[][] blkData;

	/**
	 * コンストラクタ
	 * @param dt
	 */
	public BlockDataBak(int[][] dt) {
		blkData = dt;
	}

	/**
	 * ブロックを返す
	 * @param x 0〜5
	 * @param y 0〜13
	 * @return ブロックの値:1〜5が色ぷよ
	 */
	public int getBlock(int x, int y) {
		if (y < 0 || y >= blkData.length) {
			return BLK_OUTSIDE;
		}
		if (x < 0 || x >= BLK_WIDTH) {
			return BLK_OUTSIDE;
		}
		return blkData[y][x];
	}

	/**
	 * 指定したブロックの数を求める
	 * @param blk
	 * @return 数
	 */
	public int getBlockNum(int blk) {
		int ret = 0;
		for (int y = 0; y < blkData.length; y++) {
			for (int x = 0; x < BLK_WIDTH; x++) {
				if (blkData[y][x] == blk) {
					ret++;
				}
			}
		}
		return ret;
	}

	/**
	 * 指定したＸ座標へぷよを1つ落とす
	 * 同じＸ座標を複数設定した場合、後に設定した方が上に積まれる
	 * @param blockX
	 * @param puyoColor
	 * @param fadeList 消えるリストを取得したいときに指定する
	 * @return 新しいインスタンスが返る
	 */
	public BlockData putPuyoData(int[] blockX, int[] puyoColor, ArrayList<int[]> fadeList) {
		int[][] newblk = new int[BLK_HEIGHT][BLK_WIDTH];
		for (int y = 0; y < blkData.length; y++) {
			System.arraycopy(blkData[y], 0, newblk[y], 0, BLK_WIDTH);
		}
		if (fadeList != null) {
			fadeList.clear();
		}
		for (int i = 0; i < blockX.length; i++) {
			int x = blockX[i];
			for (int y = 13; y >= 0; y--) {
				if (newblk[y][x] == BLK_SPACE) {
					newblk[y][x] = puyoColor[i];
					break;
				}
			}
		}
		nextCheck(newblk, fadeList);
		return new BlockData(newblk);
	}
	/**
	 * 指定したＸ座標へぷよを1つ落とす
	 * @param blockX
	 * @param puyoColor
	 * @param fadeList 消えるリストを取得したいときに指定する
	 * @return 新しいインスタンスが返る
	 */
	public BlockData putPuyoData(int blockX, int puyoColor, ArrayList<int[]> fadeList) {
		int[] blk = new int[1];
		int[] cl = new int[1];
		blk[0] = blockX;
		cl[0] = puyoColor;
		return putPuyoData(blk, cl, fadeList);
	}
	/**
	 * 移動中のぷよを置いた後の結果データを求める
	 * @param data 移動中のぷよ
	 * @param fadeList 消えるリストを取得したいときに指定する
	 * @return 新しいインスタンスが返る
	 */
	public BlockData putMoveData(MoveData data, ArrayList<int[]> fadeList) {
		int[][] newblk = new int[BLK_HEIGHT][BLK_WIDTH];
		for (int y = 0; y < blkData.length; y++) {
			System.arraycopy(blkData[y], 0, newblk[y], 0, BLK_WIDTH);
		}

		for (int i = 0; i < data.getPuyoColor().length; i++) {
			Point pt = data.getPoint()[i];
			newblk[pt.y][pt.x] = data.getPuyoColor()[i];
		}

		if (fadeList != null) {
			fadeList.clear();
		}
		nextCheck(newblk, fadeList);
		return new BlockData(newblk);
	}

	/**
	 * 消すためのチェック
	 * @param blk
	 * @param arlst 消えるぷよを設定するためのOUTパラメタ。null指定可能
	 */
	private static void nextCheck(int[][] blk, ArrayList<int[]> arlst) {
		// 落ちるチェック
		boolean chkflg = true;

		while (chkflg) {
			for (int x = 0; x < BLK_WIDTH; x++) {
				int sy = -1;
				for (int y = 0; y < BLK_HEIGHT ; y++) {
					if (blk[y][x] == BLK_SPACE) {
						if (sy >= 0) {
							// 落とす
							for (int y2 = y; y2 > sy; y2--) {
								blk[y2][x] = blk[y2-1][x];
							}
							blk[sy][x] = BLK_SPACE;
							sy++;
						}
					}
					else if (sy < 0) {
						sy = y;
					}
				}
			}
			// 結合チェック
			chkflg = false;
			ArrayList<Integer> tmplst = new ArrayList<Integer>();
			for (int y = 2; y < BLK_HEIGHT; y++) {
				for (int x = 0; x < BLK_WIDTH; x++) {
					if (blk[y][x] > 0 && blk[y][x] < BLK_OJAMA) {
						// チェック
						int ret = chainCheck(blk, x, y, blk[y][x]);
						if (ret >= 4) {
							// 消す
							tmplst.add(ret);
							chainCheck(blk, x, y, blk[y][x]);
							chkflg = true;
						}
					}
				}
			}
			// フラグをリセット
			for (int y = 2; y < BLK_HEIGHT; y++) {
				for (int x = 0; x < BLK_WIDTH; x++) {
					blk[y][x] &= 0xf;
				}
			}
			if (chkflg && arlst != null) {
				// 追加
				int[] dt = new int[tmplst.size()];
				for (int i = 0; i < dt.length; i++) {
					dt[i] = tmplst.get(i);
				}
				arlst.add(dt);
			}
		}
		
	}

	/**
	 * 指定の箇所に置いたときにつながる数
	 * @param puyo
	 * @return つながる数
	 *  [0] = 中心のぷよがつながる数
	 *  [1] = 隣のぷよがつながる数
	 *  両方が同じ色でつながる場合は [0] のみに結合数が入る
	 */
	public int[] checkChainNum(MoveData puyo) {
		int[] cllst = puyo.getPuyoColor();
		Point[] ptlst = puyo.getPoint();
		int[] ret = new int[cllst.length];

		Point[] pt = new Point[ptlst.length];
		int maxy = 0;
		for (int i = 0; i < ptlst.length; i++) {
			pt[i] = ptlst[i];
			if (blkData[pt[i].y][pt[i].x] > 0) {
				// NG
				return ret;
			}
			if (pt[i].y > maxy) {
				maxy = pt[i].y;
			}
		}
		// 移動できるのでぷよを落としてチェックする
		// まずはY座標の大きい方からチェックする
		for (int i = 0; i < pt.length; i++) {
			if (ptlst[i].y == maxy) {
				int y = 0;
				while (y < 13) {
					if (blkData[y+1][pt[i].x] > 0) {
						break;
					}
					y++;
				}
				pt[i] = new Point(pt[i].x, y);
				blkData[y][pt[i].x] = cllst[i];
			}
		}
		for (int i = 0; i < pt.length; i++) {
			if (ptlst[i].y != maxy) {
				int y = 0;
				while (y < 13) {
					if (blkData[y+1][pt[i].x] > 0) {
						break;
					}
					y++;
				}
				pt[i] = new Point(pt[i].x, y);
				blkData[y][pt[i].x] = cllst[i];
			}
		}

		// つながっている数をチェックする
		ArrayList<Point>[] chkpt = new ArrayList[pt.length];
		for (int i = 0; i < pt.length; i++) {
			chkpt[i] = new ArrayList<Point>();
			boolean chkflg = true;
			for (int j = 0; j < i; j++) {
				if (chkpt[j].contains(pt[i])) {
					// すでにチェック済みと同じ色
					chkflg = false;
					break;
				}
			}
			if (!chkflg) {
				continue;
			}
			getChainNum(pt[i].x, pt[i].y, cllst[i], chkpt[i]);
			ret[i] = chkpt[i].size();
		}

		// 元に戻す
		for (int i = 0; i < pt.length; i++) {
			blkData[pt[i].y][pt[i].x] = BLK_SPACE;
		}

		return ret;
	}

	/**
	 * つながっている数を計算する
	 * @param x
	 * @param y
	 * @param num
	 * @param ptlst つながっている座標を入れる
	 */
	private void getChainNum(int x, int y, int num, ArrayList<Point> ptlst) {
		if (y < 0 || y >= BLK_HEIGHT || x < 0 || x >= BLK_WIDTH) {
			return;
		}
		if (blkData[y][x] != num) {
			return;
		}
		Point pt = new Point(x, y);
		if (ptlst.contains(pt)) {
			return;
		}
		ptlst.add(pt);
		getChainNum(x + 1, y, num, ptlst);
		getChainNum(x - 1, y, num, ptlst);
		getChainNum(x, y + 1, num, ptlst);
		getChainNum(x, y - 1, num, ptlst);
	}

	/**
	 * 結合チェック
	 * @param blk
	 * @param x
	 * @param y
	 * @param num 0x100以上だと消す
	 * @return
	 */
	private static int chainCheck(int[][] blk, int x, int y, int num) {
		if (y < 2 || y >= BLK_HEIGHT || x < 0 || x >= BLK_WIDTH) {
			return 0;
		}
		if (blk[y][x] != num) {
			return 0;
		}
		if (num > 0x100) {
			// 消すモード
			blk[y][x] = BLK_SPACE;
			// 隣のおじゃまぷよも消す
			if (x > 0 && blk[y][x-1] == BLK_OJAMA) {
				blk[y][x-1] = BLK_SPACE;
			}
			if (x < 5 && blk[y][x+1] == BLK_OJAMA) {
				blk[y][x+1] = BLK_SPACE;
			}
			if (y > 2 && blk[y-1][x] == BLK_OJAMA) {
				blk[y-1][x] = BLK_SPACE;
			}
			if (y < BLK_HEIGHT - 1 && blk[y+1][x] == BLK_OJAMA) {
				blk[y+1][x] = BLK_SPACE;
			}
		}
		else {
			blk[y][x] |= 0x100;
		}
		int ret = 1;
		ret += chainCheck(blk, x - 1, y, num);
		ret += chainCheck(blk, x + 1, y, num);
		ret += chainCheck(blk, x, y - 1, num);
		ret += chainCheck(blk, x, y + 1, num);
		return ret;
	}

	/**
	 * 各場所の積み重なっている最上位のY座標を得る
	 * @return
	 */
	public int[] getBlockY() {
		int[] ret = new int[BLK_WIDTH];
		for (int x = 0; x < BLK_WIDTH; x++) {
			ret[x] = BLK_HEIGHT;
			for (int y = 0; y < BLK_HEIGHT; y++) {
				if (blkData[y][x] != BLK_SPACE) {
					ret[x] = y;
					break;
				}
			}
		}
		return ret;
	}

	static String blkCharaStr = " 12345*";

	/**
	 * ブロックの内容を文字列にする
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("+------+\n");
		for (int y = 0; y < 14; y++) {
			buf.append('|');
			for (int x = 0; x < 6; x++) {
				buf.append(blkCharaStr.charAt(blkData[y][x]));
			}
			buf.append("|\n");
		}
		buf.append("+------+");
		return buf.toString();
	}
}
