package puyopuyo.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import puyopuyo.data.CharaData;
import puyopuyo.data.CharaDataIF;
import puyopuyo.data.HistoryData;
import puyopuyo.think.AbstractPuyoThink;

public class HistoryViewDlg extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 対戦者
	 */
	private AbstractPuyoThink[] selThink;
	/**
	 * 色情報
	 */
	private int[] colorNum;

	private int[] selCharaIndex;

	static class VerticalLabelUI extends BasicLabelUI {
        protected String layoutCL(JLabel label, FontMetrics fontMetrics,
                String text, Icon icon, Rectangle viewR, Rectangle iconR,
                Rectangle textR) {
            //文字列の位置決めは空文字で代用する
            super.layoutCL(label, fontMetrics, "", icon, viewR, iconR, textR);
            textR.height = 128;
            textR.width = 16;
            return text;
        }

        protected void paintEnabledText(JLabel l, Graphics g, String s,
                int textX, int textY) {
            Graphics2D g2 = (Graphics2D) g;
            //-90度回転させる
            g2.rotate(-Math.PI / 2);
            //微調整を入れる
            super.paintEnabledText(l, g, s,-l.getSize().height + 5, textX +5);
        }
	}

	static class VerticalHeaderRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		VerticalHeaderRenderer() {
			setUI(new VerticalLabelUI());
			setHorizontalAlignment(JLabel.CENTER);
			this.setVerticalAlignment(JLabel.CENTER);
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
	             boolean isSelected, boolean hasFocus, int row, int column) {
			//JTableHeader.UIResourceTableCellRendererからコピー
	        if (table != null) {
	            JTableHeader header = table.getTableHeader();
	            if (header != null) {
	                setForeground(header.getForeground());
	                setBackground(header.getBackground());
	                setFont(header.getFont());
	            }
	        }
	 
	        setText((value == null) ? "" : value.toString());
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		    return this;
		}
	}

	static class ScoreCellRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			if (value.getClass().isArray()) {
				String text = "";
				int[] sc = (int[])value;
				Color fontcol = table.getForeground();
				if (sc[0] + sc[1] + sc[2] > 0) {
					text = sc[0] + "-" + sc[1];
				}
				if (sc[0] > sc[1]) {
					fontcol = Color.BLUE;
				}
				else if (sc[0] < sc[1]) {
					fontcol = Color.RED;
				}
				setForeground(fontcol);
				setBackground(table.getBackground());
				setValue(text);
			}
			else if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			}
			else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}
			return this;
		}
		
	}

	private JTable scoreTable;

	HistoryViewDlg(JDialog p, AbstractPuyoThink[] thk, int[] clnum) {
		super(p, "対戦成績一覧", true);
		selThink = thk;
		colorNum = clnum;
		init();
	}

	private void init() {
		super.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		ArrayList<CharaDataIF> charaList = CharaData.getCharaList();

		Object[] colname = new Object[charaList.size() + 2];
		Object[][] data = new Object[charaList.size() + 1][colname.length];
		colname[0] = "1プレイヤー";
		colname[1] = "なし";
		for (int i = 0; i < charaList.size(); i++) {
			colname[i + 2] = charaList.get(i).getCharaName();
		}
		for (int y = 0; y < data.length; y++) {
			CharaDataIF[] chara = new CharaDataIF[2];
			if (y > 0) {
				chara[0] = charaList.get(y - 1);
				data[y][0] = chara[0].getCharaName();
			}
			else {
				data[y][0] = "なし";
			}
			for (int x = 1; x < data[y].length; x++) {
				if (x > 1) {
					chara[1] = charaList.get(x - 2);
				}
				// 結果を得る
				int[] sc = HistoryData.getHistoryData().getTotalScore(selThink, chara, colorNum);
				data[y][x] = sc;
			}
		}

		scoreTable = new JTable(data, colname) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		scoreTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scoreTable.setDefaultRenderer(Object.class, new ScoreCellRenderer());

		VerticalHeaderRenderer vert = new VerticalHeaderRenderer();
		scoreTable.getColumnModel().getColumn(0).setPreferredWidth(128);
		for (int i = 1; i < colname.length; i++) {
			scoreTable.getColumnModel().getColumn(i).setHeaderRenderer(vert);
			scoreTable.getColumnModel().getColumn(i).setPreferredWidth(48);
			scoreTable.getColumnModel().getColumn(i).setIdentifier(new Integer(i - 2));
		}

		getContentPane().setLayout(new BorderLayout());

		JScrollPane scr = new JScrollPane();
		scr.getViewport().setView(scoreTable);
		add(scr, BorderLayout.CENTER);

		String pl1 = "マニュアル";
		if (selThink[0] != null) {
			pl1 = selThink[0].toString();
		}
		String pl2 = "マニュアル";
		if (selThink[1] != null) {
			pl2 = selThink[1].toString();
		}
		JLabel toplbl = new JLabel(pl1 + "("+colorNum[0]+"色)   vs   "+pl2+"("+colorNum[1]+"色)", JLabel.CENTER);
		getContentPane().add(toplbl, BorderLayout.NORTH);
		scoreTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel btnpnl = new JPanel();
		JButton okbt = new JButton("選択");
		okbt.addActionListener(this);
		okbt.setActionCommand("OK");
		JButton cancelbt = new JButton("閉じる");
		cancelbt.addActionListener(this);
		cancelbt.setActionCommand("CANCEL");
		btnpnl.add(okbt);
		btnpnl.add(cancelbt);
		getContentPane().add(btnpnl, BorderLayout.SOUTH);

		setSize(charaList.size() * 50 + 180, charaList.size() * 20 + 180);
	}

	public void actionPerformed(ActionEvent e) {
		if ("OK".equals(e.getActionCommand())) {
			int x = scoreTable.getSelectedColumn();
			int y = scoreTable.getSelectedRow();
			if (x > 0 && y >= 0) {
				//
				Integer val = (Integer)scoreTable.getColumnModel().getColumn(x).getIdentifier();
				selCharaIndex = new int[2];
				selCharaIndex[0] = y - 1;
				selCharaIndex[1] = val;
				dispose();
			}
		}
		else {
			dispose();
		}
	}

	public int[] getSelCharaIndex() {
		return selCharaIndex;
	}
}
