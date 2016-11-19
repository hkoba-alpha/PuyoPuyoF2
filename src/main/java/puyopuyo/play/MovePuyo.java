package puyopuyo.play;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import puyopuyo.think.data.MoveData;

import puyopuyo.sound.PlaySE;

public class MovePuyo {
	public static final int KEY_RIGHT = 1;
	public static final int KEY_DOWN = 2;
	public static final int KEY_LEFT = 4;
	public static final int KEY_TURNR = 16;
	public static final int KEY_TURNL = 32;

	/**
	 * ぷよの色データ
	 * 3x3のデータ
	 * [y][x]で、[1][1]が中心。４つぷよの時は[1][1]が左下
	 */
	private int[][] puyoData;

	/**
	 * 思考ルーチン用データ
	 */
	private MoveData moveData;

	/**
	 * 中心のブロックX座標
	 */
	private int blockX;

	/**
	 * 中心のY座標
	 */
	private int moveY;

	/**
	 * ぷよの色数
	 */
	private int colorNum;

	/**
	 * ぷよを置く座標
	 * int[]= [0]=x,[1]=y,[2]=cl
	 */
	private ArrayList<int[]> pointList;

	/**
	 * 今置くと消える予定の場所
	 */
	private ArrayList<Point> flushList;

	/**
	 * あと何回上に移動できるか
	 */
	private int restUpCount;
	/**
	 * 下に止まっているターン数
	 */
	private int touchCount;

	/**
	 * ターン位置
	 * 0=上,1-2=右上で回転中,3=右,6=下,9=左
	 */
	private int turnNum;

	/**
	 * 回転中を示す
	 * 正の数:右回転中,負の数:左回転中
	 */
	private int turnAdd;

	/**
	 * 左右キーのカウント
	 */
	private int slideKeyCount;
	/**
	 * ターンキーを押しているフラグ
	 */
	private boolean turnKeyFlag;
	/**
	 * ダブルクリックターン用
	 */
	private int doubleTurnCount;

	/**
	 * ４つぷよかどうかのフラグ
	 */
	private boolean bigFlag;

	/**
	 * 描画用クラス
	 * @author kobayah
	 *
	 */
	static class ViewData {
		/**
		 * 中心座標の補正位置
		 */
		Point centerPt;
		/**
		 * 転送元
		 */
		Rectangle srcRect;
		/**
		 * 転送先矩形
		 */
		Rectangle destRect;
		/**
		 * 転送先座標
		 */
		Point destPt;
		/**
		 * 回転の補正
		 */
		int turnAdd;

		ViewData(Point cpt, Rectangle src, Rectangle dstrc, Point dstpt, int tadd) {
			centerPt = cpt;
			srcRect = src;
			destRect = dstrc;
			destPt = dstpt;
			turnAdd = tadd;
		}
	}

	/**
	 * 描画用データ
	 */
	private ArrayList<ViewData> viewList;

	public MovePuyo() {
		puyoData = new int[3][3];
		flushList = new ArrayList<Point>();
		pointList = new ArrayList<int[]>();
		viewList = new ArrayList<ViewData>();
	}

	/**
	 * 長いぷよを追加する
	 * @param cl
	 * @param tadd
	 */
	private void addLongPuyo(int cl, int tadd) {
		Rectangle srcrc = new Rectangle((cl * 32 - 31), 163, 31, 58);
		Rectangle dstrc = new Rectangle(-15, -43, srcrc.width, srcrc.height);
		if (cl == 5) {
			srcrc.width = 43;
			dstrc.width = 43;
			dstrc.x -= 4;
		}
		viewList.add(new ViewData(
				new Point(15, 14),
				srcrc,
				dstrc, null, tadd
				));
		viewList.add(new ViewData(
				new Point(0, 0),
				new Rectangle(cl * 32 + 1, 289, 31, 31),
				null, new Point(0, -28), tadd
				));
	}

