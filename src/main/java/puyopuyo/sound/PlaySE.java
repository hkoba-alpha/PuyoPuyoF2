package puyopuyo.sound;

import puyopuyo.data.AbstractVoiceData;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlaySE {
	/**
	 * 連鎖ボイスの開始
	 */
	public static final int RENSA_START = 0;
	/**
	 * 連鎖ボイスの最後
	 */
	public static final int RENSA_END = 6;

	/**
	 * ぷよ移動
	 */
	public static final int PUYO_IDOU = 7;
	/**
	 * ぷよ回転
	 */
	public static final int PUYO_KAITEN = 8;
	/**
	 * ぷよ着地
	 */
	public static final int PUYO_CHAKUTI = 9;
	/**
	 * ぷよ揺れる
	 */
	public static final int PUYO_YURE = 10;

	/**
	 * おじゃまぷよ落ちてくる小
	 */
	public static final int OJAMA_DROP_S = 12;
	/**
	 * おじゃまぷよ落ちてくる大
	 */
	public static final int OJAMA_DROP_L = 13;

	/**
	 * 予告ぷよを送る小
	 */
	public static final int YOKOKU_S = 14;
	/**
	 * 予告ぷよを送る中
	 */
	public static final int YOKOKU_M = 15;
	/**
	 * 予告ぷよを送る大
	 */
	public static final int YOKOKU_L = 16;

	/**
	 * 相殺
	 */
	public static final int SOUSAI = 20;

	/**
	 * フィーバーポイントへ追加
	 */
	public static final int FEVER_ADD = 21;

	/**
	 * 全消し
	 */
	public static final int ZENKESI = 22;

	/**
	 * フィーバーモードへ突入
	 */
	public static final int FEVER_START = 23;

	/**
	 * ゲーム開始
	 */
	public static final int PLAY_START = 25;

	/**
	 * フィーバータイムが残り少ない
	 */
	public static final int FEVER_COUNT = 44;

	/**
	 * フィーバータイムが終わった
	 */
	public static final int FEVER_END = 45;

	/**
	 * 効果音のリスト
	 */
	private static HashMap<Integer, AudioClip[]> seList;

	/**
	 * 効果音の何番目を鳴らすか
	 */
	private static int[] seIndex;

	/**
	 * 初期化する
	 *
	 */
	public static void init() {
		seList = new HashMap<Integer, AudioClip[]>();
		try {
			URI uri = AbstractVoiceData.class.getResource("/se").toURI();
			Path myPath;
			if (uri.getScheme().equals("jar")) {
				FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
				myPath = fileSystem.getPath("/se");
			} else {
				myPath = Paths.get(uri);
			}
			Stream<Path> walk = Files.walk(myPath, 1);
			int max = 0;
			for (Path path: walk.collect(Collectors.toList())) {
				String fname = path.getFileName().toString().toLowerCase();
				if (fname.endsWith(".wav")) {
					int ix = Integer.parseInt(path.getFileName().toString().substring(0, 3));
					if (ix > max) {
						max = ix;
					}
					AudioClip clp1 = Applet.newAudioClip(path.toUri().toURL());
					AudioClip clp2 = Applet.newAudioClip(path.toUri().toURL());
					seList.put(ix, new AudioClip[]{clp1, clp2});
				}
			}
			seIndex = new int[max + 1];
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		/*
		File[] flst = new File("se").listFiles(
				new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".wav");
					}
				});
		if (flst != null) {
			int max = 0;
			for (File fl: flst) {
				try {
					int ix = Integer.parseInt(fl.getName().substring(0, 3));
					if (ix > max) {
						max = ix;
					}
					AudioClip clp1 = Applet.newAudioClip(fl.toURI().toURL());
					AudioClip clp2 = Applet.newAudioClip(fl.toURI().toURL());
					seList.put(ix, new AudioClip[]{clp1, clp2});
				} catch (NumberFormatException e) {
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			seIndex = new int[max + 1];
		}
		*/
	}

	/**
	 * 効果音を鳴らす
	 * @param ix
	 */
	public static void playSE(int ix) {
		AudioClip[] clp = seList.get(ix);
		if (clp != null) {
			clp[seIndex[ix]].play();
			seIndex[ix] ^= 1;
		}
	}

	/**
	 * 連鎖効果音を鳴らす
	 * @param num
	 */
	public static void playRensa(int num) {
		if (num > 0) {
			int ix = RENSA_START + num - 1;
			if (ix > RENSA_END) {
				ix = RENSA_END;
			}
			playSE(ix);
		}
	}
}
