package puyopuyo.think.data;

/**
 * おじゃまぷよやフィーバーのデータ
 * @author hkoba
 *
 */
public class EnvData {
	/**
	 * おじゃまぷよのデータ
	 * [0]=落ちてくるおじゃまぷよ
	 * [1]=予告ぷよ
	 * [2]=フィーバーモードの時のみ、残っているおじゃまぷよ
	 */
	private int[] ojamaPuyo;

	/**
	 * フィーバーの残り時間:1/10秒単位
	 * -1:通常時
	 */
	private int feverRestTime;

	/**
	 * フィーバーでたまっているカウント
	 * 7個でフィーバー
	 */
	private int feverStockCount;

	/**
	 * 次のぷよ
	 */
	private MoveData[] nextPuyo;

	/**
	 * コンストラクタ
	 * @param next
	 * @param oja
	 * @param cnt
	 * @param tm
	 */
	public EnvData(MoveData[] next, int[] oja, int cnt, int tm) {
		nextPuyo = next;
		ojamaPuyo = oja;
		feverStockCount = cnt;
		feverRestTime = tm;
	}

	/**
	 * フィーバー中の残り時間を返す
	 * @return -1:通常時、1/10秒単位
	 */
	public int getFeverRestTime() {
		return feverRestTime;
	}

	/**
	 * フィーバーまでのたまっているカウント
	 * @return 7個でフィーバー
	 */
	public int getFeverStockCount() {
		return feverStockCount;
	}

	/**
	 * おじゃまぷよ情報を返す
	 * @return [0]=即落ちてくるおじゃまぷよ,[1]=予告ぷよ,[2]=フィーバー前のおじゃまぷよ
	 */
	public int[] getOjamaPuyo() {
		return ojamaPuyo;
	}

	/**
	 * 次に降ってくるぷよ情報
	 * @return 次のぷよ
	 */
	public MoveData[] getNextPuyo() {
		return nextPuyo;
	}

}
