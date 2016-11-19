package puyopuyo.play;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class OjamaPuyo extends BlockPuyo {

	public OjamaPuyo(int bx, int y, int sp) {
		super(PUYO_OJAMA, bx, y, sp);
	}

	@Override
	public void chainCheck(PuyoData puyo) {
		if (isFall()) {
			splitOjama(puyo);
		}
	}

	@Override
	public void setFade(PuyoData puyo) {
		splitOjama(puyo);
		super.setFade(puyo);
	}

	/**
	 * 結合していたものをはずす
	 * @param puyo
	 */
	public void splitOjama(PuyoData puyo) {
		if (chainFlag > 0) {
			redraw(puyo);
			int flg = chainFlag;
			chainFlag = 0;
			srcIcon = getNormalIcon();
			redraw(puyo);
			// 隣のぷよたちもはずす
			if ((flg & CHAIN_TOP) > 0) {
				// 上がある
				BlockPuyo blk = puyo.getBlock(blockX, blockY - 1);
				if (blk instanceof OjamaPuyo) {
					((OjamaPuyo)blk).splitOjama(puyo);
				}
				if ((flg & CHAIN_RIGHT) > 0) {
					// 右がある
					blk = puyo.getBlock(blockX + 1, blockY - 1);
					if (blk instanceof OjamaPuyo) {
						((OjamaPuyo)blk).splitOjama(puyo);
					}
					blk = puyo.getBlock(blockX + 1, blockY);
					if (blk instanceof OjamaPuyo) {
						((OjamaPuyo)blk).splitOjama(puyo);
					}
				}
				else {
					// 左がある
					blk = puyo.getBlock(blockX - 1, blockY - 1);
					if (blk instanceof OjamaPuyo) {
						((OjamaPuyo)blk).splitOjama(puyo);
					}
					blk = puyo.getBlock(blockX - 1, blockY);
					if (blk instanceof OjamaPuyo) {
						((OjamaPuyo)blk).splitOjama(puyo);
					}
				}
			}
			else {
				// 下がある
				BlockPuyo blk = puyo.getBlock(blockX, blockY + 1);
				if (blk instanceof OjamaPuyo) {
					((OjamaPuyo)blk).splitOjama(puyo);
				}
				if ((flg & CHAIN_RIGHT) > 0) {
					// 右がある
					blk = puyo.getBlock(blockX + 1, blockY + 1);
					if (blk instanceof OjamaPuyo) {
						((OjamaPuyo)blk).splitOjama(puyo);
					}
					blk = puyo.getBlock(blockX + 1, blockY);
					if (blk instanceof OjamaPuyo) {
						((OjamaPuyo)blk).splitOjama(puyo);
					}
				}
				else {
					// 左がある
					blk = puyo.getBlock(blockX - 1, blockY + 1);
					if (blk instanceof OjamaPuyo) {
						((OjamaPuyo)blk).splitOjama(puyo);
					}
					blk = puyo.getBlock(blockX - 1, blockY);
					if (blk instanceof OjamaPuyo) {
						((OjamaPuyo)blk).splitOjama(puyo);
					}
				}
			}
		}
	}

	/**
	 * すべてのおじゃまが落ちた後に結合をチェックする
	 * @param puyo
	 */
	public void checkOjamaChain(PuyoData puyo) {
		if (chainFlag > 0) {
			return;
		}
		BlockPuyo blk1 = puyo.getBlock(blockX, blockY - 1);
		if (!(blk1 instanceof OjamaPuyo) || blk1.chainFlag > 0) {
			return;
		}
		BlockPuyo blk2 = puyo.getBlock(blockX + 1, blockY - 1);
		if (!(blk2 instanceof OjamaPuyo) || blk2.chainFlag > 0) {
			return;
		}
		BlockPuyo blk3 = puyo.getBlock(blockX + 1, blockY);
		if (!(blk3 instanceof OjamaPuyo) || blk3.chainFlag > 0) {
			return;
		}
		// 合体できる！！！
		redraw(puyo);
		blk1.redraw(puyo);
		blk2.redraw(puyo);
		blk3.redraw(puyo);
		chainFlag = CHAIN_RIGHT | CHAIN_TOP;
		blk1.chainFlag = CHAIN_RIGHT | CHAIN_BOTTOM;
		blk2.chainFlag = CHAIN_LEFT | CHAIN_BOTTOM;
		blk3.chainFlag = CHAIN_LEFT | CHAIN_TOP;
		pushPuyo(puyo, 0);
		redraw(puyo);
		blk1.pushPuyo(puyo, 0);
		blk1.redraw(puyo);
		blk2.pushPuyo(puyo, 0);
		blk2.redraw(puyo);
		blk3.pushPuyo(puyo, 0);
		blk3.redraw(puyo);
	}

	@Override
	public int pushPuyo(PuyoData puyo, int level) {
		if (chainFlag > 0) {
			// つぶれない
			setNormalRect(puyo);
			BlockPuyo blk = puyo.getBlock(blockX, blockY + 1);
			if (blk != null) {
				blk.pushPuyo(puyo, 0);
			}
			srcIcon = getNormalIcon();
			return itemRect.y;
		}
		return super.pushPuyo(puyo, level);
	}

	@Override
	protected Rectangle getPushRect(int by, int ht) {
		if (chainFlag > 0) {
			return new Rectangle(blockX * ICON_WIDTH, (blockY - 2) * 28, ICON_WIDTH, 28);
		}
		return super.getPushRect(by, ht);
	}

	static Rectangle[] bigOjamaRect = {
		new Rectangle(201, 226, 31, 28),	// 左上
		new Rectangle(232, 226, 31, 28),	// 右上
		new Rectangle(201, 254, 31, 28),	// 左下
		new Rectangle(232, 254, 31, 28)		// 右下
	};

	@Override
	protected Rectangle getNormalIcon() {
		if (chainFlag > 0) {
			switch (chainFlag) {
			case CHAIN_BOTTOM | CHAIN_RIGHT:
				return bigOjamaRect[0];
			case CHAIN_BOTTOM | CHAIN_LEFT:
				return bigOjamaRect[1];
			case CHAIN_TOP | CHAIN_RIGHT:
				return bigOjamaRect[2];
			case CHAIN_TOP | CHAIN_LEFT:
				return bigOjamaRect[3];
			}
		}
		return super.getNormalIcon();
	}

	@Override
	protected void drawFadePuyo(Graphics2D g, int x, int y, int sz) {
		Composite bak = g.getComposite();
		x = blockX * 31;
		y = (blockY - 2) * 28 - 2;
		if (sz < 32) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, sz / 32.0f));
		}
		g.drawImage(puyoImage, x, y, x + 32, y + 32, 0, 288, 32, 320, null);
		g.setComposite(bak);
	}

	@Override
	public void sidePush(PuyoData puyo, int push) {
		if (chainFlag > 0 && puyo != null) {
			// でかいおじゃまぷよ
			BlockPuyo blk = this;
			switch (chainFlag) {
			case CHAIN_BOTTOM | CHAIN_RIGHT:	// 左上
				blk = puyo.getBlock(blockX, blockY + 1);
				break;
			case CHAIN_BOTTOM | CHAIN_LEFT:	// 右上
				blk = puyo.getBlock(blockX - 1, blockY + 1);
				break;
			case CHAIN_TOP | CHAIN_LEFT:	// 右下
				blk = puyo.getBlock(blockX - 1, blockY);
				break;
			}
			if (blk == this) {
				// ここで実際に押す
				super.sidePush(puyo, push);
				puyo.getBlock(blockX, blockY - 1).sidePush(null, push);
				puyo.getBlock(blockX + 1, blockY - 1).sidePush(null, push);
				puyo.getBlock(blockX + 1, blockY).sidePush(null, push);
			}
			else {
				blk.sidePush(puyo, push);
				return;
			}
		}
		super.sidePush(puyo, push);
	}
}