	/**
	 * ぷよデータを設定する
	 * @param puyo [0]:上,[1]:真ん中,[2]:右上,[3]:右
	 * @prama clnum 色数
	 */
	public void setPuyo(int[] puyo, int clnum) {
		puyoData = new int[3][3];
		puyoData[0][1] = puyo[0];
		puyoData[1][1] = puyo[1];
		puyoData[0][2] = puyo[2];
		puyoData[1][2] = puyo[3];
		viewList.clear();
		bigFlag = false;
		if (puyo[2] > 0) {
			bigFlag = true;
			if (puyo[1] != puyo[2]) {
				// 2つ組ぷよ
				Rectangle dstrc = new Rectangle(-31, -28, 36, 56);
				Point dstpt = new Point(-13, -13);
				viewList.add(new ViewData(
						new Point(31, 0),
						new Rectangle(puyo[1] * 40 - 38, 228, dstrc.width, dstrc.height),
						dstrc, null, 0
						));
				viewList.add(new ViewData(
						new Point(31, 0),
						new Rectangle(puyo[2] * 40 - 38, 228, dstrc.width, dstrc.height),
						dstrc, null, 6
						));
				viewList.add(new ViewData(
						new Point(15, -16),
						new Rectangle(puyo[1] * 32 + 1, 289, 31, 31),
						null, dstpt, 0
						));
				viewList.add(new ViewData(
						new Point(15, -16),
						new Rectangle(puyo[2] * 32 + 1, 289, 31, 31),
						null, dstpt, 6
						));
			}
		}
		else if (puyo[3] > 0) {
			// L字型ぷよ
			if (puyo[0] == puyo[1]) {
				if (puyo[1] == puyo[3]) {
					// 3色同じ色
					viewList.add(new ViewData(
							new Point(15, 14),
							new Rectangle(puyo[1] * 64 + 176 - 64, 161, 64, 64),
							new Rectangle(-16, -48, 64, 64), null, 0
							));
					viewList.add(new ViewData(
							new Point(0, 0),
							new Rectangle(puyo[1] * 32 + 1, 289, 31, 31),
							null, new Point(0, -28), 0
							));
				}
				else {
					// 縦と右に1つ
					addLongPuyo(puyo[1], 0);
					viewList.add(new ViewData(
							new Point(0, 0),
							new Rectangle(1, puyo[3] * 32 - 31, 31, 30),
							null, new Point(31, 0), 0
							));
				}
			}
			else {
				// 横と上に1つ
				addLongPuyo(puyo[1], 3);
				viewList.add(new ViewData(
						new Point(0, 0),
						new Rectangle(1, puyo[0] * 32 - 31, 31, 30),
						null, new Point(0, -31), 0
						));
			}
		}
		else {
			// 普通の2つぷよ
			if (puyo[0] != puyo[1]) {
				// １つずつの2色ぷよ
				viewList.add(new ViewData(
						new Point(0, -2),
						new Rectangle(1, puyo[1] * 32 - 31, 31, 30),
						null, new Point(0, 0), 0
						));
				viewList.add(new ViewData(
						new Point(0, 0),
						new Rectangle(1, puyo[0] * 32 - 31, 31, 30),
						null, new Point(0, -31), 0
						));
			}
			else {
				// 2色同じぷよ
				addLongPuyo(puyo[1], 0);
			}
		}
		colorNum = clnum;
		turnNum = 0;
		turnAdd = 0;
		blockX = 2;
		restUpCount = 5;
		touchCount = 0;
		slideKeyCount = 0;
		doubleTurnCount = 0;
		turnKeyFlag = false;
		moveY = 29;
		pointList.clear();
		flushList.clear();
		moveData = null;
	}

