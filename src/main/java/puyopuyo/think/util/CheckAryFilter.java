package puyopuyo.think.util;

import java.util.ArrayList;

import puyopuyo.think.data.BlockData;
import puyopuyo.think.data.MoveData;

/**
 * �����̂Ղ�̈ړ��p�^�[���ɂ��ă`�F�b�N�������ʂ�Ԃ�
 * 
 * @author t-yokotani
 * 
 */
public interface CheckAryFilter {

	public static final int IGNORE_DATA = Integer.MIN_VALUE;

	public int checkScore(MoveData[] mvDtAry, BlockData preBlkDt,
                          BlockData postBlkDt, ArrayList<int[]> fadeList, int index);

}
