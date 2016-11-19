package puyopuyo.play;

import java.awt.Rectangle;

public class RedPuyo extends BlockPuyo {

	public RedPuyo(int bx, int y, int sp) {
		super(PUYO_RED, bx, y, sp);
	}

	@Override
	public Rectangle getAnimateIcon(int ani, Rectangle before) {
		if (ani % 4 != 0) {
			return before;
		}
		int ix = ani / 4;
		if (ix >= 13) {
			return null;
		}
		return new Rectangle(ix * 32 + 1, 354, 31, 30);
	}

}
