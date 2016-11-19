package puyopuyo.think.data;

import java.awt.Point;
import java.util.ArrayList;

public class MoveData implements Cloneable {
	/**
	 * 座標
	 * 4組ぷよの場合
	 *  12
	 *  03
	 */
	private Point[] movePoint;

	/**
	 * 色
	 * 4組ぷよの場合
	 *  12
	 *  03
	 */
	private int[] puyoColor;

	/**
	 * 色数
	 */
	private int colorNum;

	/**
	 * コンストラクタ
	 * @param ptlst
	 * @param cl
	 */
	public MoveData(ArrayList<Point> ptlst, ArrayList<Integer> cl, int clnum) {
		movePoint = new Point[ptlst.size()];
		ptlst.toArray(movePoint);
		puyoColor = new int[cl.size()];
		for (int i = 0; i < puyoColor.length; i++) {
			puyoColor[i] = cl.get(i);
		}
		colorNum = clnum;
	}

	/**
	 * コンストラクタ
	 * @param src
	 */
	public MoveData(MoveData src) {
		movePoint = new Point[src.movePoint.length];
		puyoColor = new int[src.puyoColor.length];
		for (int i = 0; i < puyoColor.length; i++) {
			movePoint[i] = (Point)src.movePoint[i].clone();
			puyoColor[i] = src.puyoColor[i];
		}
		colorNum = src.colorNum;
	}

	/**
	 * ぷよの座標を返す
	 * @return ぷよの数だけ座標を返す
	 */
	public Point[] getPoint() {
		return movePoint;
	}

	/**
	 * ぷよの色を返す
	 * @return ぷよの個数分色を返す
	 */
	public int[] getPuyoColor() {
		return puyoColor;
	}

	/**
	 * 中心ぷよのX座標を取得
	 * @return X座標
	 */
	public int getX() {
		return movePoint[0].x;
	}
	/**
	 * 中心ぷよのY座標を取得
	 * @return Y座標
	 */
	public int getY() {
		return movePoint[0].y;
	}
	/**
	 * 中心ぷよを取得
	 * @return ぷよ
	 */
	public int getPuyo() {
		return puyoColor[0];
	}

	/**
	 * 中心ぷよのX座標を設定する
	 * @param x X座標
	 * @param blk 移動チェックに利用するデータ, nullだとチェックなし
	 * @return 移動できたらtrue
	 */
	public boolean setX(int x, BlockData blk) {
		int ax = x - movePoint[0].x;
		for (int i = 0; i < movePoint.length; i++) {
			int nx = movePoint[i].x + ax;
			if (nx < 0 || nx >= BlockData.BLK_WIDTH) {
				return false;
			}
			if (blk != null && blk.getBlock(nx, movePoint[i].y) > 0) {
				return false;
			}
		}
		for (int i = 0; i < movePoint.length; i++) {
			movePoint[i].x += ax;
		}
		return true;
	}
	/**
	 * 移動できる分だけ移動する
	 * @param ax 正の数だと右移動、負だと左移動
	 * @param blk 移動チェックに利用するデータ, nullだとチェックなし
	 * @return 移動できた数
	 */
	public int addX(int ax, BlockData blk) {
		int ret = 0;
		while (ax != 0) {
			if (ax < 0) {
				if (!setX(movePoint[0].x - 1, blk)) {
					break;
				}
				ret--;
				ax++;
			}
			else {
				if (!setX(movePoint[0].x + 1, blk)) {
					break;
				}
				ret++;
				ax--;
			}
		}
		return ret;
	}

	/**
	 * 隣のぷ世を
	 * @param blk
	 */
	public void turnRight(BlockData blk) {
		
	}
	/**
	 * 隣のぷよを右に1回転させる
	 */
	public void turnRight() {
		turnRight(1);
	}
	/**
	 * 隣のぷよを右に指定した回数だけ回転させる
	 * @param num 回転数
	 */
	public void turnRight(int num) {
		for (int i = 0; i < num; i++) {
			if (movePoint.length == 4) {
				// ４つ組
				// 色だけを入れ替える
				if (puyoColor[0] == puyoColor[1] && puyoColor[0] == puyoColor[2]) {
					// でかぷよ
					int cl = (puyoColor[0] % colorNum) + 1;
					for (int j = 0; j < 4; j++) {
						puyoColor[j] = cl;
					}
				}
				else {
					int bak = puyoColor[3];
					for (int j = 3; j > 0; j--) {
						puyoColor[j] = puyoColor[j-1];
					}
					puyoColor[0] = bak;
				}
			}
			else {
				// ふつう
				int minx = BlockData.BLK_WIDTH;
				int maxx = 0;
				int miny = BlockData.BLK_HEIGHT;
				int maxy = 0;
				for (int j = 1; j < movePoint.length; j++) {
					int ax = movePoint[j].x - movePoint[0].x;
					int ay = movePoint[j].y - movePoint[0].y;
					movePoint[j].x = movePoint[0].x - ay;
					movePoint[j].y = movePoint[0].y + ax;
					if (movePoint[j].x < minx) {
						minx = movePoint[j].x;
					}
					if (movePoint[j].x > maxx) {
						maxx = movePoint[j].x;
					}
					if (movePoint[j].y < miny) {
						miny = movePoint[j].y;
					}
					if (movePoint[j].y > maxy) {
						maxy = movePoint[j].y;
					}
				}
				// 回転ではみ出したのを調整する
				if (minx < 0) {
					// 右へ
					for (int j = 0; j < movePoint.length; j++) {
						movePoint[j].x++;
					}
				}
				else if (maxx >= BlockData.BLK_WIDTH) {
					// 左へ
					for (int j = 0; j < movePoint.length; j++) {
						movePoint[j].x--;
					}
				}
				if (miny < 0) {
					// 下へ
					for (int j = 0; j < movePoint.length; j++) {
						movePoint[j].y++;
					}
				}
				else if (maxy >= BlockData.BLK_HEIGHT) {
					// 上へ
					for (int j = 0; j < movePoint.length; j++) {
						movePoint[j].y--;
					}
				}
			}
		}
	}

