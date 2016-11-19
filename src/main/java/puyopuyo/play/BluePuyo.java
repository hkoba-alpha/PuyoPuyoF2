package puyopuyo.play;

import java.awt.Rectangle;

public class BluePuyo extends BlockPuyo {

	public BluePuyo(int bx, int y, int sp) {
		super(PUYO_BLUE, bx, y, sp);
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
		if (ix > 16) {
			return null;
		}
		int x = (256 + ix * 32) % 512 + 1;
		int y = ((ix + 8) / 16) * 32 + 417;
		return new Rectangle(x, y, 31, 30);
	}

}
