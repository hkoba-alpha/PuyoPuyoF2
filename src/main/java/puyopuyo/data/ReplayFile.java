package puyopuyo.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import puyopuyo.play.PuyoData;
import puyopuyo.think.AbstractPuyoThink;

/**
 * リプレイ用のファイル
 * @author hkoba
 *
 */
public class ReplayFile {
	/**
	 * リプレイのキーデータ
	 */
	private ReplayData[] replayData = new ReplayData[2];
	/**
	 * 思考ルーチン名
	 */
	private String[] thinkName = new String[2];
	/**
	 * キャラクタ情報
	 */
	private CharaDataIF[] charaData = new CharaDataIF[2];
	/**
	 * ランダム情報
	 */
	private long randomSeed;
	/**
	 * 色数
	 */
	private int[] colorNum = new int[2];

	/**
	 * ファイルをロードする
	 * @param fname
	 * @return
	 */
	public static ReplayFile loadFile(String fname) {
		try {
			BufferedReader rd = new BufferedReader(new FileReader("replay/"+fname));
			ReplayFile ret = new ReplayFile();
			ArrayList<CharaDataIF> charaList = CharaData.getCharaList();
			ret.randomSeed = Long.parseLong(rd.readLine());
			for (int i = 0; i < 2; i++) {
				ret.colorNum[i] = Integer.parseInt(rd.readLine());
				ret.thinkName[i] = rd.readLine();
				String lnstr = rd.readLine();
				for (CharaDataIF ch: charaList) {
					if (ch.getCharaName().equals(lnstr)) {
						ret.charaData[i] = ch;
						break;
					}
				}
				lnstr = rd.readLine();
				byte[] keydt = new byte[lnstr.length()];
				for (int j = 0; j < keydt.length; j++) {
					keydt[j] = (byte)Integer.parseInt(lnstr.substring(j, j+1), 36);
				}
				ret.replayData[i] = new ReplayData(keydt);
			}
			rd.close();
			return ret;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * リプレイ情報を保存する
	 * @param fname
	 * @param puyo
	 * @param thk
	 * @param chara
	 * @param seed
	 */
	public static void saveFile(String fname, PuyoData[] puyo, AbstractPuyoThink[] thk, CharaDataIF[] chara, long seed) {
		try {
			File dir = new File("replay");
			dir.mkdirs();
			PrintWriter wr = new PrintWriter(new FileWriter(new File(dir, fname)));
			wr.println(seed);
			for (int i = 0; i < 2; i++) {
				wr.println(puyo[i].getColorNum());
				if (thk[i] == null) {
					wr.println("マニュアル");
				}
				else {
					wr.println(thk[i]);
				}
				if (chara[i] == null) {
					wr.println("null");
				}
				else {
					wr.println(chara[i].getCharaName());
				}
				wr.println(puyo[i].getKeyRecord());
			}
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public CharaDataIF[] getCharaData() {
		return charaData;
	}
	public ReplayData[] getReplayData() {
		replayData[0].initReplay();
		replayData[1].initReplay();
		return replayData;
	}
	public String[] getThinkName() {
		return thinkName;
	}
	public long getRandomSeed() {
		return randomSeed;
	}
	public int[] getColorNum() {
		return colorNum;
	}
}
