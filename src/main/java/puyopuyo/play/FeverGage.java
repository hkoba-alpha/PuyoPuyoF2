package puyopuyo.play;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import puyopuyo.data.FeverData;
import puyopuyo.image.DigitImage;
import puyopuyo.image.EffectImage;
import puyopuyo.sound.PlaySE;

public class FeverGage {

	/**
	 * フィーバーの飛んでいく光を描画するか
	 */
	public static boolean optFeverEffectFlag = true;

	/**
	 * フィーバーゲージの光を描画するか
	 */
	public static boolean optFeverStarFlag = true;

	static int[] feverDrawPt = {
		257,310,16,
		257,292,16,
		257,274,16,
		271,258,16,
		273,236,20,
		268,209,24,
		251,184,26,
	};

	/**
	 * フィーバーポイント
	 */
	private int feverPoint;

	/**
	 * フィーバー時間
	 */
	private int feverStockTime;

	/**
	 * カウント中のフィーバー時間
	 */
	private int feverRestTime;

	/**
	 * フィーバーモードのフラグ
	 */
	private boolean feverMode;

	/**
	 * フィーバーの種の数
	 */
	private int feverTaneNum;

	/**
	 * フィーバーの座標を覚える
	 */
	private Rectangle[] feverRect;

	/**
	 * 時間の座標
	 */
	private Rectangle timeRect;

	/**
	 * フィーバー直前の光っている状態
	 */
	private int modeNum;

	/**
	 * ゲームオーバーになった
	 */
	private boolean gameOverFlag;

	/**
	 * 追加時間を描画する
	 * 10進数で下4けたが足す時間
	 * 10000以上の値が残り時間
	 */
	private int addTimeDrawNum;

	private BufferedImage feverStarImg;

	/**
	 * フィーバーデータ
	 */
	private FeverData feverData;

	/**
	 * フィーバーゲージへとんでいく演出用
	 * @author hkoba
	 *
	 */
	class FeverPoint {
		Point destPt;
		Point srcPt;
		Rectangle destRect;
		int num;
		int add;
		double[] radian;
		static final int TURN_MAX = 600;

		FeverPoint(Point src, Rectangle dst, int n) {
			destRect = dst;
			destPt = new Point(dst.x + dst.width/2, dst.y + dst.height/2);
			srcPt = src;
			num = 0;
			add = 0;
			radian = new double[n];
			double rad = Math.atan2(destPt.y - src.y, destPt.x - src.x) + Math.PI/2;
			if (Math.random() < 0.5) {
				rad += Math.PI;
			}
			for (int i = 0; i < n; i++) {
				radian[i] = rad;
				rad += (Math.PI*2/n);
			}
		}

		void draw(Graphics2D g) {
			if (num >= TURN_MAX) {
				if (destRect.width != destRect.height) {
					// 時間
					double alp = Math.sin(Math.PI*(num-TURN_MAX)/20);
					g.setColor(new Color(255, 255, 224, (int)(alp*200)));
					g.fillRect(destRect.x, destRect.y, destRect.width, destRect.height);
				}
				else {
					// フィーバーカウント
					Rectangle rc = destRect;
					if (optFeverStarFlag) {
						AffineTransform tr = AffineTransform.getTranslateInstance(rc.x + rc.width / 2, rc.y + rc.height / 2);
						tr.rotate(Math.PI * num / 20);
						double bai = rc.width * (Math.sin(Math.PI * (num - TURN_MAX) / 20) * 0.5 + 1.0) / 50.0;
						tr.scale(bai, bai);
						tr.translate(-100, -100);
						g.drawImage(feverStarImg, tr, null);
					}
					else {
						g.setColor(new Color(255 - (num & 4)*8, 255 - (num & 4)*4, 255));
						g.fillOval(rc.x, rc.y, rc.width, rc.height);
					}
				}
			}
			else {
				double sz = Math.sin(Math.PI*num/TURN_MAX)*32;
				for (int i = 0; i < radian.length; i++) {
					int x = (destPt.x - srcPt.x)*num/TURN_MAX+srcPt.x + (int)(Math.cos(radian[i])*sz);
					int y = (destPt.y - srcPt.y)*num/TURN_MAX+srcPt.y + (int)(Math.sin(radian[i])*sz);
					g.setColor(new Color(255, 255, 128, 128));
					g.fillOval(x - 8, y - 8, 16, 16);
					g.setColor((new Color(255, 255, 224)));
					g.fillOval(x - 5, y - 5, 10, 10);
				}
			}
		}

