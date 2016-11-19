package puyopuyo.play;

import java.awt.Rectangle;

public class YellowPuyo extends BlockPuyo {
	public YellowPuyo(int bx, int y, int sp) {
		super(PUYO_YELLOW, bx, y, sp);
	}

	@Override
	protected Rectangle getNormalIcon() {
		Rectangle rc = super.getNormalIcon();
		if ((chainFlag & CHAIN_TOP) == 0) {
			rc.y--;
			rc.height++;
		}
		if ((chainFlag & CHAIN_BOTTOM) == 0) {
			rc.height++;
		}
		return rc;
	}

	@Override
	protected Rectangle getAnimateIcon(int ani, Rectangle before) {
		if ((ani % 4) != 0) {
			return before;
		}
		int ix = ani / 4;
		if (ix >= 16) {
			return null;
		}
		if ((ix & 4) > 0) {
			ix ^= 7;
		}
		if (ix >= 4) {
			ix -= 4;
		}
		int x = ix * 32 + (ix < 8 ? 1: 2);
		return new Rectangle(x, 417, 31, 30);
	}

}
