package puyopuyo.image;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

public class DigitImage {
	private static BufferedImage digitImage;

	static {
		int transcl = new Color(0, 0, 0, 0).getRGB();
		MediaTracker tr = new MediaTracker(new Canvas());
		//Image img = Toolkit.getDefaultToolkit().getImage("image/digit.png");
		Image img = Toolkit.getDefaultToolkit().getImage(DigitImage.class.getResource("/image/digit.png"));
		tr.addImage(img, 0);
		try {
			tr.waitForAll();
		} catch (InterruptedException e) {
		}
		int wd = img.getWidth(null);
		int ht = img.getHeight(null);
		int[] buf = new int[wd*ht];
		PixelGrabber grab = new PixelGrabber(img, 0, 0, wd, ht, buf, 0, wd);
		try {
			grab.grabPixels();
		} catch (InterruptedException e) {
		}
		digitImage = new BufferedImage(wd, ht, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < ht; y++) {
			for (int x = 0; x < wd; x++) {
				int cl = buf[y*wd+x];
				if (cl == buf[0]) {
					cl = transcl;
				}
				digitImage.setRGB(x, y, cl);
			}
		}
	}

	/**
	 * 大きい
	 * @param g
	 * @param x
	 * @param y
	 * @param num
	 */
	public static void drawBig(Graphics2D g, int x, int y, int num) {
		g.drawImage(digitImage, x, y, x + 20, y + 44, (num/10)*20, 0, (num/10)*20+20, 44, null);
		g.drawImage(digitImage, x+20, y, x + 40, y + 44, (num%10)*20, 0, (num%10)*20+20, 44, null);
	}

	/**
	 * 小さい時間を表示
	 * @param g
	 * @param x
	 * @param y
	 * @param num 1/100秒単位の値
	 */
	public static void drawFeverTime(Graphics2D g, int x, int y, int num) {
		int xx = x;
		int bai = 1000;
		for (int i = 0; i < 4; i++) {
			int n = (num/bai)%10;
			g.drawImage(digitImage, xx, y, xx+11, y+21, n*16+16, 160, n*16+27, 181, null);
			xx += 11;
			if (i == 1) {
				// 小数点を書く
				g.drawImage(digitImage, xx, y, xx+4, y+21, 0, 160, 4, 181, null);
				xx += 4;
			}
			bai /= 10;
		}
	}

	/**
	 * フィーバーへの追加時間を描画する
	 * @param g
	 * @param x
	 * @param y
	 * @param num
	 */
	public static void drawAddTime(Graphics2D g, int x, int y, int num) {
		int xx = x;
		int bai = 1000;
		boolean flg = false;
		for (int i = 0; i < 4; i++) {
			int n = (num/bai)%10;
			bai /= 10;
			if (!flg) {
				if (n > 0) {
					flg = true;
					g.drawImage(digitImage, xx, y, xx+12, y+20, 173, 44, 185, 64, null);
					xx += 12;
				}
				else {
					continue;
				}
			}
			g.drawImage(digitImage, xx, y, xx+12, y+20, n*12, 44, n*12+12, 64, null);
			xx += 12;
			if (i == 1) {
				// 小数点を書く
				g.drawImage(digitImage, xx, y, xx+12, y+20, 120, 44, 132, 64, null);
				xx += 12;
			}
		}
		g.drawImage(digitImage, xx, y, xx+30, y+20, 134, 44, 164, 64, null);
	}

	/**
	 * 連鎖数を描画する
	 * @param g
	 * @param x
	 * @param y
	 * @param num
	 */
	public static void drawRensa(Graphics2D g, int x, int y, int num) {
		// 数字 20x44
		// 連鎖 (0,64) - size(110,32)
		int n10 = num / 10;
		int n1 = num % 10;
		if (n10 > 0) {
			g.drawImage(digitImage, x, y, x + 20, y + 44, n10*20, 0, n10*20+20, 44, null);
		}
		g.drawImage(digitImage, x + 20, y, x + 40, y + 44, n1*20, 0, n1*20+20, 44, null);
		g.drawImage(digitImage, x + 40, y+12, x + 150, y + 44, 0, 64, 110, 96, null);
	}

	/**
	 * スコア表示する
	 * @param g
	 * @param score
	 * @param pl
	 */
	public static void drawScore(Graphics2D g, int score, int pl) {
		int sy = 100;
		int xx = 256;
		int yy = 383;

		if (pl == 2) {
			sy = 130;
			xx = 261;
			yy = 415;
		}
		int scbai = 10000000;
		for (int i = 0; i < 8; i++) {
			int sc = (score / scbai) % 10;
			g.drawImage(digitImage, xx, yy, xx + 12, yy + 28,
					sc * 12, sy, sc * 12 + 12, sy + 28, null);
			xx += 16;
			scbai /= 10;
		}
	}

	/**
	 * 勝ち負けを描画する
	 * @param g
	 * @param x
	 * @param y
	 * @param num
	 */
	public static void drawWinNum(Graphics2D g, int x, int y, int num) {
		g.drawImage(digitImage, x, y, x + 25, y + 25, 150, 100, 175, 125, null);
		int bai = 10;
		for (int i = 0; i < 2; i++) {
			int sx = ((num / bai) % 10) * 10;
			int dx = x + i * 10 + 3;
			int dy = y + 6;
			bai = 1;
			g.drawImage(digitImage, dx, dy, dx + 9, dy + 12, sx, 185, sx + 9, 197, null);
		}
	}
}
