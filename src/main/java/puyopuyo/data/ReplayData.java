package puyopuyo.data;

/**
 * リプレイ用のデータ
 * @author kobayah
 *
 */
public class ReplayData {
	/**
	 * リプレイ用のデータ
	 */
	private byte[] bufferData;

	/**
	 * リプレイの次のインデックス
	 */
	private int replayIndex;

	/**
	 * 次のレコードを読み取るまでのスキップカウント
	 */
	private int skipCount;

	/**
	 * 現在のキー状態
	 */
	private int keyFlag;

	public ReplayData(byte[] dt) {
		replayIndex = 0;
		skipCount = 0;
		bufferData = dt;
	}

	/**
	 * 次のキー状態を得る
	 * @return
	 */
	public int nextKey() {
		if (skipCount == 0) {
			// 次のキーを取る
			if (replayIndex < bufferData.length) {
				int ch = bufferData[replayIndex++];
				if (ch == KeyRecord.CODE_CONT_SHORT) {
					// 待ち状態4ビット
					skipCount = bufferData[replayIndex++];
				}
				else if (ch == KeyRecord.CODE_CONT_LONG) {
					// 待ち状態8ビット
					skipCount = (bufferData[replayIndex]*36)+bufferData[replayIndex+1]+36;
					replayIndex += 2;
				}
				else {
					// キーに変化があった
					keyFlag = 0;
					for (int i = 0; i < 5; i++) {
						if ((ch & (1<<i)) > 0) {
							keyFlag |= KeyRecord.keyCode[i];
						}
					}
				}
			}
		}
		else {
			skipCount--;
		}
		return keyFlag;
	}

	/**
	 * リプレイを最初から
	 *
	 */
	public void initReplay() {
		replayIndex = 0;
		keyFlag = 0;
		skipCount = 0;
	}
}
