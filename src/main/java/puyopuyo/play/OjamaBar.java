package puyopuyo.play;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import puyopuyo.image.EffectImage;
import puyopuyo.image.ImageData;
import puyopuyo.sound.PlaySE;

/**
 * おじゃまぷよを管理する
 * @author hkoba
 *
 */
public class OjamaBar {
	/**
	 * ぷよのビームを描画するかどうかのフラグ
	 */
	public static boolean optBeamDrawFlag = true;

	/**
	 * おじゃまの光り
	 */
	public static boolean optOjamaStarFlag = true;

	/**
	 * おじゃまの落ちる位置
	 */
	static final int[] dropOjamaX = {
		0, 3, 4, 5, 1, 4
	};

	/**
	 * 実際に降ってくるおじゃまぷよ
	 */
	private int ojamaNum;

	/**
	 * 予告でたまってきているおじゃまぷよ
	 */
	private int yokokuNum;

	/**
	 * 消した直後で、もう少ししたら予告ぷよになる
	 */
	private int stockNum;

	/**
	 * 描画位置
	 */
	private Point drawPt;

	/**
	 * おじゃまぷよが落ちるのが何回目かを示す
	 */
	private int ojamaDropIndex;

	/**
	 * フィーバー用のおじゃまは通常おじゃまを親とする
	 */
	private OjamaBar parentBar;

	/**
	 * モード
	 */
	private int modeNum;

	/**
	 * おじゃまぷよイメージ
	 */
	private Image[] ojama = new Image[6];

	/**
	 * たまる最大おじゃまぷよの数
	 */
	public static final int OJAMA_MAX = 20000;

	/**
	 * おじゃまぷよの画像
	 */
	private static BufferedImage[] ojamaImg;

	/**
	 * 隠れているかどうかのフラグ
	 */
	private boolean hideFlag;

	/**
	 * おじゃまぷよの光るイメージ
	 */
	private BufferedImage ojamaStar;

	/**
	 * ビーム描画用
	 */
	public static BasicStroke[] beamStroke;

	/**
	 * おじゃまの光るイメージ
	 */
	private int ojamaStarNum;

