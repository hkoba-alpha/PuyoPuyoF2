package puyopuyo;

import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import puyopuyo.data.CharaDataIF;
import puyopuyo.data.ReplayFile;
import puyopuyo.gui.PuyoSelDlg;
import puyopuyo.image.ImageData;
import puyopuyo.play.BlockPuyo;
import puyopuyo.play.FeverGage;
import puyopuyo.play.OjamaBar;
import puyopuyo.play.PuyoData;
import puyopuyo.sound.PlaySE;
import puyopuyo.think.AbstractPuyoThink;

public class PuyoFrame extends JFrame implements ActionListener, ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private PuyoCanvas puyoCanvas;
	private CheckboxMenuItem autoMenuItem;

	/**
	 * 描画のエフェクト
	 */
	static String[] optMenu = {
		"単独ぷよのアニメーション", "PUYO",
		"ぷよが消える時の効果", "FADE",
		"おじゃまビーム", "OJA_BEAM",
		"おじゃまバーの爆発効果", "OJA_STAR",
		"フィーバービーム", "FEV_BEAM",
		"フィーバーの光る効果", "FEV_STAR",
	};

	public PuyoFrame() {
		super("ぷよぷよフィーバー2");
		MenuBar bar = new MenuBar();
		Menu gmenu = new Menu("GAME");
		MenuItem endmn = new MenuItem("終了");
		MenuItem selmn = new MenuItem("選択...");
		MenuItem replaymn = new MenuItem("リプレイ保存");
		MenuItem cancelmn = new MenuItem("リプレイキャンセル");
		autoMenuItem = new CheckboxMenuItem("自動リトライモード");
		gmenu.add(selmn);
		gmenu.add(replaymn);
		gmenu.add(cancelmn);
		gmenu.add(autoMenuItem);
		gmenu.add(endmn);
		endmn.setActionCommand("END");
		endmn.addActionListener(this);
		selmn.setActionCommand("SEL");
		selmn.addActionListener(this);
		replaymn.setActionCommand("REPLAY");
		replaymn.addActionListener(this);
		cancelmn.setActionCommand("CANCEL");
		cancelmn.addActionListener(this);
		autoMenuItem.setActionCommand("AUTO");
		autoMenuItem.addItemListener(this);
		bar.add(gmenu);

		// オプション
		Menu optmenu = new Menu("効果");
		for (int i = 0; i < optMenu.length; i += 2) {
			CheckboxMenuItem optmn = new CheckboxMenuItem(optMenu[i]);
			optmn.setState(true);
			optmn.setActionCommand(optMenu[i + 1]);
			optmn.addItemListener(this);
			optmenu.add(optmn);
		}
		bar.add(optmenu);

		setMenuBar(bar);
		setLayout(new BorderLayout());
		puyoCanvas = new PuyoCanvas();
		setResizable(false);
		getContentPane().add(puyoCanvas, BorderLayout.CENTER);
		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(
				new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						puyoCanvas.stopPlay();
						System.exit(0);
					}

					@Override
					public void windowActivated(WindowEvent e) {
						puyoCanvas.requestFocus();
					}
				});
		// アイコン
		super.setIconImage(ImageData.loadImage("image/OJAMA1.png"));
	}

	private CharaDataIF[] charaData;
	private int[] colorNum;
	private ReplayFile replayFile;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PlaySE.init();
		PuyoFrame frm = new PuyoFrame();
		frm.setVisible(true);
		AbstractPuyoThink[] think = frm.selThink();
		if (think == null) {
			System.exit(0);
		}
		while (true) {
			String fname = frm.puyoCanvas.playLoop(think, frm.charaData, frm.colorNum, frm.replayFile);
			if (fname != null) {
				frm.showSaveMessage(fname);
			}
			else {
				AbstractPuyoThink[] ret = frm.selThink();
				if (ret != null) {
					think = ret;
				}
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if ("END".equals(cmd)) {
			dispose();
		}
		else if ("SEL".equals(cmd)) {
			puyoCanvas.stopPlay();
		}
		else if ("REPLAY".equals(cmd)) {
			puyoCanvas.savePlay();
		}
		else if ("CANCEL".equals(cmd)) {
			puyoCanvas.cancelReplay();
		}
	}

	public AbstractPuyoThink[] selThink() {
		final PuyoSelDlg dlg = new PuyoSelDlg(this);
		if (SwingUtilities.isEventDispatchThread()) {
			dlg.showDialog();
		}
		else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						dlg.showDialog();
					}
				});
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		charaData = dlg.getCharaData();
		colorNum = dlg.getColorNum();
		replayFile = dlg.getReplayFile();
		return dlg.getThink();
	}

	public void showSaveMessage(final String fname) {
		try {
			final JFrame parent = this;
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(parent, fname+"にリプレイデータを保存しました。", "リプレイ保存", JOptionPane.INFORMATION_MESSAGE);
				}
			});
		} catch (InterruptedException e) {
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == autoMenuItem) {
			//System.out.println("state="+autoMenuItem.getState());
			puyoCanvas.setAutoRetryMode(autoMenuItem.getState());
		}
		else {
			CheckboxMenuItem itm = (CheckboxMenuItem)e.getSource();
			String cmd = itm.getActionCommand();
			if ("PUYO".equals(cmd)) {
				BlockPuyo.optPuyoAnimateFlag = itm.getState();
			}
			else if ("FADE".equals(cmd)) {
				PuyoData.optPuyoFadeFlag = itm.getState();
			}
			else if ("OJA_BEAM".equals(cmd)) {
				OjamaBar.optBeamDrawFlag = itm.getState();
			}
			else if ("OJA_STAR".equals(cmd)) {
				OjamaBar.optOjamaStarFlag = itm.getState();
			}
			else if ("FEV_BEAM".equals(cmd)) {
				FeverGage.optFeverEffectFlag = itm.getState();
			}
			else if ("FEV_STAR".equals(cmd)) {
				FeverGage.optFeverStarFlag = itm.getState();
			}
		}
	}
}
