package puyopuyo.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

import puyopuyo.play.BlockPuyo;

/**
 * いろいろな効果イメージを取得する
 * @author hkoba
 *
 */
public class EffectImage {
	/**
	 * 背景が透明なイメージを取得する
	 * @param wd
	 * @param ht
	 * @return
	 */
	private static BufferedImage makeImage(int wd, int ht) {
		BufferedImage img = new BufferedImage(wd, ht, BufferedImage.TYPE_INT_ARGB);
		int cl = new Color(0, 0, 0, 0).getRGB();
		for (int y = 0; y < ht; y++) {
			for (int x = 0; x < wd; x++) {
				img.setRGB(x, y, cl);
			}
		}
		return img;
	}

	private static BufferedImage ojamaStar;

	/**
	 * おじゃまを送った時のイメージ
	 * @return
	 */
	public static BufferedImage getOjamaStar() {
		if (ojamaStar == null) {
			ojamaStar = makeImage(100, 100);
			Graphics2D g = (Graphics2D)ojamaStar.getGraphics();
			Polygon flushPoly = new Polygon();
			for (int i = 0; i < 16; i++) {
				double rad = Math.PI * i / 8;
				int sz = 24;
				if (i % 2 == 0) {
					sz += (int)(Math.random() * 8);
				}
				else {
					sz += (int)(Math.random() * 16 + 32);
				}
				flushPoly.addPoint((int)(Math.cos(rad) * sz), (int)(Math.sin(rad) * sz));
			}
			AffineTransform afbak = g.getTransform();
			g.translate(50, 50);
			g.setColor(new Color(192, 192, 255, 128));
			g.fillPolygon(flushPoly);
			g.scale(0.7f, 0.7f);
			g.setColor(Color.WHITE);
			g.fillPolygon(flushPoly);
			g.setTransform(afbak);
			//g.setColor(new Color(0, 0, 255, 32));
			//g.fillOval(10, 10, 80, 80);
			/*
			g.setColor(new Color(128, 128, 255, 64));
			g.fillOval(25, 25, 50, 50);
			for (int i = 0; i < 20; i++) {
				double sz = Math.random()*30+20;
				int x = (int)(Math.cos(Math.PI*i/10)*sz+50);
				int y = (int)(Math.sin(Math.PI*i/10)*sz+50);
				//g.setColor(new Color(128, 128, 255, 128));
				g.setColor(new Color(192, 192, 255));
				g.drawLine(50, 50, x, y);
			}
			g.setColor(new Color(192, 192, 255, 128));
			g.fillOval(35, 35, 30, 30);
			g.setColor(new Color(255, 255, 255));
			g.fillOval(40, 40, 20, 20);
			*/
		}
		return ojamaStar;
	}

	private static BufferedImage feverStarImg;

	public static BufferedImage getFeverStarImage() {
		if (feverStarImg == null) {
			feverStarImg = makeImage(200, 200);
			Graphics2D g = (Graphics2D)feverStarImg.getGraphics();
			Polygon backStar = new Polygon();
			Polygon foreStar = new Polygon();
			for (int i = 0; i < 8; i++) {
				double rad = Math.PI * i / 4;
				int wd1 = 8;
				int wd2 = 14;
				if (i % 2 == 0) {
					wd1 = 80;
					wd2 = 100;
				}
				backStar.addPoint((int)(Math.sin(rad) * wd2) + 100, (int)(-Math.cos(rad) * wd2) + 100);
				foreStar.addPoint((int)(Math.sin(rad) * wd1) + 100, (int)(-Math.cos(rad) * wd1) + 100);
			}
			g.setColor(new Color(128, 128, 255, 96));
			g.fillPolygon(backStar);
			g.fillOval(100-30, 100-30, 60, 60);
			//g.drawOval(50-18, 50-18, 36, 36);
			g.setColor(Color.WHITE);
			g.fillPolygon(foreStar);
			g.fillOval(100-25, 100-25, 50, 50);
		}
		return feverStarImg;
	}

	static Color[] puyoBgColor = {
		new Color(222, 0, 0, 64),
		new Color(0, 227, 0, 64),
		new Color(0, 65, 214, 64),
		new Color(222, 211, 0, 64),
		new Color(240, 0, 231, 64)
	};

	public static BufferedImage[] puyoFadeImg;

	/**
	 * ぷよが消えるときのエフェクト
	 * @param puyo
	 * @return
	 */
	public static BufferedImage getPuyoFadeImage(int puyo) {
		if (puyoFadeImg == null) {
			puyoFadeImg = new BufferedImage[5];
			/*
			Image img = BlockPuyo.getPuyoImage();
			int[] pixels = new int[32 * 32 * 5];
			PixelGrabber pg = new PixelGrabber(img, 288, 480, 32 * 5, 32, pixels, 0, 32 * 5);
			try {
				pg.grabPixels();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			*/
			for (int i = 0; i < 5; i++) {
				puyoFadeImg[i] = makeImage(32, 32);
				//Graphics2D g = (Graphics2D)puyoFadeImg[i].getGraphics();
				Graphics2D g = (Graphics2D)puyoFadeImg[i].createGraphics();
				Color cl = puyoBgColor[i];
				for (int j = 0; j < 4; j++) {
					g.setColor(new Color(cl.getRed(), cl.getGreen(), cl.getBlue(), j * 64 + 63));
					g.fillOval(j * 2, j * 2, 32 - j * 4, 32 - j * 4);
				}
				g.setColor(new Color(255, 255, 255, 128));
				g.fillOval(10, 10, 12, 12);
				g.fillOval(12, 12, 8, 8);
				/*
				for (int y = 0; y < 32; y++) {
					int ix = y * 32 * 5 + i * 32;
					for (int x = 0; x < 32; x++) {
						if (pixels[ix + x] != pixels[0]) {
							puyoFadeImg[i].setRGB(x, y, pixels[ix + x]);
						}
					}
				}
				*/
			}
		}
		return puyoFadeImg[puyo - 1];
	}
}