	/**
	 * 移動する
	 * @param puyo
	 * @param key
	 * @return true:まだ移動の続きがある, false:移動終了
	 */
	public boolean turnNext(PuyoData puyo, int key) {
		// 移動のチェック
		int nx = blockX;
		if ((key & KEY_LEFT) > 0) {
			slideKeyCount++;
			if (slideKeyCount == 1 || slideKeyCount > 4) {
				if (nx > 0) {
					nx--;
				}
			}
		}
		else if ((key & KEY_RIGHT) > 0) {
			slideKeyCount++;
			if (slideKeyCount == 1 || slideKeyCount > 4) {
				if (nx < 5) {
					nx++;
				}
			}
		}
		else {
			slideKeyCount = 0;
		}

		if ((key & (KEY_TURNR|KEY_TURNL)) == 0) {
			turnKeyFlag = false;
		}
		if (doubleTurnCount > 0) {
			doubleTurnCount--;
		}
		int[] topy = puyo.getTopY();
		// ターンのチェック
		if (turnAdd == 0) {
			int newadd = 0;
			// ターンチェックができる
			if ((key & KEY_TURNL) > 0 && !turnKeyFlag) {
				// 左回転
				if (doubleTurnCount > 0) {
					// 2回転
					newadd = -6;
				}
				else {
					newadd = -3;
				}
			}
			else if ((key & KEY_TURNR) > 0 && !turnKeyFlag) {
				// 右回転
				if (doubleTurnCount > 0) {
					// 2回転
					newadd = 6;
				}
				else {
					newadd = 3;
				}
			}
			//
			if (newadd != 0) {
				turnKeyFlag = true;
				int[][] turn = new int[3][3];
				if (bigFlag) {
					// 必ず回転できる
					if (puyoData[0][1] != puyoData[1][2]) {
						// 2色ペア
						if (newadd < 0) {
							// 左回転
							turn[0][1] = puyoData[0][2];
							turn[0][2] = puyoData[1][2];
							turn[1][2] = puyoData[1][1];
							turn[1][1] = puyoData[0][1];
							turnAdd = -3;
						}
						else {
							// 右回転
							turn[0][1] = puyoData[1][1];
							turn[0][2] = puyoData[0][1];
							turn[1][2] = puyoData[0][2];
							turn[1][1] = puyoData[1][2];
							turnAdd = 3;
						}
					}
					else {
						// でかいぷよ
						turnNum = (turnNum + colorNum + (int)Math.signum(newadd)) % colorNum;
						int cl = ((puyoData[0][1] + colorNum + (int)Math.signum(newadd) - 1)%colorNum)+1;
						turn[0][1] = cl;
						turn[0][2] = cl;
						turn[1][1] = cl;
						turn[1][2] = cl;
					}
					puyoData = turn;
					moveData = null;
					PlaySE.playSE(PlaySE.PUYO_KAITEN);
				}
				else {
					// 普通の回転
					boolean okflg = false;
					int pushX = -1;
					int cy = (moveY + 27) / 28;
					turn[1][1] = puyoData[1][1];
					if (newadd == -6 || newadd == 6) {
						// 2回転
						turn[0][1] = puyoData[2][1];
						turn[1][2] = puyoData[1][0];
						turn[2][1] = puyoData[0][1];
						turn[1][0] = puyoData[1][2];
					}
					else if (newadd < 0) {
						// 左回転
						turn[0][1] = puyoData[1][2];
						turn[1][2] = puyoData[2][1];
						turn[2][1] = puyoData[1][0];
						turn[1][0] = puyoData[0][1];
					}
					else {
						// 右回転
						turn[0][1] = puyoData[1][0];
						turn[1][2] = puyoData[0][1];
						turn[2][1] = puyoData[1][2];
						turn[1][0] = puyoData[2][1];
					}
					// 回転できるかをチェックする
					// 左右チェック
					if (turn[1][0] > 0) {
						// 左チェック
						if (nx == 0) {
							nx++;
						}
						else if (topy[nx-1] <= cy) {
							// ぶつかる
							pushX = nx-1;
							nx++;
						}
					}
					if (turn[1][2] > 0) {
						// 右チェック
						if (nx == 5) {
							nx--;
						}
						else if (topy[nx+1] <= cy) {
							// ぶつかる
							pushX = nx+1;
							nx--;
						}
					}
					// 下チェック
					if (nx >= 0 && nx < 6) {
						if (turn[2][1] > 0) {
							// 下がある
							if (topy[nx] >= cy + 2) {
								// OK
								okflg = true;
							}
							else if (restUpCount > 0 && topy[nx] >= cy + 1) {
								// 1つ上にあがればOK
								restUpCount--;
								moveY = (topy[nx] - 2) * 28;
								okflg = true;
							}
						}
						else if (topy[nx] > cy) {
							okflg = true;
						}
						else if (restUpCount > 0 && topy[nx] >= cy) {
							// 1つ上にあがればOK
							// 必要ない？
							restUpCount--;
							moveY = (topy[nx] - 1) * 28;
							okflg = true;
						}
					}
					//
					if (okflg) {
						turnAdd = newadd;
						puyoData = turn;
						moveData = null;
						blockX = nx;
						PlaySE.playSE(PlaySE.PUYO_KAITEN);
						if (pushX >= 0) {
							// 押す
							int by = (moveY + 14 )/28;
							int add = 1;
							if (pushX < blockX) {
								add = -1;
							}
							boolean pushflg = false;
							for (int y = by - 3; y <= by + 3; y++) {
								BlockPuyo pu = puyo.getBlock(pushX, y);
								if (pu != null) {
									pu.sidePush(puyo, add*(6-Math.abs(y - by)));
									pushflg = true;
								}
							}
							if (pushflg) {
								PlaySE.playSE(PlaySE.PUYO_YURE);
							}
						}
					}
					else {
						// ダブルクリックのチェック
						doubleTurnCount = 8;
					}
				}
			}
		}
		// 横移動のチェック
		if (blockX != nx && nx >= 0 && nx < 6) {
			int cy = (moveY + 27) / 28;
			// 移動のチェック
			if (bigFlag) {
				// 大きいぷよ
				if (nx < 5 && topy[nx] > cy && topy[nx+1] > cy) {
					// OK
					blockX = nx;
					moveData = null;
					PlaySE.playSE(PlaySE.PUYO_IDOU);
				}
			}
			else {
				// 普通のぷよ
				if (puyoData[1][0] > 0) {
					// 左がある
					if (nx < 1 || topy[nx-1] <= cy) {
						// NG
						nx = blockX;
					}
				}
				if (puyoData[1][2] > 0) {
					// 右がある
					if (nx > 4 || topy[nx+1] <= cy) {
						// NG
						nx = blockX;
					}
				}
				if (puyoData[2][1] > 0) {
					// 下がある
					if (topy[nx] <= cy + 1) {
						// NG
						nx = blockX;
					}
				}
				else if (topy[nx] <= cy) {
					nx = blockX;
				}
				// 移動する
				if (blockX != nx) {
					// 移動できた!!!
					blockX = nx;
					moveData = null;
					PlaySE.playSE(PlaySE.PUYO_IDOU);
				}
			}
		}
		// ターンのチェック
		if (turnAdd < 0) {
			turnNum = (turnNum + 11) % 12;
			turnAdd++;
		}
		else if (turnAdd > 0) {
			turnNum = (turnNum + 1) % 12;
			turnAdd--;
		}
		// 下に落ちるチェック
		int ny = moveY + 2;
		boolean dwnflg = false;
		if ((key & KEY_DOWN) > 0) {
			ny = moveY + 28;
			dwnflg = true;
		}
		boolean touchFlag = false;
		int topby = (ny + 27) / 28 - 1;
		for (int x = 0; x < 3; x++) {
			for (int y = 1; y < 3; y++) {
				if (puyoData[y][x] > 0) {
					if (topy[blockX+x-1] <= topby + y) {
						// NG
						touchFlag = true;
						ny = (topy[blockX+x-1] - y) * 28;
					}
				}
			}
		}
		if (ny >= moveY - 28) {
			if ((ny + 27)/28 != (moveY + 27)/28) {
				moveData = null;
			}
			moveY = ny;
		}
		if (touchFlag) {
			// チェック
			touchCount++;
			if (touchCount > 20 || (key & KEY_DOWN) > 0) {
				// 終了
				// 光るのを消す
				for (Point pt: flushList) {
					puyo.getBlock(pt.x, pt.y).setFlush(puyo, false);
				}
				// ぷよを着地させる
				for (int yy = 2; yy >= 0; yy--) {
					for (int x = 0; x < 3; x++) {
						if (puyoData[yy][x] > 0) {
							BlockPuyo pu = BlockPuyo.createPuyo(puyoData[yy][x], blockX-1+x, moveY-28-56+yy*28, 1);
							puyo.addPuyo(pu);
							pu.redraw(puyo);
						}
					}
				}
				PlaySE.playSE(PlaySE.PUYO_CHAKUTI);
				return false;
			}
		}
		else {
			touchCount = 0;
		}
		if (dwnflg) {
			puyo.addDropScore();
		}
		if (moveData == null) {
			// 移動されていると moveData がnull
			calcFlushPuyo(puyo);
		}
		return true;
	}
	/**
	 * 回転中かどうかを得る
	 * @return
	 */
	public boolean isTurning() {
		return turnAdd != 0;
	}

