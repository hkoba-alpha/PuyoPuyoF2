package puyopuyo;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import puyopuyo.data.CharaDataIF;
import puyopuyo.data.HistoryData;
import puyopuyo.data.ReplayFile;
import puyopuyo.image.DigitImage;
import puyopuyo.image.ImageData;
import puyopuyo.play.MovePuyo;
import puyopuyo.play.PuyoData;
import puyopuyo.think.AbstractPuyoThink;

public class PuyoCanvas extends Canvas implements KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private BufferedImage frameImage;

	public PuyoCanvas() {
		addKeyListener(this);
		super.setBackground(new Color(255, 215, 247));
	}

	private PuyoData puyoData1;
	private PuyoData puyoData2;
	private AbstractPuyoThink think1;
	private AbstractPuyoThink think2;
	private volatile boolean loopFlag;
	private volatile boolean finishFlag;
	private volatile boolean saveFlag;

	private AudioClip normalBgm;
	private AudioClip feverBgm;
	private AudioClip gameOverBgm;
	private volatile boolean replayMode;
	private boolean autoRetryMode = false;

	private static void play(AudioClip clip) {
		if (clip != null) {
			clip.play();
		}
	}
	private static void loop(AudioClip clip) {
		if (clip != null) {
			clip.loop();
		}
	}
	private static void stop(AudioClip clip) {
		if (clip != null) {
			clip.stop();
		}
	}

	static Color[] fontColor = {
		new Color(64, 64, 255), new Color(255, 64, 64)
	};

	/**
	 * 影付きテキストを描画する
	 * @param g
	 * @param txt
	 * @param cx
	 * @param ty
	 */
	private static void drawText(Graphics2D g, String txt, int cx, int ty, Color fgcl, Color bgcl) {
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D rc = fm.getStringBounds(txt, g);
		g.setColor(bgcl);
		int x = (int)(cx - rc.getWidth() / 2);
		int y = ty + fm.getAscent();
		g.drawString(txt, x - 1, y);
		g.drawString(txt, x + 1, y);
		g.drawString(txt, x, y - 1);
		g.drawString(txt, x, y + 1);
		g.setColor(fgcl);
		g.drawString(txt, x, y);
	}

	private AudioClip loadBgm(String fname) {
		URL bgm = PuyoCanvas.class.getResource("/" + fname);
		if (bgm != null) {
			return Applet.newAudioClip(bgm);
		}
		return null;
	}

	public String playLoop(AbstractPuyoThink[] thk, CharaDataIF[] chara, int[] clnum, ReplayFile replay) {
		String saveFname = null;
		frameImage = ImageData.loadImage("image/frame.png");
		Graphics2D fg = (Graphics2D)frameImage.getGraphics();
		fg.setFont(new Font("Dialog", Font.BOLD, 14));
		if (replay != null) {
			// リプレイモード
			//thk[0] = null;
			//thk[1] = null;
			drawText(fg, "リプレイモード", 320, 20, Color.WHITE, Color.BLACK);
			chara = replay.getCharaData();
			clnum = replay.getColorNum();
		}
		for (int i = 0; i < 2; i++) {
			String txt = "マニュアル";
			Color fgcl = new Color(255 - i * 255, 0, i * 255);
			fg.setColor(new Color(fgcl.getRed(), fgcl.getGreen(), fgcl.getBlue(), 64));
			fg.fillOval(i * 357 + 40, 2, 200, 40);
			if (replay != null) {
				txt = replay.getThinkName()[i];
			}
			else if (thk[i] != null) {
				txt = thk[i].toString();
			}
			drawText(fg, txt + "("+clnum[i]+"色)", 141 + i * 357, 4, fgcl, Color.WHITE);
			if (chara[i] != null) {
				drawText(fg, chara[i].getCharaName(), 141 + i * 357, 20, fgcl, Color.WHITE);
			}
		}
		fg.dispose();

		super.createBufferStrategy(2);
		finishFlag = false;
		BufferStrategy bstr = super.getBufferStrategy();
		if (normalBgm == null) {
			normalBgm = loadBgm("bgm/normal.wav");
		}
		if (feverBgm == null) {
			feverBgm = loadBgm("bgm/fever.wav");
		}
		if (gameOverBgm == null) {
			gameOverBgm = loadBgm("bgm/gameover.wav");
		}
		while (!finishFlag) {
			// 思考ルーチンは、毎回新しいインスタンスを作成する
			for (int i = 0; i < 2; i++) {
				if (thk[i] != null) {
					try {
						thk[i] = thk[i].getClass().newInstance();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}

			saveFlag = false;
			long seed = System.currentTimeMillis();
			if (replay != null) {
				seed = replay.getRandomSeed();
				replayMode = true;
			}
			else {
				replayMode = false;
			}
			PuyoData.initBgImage(frameImage);
			puyoData1 = new PuyoData(42, seed);
			puyoData2 = new PuyoData(399, seed);
			if (replay != null) {
				puyoData1.setReplayData(replay.getReplayData()[0]);
				puyoData2.setReplayData(replay.getReplayData()[1]);
			}
			think1 = thk[0];
			think2 = thk[1];
			puyoData1.setEnemyPuyo(puyoData2);
			puyoData2.setEnemyPuyo(puyoData1);
			loopFlag = true;
	
			if (chara[0] != null) {
				puyoData1.setCharaData(chara[0].getTsumoData(), chara[0].getNormalRensa(), chara[0].getFeverRensa());
				puyoData1.setVoiceData(chara[0].getVoiceData());
			}
			if (chara[1] != null) {
				puyoData2.setCharaData(chara[1].getTsumoData(), chara[1].getNormalRensa(), chara[1].getFeverRensa());
				puyoData2.setVoiceData(chara[1].getVoiceData());
			}
			puyoData1.setColorNum(clnum[0]);
			puyoData2.setColorNum(clnum[1]);

			boolean feverMode = false;
			boolean gameOver = false;
			// SCORE-START
			int winPlayer = 0;
			if (replay == null) {
				int[] score = HistoryData.getHistoryData().getTotalScore(thk, chara, clnum);
				int sc1 = score[0];
				int sc2 = score[1];
				if (sc1 > sc2 && sc1 > 99) {
					sc2 -= (sc1 - 99);
					sc1 = 99;
					if (sc2 < 0) {
						sc2 = 0;
					}
				}
				else if (sc2 > 99) {
					sc1 -= (sc2 - 99);
					sc2 = 99;
					if (sc1 < 0) {
						sc1 = 0;
					}
				}
				fg = (Graphics2D)frameImage.getGraphics();
				// 勝敗を描画する
				DigitImage.drawWinNum(fg, 279, 277, sc1);
				DigitImage.drawWinNum(fg, 334, 277, sc2);
				fg.dispose();
			}
			// SCORE-END
			int autoCount = 0;

			System.gc();

			loop(normalBgm);
			long nxttm = System.currentTimeMillis() + 25;

			while (loopFlag) {
				Graphics2D g2 = (Graphics2D)bstr.getDrawGraphics();
				/*
				if (frameImage != null) {
					g2.drawImage(frameImage, 0, 0, this);
				}
				else {
					g2.clearRect(0, 0, 700, 420);
				}
				*/
				//g2.fillRect(0, 0, 700, 420);
				PuyoData.GameMode mode1 = puyoData1.turnNext();
				PuyoData.GameMode mode2 = puyoData2.turnNext();

				boolean flg = puyoData1.isFeverMode() | puyoData2.isFeverMode();
				boolean overflg = puyoData1.isGameOver() | puyoData2.isGameOver();
				if (overflg) {
					if (!gameOver) {
						stop(normalBgm);
						stop(feverBgm);
						play(gameOverBgm);
					}
					gameOver = true;
				}
				else if (flg != feverMode) {
					// 音楽が変わる
					feverMode = flg;
					if (feverMode) {
						stop(normalBgm);
						loop(feverBgm);
					}
					else {
						stop(feverBgm);
						loop(normalBgm);
					}
				}

				puyoData1.drawBack(g2);
				puyoData2.drawBack(g2);
				puyoData1.drawFore(g2);
				puyoData2.drawFore(g2);
				puyoData1.drawStar(g2);
				puyoData2.drawStar(g2);
				bstr.show();
				// 思考ルーチン
				if (!replayMode && think1 != null) {
					if (mode1 == PuyoData.GameMode.MoveMode) {
						think1.nextMoveEvent(
								puyoData1,
								puyoData1.getBlockData(),
								puyoData1.getMoveData(),
								puyoData1.getThinkEnv(),
								puyoData2.getBlockData(),
								puyoData2.getMoveData(),
								puyoData2.getThinkEnv());
					}
					else {
						think1.waitEvent(
								puyoData1,
								puyoData1.getBlockData(),
								puyoData1.getThinkEnv(),
								puyoData2.getBlockData(),
								puyoData2.getMoveData(),
								puyoData2.getThinkEnv());
						// SCORE-START
						if (gameOver && winPlayer != 1 && mode1 == PuyoData.GameMode.NextMode) {
							// 次のぷよを出せる状態になっているとしたら勝ち
							winPlayer = 1;
							puyoData1.setWinner();
						}
						// SCORE-END
					}
				}
				if (!replayMode && think2 != null) {
					if (mode2 == PuyoData.GameMode.MoveMode) {
						think2.nextMoveEvent(
								puyoData2,
								puyoData2.getBlockData(),
								puyoData2.getMoveData(),
								puyoData2.getThinkEnv(),
								puyoData1.getBlockData(),
								puyoData1.getMoveData(),
								puyoData1.getThinkEnv());
					}
					else {
						think2.waitEvent(
								puyoData2,
								puyoData2.getBlockData(),
								puyoData2.getThinkEnv(),
								puyoData1.getBlockData(),
								puyoData1.getMoveData(),
								puyoData1.getThinkEnv());
						// SCORE-START
						if (gameOver && winPlayer != 2 && mode2 == PuyoData.GameMode.NextMode) {
							// 次のぷよを出せる状態になっているとしたら勝ち
							winPlayer = 2;
							puyoData2.setWinner();
						}
						// SCORE-END
					}
				}
				// 自動リトライモードかチェックする
				if (autoRetryMode && gameOver) {
					if (autoCount > 0) {
						autoCount--;
						if (autoCount == 0) {
							loopFlag = false;
						}
					}
					else if (winPlayer != 0 || (puyoData1.isGameOver() && puyoData2.isGameOver())) {
						autoCount = 40 * 3;
					}
				}
				try {
					long waittm = nxttm - System.currentTimeMillis();
					if (waittm < 1) {
						if (waittm > -25) {
							nxttm += 25;
						}
						else {
							//nxttm = nxttm - waittm + 25;
							nxttm = nxttm - waittm + 5;
						}
						waittm = 1;
					}
					else {
						nxttm += 25;
					}
					Thread.currentThread().join(waittm);
					//Thread.currentThread().join(1);
				} catch (InterruptedException e) {
				}
			}
			// 終了
			stop(normalBgm);
			stop(feverBgm);
			// 保存テスト
			if (replay == null && saveFlag) {
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
				saveFname = df.format(new Date())+".txt";
				ReplayFile.saveFile(saveFname, new PuyoData[]{puyoData1, puyoData2}, thk, chara, seed);
			}
			// SCORE-START
			if (replay == null) {
				HistoryData.getHistoryData().setScore(thk, chara, clnum, winPlayer);
			}
			// SCORE-END
		}
		synchronized (this) {
			this.notifyAll();
		}
		return saveFname;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(640, 480);
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			if (think1 == null) {
				puyoData1.pushKey(MovePuyo.KEY_LEFT);
			}
			if (think2 == null) {
				puyoData2.pushKey(MovePuyo.KEY_LEFT);
			}
			break;
		case KeyEvent.VK_RIGHT:
			if (think1 == null) {
				puyoData1.pushKey(MovePuyo.KEY_RIGHT);
			}
			if (think2 == null) {
				puyoData2.pushKey(MovePuyo.KEY_RIGHT);
			}
			break;
		case KeyEvent.VK_DOWN:
			if (think1 == null) {
				puyoData1.pushKey(MovePuyo.KEY_DOWN);
			}
			if (think2 == null) {
				puyoData2.pushKey(MovePuyo.KEY_DOWN);
			}
			break;
		case KeyEvent.VK_Z:
			if (think1 == null) {
				puyoData1.pushKey(MovePuyo.KEY_TURNL);
			}
			if (think2 == null) {
				puyoData2.pushKey(MovePuyo.KEY_TURNL);
			}
			break;
		case KeyEvent.VK_X:
		case KeyEvent.VK_UP:
			if (think1 == null) {
				puyoData1.pushKey(MovePuyo.KEY_TURNR);
			}
			if (think2 == null) {
				puyoData2.pushKey(MovePuyo.KEY_TURNR);
			}
			break;
		case KeyEvent.VK_ENTER:
			loopFlag = false;
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			if (think1 == null) {
				puyoData1.releaseKey(MovePuyo.KEY_LEFT);
			}
			if (think2 == null) {
				puyoData2.releaseKey(MovePuyo.KEY_LEFT);
			}
			break;
		case KeyEvent.VK_RIGHT:
			if (think1 == null) {
				puyoData1.releaseKey(MovePuyo.KEY_RIGHT);
			}
			if (think2 == null) {
				puyoData2.releaseKey(MovePuyo.KEY_RIGHT);
			}
			break;
		case KeyEvent.VK_DOWN:
			if (think1 == null) {
				puyoData1.releaseKey(MovePuyo.KEY_DOWN);
			}
			if (think2 == null) {
				puyoData2.releaseKey(MovePuyo.KEY_DOWN);
			}
			break;
		case KeyEvent.VK_Z:
			if (think1 == null) {
				puyoData1.releaseKey(MovePuyo.KEY_TURNL);
			}
			if (think2 == null) {
				puyoData2.releaseKey(MovePuyo.KEY_TURNL);
			}
			break;
		case KeyEvent.VK_X:
		case KeyEvent.VK_UP:
			if (think1 == null) {
				puyoData1.releaseKey(MovePuyo.KEY_TURNR);
			}
			if (think2 == null) {
				puyoData2.releaseKey(MovePuyo.KEY_TURNR);
			}
			break;
		}
	}

	public synchronized void stopPlay() {
		if (loopFlag) {
			finishFlag = true;
			loopFlag = false;
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
		}
		repaint();
	}

	public void savePlay() {
		saveFlag = true;
		stopPlay();
	}

	/**
	 * リプレイをキャンセル
	 * タイミング次第でちょっとNG
	 */
	public void cancelReplay() {
		replayMode = false;
		puyoData1.setReplayData(null);
		puyoData2.setReplayData(null);
	}

	/**
	 * ゲームオーバーのたびに自動的にリトライするかどうかをチェックする
	 * @param flg
	 */
	public void setAutoRetryMode(boolean flg) {
		autoRetryMode = flg;
	}
}
