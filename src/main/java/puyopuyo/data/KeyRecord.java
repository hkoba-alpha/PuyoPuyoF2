package puyopuyo.data;

import puyopuyo.play.MovePuyo;

/**
 * キー情報を覚えておくクラス
0-OFF
1-ON
xxxx
 KEY(0-右,1-下,2-左,3-右回り,4-左回り,5:同時押しなので次も連続,6:2〜17ターン以内待ち,7:それ以上待ち)

6x:x+1ターン待ち
7xx:(xx+17)ターン待ち(最大：272ターン)
 * @author kobayah
 *
 */
public class KeyRecord {
	/**
	 * 継続4ビット
	 */
	public static final int CODE_CONT_SHORT = 32;
	/**
	 * 継続8ビット
	 */
	public static final int CODE_CONT_LONG = 33;
	/**
	 * キーコード
	 */
	public static int[] keyCode = {
		MovePuyo.KEY_RIGHT,
		MovePuyo.KEY_DOWN,
		MovePuyo.KEY_LEFT,
		MovePuyo.KEY_TURNR,
		MovePuyo.KEY_TURNL
	};

	/**
	 * キー情報を保存しているバッファサイズ
	 */
	private int bufferSize;

	/*
	 * 同じキー状態が続いているターン数
	 */
	private int contTurn;

	/**
	 * 現在のキー状態
	 */
	private int keyFlag;

	/**
	 * キーのバッファデータ
	 */
	private byte[] bufferData;

	public KeyRecord() {
		bufferData = new byte[256];
	}

	/**
	 * キー情報を保存する
	 * @param key
	 */
	public void recordKey(int key) {
		key &= 0x37;
		if (keyFlag != key) {
			// キー情報が変わった
			recordContinue(contTurn);
			contTurn = 0;
			int flg = 0;
			for (int i = 0; i < keyCode.length; i++) {
				if ((key & keyCode[i]) > 0) {
					flg |= (1<<i);
				}
			}
			addInfo(flg);
			keyFlag = key;
		}
		else {
			contTurn++;
		}
	}

	/**
	 * 継続キー情報を保存する
	 * @param turn
	 */
	private void recordContinue(int turn) {
		while (turn > 0) {
			turn--;
			if (turn < 36) {
				addInfo(CODE_CONT_SHORT);
				addInfo(turn);
				turn = 0;
			}
			else {
				// 最後の大きい継続
				addInfo(CODE_CONT_LONG);
				int num = turn - 36;
				if (num > 36*36-1) {
					num = 36*36-1;
				}
				addInfo(num / 36);
				addInfo(num % 36);
				turn -= (num+36);
			}
		}
	}

	/**
	 * キーデータをバッファへ追加する
	 * @param dt
	 */
	private void addInfo(int dt) {
		if (bufferSize >= bufferData.length) {
			// 拡張する
			byte[] newbuf = new byte[bufferSize + 256];
			System.arraycopy(bufferData, 0, newbuf, 0, bufferSize);
			bufferData = newbuf;
		}
		bufferData[bufferSize++] = (byte)dt;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < bufferSize; i++) {
			buf.append(Integer.toString(bufferData[i], 36));
		}
		return buf.toString();
	}
}
