package puyopuyo.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import puyopuyo.data.CharaData;
import puyopuyo.data.CharaDataIF;
import puyopuyo.data.HistoryData;
import puyopuyo.data.ReplayFile;
import puyopuyo.think.AbstractPuyoThink;

public class PuyoSelDlg extends JDialog implements ActionListener, ItemListener, ListSelectionListener, MouseListener {

	public static final int BUTTON_OK = 1;
	public static final int BUTTON_CANCEL = 0;

	static class ThinkList implements ListModel {
		ArrayList<AbstractPuyoThink> thinkList = new ArrayList<AbstractPuyoThink>();

		public void initThink() {
			try {
				//BufferedReader rd = new BufferedReader(new FileReader("puyopuyo.txt"));
				BufferedReader rd = new BufferedReader(new InputStreamReader(PuyoSelDlg.class.getResourceAsStream("/puyopuyo.txt")));
				String lnstr;
				while ((lnstr = rd.readLine()) != null) {
					if (lnstr.trim().length() == 0) {
						continue;
					}
					if (lnstr.charAt(0) == '#') {
						continue;
					}
					try {
						Class cls = Class.forName(lnstr);
						AbstractPuyoThink thk = (AbstractPuyoThink)cls.newInstance();
						thinkList.add(thk);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (ClassCastException e) {
						e.printStackTrace();
					}
				}
				rd.close();
			} catch (FileNotFoundException e) {
				thinkList.add(new puyopuyo.think.RandomThink());
				thinkList.add(new puyopuyo.think.HkobaThink());
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public int getSize() {
			return thinkList.size() + 1;
		}

		public Object getElementAt(int index) {
			if (index == 0) {
				return "マニュアル";
			}
			return thinkList.get(index - 1);
		}

		public void addListDataListener(ListDataListener l) {
		}

		public void removeListDataListener(ListDataListener l) {
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JList selList1;
	private JList selList2;
	private ThinkList thinkList;

	private JList charList1;
	private JList charList2;

	private ButtonGroup buttonGrp1;
	private ButtonGroup buttonGrp2;
	/**
	 * リプレイかどうかを選ぶ
	 */
	private JComboBox replayCombo;

	private ArrayList<CharaDataIF> charaList;

	private int buttonCode;
	private ReplayFile replayFile;
	private JLabel historyLabel;
	private AbstractPuyoThink[] selThink;

	public PuyoSelDlg(Frame p) {
		super(p, "プレイヤー選択", true);
		init();
	}

	private void init() {
		super.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		selThink = null;
		thinkList = new ThinkList();
		thinkList.initThink();
		charaList = CharaData.getCharaList();

		JPanel selpnl = new JPanel();
		selpnl.setLayout(new GridLayout(2, 2));
		setLayout(new BorderLayout());
		selList1 = new JList(thinkList);
		selList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selList2 = new JList(thinkList);
		selList2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		selList1.addListSelectionListener(this);
		selList2.addListSelectionListener(this);

		JScrollPane scr1 = new JScrollPane();
		scr1.getViewport().setView(selList1);
		JScrollPane scr2 = new JScrollPane();
		scr2.getViewport().setView(selList2);

		Vector<CharaDataIF> veclst = new Vector<CharaDataIF>(charaList);
		charList1 = new JList(veclst);
		charList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		charList2 = new JList(veclst);
		charList2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scr3 = new JScrollPane();
		scr3.getViewport().setView(charList1);
		JScrollPane scr4 = new JScrollPane();
		scr4.getViewport().setView(charList2);
		charList1.addListSelectionListener(this);
		charList2.addListSelectionListener(this);

		JPanel chpnl1 = new JPanel();
		chpnl1.setLayout(new BorderLayout());
		JPanel btpnl1 = new JPanel();
		btpnl1.setLayout(new GridLayout(3, 1));
		JPanel chpnl2 = new JPanel();
		chpnl2.setLayout(new BorderLayout());
		JPanel btpnl2 = new JPanel();
		btpnl2.setLayout(new GridLayout(3, 1));

		buttonGrp1 = new ButtonGroup();
		buttonGrp2 = new ButtonGroup();
		for (int i = 3; i < 6; i++) {
			JRadioButton bt1 = new JRadioButton(i+"色");
			JRadioButton bt2 = new JRadioButton(i+"色");
			bt1.setActionCommand(Integer.toString(i));
			bt2.setActionCommand(Integer.toString(i));
			buttonGrp1.add(bt1);
			buttonGrp2.add(bt2);
			btpnl1.add(bt1);
			btpnl2.add(bt2);
			bt1.setSelected(true);
			bt2.setSelected(true);

			bt1.addActionListener(this);
			bt2.addActionListener(this);
		}
		chpnl1.add(btpnl1, BorderLayout.WEST);
		chpnl1.add(scr3, BorderLayout.CENTER);
		chpnl2.add(btpnl2, BorderLayout.WEST);
		chpnl2.add(scr4, BorderLayout.CENTER);

		selpnl.add(scr1);
		selpnl.add(scr2);
		selpnl.add(chpnl1);
		selpnl.add(chpnl2);
		super.getContentPane().add(selpnl, BorderLayout.CENTER);

		// リプレイ
		JPanel replaypnl = new JPanel();
		replaypnl.setLayout(new BorderLayout());
		replayCombo = new JComboBox();
		replayCombo.addItem("[リプレイなし]");
		replaypnl.add(new JLabel("リプレイ："), BorderLayout.WEST);
		replaypnl.add(replayCombo, BorderLayout.CENTER);
		super.getContentPane().add(replaypnl, BorderLayout.NORTH);
		File dir = new File("replay");
		String[] files = dir.list();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				//
				replayCombo.addItem(files[i]);
			}
		}
		replayCombo.addItemListener(this);

		JPanel botpnl = new JPanel();
		botpnl.setLayout(new GridLayout(2, 1));
		historyLabel = new JLabel();
		historyLabel.setHorizontalAlignment(JLabel.CENTER);
		botpnl.add(historyLabel);

		JPanel btnpnl = new JPanel();
		JButton okbt = new JButton("  OK  ");
		JButton cancelbt = new JButton("CANCEL");
		btnpnl.add(okbt);
		btnpnl.add(cancelbt);
		okbt.setActionCommand("OK");
		okbt.addActionListener(this);
		cancelbt.setActionCommand("CANCEL");
		cancelbt.addActionListener(this);
		botpnl.add(btnpnl);

		super.getContentPane().add(botpnl, BorderLayout.SOUTH);

		setSize(400, 350);

		historyLabel.addMouseListener(this);
		historyLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	/**
	 * ダイアログを表示する
	 * @return BUTTON_OK or BUTTON_CANCEL
	 */
	public int showDialog() {
		buttonCode = BUTTON_CANCEL;
		setVisible(true);
		return buttonCode;
	}

	public void actionPerformed(ActionEvent e) {
		if ("OK".equals(e.getActionCommand())) {
			int ix = replayCombo.getSelectedIndex();
			if (ix > 0) {
				// リプレイを選んでいる
				if (replayFile == null) {
					return;
				}
			}
			else if (selList1.getSelectedIndex() < 0 || selList2.getSelectedIndex() < 0) {
				return;
			}
			buttonCode = BUTTON_OK;
			dispose();
		}
		else if ("CANCEL".equals(e.getActionCommand())) {
			buttonCode = BUTTON_CANCEL;
			dispose();
		}
		else {
			setHistoryData();
		}
	}

	public AbstractPuyoThink[] getThink() {
		if (buttonCode != BUTTON_OK) {
			return null;
		}
		return selThink;
	}

	public CharaDataIF[] getCharaData() {
		CharaDataIF[] ret = new CharaDataIF[2];
		int ix = charList1.getSelectedIndex();
		if (ix >= 0 && ix < charaList.size()) {
			ret[0] = charaList.get(ix);
		}
		ix = charList2.getSelectedIndex();
		if (ix >= 0 && ix < charaList.size()) {
			ret[1] = charaList.get(ix);
		}
		return ret;
	}

	public int[] getColorNum() {
		int[] ret = new int[2];
		ret[0] = Integer.parseInt(buttonGrp1.getSelection().getActionCommand());
		ret[1] = Integer.parseInt(buttonGrp2.getSelection().getActionCommand());
		return ret;
	}

	/**
	 * リプレイだった場合
	 * @return
	 */
	public ReplayFile getReplayFile() {
		return replayFile;
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			// 選択先
			int ix = replayCombo.getSelectedIndex();
			if (ix > 0) {
				replayFile = ReplayFile.loadFile(replayCombo.getSelectedItem().toString());
				if (replayFile == null) {
					// ERROR
					ix = 0;
				}
				else {
					CharaDataIF[] chsel = new CharaDataIF[2];
					Object[] thksel = new Object[2];
					for (int i = 0; i < 2; i++) {
						CharaDataIF chdt = replayFile.getCharaData()[i];
						if (chdt != null) {
							for (int j = 0; j < this.charaList.size(); j++) {
								if (chdt.getCharaName().equals(this.charaList.get(j).getCharaName())) {
									chsel[i] = this.charaList.get(j);
									break;
								}
							}
						}
						for (int j = 0; j < this.thinkList.getSize(); j++) {
							if (this.thinkList.getElementAt(j).toString().equals(replayFile.getThinkName()[i])) {
								thksel[i] = this.thinkList.getElementAt(j);
							}
						}
						ButtonGroup grp = this.buttonGrp1;
						if (i == 1) {
							grp = this.buttonGrp2;
						}
						String btcmd = Integer.toString(replayFile.getColorNum()[i]);
						for (Enumeration<AbstractButton> en = grp.getElements(); en.hasMoreElements();) {
							AbstractButton bt = en.nextElement();
							if (btcmd.equals(bt.getActionCommand())) {
								bt.setSelected(true);
								break;
							}
						}
					}
					if (chsel[0] != null) {
						this.charList1.setSelectedValue(chsel[0], true);
					}
					else {
						this.charList1.setSelectedIndices(new int[0]);
					}
					if (chsel[1] != null) {
						this.charList2.setSelectedValue(chsel[1], true);
					}
					else {
						this.charList2.setSelectedIndices(new int[0]);
					}
					this.selList1.setSelectedValue(thksel[0], true);
					this.selList2.setSelectedValue(thksel[1], true);
				}
			}
			else {
				replayFile = null;
			}
			boolean flg = (ix == 0);
			for (Enumeration<AbstractButton> en = this.buttonGrp1.getElements(); en.hasMoreElements();) {
				en.nextElement().setEnabled(flg);
			}
			for (Enumeration<AbstractButton> en = this.buttonGrp2.getElements(); en.hasMoreElements();) {
				en.nextElement().setEnabled(flg);
			}
			this.charList1.setEnabled(flg);
			this.charList2.setEnabled(flg);
			this.selList1.setEnabled(flg);
			this.selList2.setEnabled(flg);
			setHistoryData();
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			if (e.getSource() == selList1 || e.getSource() == selList2) {
				// 思考ルーチンの選択が変わった
				selThink = new AbstractPuyoThink[2];
				if (selList1.getSelectedIndex() > 0) {
					selThink[0] = thinkList.thinkList.get(selList1.getSelectedIndex() - 1);
				}
				else {
					selThink[0] = null;
				}
				if (selList2.getSelectedIndex() > 0) {
					selThink[1] = thinkList.thinkList.get(selList2.getSelectedIndex() - 1);
				}
				else {
					selThink[1] = null;
				}
			}
			setHistoryData();
		}
	}

	private void setHistoryData() {
		if (selThink == null) {
			return;
		}
		CharaDataIF[] chara = getCharaData();
		int[] clnum = getColorNum();
		int[] sc = HistoryData.getHistoryData().getTotalScore(selThink, chara, clnum);
		historyLabel.setText("対戦成績："+sc[0]+"-"+sc[1]+"  引き分け:"+sc[2]);
	}

	public void mouseClicked(MouseEvent e) {
		if (selThink != null) {
			HistoryViewDlg dlg = new HistoryViewDlg(this, selThink, getColorNum());
			dlg.setVisible(true);
			int[] sel = dlg.getSelCharaIndex();
			if (sel != null) {
				if (sel[0] < 0) {
					charList1.setSelectedIndices(new int[0]);
				}
				else {
					charList1.setSelectedValue(charList1.getModel().getElementAt(sel[0]), true);
				}
				if (sel[1] < 0) {
					charList2.setSelectedIndices(new int[0]);
				}
				else {
					charList2.setSelectedValue(charList2.getModel().getElementAt(sel[1]), true);
				}
			}
		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
		if (selThink != null) {
			historyLabel.setOpaque(true);
			historyLabel.setBackground(Color.YELLOW);
			historyLabel.repaint();
		}
	}

	public void mouseExited(MouseEvent e) {
		historyLabel.setOpaque(false);
		historyLabel.getParent().repaint();
	}
}