	static {
		beamStroke = new BasicStroke[8];
		for (int i = 0; i < beamStroke.length; i++) {
			beamStroke[i] = new BasicStroke(i * 2 + 1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		}
	}

	/**
	 * おじゃまぷよのイメージを取得
	 * @param level
	 * @return
	 */
	private static Image getOjamaImage(int level) {
		if (ojamaImg != null) {
			return ojamaImg[level];
		}
		ojamaImg = new BufferedImage[6];
		for (int i = 0; i < ojamaImg.length; i++) {
			ojamaImg[i] = ImageData.loadImage("image/OJAMA"+i+".png");
		}
		return ojamaImg[level];
	}

	/**
	 * フィーバー用
	 */
	private FeverGage feverGage;

	/**
	 * おじゃまビームのデータ
	 * [0]=x,[1]=y
	 * [2]= 0:自分,1:親,2:敵
	 */
	private ArrayList<int[]> beamData;

	/**
	 * 送信中の敵のおじゃまバー
	 */
	private OjamaBar enemyBar;

	/**
	 * おじゃまビームカウント
	 */
	private int beamCount;

	public OjamaBar(FeverGage fev) {
		parentBar = null;
		feverGage = fev;
		hideFlag = false;
		ojamaStar = EffectImage.getOjamaStar();
	}
	public OjamaBar(OjamaBar p) {
		parentBar = p;
		ojamaStar = EffectImage.getOjamaStar();
	}

	private int sousaiOjama(int num) {
		int newnum = num;
		if (ojamaNum > 0) {
			if (newnum > ojamaNum) {
				newnum -= ojamaNum;
				ojamaNum = 0;
			}
			else {
				ojamaNum -= newnum;
				newnum = 0;
			}
		}
		if (newnum > 0 && yokokuNum > 0) {
			if (newnum > yokokuNum) {
				newnum -= yokokuNum;
				yokokuNum = 0;
			}
			else {
				yokokuNum -= newnum;
				newnum = 0;
			}
		}
		if (newnum > 0 && stockNum > 0) {
			if (newnum > stockNum) {
				newnum -= stockNum;
				stockNum = 0;
			}
			else {
				stockNum -= newnum;
				newnum = 0;
			}
		}
		return newnum;
	}

	/**
	 * おじゃまビームのポイントを作る
	 * @param stpt
	 * @param edpt
	 * @param num
	 */
	private void setBeamPoint(Point stpt, Point edpt, int num) {
		// まずは直線で
		int addx = 0;
		int addy = -30;
		if (stpt.x > 320) {
			// 右から
			addx = (580 - stpt.x) / 3;
		}
		else {
			// 左から
			addx = (60 - stpt.x) / 2;
		}
		if (num < 10) {
			addx /= 8;
		}
		for (int i = 0; i < num; i++) {
			int[] dt = new int[2];
			double rad = Math.PI * i / num;
			dt[0] = (edpt.x - stpt.x) * i / num + stpt.x + (int)(Math.sin(rad) * addx);
			dt[1] = (edpt.y - stpt.y) * i / num + stpt.y + (int)(Math.sin(rad) * addy);
			beamData.add(dt);
		}
	}

	/**
	 * おじゃまぷよを送出する。
	 * 自分にたまっていなければ即敵に送る
	 * @param enebar 敵のおじゃまバー
	 * @param num    おじゃまの数
	 * @param pt     ビームの開始座標
	 */
	public void sendOjamaPuyo(OjamaBar enebar, int num, Point pt) {
		int newnum = sousaiOjama(num);
		enemyBar = enebar;
		ArrayList<int[]> barpt = new ArrayList<int[]>();
		beamData = new ArrayList<int[]>();
		beamCount = 0;

		if (newnum < num) {
			// いくつか相殺しているので自分へ送る
			Point tmppt = getCenterPoint();
			barpt.add(new int[]{tmppt.x, tmppt.y, 0});
		}
		else {
			// 相殺していない
			if (feverGage != null) {
				feverGage.addStockTime(1, pt);
			}
		}
		if (newnum > 0 && parentBar != null) {
			// 親のおじゃまバーが存在する
			if (parentBar.ojamaNum + parentBar.yokokuNum + parentBar.stockNum > 0) {
				// 親へ相殺する
				Point tmppt = parentBar.getCenterPoint();
				barpt.add(new int[]{tmppt.x, tmppt.y, 1});
				newnum = parentBar.sousaiOjama(newnum);
			}
		}

		// ここで送信先をいろいろ選ぶ
		// newnum=0 なら相殺のみ
		// newnum>0 && sousai=true なら相殺して転送
		// newnum>0 && sousai=false なら即相手に送る
		int aninum = 14;
		if (newnum > 0 && newnum != num) {
			// 相殺して相手へ送る
			aninum = 10;
		}
		if (parentBar != null) {
			// フィーバー
			aninum = aninum / 2;
		}
		if (barpt.size() > 0) {
			setBeamPoint(pt, new Point(barpt.get(0)[0], barpt.get(0)[1]), aninum);
			int[] ptdt = barpt.get(barpt.size() - 1);
			pt = new Point(ptdt[0], ptdt[1]);
		}
		beamData.addAll(barpt);
		if (newnum > 0) {
			enebar.addStock(newnum);
			Point tmppt = enebar.getCenterPoint();
			setBeamPoint(pt, enebar.getCenterPoint(), aninum);
			beamData.add(new int[]{tmppt.x, tmppt.y, 2});
		}
	}

	private void addStock(int num) {
		if (hideFlag) {
			parentBar.addStock(num);
		}
		else {
			stockNum += num;
		}
	}

	/**
	 * 予告ぷよをおじゃまぷよとして確定する
	 *
	 */
	public void applyYokoku() {
		if (hideFlag) {
			parentBar.applyYokoku();
			return;
		}
		ojamaNum += yokokuNum;
		yokokuNum = 0;
		if (ojamaNum > OJAMA_MAX) {
			ojamaNum = OJAMA_MAX;
		}
	}

	/**
	 * 送信中のぷよを予告ぷよとして確定する
	 *
	 */
	private void applyStock() {
		if (hideFlag) {
			parentBar.applyStock();
			return;
		}
		ojamaStarNum = 30;
		if (stockNum >= 30) {
			PlaySE.playSE(PlaySE.YOKOKU_L);
		}
		else if (stockNum >= 18) {
			PlaySE.playSE(PlaySE.YOKOKU_M);
		}
		else if (stockNum >= 6) {
			PlaySE.playSE(PlaySE.YOKOKU_S);
		}
		else {
			PlaySE.playSE(PlaySE.SOUSAI);
		}
		yokokuNum += stockNum;
		stockNum = 0;
		if (ojamaNum + yokokuNum > OJAMA_MAX) {
			yokokuNum = OJAMA_MAX - ojamaNum;
		}
		// イメージを作成する
		modeNum = 0;
	}

	private void resetImage() {
		int oja = ojamaNum + yokokuNum;
		for (int x = 0; x < 6; x++) {
			if (oja >= 720) {
				ojama[x] = getOjamaImage(5);
				oja -= 720;
			}
			else if (oja >= 360) {
				ojama[x] = getOjamaImage(4);
				oja -= 360;
			}
			else if (oja >= 180) {
				ojama[x] = getOjamaImage(3);
				oja -= 180;
			}
			else if (oja >= 30) {
				ojama[x] = getOjamaImage(2);
				oja -= 30;
			}
			else if (oja >= 6) {
				ojama[x] = getOjamaImage(1);
				oja -= 6;
			}
			else if (oja > 0) {
				ojama[x] = getOjamaImage(0);
				oja--;
			}
			else {
				ojama[x] = null;
			}
		}
	}

	/**
	 * おじゃまぷよのビームを飛ばす先の座標を返す
	 * @return
	 */
	private Point getCenterPoint() {
		if (hideFlag) {
			return parentBar.getCenterPoint();
		}
		return new Point(drawPt.x + 99, drawPt.y + 17);
	}

	/**
	 * ターンを進める
	 *
	 */
	public void turnNext() {
		modeNum++;
		if (modeNum == 5) {
			resetImage();
		}
		if (beamData != null) {
			beamCount++;
			if (beamCount < beamData.size()) {
				int[] dt = beamData.get(beamCount);
				if (dt.length > 2) {
					// 特別
					switch (dt[2]) {
					case 0:
						if (feverGage != null) {
							// フィーバーポイントをためる
							feverGage.addFeverPoint(new Point(dt[0], dt[1]));
						}
						this.applyStock();
						break;
					case 1:
						parentBar.applyStock();
						break;
					case 2:
						enemyBar.applyStock();
						break;
					}
				}
			}
			else if (beamCount >= beamData.size() + 5) {
				beamData = null;
				beamCount = 0;
			}
		}
		if (ojamaStarNum > 0) {
			ojamaStarNum--;
		}
	}

	/**
	 * 描画の左上座標を設定する
	 * @param x
	 * @param y
	 */
	public void setDrawPoint(int x, int y) {
		drawPt = new Point(x, y);
	}

	/**
	 * 描画
	 * @param g
	 */
	public void drawBack(Graphics2D g) {
		if (hideFlag) {
			return;
		}
		g.setColor(new Color(197, 139, 222, 192));
		g.fillRect(drawPt.x, drawPt.y, 198, 32);
		int lvl = modeNum - 5;
		if (lvl < 0) {
			lvl = -lvl * 2;
		}
		if (lvl > 10) {
			lvl = 10;
		}
		for (int x = 5; x >= 0; x--) {
			if (ojama[x] != null) {
				int dx = drawPt.x + (x * 31) * lvl / 10 + 6;
				int ht = ojama[x].getHeight(null);
				int dy = drawPt.y + 30 - ht;
				g.drawImage(ojama[x], dx, dy, null);
			}
		}
	}
	public void drawBeam(Graphics2D g) {
		if (hideFlag) {
			return;
		}
		if (optBeamDrawFlag && beamData != null) {
			int dix = beamCount;
			int six = beamCount / 2;
			if (six < dix - 5) {
				six = dix - 5;
			}
			if (dix >= beamData.size()) {
				dix = beamData.size() - 1;
			}
			int[] xp = new int[dix - six + 1];
			int[] yp = new int[dix - six + 1];
			int ix = 0;
			for (int i = six; i <= dix; i++) {
				int[] dt = beamData.get(i);
				xp[ix] = dt[0];
				yp[ix++] = dt[1];
			}

			if (xp.length > 1) {
				Stroke bakpen = g.getStroke();
				//g.setColor(new Color(96, 96, 255, 128));
				g.setColor(new Color(96, 96, 255, 64));
				for (int i = 0; i < xp.length - 1; i++) {
					g.setStroke(beamStroke[i + 3]);
					g.drawLine(xp[i], yp[i], xp[i + 1], yp[i + 1]);
				}
				g.fillOval(xp[xp.length - 1] - 12, yp[xp.length - 1] - 12, 24, 24);
				g.setColor(Color.WHITE);
				for (int i = 0; i < xp.length - 1; i++) {
					g.setStroke(beamStroke[i]);
					g.drawLine(xp[i], yp[i], xp[i + 1], yp[i + 1]);
				}
				g.fillOval(xp[xp.length - 1] - 10, yp[xp.length - 1] - 10, 20, 20);
				g.setStroke(bakpen);
			}
		}
	}
	public void drawStar(Graphics2D g) {
		if (!hideFlag && optOjamaStarFlag) {
			if (ojamaStarNum > 0) {
				Point pt = getCenterPoint();
				AffineTransform tr = AffineTransform.getTranslateInstance(pt.x, pt.y);
				tr.rotate(Math.PI * ojamaStarNum / 26);
				double size = Math.sin(Math.PI * ojamaStarNum / 32) * 0.8 + 0.4;
				tr.scale(size, size);
				tr.translate(-ojamaStar.getWidth() / 2, -ojamaStar.getHeight() / 2);
				g.drawImage(ojamaStar, tr, null);
			}
		}
	}

	/**
	 * たまっているおじゃまぷよを落とす
	 * @param puyo
	 * @return
	 */
	public int dropOjama(PuyoData puyo) {
		int ojama = ojamaNum;
		if (ojama > 30) {
			ojama = 30;
		}
		int ret = ojama;
		if (ojama > 0) {
			// 落とす
			ojamaNum -= ojama;
			int y = -80;
			while (ojama >= 6) {
				for (int x = 0; x < 6; x++) {
					puyo.addPuyo(BlockPuyo.createPuyo(BlockPuyo.PUYO_OJAMA, x, y, 0));
				}
				ojama -= 6;
				y -= 28;
			}
			if (ojama > 0) {
				// 6個未満のあまったぷよ
				for (int i = 0; i < ojama; i++) {
					puyo.addPuyo(BlockPuyo.createPuyo(BlockPuyo.PUYO_OJAMA, dropOjamaX[(ojamaDropIndex + i) % 6], y, 0));
				}
				// ここでやるべきか、ifの外に出して常に進めるべきか・・・？
				ojamaDropIndex++;
			}
			modeNum = 0;
		}
		return ret;
	}
	public int getOjamaNum() {
		return ojamaNum;
	}
	public int getYokokuNum() {
		return yokokuNum;
	}

	public void setHideFlag(boolean flg) {
		hideFlag = flg;
		if (flg) {
			// 親へ転送する
			if (parentBar != null) {
				parentBar.ojamaNum += ojamaNum;
				parentBar.yokokuNum += yokokuNum;
				parentBar.stockNum += stockNum;
				parentBar.modeNum = 0;
				ojamaNum = yokokuNum = stockNum = 0;
			}
			resetImage();
		}
	}

}
