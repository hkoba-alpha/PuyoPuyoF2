package puyopuyo.data;

/**
 * ボイスデータ3
 * ラフィーナ、リデル、クルーク、おにおん、さかな王子、ほほうどり
 * @author hkoba
 *
 */
public class VoiceData3 extends AbstractVoiceData {

	protected VoiceData3(String prefix) {
		super(prefix);
	}

	static String[] finishData = {
		"1122",
		"1122",
		"1122",
		"1122",	// 5
		"1245",
		"1245",
		"2154",
		"2154",
		"4666",	// 10
		"5666",
		"4666",
		"5666",
		"4666",
		"5666",	// 15
	};

	@Override
	protected int getFinishIndex(int rensanum, int clnum) {
		// [8]がない
		int num = 0;
		if (clnum == 5 || clnum == 6) {
			num = 1;
		}
		else if (clnum >= 7 && clnum <= 10) {
			num = 2;
		}
		else if (clnum >= 11) {
			num = 3;
		}
		int ix = rensanum - 2;
		if (ix >= finishData.length) {
			ix = finishData.length - 1;
		}
		return finishData[ix].charAt(num) - '0' + 5;
	}

	@Override
	protected String getVoiceIndex(int num) {
		if (num <= 8) {
			return "0123455F";
		}
		else if (num <= 12) {
			return "0125F35F455F";
		}
		return "0125F135F14555555555";
	}
}
