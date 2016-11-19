package puyopuyo.data;

public interface CharaDataIF {
	/**
	 * ツモデータを16文字で返す
	 *  2:2ぷよ
	 *  3:L字ぷよ
	 *  4:2色2組ぷよ
	 *  *:でかぷよ
	 * @return ツモデータ
	 */
	public String getTsumoData();

	/**
	 * 通常時連鎖倍率をカンマ区切りの数値文字列で返す
	 * 最初は0連鎖を示す "0" から始める
	 * @return
	 */
	public String getNormalRensa();

	/**
	 * フィーバー時連鎖倍率をカンマ区切りの数値文字列で返す
	 * 最初は0連鎖を示す "0" から始める
	 * @return
	 */
	public String getFeverRensa();

	/**
	 * キャラクタの名前を返す
	 * @return 名前
	 */
	public String getCharaName();

	/**
	 * ボイスデータを取得する
	 * @return
	 */
	public VoiceDataIF getVoiceData();
}
