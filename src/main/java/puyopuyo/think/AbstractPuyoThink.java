package puyopuyo.think;

import java.awt.Point;

import puyopuyo.play.MovePuyo;
import puyopuyo.play.PuyoData;
import puyopuyo.think.data.BlockData;
import puyopuyo.think.data.EnvData;
import puyopuyo.think.data.MoveData;

public abstract class AbstractPuyoThink {
	/**
	 * 負の数だと左、正の数だと右
	 */
	private int moveCount;

	/**
	 * 落とす数
	 */
	private int dropCount;

	/**
	 * 落ちるのをスキップするカウント
	 */
	private int skipCount;

	/**
	 * 負の数だと左ターン、正の数だと右ターン
	 */
	private int turnCount;

	/**
	 * 移動ぷよの最後のY座標
	 */
	private int lastY;

	/**
	 * 移動先予定のX座標
	 */
	private int planX;

	/**
	 * 移動していない時のイベント
	 * @param puyo
	 * @param meBlk
	 * @param meEnv
	 * @param eneBlk
	 * @param enePuyo
	 * @param eneEnv
	 */
	public void waitEvent(PuyoData puyo, BlockData meBlk, EnvData meEnv, BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {
		moveCount = 0;
		skipCount = 0;
		turnCount = 0;
		dropCount = 0;
		if (puyo.getKeyFlag() > 0) {
			puyo.releaseKey(puyo.getKeyFlag());
			//System.out.println("WaitEvent#####");
		}
		lastY = 1;
		planX = 2;
	}

	/**
	 * 移動中のイベントを処理する
	 * @param puyo
	 * @param meBlk
	 * @param mePuyo
	 * @param meEnv
	 * @param eneBlk
	 * @param enePuyo
	 * @param eneEnv
	 */
	public void nextMoveEvent(PuyoData puyo, BlockData meBlk, MoveData mePuyo, EnvData meEnv, BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {
		int key = puyo.getKeyFlag();
		int cury = mePuyo.getY();
		boolean mvflg = skipCount > 0;
		boolean turnflg = false;

		// 現在の座標
		// 回転で横座標が変わった時の対応
		Point[] nowpt = mePuyo.getPoint();
		if (nowpt[0].x != planX) {
			/*
			System.out.println("回転で勝手に移動した："+nowpt[0].x+" ... "+planX);
			System.out.println(mePuyo);
			System.out.println(meBlk);
			*/
			if (turnCount != 0 || mePuyo.clone().setX(planX, meBlk)) {
				// 回転中か移動先へ行ける場合に調整する
				moveCount += (planX - nowpt[0].x);
			}
		}
		planX = nowpt[0].x;

		if (cury != lastY) {
			lastY = cury;
			if (skipCount > 0) {
				skipCount--;
			}
		}
		if (turnCount < 0) {
			puyo.releaseKey(MovePuyo.KEY_TURNR);
			// 左ターン
			if ((key & MovePuyo.KEY_TURNL) > 0) {
				puyo.releaseKey(MovePuyo.KEY_TURNL);
			}
			else if (!puyo.isTurning()) {
				//System.out.println("左回転！！！");
				puyo.pushKey(MovePuyo.KEY_TURNL);
				turnCount++;
			}
			mvflg = true;
			turnflg = true;
		}
		else if (turnCount > 0) {
			puyo.releaseKey(MovePuyo.KEY_TURNL);
			// 右ターン
			if ((key & MovePuyo.KEY_TURNR) > 0) {
				puyo.releaseKey(MovePuyo.KEY_TURNR);
			}
			else if (!puyo.isTurning()) {
				//System.out.println("右回転＊＊＊＊");
				puyo.pushKey(MovePuyo.KEY_TURNR);
				turnCount--;
			}
			mvflg = true;
			turnflg = true;
		}
		else {
			// ターンなし
			puyo.releaseKey(MovePuyo.KEY_TURNL|MovePuyo.KEY_TURNR);
		}
		puyo.releaseKey(MovePuyo.KEY_DOWN);
		if (moveCount < 0) {
			// 左移動
			puyo.releaseKey(MovePuyo.KEY_RIGHT);
			if ((key & MovePuyo.KEY_LEFT) > 0) {
				puyo.releaseKey(MovePuyo.KEY_LEFT);
			}
			else {
				puyo.pushKey(MovePuyo.KEY_LEFT);
				moveCount++;
				planX--;
				//System.out.println("左移動_to"+planX);
			}
			mvflg = true;
		}
		else if (moveCount > 0) {
			// 右移動
			puyo.releaseKey(MovePuyo.KEY_LEFT);
			if ((key & MovePuyo.KEY_RIGHT) > 0) {
				puyo.releaseKey(MovePuyo.KEY_RIGHT);
			}
			else {
				puyo.pushKey(MovePuyo.KEY_RIGHT);
				moveCount--;
				planX++;
				//System.out.println("右移動_to:"+planX);
			}
			mvflg = true;
		}
		else {
			puyo.releaseKey(MovePuyo.KEY_LEFT|MovePuyo.KEY_RIGHT);
			// 下移動
			if (dropCount > 0 && !turnflg) {
				if (skipCount > 0) {
					puyo.pushKey(MovePuyo.KEY_DOWN);
				}
				else {
					dropCount = 0;
				}
				mvflg = true;
			}
		}
		if (!mvflg) {
			// 移動が何もなかった
			try {
				think(meBlk, mePuyo, meEnv, eneBlk, enePuyo, eneEnv);
			} catch (Exception e) {
				e.printStackTrace();
				this.skipDrop(14);
			}
		}
	}
	/**
	 * 思考ルーチン
	 * @param meBlk  自分のブロック
	 * @param mePuyo 自分の移動中ぷよ
	 * @param meEnv　 自分のその他ぷよ情報
	 * @param eneBlk 敵のブロック
	 * @param enePuyo 敵の移動中ぷよ。移動していなければ null
	 * @param eneEnv  敵のその他ぷよ情報
	 */
	protected abstract void think(BlockData meBlk, MoveData mePuyo, EnvData meEnv, BlockData eneBlk, MoveData enePuyo, EnvData eneEnv);

	/**
	 * 思考ルーチンの名前を返す
	 * @return 名前
	 */
	protected abstract String getThinkName();

	/**
	 * 左に指定した数だけ移動する
	 * @param num 移動量
	 */
	protected void moveLeft(int num) {
		moveCount -= num;
	}

	/**
	 * 右に指定した数だけ移動する
	 * @param num 移動量
	 */
	protected void moveRight(int num) {
		moveCount += num;
	}
	/**
	 * 左に指定した数だけ回転する
	 * @param num 回転数
	 */
	protected void turnLeft(int num) {
		turnCount -= num;
	}
	/**
	 * 右に指定した数だけ回転する
	 * @param num 回転数
	 */
	protected void turnRight(int num) {
		turnCount += num;
	}
	/**
	 * 下に指定した数だけ早く落とす
	 * @param num 14以上だとくっつくまで落とす
	 */
	protected void moveDrop(int num) {
		dropCount += num;
		skipCount = dropCount;
	}
	/**
	 * 指定した数だけ自動的に落ちるのを待つ
	 * @param num 待つ数
	 */
	protected void skipDrop(int num) {
		skipCount += num;
	}

	public String toString() {
		return getThinkName();
	}
}