		/**
		 * ターンを進める
		 * @return 続きがあれば true
		 */
		boolean turnNext() {
			num += add;
			add++;
			if (num >= TURN_MAX) {
				if (add > 1) {
					num = TURN_MAX;
					if (destRect.width == destRect.height) {
						PlaySE.playSE(PlaySE.FEVER_ADD);
					}
					else {
						// 時間を足す
						addFeverDrawTime(100);
					}
				}
				else {
					num++;
				}
				add = 0;
				if (num > TURN_MAX + 20) {
					return false;
				}
			}
			return true;
		}
	};
	/**
	 * フィーバーゲージへ飛んでいく演出
	 */
	private ArrayList<FeverPoint> feverEffect = new ArrayList<FeverPoint>();
	/**
	 * 時間ゲージへ飛んでいく演出
	 */
	private ArrayList<FeverPoint> timeEffect = new ArrayList<FeverPoint>();

	public FeverGage(boolean rev, long seed) {
		feverData = new FeverData(seed);
		feverStarImg = EffectImage.getFeverStarImage();
		feverStockTime = 15;
		feverTaneNum = 5;
		feverRect = new Rectangle[7];
		for (int i = 0; i < 7; i++) {
			int x = feverDrawPt[i * 3];
			int y = feverDrawPt[i * 3 + 1];
			int sz = feverDrawPt[i * 3 + 2];
			if (rev) {
				x = 640 - x - sz;
			}
			feverRect[i] = new Rectangle(x, y, sz, sz);
		}
		timeRect = new Rectangle(266, 328, 11*4+4, 21);
		if (rev) {
			timeRect.x = 320 + (320 - timeRect.x - timeRect.width);
		}
	}

	/**
	 * フィーバーポイントをためる
	 * @param stpt
	 */
	public void addFeverPoint(Point stpt) {
		if (feverPoint >= 7) {
			return;
		}
		int n = (feverPoint + 4) / 3;
		feverEffect.add(new FeverPoint(stpt, feverRect[feverPoint], n));
		feverPoint++;
	}

	/**
	 * 通常時にためるフィーバー時間
	 * @param tm
	 * @param stpt
	 */
	public void addStockTime(int tm, Point stpt) {
		if (feverStockTime >= 30) {
			return;
		}
		int n = 1;
		if (feverStockTime >= 25) {
			n = 3;
		}
		else if (feverStockTime >= 20) {
			n = 2;
		}
		timeEffect.add(new FeverPoint(stpt, timeRect, n));
		feverStockTime++;
	}

	/**
	 * 全消しボーナス
	 *
	 */
	public void zenkesiBonus() {
		if (feverMode) {
			if (feverRestTime > 0) {
				feverRestTime += (5 * 40);
				addFeverDrawTime(500);
			}
			feverTaneNum += 2;
			if (feverTaneNum > 15) {
				feverTaneNum = 15;
			}
		}
		else {
			if (feverStockTime < 30) {
				int tm = 30 - feverStockTime;
				if (tm > 5) {
					tm = 5;
				}
				addFeverDrawTime(tm * 100);
			}
			feverStockTime += 5;
			if (feverStockTime > 30) {
				feverStockTime = 30;
			}
			// フィーバーへ入る時なら・・・
			if (feverPoint >= 7) {
				feverTaneNum += 2;
				if (feverTaneNum > 15) {
					feverTaneNum = 15;
				}
			}
		}
	}

	/**
	 * たまっているフィーバーポイント
	 * @return
	 */
	public int getFeverPoint() {
		return feverPoint;
	}

	/**
	 * フィーバーモードを切り替える
	 * @param flg
	 */
	public void setFeverMode(boolean flg) {
		if (flg) {
			feverRestTime = feverStockTime * 40;
		}
		else {
			feverStockTime = 15;
			feverPoint = 0;
		}
		feverMode = flg;
	}
	/**
	 * フィーバーモード
	 * @return
	 */
	public boolean isFeverMode() {
		return feverMode;
	}

	/**
	 * フィーバー時間
	 * @return
	 */
	public int getFeverRestTime() {
		if (!feverMode) {
			return feverStockTime * 40;
		}
		return feverRestTime;
	}

	/**
	 * ターンを進める
	 *
	 */
	public void turnNext() {
		if (feverMode) {
			if (feverRestTime > 0) {
				feverRestTime--;
				if (feverRestTime % 40 == 0 && !gameOverFlag) {
					int tm = feverRestTime / 40;
					if (tm == 0) {
						PlaySE.playSE(PlaySE.FEVER_END);
					}
					else if (tm <= 5) {
						PlaySE.playSE(PlaySE.FEVER_COUNT);
					}
				}
			}
		}
		for (int i = 0; i < feverEffect.size(); i++) {
			//
			FeverPoint pt = feverEffect.get(i);
			if (!pt.turnNext()) {
				// 終了
				feverEffect.remove(i);
				i--;
				continue;
			}
		}
		for (int i = 0; i < timeEffect.size(); i++) {
			//
			FeverPoint pt = timeEffect.get(i);
			if (!pt.turnNext()) {
				// 終了
				timeEffect.remove(i);
				i--;
				continue;
			}
		}
		if (addTimeDrawNum > 0) {
			addTimeDrawNum -= 10000;
		}
		modeNum++;
	}