	/**
	 * 思考ルーチン用に移動データを返す
	 * @return
	 */
	public MoveData getMoveData() {
		if (moveData == null) {
			//
			ArrayList<Point> ptlst = new ArrayList<Point>();
			ArrayList<Integer> collst = new ArrayList<Integer>();
			int bx = blockX;
			int by = (moveY + 27)/28;
			ptlst.add(new Point(bx, by));
			collst.add(puyoData[1][1]);
			for (int ay = 0; ay < 3; ay++) {
				for (int ax = 0; ax < 3; ax++) {
					if (ax != 1 || ay != 1) {
						// 追加
						if (puyoData[ay][ax] > 0) {
							ptlst.add(new Point(bx+ax-1, by+ay-1));
							collst.add(puyoData[ay][ax]);
						}
					}
				}
			}
			moveData = new MoveData(ptlst, collst, colorNum);
		}
		return moveData;
	}

	/**
	 * 置くと消える場所を計算する
	 * @param puyo
	 */
	private void calcFlushPuyo(PuyoData puyo) {
		// 直前に光っていたところを消す
		ArrayList<Point> baklst = flushList;
		pointList.clear();
		flushList = new ArrayList<Point>();
		int[] topy = puyo.getTopY();

		// まずは置くところを計算する
		for (int ax = 0; ax < 3; ax++) {
			int bx = blockX + ax - 1;
			if (bx < 0 || bx > 5) {
				continue;
			}
			int ny = topy[bx] - 1;
			for (int ay = 2; ay >= 0; ay--) {
				if (puyoData[ay][ax] > 0 && ny >= 0) {
					int[] dt = new int[3];
					dt[0] = bx;
					dt[1] = ny;
					dt[2] = puyoData[ay][ax];
					pointList.add(dt);
					ny--;
				}
			}
		}
		// 次に置いたところからつながるものを探す
		ArrayList<Point> ptlst = new ArrayList<Point>();
		for (int[] pt: pointList) {
			ptlst.clear();
			checkChain(puyo, pt[0], pt[1], ptlst, pt[2]);
			if (ptlst.size() >= 4) {
				// 消える！！
				for (Point blkpt: ptlst) {
					if (flushList.contains(blkpt)) {
						continue;
					}
					if (puyo.getBlock(blkpt.x, blkpt.y) != null) {
						// OK
						flushList.add(blkpt);
					}
				}
			}
		}
		// 新たに光らせる
		for (Point pt: flushList) {
			BlockPuyo blk = puyo.getBlock(pt.x, pt.y);
			if (blk != null) {
				blk.setFlush(puyo, true);
			}
		}
		// 前に光っていて、今回対象外のところを消す
		for (Point pt: baklst) {
			if (!flushList.contains(pt)) {
				BlockPuyo blk = puyo.getBlock(pt.x, pt.y);
				if (blk != null) {
					blk.setFlush(puyo, false);
				}
			}
		}
		//
	}

