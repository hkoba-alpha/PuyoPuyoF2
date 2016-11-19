package puyopuyo.think;

import puyopuyo.think.data.BlockData;
import puyopuyo.think.data.EnvData;
import puyopuyo.think.data.MoveData;

public class RandomThink extends AbstractPuyoThink {

	@Override
	protected void think(BlockData meBlk, MoveData mePuyo, EnvData meEnv,
			BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {
		// てきとう
		int rnd = (int)(Math.random() * 7);
		switch (rnd) {
		case 0:
			super.moveLeft(1);
			super.skipDrop(1);
			break;
		case 1:
			super.moveRight(1);
			super.skipDrop(1);
			break;
		case 2:
			super.turnLeft(1);
			super.skipDrop(1);
			break;
		case 3:
			super.turnRight(1);
			super.skipDrop(1);
			break;
		case 4:
			super.moveDrop(2);
			break;
		default:
			super.skipDrop(rnd - 3);
			break;
		}
	}

	@Override
	protected String getThinkName() {
		return "SKELETON.Tレベル";
	}

}
