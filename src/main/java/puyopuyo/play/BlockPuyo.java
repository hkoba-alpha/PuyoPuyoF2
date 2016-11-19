package puyopuyo.play;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class BlockPuyo {
	/**
	 * ぷよのアニメーションを行うかどうかのフラグ
	 */
	public static boolean optPuyoAnimateFlag = true;

	protected Rectangle itemRect;

	protected int blockX;

	protected int blockY;

	protected int puyoColor;

	protected int chainFlag;

	/**
	 * 0より大きければ光る
	 */
	protected int flushMode;

	/**
	 * 消えていくモード
	 * 0だと消えていない
	 * 1以上で消える
	 */
	protected int fadeMode;

	/**
	 * 転送元のアイコン
	 */
	protected Rectangle srcIcon;

	/**
	 * 横方向で押している強さ
	 */
	protected int sideAdd;

	/**
	 * 横方向で動いている
	 * 3で反転
	 * 6でひとつ少なく
	 */
	protected int sideModeNum;

	/**
	 * 落ちる速度。
	 * 0以上だと自由落下中
	 * -1以下だとくっつき中
	 * -1 = くっついた直後 
	 */
	protected int downSpeed;

	/**
	 * 下のぷよを押すレベル
	 * downSpeed が -1, -2, -3 に対応して以下の配列を使う
	 */
	static final int[] pushLevelData = {
		 8, 10,12,11,9, 7, 5, 3, 2, -1,-1,-1,0, 0, 0
	};
	static final int[] pushPuyoHeight = {
		24,20,18,21,24,26,28,30,34,37,38,37,34,32,28
	};
	static final int[] fadeModeSize = {
		44, 44, 44, 44, 44, 44, 44, 44, 44, 40, 36, 32, 28, 24, 20, 16
	};

	public static final int CHAIN_TOP = 2;
	public static final int CHAIN_RIGHT = 4;
	public static final int CHAIN_BOTTOM = 1;
	public static final int CHAIN_LEFT = 8;

	public static final int ICON_WIDTH = 31;

	public static final int PUYO_RED = 1;
	public static final int PUYO_GREEN = 2;
	public static final int PUYO_BLUE = 3;
	public static final int PUYO_YELLOW = 4;
	public static final int PUYO_PURPLE = 5;
	public static final int PUYO_OJAMA = 6;

	static Image puyoImage;
	static Image flushImage;

	static class MyImageFilter extends RGBImageFilter {
		@Override
		public int filterRGB(int x, int y, int rgb) {
			if ((rgb & 0x00f0f0f0) > 0) {
				return 0xffffffff;
			}
			return rgb;
		}
	}

	static {
		//puyoImage = Toolkit.getDefaultToolkit().getImage("image/allpuyo.png");
		puyoImage = Toolkit.getDefaultToolkit().getImage(BlockPuyo.class.getResource("/image/allpuyo.png"));
		MediaTracker tr = new MediaTracker(new Canvas());
		tr.addImage(puyoImage, 0);
		try {
			tr.waitForAll();
		} catch (InterruptedException e) {
		}
		// ぷよイメージを作り直す
		BufferedImage bufimg = new BufferedImage(puyoImage.getWidth(null), puyoImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bufimg.createGraphics();
		g2.drawImage(puyoImage, 0, 0, null);
		try {
			//bufimg = ImageIO.read(new File("image/allpuyo.png"));
			bufimg = ImageIO.read(BlockPuyo.class.getResourceAsStream("/image/allpuyo.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		puyoImage = bufimg;

		BufferedImage newimg = new BufferedImage(puyoImage.getWidth(null), puyoImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < bufimg.getHeight(); y++) {
			for (int x = 0; x < bufimg.getWidth(); x++) {
				int rgb = bufimg.getRGB(x, y);
				if ((rgb & 0x00f0f0f0) > 0) {
					rgb = 0xffffffff;
				}
				newimg.setRGB(x, y, rgb);
			}
		}
		flushImage = newimg;

		//FilteredImageSource filt = new FilteredImageSource(puyoImage.getSource(), new MyImageFilter());
		//flushImage = Toolkit.getDefaultToolkit().createImage(filt);
	}

	/**
	 * アニメーション用
	 */
	protected int aniModeNum;

	public static Image getPuyoImage() {
		return puyoImage;
	}

	public static BlockPuyo createPuyo(int cl, int bx, int y, int sp) {
		switch (cl) {
		case PUYO_RED:
			return new RedPuyo(bx, y, sp);
		case PUYO_GREEN:
			return new GreenPuyo(bx, y, sp);
		case PUYO_BLUE:
			return new BluePuyo(bx, y, sp);
		case PUYO_YELLOW:
			return new YellowPuyo(bx, y, sp);
		case PUYO_PURPLE:
			return new PurplePuyo(bx, y, sp);
		case PUYO_OJAMA:
			return new OjamaPuyo(bx, y, sp);
		}
		return new BlockPuyo(cl, bx, y, sp);
	}

	protected BlockPuyo(int cl, int bx, int y, int sp) {
		blockX = bx;
		blockY = 0;
		itemRect = new Rectangle(blockX * ICON_WIDTH, y, ICON_WIDTH, 32);
		downSpeed = sp;
		puyoColor = cl;
		aniModeReset();
		srcIcon = getNormalIcon();
	}

	public int getBlockY() {
		return blockY;
	}

	public void setBlockY(int blockY) {
		this.blockY = blockY;
	}

	public int getBlockX() {
		return blockX;
	}

	/**
	 * 落下速度を設定する
	 * @param val
	 */
	public void setDownSpeed(int val) {
		downSpeed = val;
	}

	public void setFade(PuyoData puyo) {
		redraw(puyo);
		fadeMode = 1;
		aniModeNum = -1;
		redraw(puyo);
	}
	public boolean isFade() {
		return fadeMode > 0;
	}

	/**
	 * ぷよの色。
	 * @return 1〜5,6はおじゃまぷよ
	 */
	public int getPuyoColor() {
		return puyoColor;
	}
	/**
	 * 描画する
	 * @param g
	 * @param drawRc
	 */
	public void draw(Graphics2D g, Rectangle drawRc, PuyoData puyo) {
		if (drawRc.intersects(itemRect)) {
			// 実際に描画する
			if (fadeMode > 0) {
				// 消えかけ
				if (fadeMode > fadeModeSize.length) {
					return;
				}
				int cx = itemRect.x + itemRect.width / 2;
				int cy = itemRect.y + itemRect.height / 2;
				int sz = fadeModeSize[fadeMode - 1];
				drawFadePuyo(g, cx - sz / 2, cy - sz / 2, sz);
			}
			else {
				drawNormalPuyo(g, puyo);
			}
		}
	}

	protected void drawFadePuyo(Graphics2D g, int x, int y, int sz) {
		int sx1 = puyoColor * 32 + 160;
		int sy1 = 288;
		if (puyoColor == PUYO_OJAMA) {
			// おじゃまぷよ
			sx1 = 0;
			sy1 = 288;
		}
		int sx2 = sx1 + 32;
		int sy2 = sy1 + 32;

		g.drawImage(puyoImage, x, y, x + sz, y + sz, sx1, sy1, sx2, sy2, null);
	}
	protected void drawNormalPuyo(Graphics2D g, PuyoData puyo) {
		int sx1 = srcIcon.x;
		int sx2 = sx1 + srcIcon.width;
		int sy1 = srcIcon.y;
		int sy2 = sy1 + srcIcon.height;

		int dx1 = itemRect.x;
		int dx2 = dx1 + itemRect.width;
		int dy1 = itemRect.y;
		int dy2 = dy1 + itemRect.height;

		if (itemRect.height == 28 && srcIcon.height > 28) {
			// 通常状態
			dy1 -= (srcIcon.height - 28)/2;
			dy2 = dy1 + srcIcon.height;
		}
		if (itemRect.width == 31 && srcIcon.width > 31) {
			dx1 -= (srcIcon.width - 31) / 2;
			dx2 = dx1 + srcIcon.width;
		}

		g.drawImage(puyoImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
		if (flushMode > 0) {
			// 光る
			int lvl = flushMode & 7;
			if (lvl >= 4) {
				lvl = 7 - lvl;
			}
			Composite bak = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, lvl / 6.0f + 0.2f));
			g.drawImage(flushImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
			g.setComposite(bak);
		}
		/*
		if ((chainFlag & CHAIN_LEFT) > 0) {
			BlockPuyo blk = puyo.getBlock(blockX - 1, blockY);
			Rectangle rc = blk.getItemRect();
			if (rc.y < dy1) {
				dy1 = rc.y;
			}
			if (rc.y + rc.height > dy2) {
				dy2 = rc.y + rc.height;
			}
			//g.drawImage(puyoImage, dx1, dy1, dx1 + 1, dy2, sx1 + 1, sy1, sx1 + 2, sy2, null);
		}
		*/
	}

	/**
	 * 自由落下中かどうかをチェック
	 * @return
	 */
	public boolean isFall() {
		return downSpeed >= 0;
	}

	/**
	 * ターンを進める
	 * @param puyo
	 */
	public void turnNext(PuyoData puyo) {
		if (downSpeed == 0) {
			// 落ち始める
			BlockPuyo blk = puyo.getBlock(blockX, blockY + 1);
			if (blk != null) {
				if (itemRect.y + 32 < blk.getItemRect().y) {
					downSpeed = 8;
				}
			}
			else {
				downSpeed = 8;
			}
		}
		else if (downSpeed > 0) {
			// 落ちる
			redraw(puyo);
			itemRect.y += downSpeed;
			int by = (blockY - 1) * 28;
			BlockPuyo blk = puyo.getBlock(blockX, blockY + 1);
			if (blk != null) {
				int newy = blk.getItemRect().y + 1;
				if (newy > by) {
					by = newy;
				}
			}
			if (itemRect.y + 28 > by) {
				// くっついた
				downSpeed = -1;
				setNormalRect(puyo);
			}
			else {
				downSpeed += 2;
				if (downSpeed > 28) {
					downSpeed = 28;
				}
			}
			redraw(puyo);
		}
		else if (downSpeed >= -pushLevelData.length) {
			redraw(puyo);
			BlockPuyo blk = puyo.getBlock(blockX, blockY + 1);
			int by = (blockY - 1) * 28;
			if (blk != null) {
				by = blk.pushPuyo(puyo, pushLevelData[-downSpeed - 1]);
			}
			int ht = pushPuyoHeight[-downSpeed - 1];
			itemRect = getPushRect(by, ht);
			downSpeed--;
			redraw(puyo);
		}
		if (fadeMode > 0) {
			redraw(puyo);
			fadeMode++;
			return;
		}
		else if (chainFlag == 0) {
			// アニメーションチェック
			aniModeNum++;
			if (aniModeNum >= 0) {
				Rectangle newrc = getAnimateIcon(aniModeNum, srcIcon);
				if (newrc == null) {
					// 終了
					if (aniModeNum == 0) {
						// 初めてでアニメーションなし
						// 再描画は不要
						aniModeNum = -10000;
					}
					else {
						redraw(puyo);
						aniModeReset();
						srcIcon = getNormalIcon();
						redraw(puyo);
					}
				}
				else if (newrc != srcIcon) {
					// 変化があった
					redraw(puyo);
					srcIcon = newrc;
					redraw(puyo);
				}
			}
		}
		if (flushMode > 0) {
			flushMode++;
			redraw(puyo);
		}
		if (sideAdd != 0) {
			// 移動する
			redraw(puyo);
			itemRect.x += sideAdd;
			redraw(puyo);
			sideModeNum++;
			if (sideModeNum == 3) {
				sideAdd = -sideAdd;
			}
			else if (sideModeNum == 6) {
				sideModeNum = 0;
				if (sideAdd < 0) {
					sideAdd++;
				}
				else {
					sideAdd--;
				}
			}
		}
	}

	/**
	 * 対象を光るモードにする
	 * @param puyo
	 * @param flg
	 */
	public void setFlush(PuyoData puyo, boolean flg) {
		if (flg) {
			if (flushMode == 0) {
				flushMode = 1;
			}
		}
		else if (flushMode > 0) {
			flushMode = 0;
			redraw(puyo);
		}
	}

	/**
	 * 隣との結合をチェックする
	 * @param puyo
	 */
	public void chainCheck(PuyoData puyo) {
		int flg = 0;
		BlockPuyo blk = puyo.getBlock(blockX, blockY - 1);
		if (blk != null && blk.getPuyoColor() == puyoColor) {
			// チェック
			Rectangle rc = blk.getItemRect();
			if (rc.y + rc.height >= itemRect.y) {
				flg |= CHAIN_TOP;
			}
		}
		blk = puyo.getBlock(blockX, blockY + 1);
		if (blk != null && blk.getPuyoColor() == puyoColor) {
			// チェック
			Rectangle rc = blk.getItemRect();
			if (itemRect.y + itemRect.height >= rc.y) {
				flg |= CHAIN_BOTTOM;
			}
		}
		blk = puyo.getBlock(blockX + 1, blockY);
		if (blk != null && blk.getPuyoColor() == puyoColor) {
			// チェック
			if (Math.abs(itemRect.y - blk.getItemRect().y) < 20) {
				flg |= CHAIN_RIGHT;
			}
		}
		blk = puyo.getBlock(blockX - 1, blockY);
		if (blk != null && blk.getPuyoColor() == puyoColor) {
			// チェック
			if (Math.abs(itemRect.y - blk.getItemRect().y) < 20) {
				flg |= CHAIN_LEFT;
			}
		}
		if (flg != chainFlag) {
			// 新たにくっついた
			redraw(puyo);
			chainFlag = flg;
			aniModeReset();
			srcIcon = getNormalIcon();
			redraw(puyo);
		}
	}

	/**
	 * ノーマル表示するアイコンのイメージを返す
	 * @return
	 */
	protected Rectangle getNormalIcon() {
		if (puyoColor == PUYO_OJAMA) {
			return new Rectangle(1, 290, ICON_WIDTH, 28);
		}
		int sx = chainFlag * 32 + 1;
		int wd = ICON_WIDTH;
		int sy = (puyoColor - 1) * 32 + 2;
		int ht = 28;

		return new Rectangle(sx, sy, wd, ht);
	}

	/**
	 * 
	 * @return
	 */
	public Rectangle getItemRect() {
		return itemRect;
	}

	/**
	 * 標準の位置に設定する
	 *
	 */
	protected void setNormalRect(PuyoData puyo) {
		itemRect = new Rectangle(blockX * ICON_WIDTH, (blockY - 2) * 28, ICON_WIDTH, 28);
	}

	/**
	 * 再描画をエントリする
	 * @param puyo
	 */
	public void redraw(PuyoData puyo) {
		Rectangle rc = itemRect;
		if (fadeMode > 0) {
			if (fadeMode > fadeModeSize.length) {
				return;
			}
			int cx = rc.x + rc.width / 2;
			int cy = rc.y + rc.height / 2;
			int sz = fadeModeSize[fadeMode - 1];
			if (puyoColor == PUYO_OJAMA) {
				// 特別
				sz = 32;
			}
			rc = new Rectangle (cx - sz / 2, cy - sz / 2, sz, sz);
		}
		else {
			if (srcIcon.width > 31) {
				rc = new Rectangle(rc);
				rc.x -= (srcIcon.width - 31)/2;
				rc.width = srcIcon.width;
			}
			if (itemRect.height == 28 && srcIcon.height > 28) {
				rc = new Rectangle(rc);
				rc.y -= (srcIcon.height - 28)/2;
				rc.height = srcIcon.height;
			}
		}
		
		int sx = rc.x / ICON_WIDTH;
		int ex = (rc.x + rc.width + ICON_WIDTH - 1) / ICON_WIDTH;
		for (int x = sx; x < ex; x++) {
			puyo.redraw(x, rc.y, rc.height);
		}
	}

	/**
	 * ぷよを押して、そのY座標を返す
	 * @param level 0〜12 大きいほどつぶす
	 * @return
	 */
	public int pushPuyo(PuyoData puyo, int level) {
		int ay = level / 4;
		if (level == 0) {
			if (downSpeed != -100) {
				redraw(puyo);
			}
			BlockPuyo blk = puyo.getBlock(blockX, blockY + 1);
			if (blk != null) {
				blk.pushPuyo(puyo, 0);
			}
			setNormalRect(puyo);
			if (downSpeed != -100) {
				redraw(puyo);
				downSpeed = -100;
			}
		}
		else {
			redraw(puyo);
			downSpeed = -90;
			BlockPuyo blk = puyo.getBlock(blockX, blockY + 1);
			int by = (blockY - 1) * 28;
			if (blk != null) {
				int nxtlvl = level - 3;
				if (level < 0) {
					nxtlvl = level + 1;
				}
				else if (nxtlvl < 0) {
					nxtlvl = 0;
				}
				by = blk.pushPuyo(puyo, nxtlvl);
			}
			int ht = 28 - level * 2 + ay;
			int ax = (level / 3) * 2;
			itemRect = new Rectangle(blockX * ICON_WIDTH - ax, by - ht, ICON_WIDTH + ax * 2, ht);
		}
		redraw(puyo);
		return itemRect.y + ay;
	}
	/**
	 * 自分が下のぷよを押している時の大きさを得る
	 * @param by
	 * @param ht
	 * @return
	 */
	protected Rectangle getPushRect(int by, int ht) {
		int wd = ICON_WIDTH;
		int sx = blockX * ICON_WIDTH;
		int dx = (ht - 24) / 4;
		if (downSpeed == -pushPuyoHeight.length) {
			// 元に戻す
			dx = 0;
		}
		if (dx > 0) {
			if ((chainFlag & CHAIN_LEFT) == 0) {
				sx += dx;
				wd -= dx;
			}
			if ((chainFlag & CHAIN_RIGHT) == 0) {
				wd -= dx;
			}
		}
		return new Rectangle(sx, by - ht, wd, ht);
	}

	/**
	 * 結合フラグを得る
	 * @return
	 */
	public int getChainFlag() {
		return 0;
	}

	/**
	 * アニメーション用のアイコン座標を返す
	 * @param ani 
	 * @param before 直前の座標
	 * @return 変化がなければ before と同じインスタンス。アニメーション終了なら null を返す
	 */
	protected Rectangle getAnimateIcon(int ani, Rectangle before) {
		return null;
	}

	/**
	 * 単独ぷよのアニメーションの初期化
	 *
	 */
	protected void aniModeReset() {
		if (!optPuyoAnimateFlag) {
			aniModeNum = -100000;
			return;
		}
		if (chainFlag == 0) {
			aniModeNum = -(int)(Math.random() * 400 + 40*3);
		}
		else {
			aniModeNum = -100000;
		}
	}

	/**
	 * 横方向から押される
	 * @param puyo
	 * @param push 負の数:左へ押す,正の数:右へ押す
	 */
	public void sidePush(PuyoData puyo, int push) {
		if (sideAdd == 0) {
			sideAdd = push;
			sideModeNum = 0;
		}
	}
}