	/**
	 * 隣のぷよを左に1回転させる
	 */
	public void turnLeft() {
		turnLeft(1);
	}
	/**
	 * 隣のぷよを左に指定した回数だけ回転させる
	 * @param num 回転数
	 */
	public void turnLeft(int num) {
		if (movePoint.length == 4) {
			// ４つ組
			// 色だけを入れ替える
			if (puyoColor[0] == puyoColor[1] && puyoColor[0] == puyoColor[2]) {
				// でかぷよ
				turnRight((colorNum - num) % colorNum);
				return;
			}
		}
		turnRight((4 - num) & 3);
	}

	/**
	 * クローン作成
	 */
	public MoveData clone() {
		return new MoveData(this);
	}

	/**
	 * 色数を返す
	 * @return 色数
	 */
	public int getColorNum() {
		return colorNum;
	}

	/**
	 * 回転ボタンで色が切り替わるでかぷよかどうかを返す
	 * @return true:でかぷよ,false:普通のぷよか2色組
	 */
	public boolean isBigPuyo() {
		if (puyoColor.length == 4) {
			return (puyoColor[0] == puyoColor[1] && puyoColor[0] == puyoColor[2]);
		}
		return false;
	}

	/**
	 * 差分の移動を取得
	 * @param puyo 移動先のぷよ
	 * @return データ
	 *  [0] = 移動データ
	 *  [1] = 回転データ
	 */
	public int[] getDiff(MoveData puyo) {
		int[] ret = new int[2];
		// dummy
		if (puyoColor.length != puyo.puyoColor.length) {
			System.out.println("CheckError:");
			System.out.println(this);
			System.out.println(puyo);
		}
		ret[0] = puyo.movePoint[0].x - movePoint[0].x;

		if (puyoColor.length == 4) {
			// 大きいぷよ
			if (isBigPuyo()) {
				int df = (puyo.puyoColor[0] - puyoColor[0] + colorNum) % colorNum;
				
				if (df == 0) {
					// 回転なし
				}
				else if (df == 1) {
					ret[1] = 1;
				}
				else if (df == colorNum - 1) {
					ret[1] = -1;
				}
				else if (df == 2) {
					ret[1] = 2;
				}
				else if (df == 3) {
					ret[1] = -2;
				}
			}
			else {
				// 2組のぷよ
				if (puyo.puyoColor[0] == puyoColor[0] && puyo.puyoColor[1] == puyoColor[1]) {
					// 回転なし
				}
				else if (puyo.puyoColor[1] == puyoColor[0] && puyo.puyoColor[2] == puyoColor[1]) {
					ret[1] = 1;
				}
				else if (puyo.puyoColor[2] == puyoColor[0] && puyo.puyoColor[3] == puyoColor[1]) {
					ret[1] = 2;
				}
				else {
					ret[1] = -1;
				}
			}
			return ret;
		}

		int dx1 = movePoint[1].x - movePoint[0].x;
		int dy1 = movePoint[1].y - movePoint[0].y;
		int dx2 = puyo.movePoint[1].x - puyo.movePoint[0].x;
		int dy2 = puyo.movePoint[1].y - puyo.movePoint[0].y;
		if (dx1 == dx2) {
			if (dy1 != dy2) {
				ret[1] = 2;
			}
		}
		else if (dy1 == dy2) {
			if (dx1 != dx2) {
				ret[1] = 2;
			}
		}
		else if (dy1 == dx2 && dx1 == -dy2) {
			// 左回り
			ret[1] = -1;
		}
		else if (dy1 == -dx2 && dx1 == dy2) {
			// 右回り
			ret[1] = 1;
		}
		return ret;
	}

	/**
	 * 文字列にする
	 */
	public String toString() {
		StringBuffer ret = new StringBuffer();
		char[][] buf = new char[3][];
		ret.append("+------+\n");
		int y = movePoint[0].y;
		buf[0] = "      ".toCharArray();
		buf[1] = "      ".toCharArray();
		buf[2] = "      ".toCharArray();
		for (int i = 0; i < movePoint.length; i++) {
			buf[movePoint[i].y - y + 1][movePoint[i].x] = (char)(puyoColor[i]+'0');
		}
		ret.append("|"+new String(buf[0])+"|"+(y-1)+"\n");
		ret.append("|"+new String(buf[1])+"|"+y+"\n");
		ret.append("|"+new String(buf[2])+"|"+(y+1)+"\n");
		ret.append("+------+");
		return ret.toString();
	}
}
