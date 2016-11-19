package puyopuyo.image;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;

public class ImageData {
	/**
	 * イメージをロードする
	 * @param fname
	 * @return
	 */
	public static BufferedImage loadImage(String fname) {
		int transcl = new Color(0, 0, 0, 0).getRGB();
		MediaTracker tr = new MediaTracker(new Canvas());
		//Image img = Toolkit.getDefaultToolkit().getImage(fname);
		Image img = Toolkit.getDefaultToolkit().getImage(ImageData.class.getResource("/" + fname));
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
		BufferedImage retimg = new BufferedImage(wd, ht, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < ht; y++) {
			for (int x = 0; x < wd; x++) {
				int cl = buf[y*wd+x];
				if (cl == buf[0]) {
					cl = transcl;
				}
				retimg.setRGB(x, y, cl);
			}
		}
		return retimg;
	}

	public static final int READY_IMAGE = 0;
	public static final int GO_IMAGE = 1;
	public static final int FEVER_IMAGE = 2;
	public static final int WIN_IMAGE = 3;
	public static final int LOSE_IMAGE = 4;
	public static final int ZENKESI_IMAGE = 5;

	private static String[] fileList = {
		"ready.png",
		"go.png",
		"fever.png",
		"win.png",
		"lose.png",
		"zenkesi.png"
	};
	private static ArrayList<BufferedImage> imageList;

	/**
	 * 画像を取得する
	 * @param type
	 * @return
	 */
	public static BufferedImage getImage(int type) {
		if (imageList == null) {
			imageList = new ArrayList<BufferedImage>();
			for (int i = 0; i < fileList.length; i++) {
				imageList.add(loadImage("image/"+fileList[i]));
			}
		}
		return imageList.get(type);
	}
}
