package puyopuyo.play;

import java.awt.Rectangle;

public class GreenPuyo extends BlockPuyo {

	public GreenPuyo(int bx, int y, int sp) {
		super(PUYO_GREEN, bx, y, sp);
	}

	static final int[] aniX = {
		226, 277, 332, 378, 423, 472
	};

	@Override
	protected Rectangle getAnimateIcon(int ani, Rectangle before) {
		if ((ani % 4) != 0 && (ani < 40 || ani >= 48)) {
			return before;
		}
		int ix = ani / 4;
		if (ix >= 18) {
			return null;
		}
		if (ix == 10 || ix == 11) {
			// 急速に戻る
			ix = 48 - ani;
		}
		int x = (416 + ix * 32) % 512 + 1;
		int y = ((ix + 13) / 16) * 32 + 354;
		int wd = 31;
		int ht = 28;
		if (ix >= 12) {
			// 特殊
			x = aniX[ix - 12];
		}
		return new Rectangle(x, y, wd, ht);
	}

}
