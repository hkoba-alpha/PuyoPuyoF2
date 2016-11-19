package puyopuyo.play;

import java.awt.Rectangle;

public class PurplePuyo extends BlockPuyo {

	public PurplePuyo(int bx, int y, int sp) {
		super(PUYO_PURPLE, bx, y, sp);
	}

	@Override
	protected Rectangle getNormalIcon() {
		Rectangle rc = super.getNormalIcon();
		rc.y++;
		return rc;
	}

	static int[] aniXW = {
		1, 31,
		34, 35,
		73, 31,
		109, 39,
		160, 33,
		196, 39,
		245, 35
	};

	@Override
	protected Rectangle getAnimateIcon(int ani, Rectangle before) {
		if ((ani % 4) != 0 && (ani < 14*4)) {
			return before;
		}
		int ix = ani / 4;
		if (ix > 14) {
			return null;
		}
		if (ix == 14) {
			// 早く動かす
			ix = 6 - (ani % 4);
		}
		int x = ix * 32 + 289;
		int y = 451;
		int wd = 31;
		int ht = 28;
		if (ix >= 7) {
			// 特殊
			y += 31;
			ht = 30;
			x = aniXW[(ix - 7) * 2];
			wd = aniXW[(ix - 7) * 2 + 1];
		}
		return new Rectangle(x, y, wd, ht);
	}

}
