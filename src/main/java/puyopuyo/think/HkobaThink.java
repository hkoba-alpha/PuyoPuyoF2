package puyopuyo.think;

import java.util.ArrayList;

import puyopuyo.think.data.BlockData;
import puyopuyo.think.data.EnvData;
import puyopuyo.think.data.MoveData;
import puyopuyo.think.util.CheckData;
import puyopuyo.think.util.CheckFilter;
import puyopuyo.think.util.CheckMostDelete;
import puyopuyo.think.util.CheckMostRensa;

public class HkobaThink extends AbstractPuyoThink implements CheckFilter {

	static int[] limitY = {
		5, 5, 11, 9, 5, 5
	};

	@Override
	protected void think(BlockData meBlk, MoveData mePuyo, EnvData meEnv,
			BlockData eneBlk, MoveData enePuyo, EnvData eneEnv) {
		int[] meOja = meEnv.getOjamaPuyo();
		int feverRest = meEnv.getFeverRestTime();
		//
		//System.out.println(mePuyo.toString());
		//System.out.println(meBlk.toString());
		int[] dy = meBlk.getBlockY();
		boolean fadeflg = false;
		CheckData checkdt = new CheckData(meBlk, mePuyo);
		for (int x = 0; x < 6; x++) {
			if (dy[x] < limitY[x]) {
				fadeflg = true;
				break;
			}
		}
		//System.out.println("=======思考ルーチン開始=====:"+mePuyo);
		MoveData nextpuyo = null;
		if (feverRest >= 0) {
			// フィーバーモード
			nextpuyo = checkdt.selectData(new CheckMostRensa(3));
			//System.out.println("思考ルーチン：フィーバー中:"+nextpuyo);
		}
		if (nextpuyo == null) {
			if (fadeflg || meOja[0]+meOja[1] > 6) {
				nextpuyo = checkdt.selectData(new CheckMostDelete());
				//System.out.println("思考ルーチン：もっとも消す:"+nextpuyo);
			}
		}
		if (nextpuyo == null) {
			//nextpuyo = getMostChain(meBlk, mePuyo.clone());
			nextpuyo = checkdt.selectData(this);
			//System.out.println("思考ルーチン：消さずにつながる:"+nextpuyo);
		}

		if (nextpuyo != null) {
			/*
			for (int y = 0; y < 14; y++) {
				char[] buf = new char[7];
				buf[6] = '|';
				for (int x = 0; x < 6; x++) {
					//
					buf[x] = ".12345X".charAt(meBlk.getBlock(x, y));
				}
				System.out.println(new String(buf));
			}
			System.out.println("-------------------");
			*/
			int[] mv = mePuyo.getDiff(nextpuyo);
			/*
			for (int i = 0; i < mePuyo.getPoint().length; i++) {
				System.out.print("["+i+":("+mePuyo.getPoint()[i].x+","+mePuyo.getPoint()[i].y+")="+mePuyo.getPuyoColor()[i]+"]");
			}
			for (int i = 0; i < nextpuyo.getPoint().length; i++) {
				System.out.print("["+i+":("+nextpuyo.getPoint()[i].x+","+nextpuyo.getPoint()[i].y+")="+nextpuyo.getPuyoColor()[i]+"]");
			}
			*/
			//System.out.println("DIFF:[0]="+mv[0]+",[1]="+mv[1]);
			moveRight(mv[0]);
			turnRight(mv[1]);
		}
		moveDrop(14);
		//skipDrop(14);
	}

	private static int[] subScore = {
		10, 10, 0, 4, 10, 10
	};
	/**
	 * 4つ未満で一番長くくっつくところを探す
	 * @param meBlk
	 * @param mePuyo
	 * @return
	 */
	private MoveData getMostChain(BlockData meBlk, MoveData mePuyo) {
		MoveData ret = null;
		int maxnum = 0;
		int[] topy = meBlk.getBlockY();
		ArrayList<MoveData> retlst = new ArrayList<MoveData>();
		for (int turn = 0; turn < 4; turn++) {
			MoveData tmpdt = mePuyo.clone();
			tmpdt.turnRight(turn);
			tmpdt.addX(-6, meBlk);
			for (int x = 0; x < 6; x++) {
				int[] chk = meBlk.checkChainNum(tmpdt);
				boolean flg = true;
				int num = 0;
				for (int i = 0; i < chk.length; i++) {
					if (chk[i] >= 4) {
						flg = false;
					}
					num += (chk[i]*100 + topy[tmpdt.getPoint()[i].x] + subScore[tmpdt.getPoint()[i].x]);
				}
				if (flg) {
					if (num == maxnum) {
						retlst.add(tmpdt.clone());
					}
					else if (num > maxnum) {
						maxnum = num;
						retlst.clear();
						retlst.add(tmpdt.clone());
					}
				}
				if (tmpdt.addX(1, meBlk) != 1) {
					// 終了
					break;
				}
			}
		}
		if (retlst.size() == 0) {
			ret = mePuyo.clone();
		}
		else {
			int ix = (int)(Math.random() * retlst.size());
			ret = retlst.get(ix);
		}
		// TEST
		if (ret != null) {
			BlockData blk = meBlk.putMoveData(ret, null);
			//if (blk.getBlockNum(0) != -1) {
			if (blk.getBlockNum(0) >= meBlk.getBlockNum(0)) {
				System.out.println("ERROR!!!!");
				for (int y = 0; y < 14; y++) {
					char[] buf = new char[13];
					buf[6] = '|';
					for (int x = 0; x < 6; x++) {
						//
						buf[x] = ".12345X".charAt(meBlk.getBlock(x, y));
						buf[x+7] = ".12345X".charAt(blk.getBlock(x, y));
					}
					System.out.println(new String(buf));
				}
				for (int i = 0; i < ret.getPoint().length; i++) {
					System.out.print("["+i+":("+ret.getPoint()[i].x+","+ret.getPoint()[i].y+")="+ret.getPuyoColor()[i]+"]");
				}
				System.out.println("-------------------");
			}
		}
		return ret;
	}

	@Override
	protected String getThinkName() {
		return "小林思考ルーチン初級";
	}

	/**
	 * 4個未満で最もつながるところを探す
	 */
	public int checkScore(BlockData blkdt, MoveData mvdt) {
		int[] num = blkdt.checkChainNum(mvdt);
		int ret = 0;
		int[] topy = blkdt.getBlockY();
		for (int i = 0; i < num.length; i++) {
			if (num[i] >= 4) {
				return IGNORE_DATA;
			}
			ret += num[i]*20;
			int x = mvdt.getPoint()[i].x;
			if (topy[x] <= limitY[x]) {
				ret -= (limitY[x] - topy[x] + 1);
			}
		}
		return ret;
	}

}