	/**
	 * つながりを取得する
	 * @param puyo
	 * @param bx
	 * @param by
	 * @param ptlst
	 * @param cl
	 */
	private void checkChain(PuyoData puyo, int bx, int by, ArrayList<Point> ptlst, int cl) {
		BlockPuyo blk = puyo.getBlock(bx, by);
		if (blk == null) {
			boolean okflg = false;
			for (int[] pt: pointList) {
				if (bx == pt[0] && by == pt[1] && cl == pt[2]) {
					// OK
					okflg = true;
					break;
				}
			}
			if (!okflg) {
				return;
			}
		}
		else if (blk.getPuyoColor() != cl) {
			return;
		}
		Point pt = new Point(bx, by);
		if (ptlst.contains(pt)) {
			return;
		}
		ptlst.add(pt);
		checkChain(puyo, bx + 1, by, ptlst, cl);
		checkChain(puyo, bx - 1, by, ptlst, cl);
		checkChain(puyo, bx, by + 1, ptlst, cl);
		checkChain(puyo, bx, by - 1, ptlst, cl);
	}

	static int[] bigPuyoPt = {
		264, 226,
		328, 226,
		392, 224,
		370, 289,
		432, 293,
	};

	/**
	 * 画像も含めて回転させて表示する
	 * @param g
	 * @param cx  中心の座標
	 * @param cy
	 * @param src アイコン座標
	 * @param dst 転送先の相対座標
	 * @param turn 回転
	 */
	private static void drawTurn(Graphics2D g, int cx, int cy, Rectangle src, Rectangle dst, int turn) {
		AffineTransform bak = g.getTransform();
		AffineTransform tr = AffineTransform.getTranslateInstance(cx, cy);
		tr.rotate(Math.PI * turn / 6);
		g.setTransform(tr);
		g.drawImage(BlockPuyo.getPuyoImage(), dst.x, dst.y, dst.x + dst.width, dst.y + dst.height,
				src.x, src.y, src.x + src.width, src.y + src.height, null);
		g.setTransform(bak);
	}
	/**
	 * 画像はそのままで座標だけ回転させる
	 * @param g
	 * @param cx
	 * @param cy
	 * @param src
	 * @param dst
	 * @param turn
	 */
	private static void drawTurn(Graphics2D g, int cx, int cy, Rectangle src, Point dst, int turn) {
		double rad = Math.PI * turn / 6;
		double sn = Math.sin(rad);
		double cs = Math.cos(rad);
		int dx1 = cx + (int)(dst.x * cs - dst.y * sn);
		int dy1 = cy + (int)(dst.x * sn + dst.y * cs);
		g.drawImage(BlockPuyo.getPuyoImage(), dx1, dy1, dx1 + src.width, dy1 + src.height,
				src.x, src.y, src.x + src.width, src.y + src.height, null);
	}