	/**
	 * フィーバー中の連鎖が終了した
	 * @param num
	 * @return 連鎖予定からの差 1:連鎖通り,0:1つ少ない,-1:少なすぎ
	 */
	public int fixFeverRensa(int num) {
		int ret = num - feverTaneNum + 1;
		if (num < feverTaneNum - 2) {
			feverTaneNum -= 2;
			if (feverTaneNum < 3) {
				feverTaneNum = 3;
			}
		}
		else {
			feverTaneNum = num + 1;
			if (feverTaneNum > 15) {
				feverTaneNum = 15;
			}
		}
		if (feverRestTime > 0) {
			// 時間を足すかも
			if (num > 2) {
				feverRestTime += ((num - 2) * 20);
				addFeverDrawTime((num - 2) * 50);
			}
		}
		return ret;
	}

	/**
	 * フィーバーの種を落とす
	 * @param puyo
	 */
	public void dropFeverData(PuyoData puyo) {
		feverData.dropFeverData(puyo, feverTaneNum);
	}

	/**
	 * 連鎖数を指定してフィーバーの種を落とす
	 * @param puyo
	 * @param num
	 */
	public void dropFeverData(PuyoData puyo, int num) {
		feverData.dropFeverData(puyo, num);
	}

	public void drawBack(Graphics2D g) {
		if (!feverMode) {
			int fevnum = feverPoint - feverEffect.size();
			for (int i = 0; i < 7; i++) {
				if (i < fevnum) {
					// 点灯している
					g.setColor(new Color(255, 224 - i * 32, 0));
				}
				else {
					g.setColor(Color.WHITE);
				}
				Rectangle rc = feverRect[i];
				g.fillRect(rc.x, rc.y, rc.width, rc.height);
			}
		}
	}
	public void drawFore(Graphics2D g) {
		if (!feverMode && feverPoint >= 6) {
			// 光らせる
			int fevnum = feverPoint - feverEffect.size();
			int num = (modeNum / 5) & 7;
			if (num < fevnum) {
				Rectangle rc = feverRect[num];
				Stroke bak = g.getStroke();
				g.setColor(new Color(255, 196, 128));
				g.fillOval(rc.x, rc.y, rc.width, rc.height);
				g.setColor(new Color(255, 224, 192));
				g.setStroke(OjamaBar.beamStroke[1]);
				g.drawOval(rc.x, rc.y, rc.width, rc.height);
				g.setStroke(bak);
			}
		}
		int tm = feverStockTime * 100;
		if (feverMode) {
			tm = feverRestTime * 25 / 10;
		}
		else {
			tm -= (timeEffect.size() * 100);
		}
		DigitImage.drawFeverTime(g, timeRect.x, timeRect.y, tm);
		if (feverMode) {
			// フィーバー用の描画
			double rad = Math.PI * modeNum / 32;
			for (Rectangle rc: feverRect) {
				if (optFeverStarFlag) {
					AffineTransform tr = AffineTransform.getTranslateInstance(rc.x + rc.width / 2, rc.y + rc.height / 2);
					tr.rotate(rad);
					double bai = rc.width / 50.0;
					tr.scale(bai, bai);
					tr.translate(-100, -100);
					g.drawImage(feverStarImg, tr, null);
				}
				else {
					g.setColor(new Color(255 - (modeNum & 8)*4, 255 - (modeNum & 8)*2, 255));
					g.fillOval(rc.x, rc.y, rc.width, rc.height);
				}
			}
		}
		if (optFeverEffectFlag) {
			for (FeverPoint dt: feverEffect) {
				dt.draw(g);
			}
			for (FeverPoint dt: timeEffect) {
				dt.draw(g);
			}
		}
		if (addTimeDrawNum > 0) {
			// 時間のプラスも追描画加する
			int x = timeRect.x;
			int y = 308 + (addTimeDrawNum/10000);
			if (timeRect.x < 320) {
				x = timeRect.x + timeRect.width - 90;
			}
			DigitImage.drawAddTime(g, x, y, addTimeDrawNum % 10000);
		}
	}

	/**
	 * フィーバー描画用の時間に足す
	 *
	 */
	private void addFeverDrawTime(int num) {
		if (addTimeDrawNum > 50000) {
			addTimeDrawNum = 200000 + (addTimeDrawNum % 10000) + num;
		}
		else {
			addTimeDrawNum = 200000 + num;
		}
	}

	/**
	 * ゲームオーバーになったことを伝える
	 *
	 */
	public void setGameOver() {
		gameOverFlag = true;
	}

	/**
	 * ランダムデータを返す
	 * @param num
	 * @return
	 */
	public int getRandomInt(int num) {
		return feverData.getRandomInt(num);
	}
}
