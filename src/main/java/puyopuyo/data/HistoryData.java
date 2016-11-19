package puyopuyo.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import puyopuyo.think.AbstractPuyoThink;

public class HistoryData {

	private HashMap<String, int[]> scoreData;

	private static HistoryData historyData = new HistoryData(); 

	private HistoryData() {
		scoreData = new HashMap<String, int[]>();
		loadScore();
	}

	public static HistoryData getHistoryData() {
		return historyData;
	}

	private static String getKeyName(AbstractPuyoThink[] thk, CharaDataIF[] chara, int[] clnum, boolean[] revflg) {
		String nm1 = clnum[0] + ",";
		if (thk[0] == null) {
			nm1 += "null";
		}
		else {
			nm1 += thk[0].getClass().getName();
		}
		if (chara[0] == null) {
			nm1 += ",null";
		}
		else {
			nm1 += ("," + chara[0].getCharaName());
		}
		String nm2 = clnum[1] + ",";
		if (thk[1] == null) {
			nm2 += "null";
		}
		else {
			nm2 += thk[1].getClass().getName();
		}
		if (chara[1] == null) {
			nm2 += ",null";
		}
		else {
			nm2 += ("," + chara[1].getCharaName());
		}
		if (nm1.compareTo(nm2) > 0) {
			revflg[0] = true;
			return nm2 + "-" + nm1;
		}
		revflg[0] = false;
		return nm1 + "-" + nm2;
	}

	/**
	 * 今までのスコアを取得する
	 * @param thk
	 * @param chara
	 * @param clnum
	 * @return
	 */
	public int[] getTotalScore(AbstractPuyoThink[] thk, CharaDataIF[] chara, int[] clnum) {
		boolean[] revflg = new boolean[1];
		int[] ret = scoreData.get(getKeyName(thk, chara, clnum, revflg));
		if (ret == null) {
			return new int[3];
		}
		if (revflg[0]) {
			return new int[]{ret[1], ret[0], ret[2]};
		}
		return ret;
	}

	/**
	 * スコアを設定する
	 * @param thk
	 * @param chara
	 * @param clnum
	 * @param win 1:１プレイヤーの勝ち,2:２プレイヤーの勝ち,それ以外:引き分け
	 */
	public void setScore(AbstractPuyoThink[] thk, CharaDataIF[] chara, int[] clnum, int win) {
		boolean[] revflg = new boolean[1];
		String keystr = getKeyName(thk, chara, clnum, revflg);
		int[] ret = scoreData.get(keystr);
		if (ret == null) {
			ret = new int[3];
			scoreData.put(keystr, ret);
		}
		if (win == 1) {
			if (revflg[0]) {
				ret[1]++;
			}
			else {
				ret[0]++;
			}
		}
		else if (win == 2) {
			if (revflg[0]) {
				ret[0]++;
			}
			else {
				ret[1]++;
			}
		}
		else {
			ret[2]++;
		}
		saveScore();
	}

	private void saveScore() {
		try {
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("puyoscore.dat"));
			os.writeObject(scoreData);
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadScore() {
		try {
			ObjectInputStream is = new ObjectInputStream(new FileInputStream("puyoscore.dat"));
			scoreData = (HashMap<String, int[]>)is.readObject();
			is.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