	/**
	 * 描画する
	 * @param g
	 * @param sx
	 * @param sy
	 */
	public void draw(Graphics2D g, int sx, int sy) {
		int cx = blockX * 31 + sx;
		int cy = moveY + sy;
		for (int[] dt: pointList) {
			if (dt[1] >= 2) {
				// ここで表示
				int dx = dt[0] * 31 + 11 + sx;
				int dy = dt[1] * 28 + 10 + sy;
				int sx1 = 275 + dt[2] * 16;
				g.drawImage(BlockPuyo.getPuyoImage(), dx, dy, dx + 9, dy + 9,
						sx1, 339, sx1 + 9, 348, null);
			}
		}
		if (bigFlag && puyoData[1][1] == puyoData[0][2]) {
			// でかぷよ
			int dx1 = cx;
			int dx2 = cx + 62;
			int dy1 = cy - 28;
			int dy2 = dy1 + 56;
			int sx1 = bigPuyoPt[(puyoData[1][1] - 1) * 2];
			int sy1 = bigPuyoPt[(puyoData[1][1] - 1) * 2 + 1];
			int sx2 = sx1 + 62;
			int sy2 = sy1 + 56;
			if (puyoData[1][1] == 3 || puyoData[1][1] == 4) {
				// ちょっと大きい
				dy1 -= 2;
				dy2 += 2;
				sy2 += 4;
			}
			g.drawImage(BlockPuyo.getPuyoImage(), dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
			return;
		}
		// 普通
		for (ViewData dt: viewList) {
			if (dt.destRect != null) {
				drawTurn(g, cx + dt.centerPt.x, cy + dt.centerPt.y, dt.srcRect, dt.destRect, turnNum + dt.turnAdd);
			}
			if (dt.destPt != null) {
				drawTurn(g, cx + dt.centerPt.x, cy + dt.centerPt.y, dt.srcRect, dt.destPt, turnNum + dt.turnAdd);
			}
		}
	}

	/**
	 * ３つぷよ以上の大きさか
	 * @return
	 */
	public boolean isWideSize() {
		return puyoData[1][2] > 0;
	}
}
