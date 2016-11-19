package puyopuyo.data;

/**
 * ボイスデータ２
 * おしゃれコウベ、こづれフランケン、アコール先生、ポポイ
 * @author hkoba
 *
 */
public class VoiceData2 extends AbstractVoiceData {
	public VoiceData2(String prefix) {
		super(prefix);
	}

	static String[] finishData = {
		"1233",
		"1233",
		"1233",
		"1233",	// 5
		"2334",
		"2334",
		"3345",
		"3345",
		"4455",	// 10
		"4455",
		"5544",
		"4455",
		"4455",
		"5555",	// 15
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
			return "01234F34555F";
		}
		return "01234F34F123455555555";
	}
}
