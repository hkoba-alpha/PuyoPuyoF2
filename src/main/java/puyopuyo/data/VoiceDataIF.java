package puyopuyo.data;

import java.applet.AudioClip;

public interface VoiceDataIF {
	/**
	 * おじゃまぷよダメージ弱
	 * 6〜17
	 */
	public static final int OJAMA_S = 14;
	/**
	 * おじゃまぷよダメージ強
	 * 18〜
	 */
	public static final int OJAMA_L = 15;
	/**
	 * フィーバー突入
	 */
	public static final int FEVER_START = 13;
	/**
	 * やったね
	 */
	public static final int WINNER_VOICE = 16;
	/**
	 * ばたんきゅ〜
	 */
	public static final int LOSE_VOICE = 17;
	/**
	 * フィーバー成功
	 */
	public static final int FEVER_SUCCESS = 18;
	/**
	 * フィーバー失敗
	 */
	public static final int FEVER_FAIL = 19;

	/**
	 * 連鎖ボイスの声を取得する
	 * @param rensa [0]=1連鎖目の数、[1]=2連鎖目の数・・・
	 * @return 連鎖数分の声のデータ
	 */
	public AudioClip[] getRensaVoice(int[] rensa);

	/**
	 * 声データを取得する
	 * @param type
	 * @return
	 */
	public AudioClip getVoice(int type);
}
