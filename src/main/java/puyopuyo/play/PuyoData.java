package puyopuyo.play;

import java.applet.AudioClip;
import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import puyopuyo.data.AbstractVoiceData;
import puyopuyo.data.KeyRecord;
import puyopuyo.data.ReplayData;
import puyopuyo.data.VoiceDataIF;
import puyopuyo.image.DigitImage;
import puyopuyo.image.EffectImage;
import puyopuyo.image.ImageData;
import puyopuyo.sound.PlaySE;
import puyopuyo.think.data.BlockData;
import puyopuyo.think.data.EnvData;
import puyopuyo.think.data.MoveData;

public class PuyoData {
    /**
     * ぷよが消えるときの効果
     */
    public static boolean optPuyoFadeFlag = true;

    /**
     * 通常時のBGイメージ
     */
    private static Image bgImage;

    /**
     * フレームのイメージ
     */
    private static Image frameImage;

    /**
     * フィーバー用のBGイメージ
     */
    private BufferedImage feverBgImage;

    /**
     * 次ぷよ用のイメージ
     */
    private BufferedImage nextPuyoImage;

    /**
     * 再描画用
     */
    private Rectangle[] redrawRect;

    /**
     * ぷよデータ
     */
    private BlockPuyo[][] blockPuyo = new BlockPuyo[14][6];

    /**
     * フィーバーモード中に覚えておく古いデータ
     */
    private BlockPuyo[][] backupPuyo;

    /**
     * キーデータ
     */
    private int keyFlag;

    /**
     * 色数
     */
    private int colorNum;

    /**
     * 全消し表示のカウント
     */
    private int zenkesiCount;

    /**
     * 余っているスコア
     */
    private int stockScore;

    /**
     * スコアデータ
     */
    private int scoreValue;

    /**
     * 経過時間
     */
    private int timeCount = 0;

    /**
     * ツモデータ
     */
    private String tsumoData = "2223222*22232224";
    /**
     * 通常字の連鎖倍率
     */
    private int[] normalRensa = {
            0, 8, 16, 22, 34, 67, 11, 167, 223, 280, 350, 421
    };
    /**
     * フィーバー時の連鎖倍率
     */
    private int[] feverRensa = {
            0, 6, 12, 15, 20, 33, 55, 83, 111, 167, 195, 201, 239, 279, 307, 335
    };

    /**
     * ツモ順番
     */
    private int tsumoIndex;

    /**
     * L字型のフラグ
     */
    private boolean lTypeFlag;

    /**
     * フィーバーで消した時に種通りかどうか
     */
    private int feverRensaOut;

    /**
     * おじゃまぷよが落ちた後に揺れる
     */
    private int ojamaSwingNum;

    /**
     * 連鎖で声を出す
     */
    private AudioClip[] rensaVoice;

    public static enum GameMode {
        FallMode,    // 自由落下中
        FadeCheck,    // 消えるのをチェックするモード
        MoveMode,    // ぷよが移動するモード
        PatternFallMode,    // おじゃまぷよやフィーバー種が落ちるモード
        NextMode,    // 次ぷよが来るモード
        FeverStartMode,    // フィーバーが開始する
        FeverEndMode,    // フィーバーが終わる
        FeverBombMode,    // フィーバー中でぷよがはじける
        GameOverMode,    // ゲームオーバー
        ReadyGoMode,    // 開始
    }

    ;
    private GameMode gameMode;

    /**
     * モードの進み具合を保持
     */
    private int modeNum;
    /**
     * スクリーンイメージ
     */
    private BufferedImage scrImage;

    /**
     * 敵のぷよデータ
     */
    private PuyoData enemyPuyoData;

    /**
     * 自分のおじゃまバー
     */
    private OjamaBar normalOjama;

    /**
     * フィーバー時のおじゃまバー
     */
    private OjamaBar feverOjama;

    /**
     * 敵のおじゃまバー
     */
    private OjamaBar enemyOjama;

    /**
     * 移動用のぷよ
     */
    private MovePuyo movePuyo;

    /**
     * 次のぷよ
     */
    private MovePuyo[] nextPuyo;

    /**
     * 描画の座標
     */
    private int drawX;

    /**
     * ランダムデータ
     */
    private Random randomData;

    /**
     * 連鎖数
     */
    private int rensaNum;

    /**
     * 思考ルーチン用ブロックデータ
     */
    private BlockData thinkBlkData;

    /**
     * 効果音のために最後に落ちたおじゃまぷよ
     */
    private int lastDropOjama;

    /**
     * フィーバーモードかどうかのフラグ
     */
    private boolean feverModeFlag;

    /**
     * ボイスデータ
     */
    private VoiceDataIF voiceData;

    /**
     * フィーバー
     */
    private FeverGage feverGage;

    /**
     * フィーバーで飛んでいくぷよのデータ
     *
     * @author hkoba
     */
    class BombPuyo {
        int width;
        int cx;
        int cy;
        int srcX;
        int addX;
        int addY;

        BombPuyo(int bx, int by, int cl) {
            cx = bx * 31 + 15 + 6;
            cy = (by - 2) * 28 + 14;
            width = 16;
            srcX = cl * 32 + 160;
            if (cl == 6) {
                srcX = 0;
            }
            addX = feverGage.getRandomInt(13) - 6 - 6 + bx * 2;
            addY = -feverGage.getRandomInt(10);
        }

        boolean turnNext() {
            cy += addY;
            addY += 2;
            cx += addX;
            if (addX < 0) {
                addX--;
            } else {
                addX++;
            }
            if (width < 32) {
                width += 2;
            }
            if (cx + width <= 0 || cx - width >= 198
                    || cy - width >= 12 * 28) {
                return false;
            }
            return true;
        }

        void draw(Graphics2D g) {
            g.drawImage(BlockPuyo.getPuyoImage(), cx - width, cy - width, cx + width, cy + width,
                    srcX, 288, srcX + 32, 320, null);
        }
    }

    /**
     * フィーバー時で消えていくデータ
     */
    private ArrayList<BombPuyo> bombData;

    /**
     * 消した時の消えていく状態
     *
     * @author kobayah
     */
    class FadePuyo {
        int px;
        int py;
        int addX;
        int addY;
        int puyoColor;
        int width;

        final int[] startAddX = {-26, -23, -20, -17, -13, 7, 13, 17, 20, 23, 26};

        /**
         * @param x
         * @param y
         * @param cl
         * @param ax1
         * @param ax2
         */
        FadePuyo(int x, int y, int cl, int ix) {
            //px = x + (int)(Math.random() * 24 - 12);
            //py = y + (int)(Math.random() * 14 - 5);
            px = x;
            py = y;
            puyoColor = cl;
            width = 4;
            int rnd1 = startAddX[ix + 5];
            int rnd2 = startAddX[ix + 5];
            if (ix < 0) {
                rnd2 = startAddX[ix + 6] - 1;
                if (ix == -1) {
                    rnd2 = -rnd2;
                }
            } else if (ix > 0) {
                rnd1 = startAddX[ix + 4] + 1;
            } else {
                rnd1 = -rnd2;
            }
            addX = (int) (Math.random() * (rnd2 - rnd1 + 1) + rnd1);
            addY = -10 - (int) (Math.random() * 5);
        }

        /**
         * 続きがあれば true
         *
         * @return
         */
        boolean turnNext() {
            if (width > 40) {
                return false;
            }
            px += (addX / 2);
            py += (addY / 2);
            width += 2;
            if (addY < 0) {
                addY++;
            }
            if (addX < 0) {
                addX++;
            } else if (addX > 0) {
                addX--;
            }
            return true;
        }

        void draw(Graphics2D g) {
            int wd = width;
            if (wd > 32) {
                wd = 32;
            }
            int ht = wd / 3;
            g.drawImage(EffectImage.getPuyoFadeImage(puyoColor), px - wd, py - ht, px + wd, py + ht,
                    0, 0, 32, 32, null);
        }
    }

    private ArrayList<FadePuyo> fadePuyoData = new ArrayList<FadePuyo>();

    /**
     * 連鎖文字を表示する
     */
    private int drawRensaNum;
    /**
     * 連鎖文字を表示する位置
     */
    private Point drawRensaPt;

    public PuyoData(int sx, long seed) {
        // あらかじめ読み込ませる
        EffectImage.getPuyoFadeImage(1);

        feverGage = new FeverGage(sx > 100, seed);
        normalOjama = new OjamaBar(feverGage);
        feverOjama = new OjamaBar(normalOjama);
        normalOjama.setDrawPoint(sx, 66);
        feverOjama.setHideFlag(true);

        scrImage = new BufferedImage(198, 336, BufferedImage.TYPE_INT_RGB);
        feverBgImage = new BufferedImage(198, 336, BufferedImage.TYPE_INT_RGB);
        nextPuyoImage = new BufferedImage(64, 128, BufferedImage.TYPE_INT_RGB);
        drawX = sx;
        randomData = new Random(seed);
        redrawRect = new Rectangle[6];
        movePuyo = new MovePuyo();
        nextPuyo = new MovePuyo[2];
        nextPuyo[0] = new MovePuyo();
        nextPuyo[1] = new MovePuyo();
        makeThinkBlock();
        makeNextPuyo(3);
        makeNextPuyo(3);
        gameMode = GameMode.ReadyGoMode;
        redrawAll();
        makeNextImage();
    }

    public void redrawAll() {
        //redrawRect[0] = new Rectangle(0, 0, 6*32, 28*14);
        redrawRect[0] = new Rectangle(0, 0, 6 * 32, 28 * 12);
        for (int i = 1; i < 6; i++) {
            redrawRect[i] = redrawRect[0];
        }
    }

    /**
     * 再描画する
     *
     * @param bx ブロック座標
     * @param y  Y座標
     * @param ht 高さ
     */
    public void redraw(int bx, int y, int ht) {
        if (bx < 0 || bx > 5) {
            return;
        }
        if (y >= 28 * 12 || y + ht <= 0) {
            return;
        }
        Rectangle rc = redrawRect[bx];
        if (rc == null) {
            redrawRect[bx] = new Rectangle(0, y, 32 * 6, ht);
        } else {
            if (y < rc.y) {
                rc.height += (rc.y - y);
                rc.y = y;
            }
            if (y + ht > rc.y + rc.height) {
                rc.height = y + ht - rc.y;
            }
        }
    }

    public BlockPuyo getBlock(int bx, int by) {
        if (bx < 0 || bx > 5 || by < 0 || by > 13) {
            return null;
        }
        return blockPuyo[by][bx];
    }

    /**
     * ぷよを置く
     *
     * @param blk
     */
    public void addPuyo(BlockPuyo blk) {
        int bx = blk.getBlockX();
        for (int y = 13; y >= 0; y--) {
            if (blockPuyo[y][bx] == null) {
                blk.setBlockY(y);
                blockPuyo[y][bx] = blk;
                break;
            }
        }
    }

    /**
     * マージンタイム経過後のおじゃまレート
     */
    static final int[] marginOjamaRate = {
            90, 60, 45, 30, 22, 15, 10, 7, 5, 3, 2, 1
    };

    /**
     * ターンを経過させる
     *
     * @return
     */
    public GameMode turnNext() {
        transView = null;

        normalOjama.turnNext();
        feverOjama.turnNext();
        feverGage.turnNext();

        // 消える効果のアニメーション
        for (int i = 0; i < fadePuyoData.size(); i++) {
            FadePuyo dt = fadePuyoData.get(i);
            if (!dt.turnNext()) {
                fadePuyoData.remove(i);
                i--;
            }
        }

        if (gameMode == GameMode.FallMode) {
            if (!turnFall()) {
                gameMode = GameMode.FadeCheck;
                modeNum = 0;
            }
        } else if (gameMode == GameMode.PatternFallMode) {
            if (modeNum == 0) {
                if (!turnFall()) {
                    modeNum = 1;
                    // 効果音
                    if (lastDropOjama >= 18) {
                        PlaySE.playSE(PlaySE.OJAMA_DROP_L);
                        ojamaSwingNum = 18;
                    } else if (lastDropOjama > 0) {
                        PlaySE.playSE(PlaySE.OJAMA_DROP_S);
                        if (lastDropOjama >= 6) {
                            ojamaSwingNum = 10;
                        }
                    }
                }
            } else {
                modeNum++;
                if (modeNum >= 10) {
                    chainOjamaCheck();
                    makeNextPuyo(colorNum);
                    if (voiceData != null) {
                        if (lastDropOjama >= 18) {
                            AudioClip clp = voiceData.getVoice(VoiceDataIF.OJAMA_L);
                            if (clp != null) {
                                clp.play();
                            }
                        } else if (lastDropOjama >= 6) {
                            AudioClip clp = voiceData.getVoice(VoiceDataIF.OJAMA_S);
                            if (clp != null) {
                                clp.play();
                            }
                        }
                    }
                    lastDropOjama = 0;
                }
                turnFall();
            }
        } else if (gameMode == GameMode.FadeCheck) {
            turnFadeCheck();
        } else if (gameMode == GameMode.MoveMode) {
            // 移動
            turnMoveMode();
        } else if (gameMode == GameMode.NextMode) {
            // つぎのぷよ
            turnNextMode();
        } else if (gameMode == GameMode.FeverStartMode) {
            // フィーバー開始
            modeNum++;
            makeFeverTransform();
            normalOjama.setDrawPoint(drawX + modeNum / 5, 66 - modeNum);
            feverOjama.setHideFlag(false);
            if (drawX > 100) {
                // 右側
                feverOjama.setDrawPoint(drawX + 240 - modeNum * 12, 66);
            } else {
                feverOjama.setDrawPoint(drawX + modeNum * 12 - 240, 66);
            }
            if (modeNum == 10) {
                // 入れ替え
                feverModeFlag = true;
                backupPuyo = blockPuyo;
                blockPuyo = new BlockPuyo[14][6];
                makeFeverBg();
                this.makeThinkBlock();
                redrawAll();
                PlaySE.playSE(PlaySE.FEVER_START);
            } else if (modeNum >= 20) {
                feverGage.setFeverMode(true);
                modeNum = 0;
                checkTurnBefore();
            }
        } else if (gameMode == GameMode.FeverEndMode) {
            // フィーバー終了
            modeNum++;
            makeFeverTransform();
            normalOjama.setDrawPoint(drawX + 4 - modeNum / 5, 46 + modeNum);
            if (drawX > 100) {
                // 右側
                feverOjama.setDrawPoint(drawX + modeNum * 12, 66);
            } else {
                feverOjama.setDrawPoint(drawX - modeNum * 12, 66);
            }
            if (modeNum == 10) {
                // 入れ替え
                feverModeFlag = false;
                blockPuyo = backupPuyo;
                redrawAll();
                this.makeThinkBlock();
                feverGage.setFeverMode(false);
            } else if (modeNum >= 20) {
                modeNum = 0;
                feverOjama.setHideFlag(true);
                checkTurnBefore();
            }
        } else if (gameMode == GameMode.FeverBombMode) {
            turnFeverBombMode();
        } else if (gameMode == GameMode.GameOverMode) {
            turnGameOverMode();
        } else if (gameMode == GameMode.ReadyGoMode) {
            modeNum++;
            if (modeNum > 0) {

            }
        } else {
            turnBlockPuyo();
        }
        timeCount++;
        if ((timeCount & 0x63) == 0 || (timeCount & 0x20) == 0x20) {
            redraw(2, 0, 30);
            redraw(3, 0, 30);
        }
        if (drawRensaNum > 0) {
            drawRensaNum--;
            if (drawRensaNum > 10) {
                drawRensaPt.y -= 4;
            }
        }
        if (ojamaSwingNum > 0 && transView == null) {
            // おじゃまが落ちてきて揺れている
            double wd = 0.03;
            if (ojamaSwingNum < 6) {
                wd = 0.02;
            }
            transView = AffineTransform.getTranslateInstance(drawX + 99, 104 + 12 * 28);
            double rad = Math.sin(ojamaSwingNum * Math.PI / 6) * wd;
            transView.rotate(rad);
            transView.translate(-99, -12 * 28);
            ojamaSwingNum--;
        }
        return gameMode;
    }

    /**
     * フィーバー回転中の変換座標
     */
    private void makeFeverTransform() {
        double wd = -Math.cos(Math.PI * modeNum / 20) * 99;
        double ht = -Math.sin(Math.PI * modeNum / 20) * 10;
        transView = AffineTransform.getTranslateInstance(drawX + 99, 104 + 12 * 14);
        transView.scale(Math.abs(wd) / 99, 1.0);
        if (wd != 0) {
            transView.shear(0, ht / wd);
        }
        transView.translate(-99, -12 * 14);
    }

    /**
     * 通常の表示ではない時に使用する
     */
    private AffineTransform transView;

    /**
     * 勝利フラグ
     */
    private boolean winnerFlag;

    /**
     * リプレイデータ
     */
    private ReplayData replayData;

    /**
     * キー保存情報
     */
    private KeyRecord keyRecord = new KeyRecord();

    /**
     * ゲームオーバー
     */
    private void turnGameOverMode() {
        modeNum++;
        if (modeNum == 50) {
            // ちょっとあとに
            if (voiceData != null && voiceData.getVoice(VoiceDataIF.LOSE_VOICE) != null) {
                voiceData.getVoice(VoiceDataIF.LOSE_VOICE).play();
            }
        }
        transView = AffineTransform.getTranslateInstance(drawX + 99, 104 + modeNum * 10 + 100);
        transView.rotate(Math.PI * modeNum / 256);
        transView.translate(-99, -100);
    }

    /**
     * フィーバーで消して残りを吹き飛ばすモード
     */
    private void turnFeverBombMode() {
        modeNum++;
        for (int i = 0; i < bombData.size(); i++) {
            if (!bombData.get(i).turnNext()) {
                bombData.remove(i);
                i--;
            }
        }
        if (bombData.size() == 0) {
            checkTurnBefore();
        }
    }

    /**
     * おじゃまレートを返す
     *
     * @return
     */
    public int getOjamaRate() {
        if (timeCount >= 192 * 40) {
            int ix = (timeCount - 192 * 40) / (16 * 40);
            if (ix >= marginOjamaRate.length) {
                return 1;
            } else {
                return marginOjamaRate[ix];
            }
        } else {
            return 120;
        }
    }

    private void turnMoveMode() {
        int key = keyFlag;
        if (replayData != null) {
            key = replayData.nextKey();
        } else {
            keyRecord.recordKey(key);
        }
        if (!movePuyo.turnNext(this, key)) {
            makeThinkBlock();
            gameMode = GameMode.FallMode;
            enemyOjama = enemyPuyoData.getOjamaBar();
        }
        turnBlockPuyo();
    }

    /**
     * つぎのぷよを作成する
     *
     * @param cl
     */
    private void makeNextPuyo(int cl) {
        BlockData blkdt = getBlockData();
        if (blkdt.getBlock(2, 2) > 0 || blkdt.getBlock(3, 2) > 0) {
            feverGage.setGameOver();
            gameMode = GameMode.GameOverMode;
            modeNum = 0;
            return;
        }

        MovePuyo bak = movePuyo;
        movePuyo = nextPuyo[0];
        nextPuyo[0] = nextPuyo[1];
        nextPuyo[1] = bak;
        int[] puyo = new int[4];
        rensaNum = 0;

        int cl1 = randomData.nextInt(cl) + 1;
        int cl2 = randomData.nextInt(cl) + 1;
        switch (tsumoData.charAt(tsumoIndex)) {
            case '*':    // でかぷよ
                puyo[0] = puyo[1] = puyo[2] = puyo[3] = cl1;
                lTypeFlag = !lTypeFlag;
                break;
            case '3':    // Ｌ字
                if (lTypeFlag) {
                    puyo[0] = cl2;
                    puyo[1] = puyo[3] = cl1;
                } else {
                    puyo[0] = puyo[1] = cl1;
                    puyo[3] = cl2;
                }
                break;
            case '4':    // 2組4個ぷよ
                while (cl1 == cl2) {
                    cl2 = feverGage.getRandomInt(colorNum) + 1;
                }
                puyo[0] = puyo[1] = cl1;
                puyo[2] = puyo[3] = cl2;
                break;
            default:    // 2個ぷよ
                puyo[0] = cl1;
                puyo[1] = cl2;
                break;
        }
        tsumoIndex = (tsumoIndex + 1) % tsumoData.length();
        bak.setPuyo(puyo, cl);
        gameMode = GameMode.NextMode;
        modeNum = 15;
    }

    private void makeNextImage() {
        // 次ぷよのイメージを作成する
        Graphics2D g = (Graphics2D) nextPuyoImage.getGraphics();
        g.setColor(new Color(74, 65, 131));
        g.fillRect(0, 0, 64, 128);
        MovePuyo mv1 = nextPuyo[0];
        MovePuyo mv2 = nextPuyo[1];
        int sx1 = -31 * 2 + 10;
        int sy1 = 5;
        int sx2 = sx1 + 20;
        int sy2 = 64;
        if (drawX > 100) {
            // 右側
            sx1 += 16;
            sx2 = sx1 - 21;
        }
        if (gameMode == GameMode.NextMode) {
            if (modeNum > 7) {
                sy1 -= (15 - modeNum) * 7;
                sy2 -= (15 - modeNum) * 7;
                if (drawX > 100) {
                    // 右側
                    sx2 += (15 - modeNum) * 2;
                } else {
                    sx2 -= (15 - modeNum) * 2;
                }
                mv1 = movePuyo;
                mv2 = nextPuyo[0];
            } else {
                sy2 += modeNum * 3;
                if (drawX > 100) {
                    sx2 -= modeNum * 5;
                } else {
                    sx2 += modeNum * 5;
                }
            }
        }
        if (mv1.isWideSize()) {
            sx1 -= 15;
        }
        if (mv2.isWideSize()) {
            sx2 -= 15;
        }
        mv1.draw(g, sx1, sy1);
        mv2.draw(g, sx2, sy2);
        g.dispose();
    }

    private void turnNextMode() {
        modeNum--;
        if (modeNum == 0) {
            gameMode = GameMode.MoveMode;
        }
        makeNextImage();
        turnBlockPuyo();
    }

    private boolean turnFall() {
        boolean modeflg = false;
        turnBlockPuyo();
        for (int y = 13; y >= 0; y--) {
            for (int x = 0; x < 6; x++) {
                if (blockPuyo[y][x] != null) {
                    blockPuyo[y][x].chainCheck(this);
                    modeflg |= blockPuyo[y][x].isFall();
                }
            }
        }
        return modeflg;
    }

    /**
     * でかいおじゃまぷよをチェックする
     */
    private void chainOjamaCheck() {
        // 最上段を超えてはつなげない
        for (int y = 13; y > 2; y--) {
            for (int x = 0; x < 6; x++) {
                BlockPuyo blk = blockPuyo[y][x];
                if (blk instanceof OjamaPuyo) {
                    ((OjamaPuyo) blk).checkOjamaChain(this);
                }
            }
        }
    }

    private void turnFadeCheck() {
        modeNum++;
        int edmode = feverModeFlag ? 30 : 40;
        if (modeNum < 10) {
            // しばらくは落とす処理を継続
            turnFall();
        } else if (modeNum == 10) {
            // 消えるのをチェックする
            if (!checkChain()) {
                // ターン終了！！！
                checkTurnEnd();
            }
        } else if (modeNum < edmode) {
            // 消えているので、少しアニメーションする
            // フィーバーモードの状態によって、待ち時間が変わる
            // ここでおじゃまぷよも飛ばしている
            turnBlockPuyo();
            if (modeNum == 20) {
                // 連鎖の効果音
                PlaySE.playRensa(rensaNum);
                // このくらい？
                // おじゃまを飛ばす
                sendOjamaPuyo();
            }
        } else {
            // また落とす
            gameMode = GameMode.FallMode;
            for (int x = 0; x < 6; x++) {
                for (int y = 13; y >= 0; y--) {
                    BlockPuyo blk = getBlock(x, y);
                    if (blk == null) {
                        break;
                    }
                    if (blk.isFade()) {
                        blockPuyo[y][x] = null;
                        // 消す
                        for (int yy = y - 1; yy >= 0; yy--) {
                            blk = getBlock(x, yy);
                            if (blk == null) {
                                break;
                            }
                            if (blk instanceof OjamaPuyo) {
                                ((OjamaPuyo) blk).splitOjama(this);
                            }
                            blk.setDownSpeed(0);
                            blk.setBlockY(yy + 1);
                            blockPuyo[yy + 1][x] = blk;
                            blockPuyo[yy][x] = null;
                        }
                        // もう一度
                        y++;
                    }
                }
            }
            makeThinkBlock();
            modeNum = 0;
        }
    }

    private void turnBlockPuyo() {
        for (int y = 13; y >= 0; y--) {
            for (int x = 0; x < 6; x++) {
                if (blockPuyo[y][x] != null) {
                    blockPuyo[y][x].turnNext(this);
                }
            }
        }
    }

    private boolean checkChain() {
        boolean ret = false;
        ArrayList<Point> ptlst = new ArrayList<Point>();
        chainOjamaCheck();
        for (int x = 0; x < 6; x++) {
            for (int y = 13; y >= 2; y--) {
                BlockPuyo blk = getBlock(x, y);
                if (blk == null) {
                    break;
                }
                if (blk.isFade() || blk.getPuyoColor() == 6) {
                    continue;
                }
                ptlst.clear();
                checkNest(ptlst, x, y, blk.getPuyoColor());
                if (ptlst.size() >= 4) {
                    // 消える
                    for (Point pt : ptlst) {
                        getBlock(pt.x, pt.y).setFade(this);
                        // となりのおじゃまも消す
                        blk = getBlock(pt.x - 1, pt.y);
                        if (blk != null && blk.getPuyoColor() == 6) {
                            blk.setFade(this);
                        }
                        blk = getBlock(pt.x + 1, pt.y);
                        if (blk != null && blk.getPuyoColor() == 6) {
                            blk.setFade(this);
                        }
                        blk = getBlock(pt.x, pt.y - 1);
                        if (blk != null && blk.getPuyoColor() == 6) {
                            blk.setFade(this);
                        }
                        blk = getBlock(pt.x, pt.y + 1);
                        if (blk != null && blk.getPuyoColor() == 6) {
                            blk.setFade(this);
                        }
                    }
                    ret = true;
                }
            }
        }
        if (ret) {
            if (rensaNum == 0) {
                // はじめての連鎖なのでチェックする
                ArrayList<int[]> dtlst = new ArrayList<int[]>();
                this.getBlockData().putPuyoData(new int[0], new int[0], dtlst);
                int[] num = new int[dtlst.size()];
                for (int i = 0; i < dtlst.size(); i++) {
                    int[] dt = dtlst.get(i);
                    for (int j = 0; j < dt.length; j++) {
                        num[i] += dt[j];
                    }
                }
                if (voiceData != null) {
                    rensaVoice = voiceData.getRensaVoice(num);
                } else {
                    rensaVoice = new AudioClip[dtlst.size()];
                }
            }
            // ここで連鎖ボイス
            if (rensaNum < rensaVoice.length) {
                // 声が出る
                // 直前の声は消してしまう
                if (rensaNum > 0 && rensaVoice[rensaNum - 1] != null) {
                    rensaVoice[rensaNum - 1].stop();
                }
                if (rensaVoice[rensaNum] != null) {
                    rensaVoice[rensaNum].play();
                }
            }
            rensaNum++;
            if (feverModeFlag && rensaNum == rensaVoice.length) {
                // 連鎖終了
                feverRensaOut = feverGage.fixFeverRensa(rensaNum);
            }
        }
        return ret;
    }

    private void checkNest(ArrayList<Point> ptlst, int x, int y, int cl) {
        if (x < 0 || x > 5 || y < 2 || y > 13) {
            return;
        }
        BlockPuyo blk = getBlock(x, y);
        if (blk == null || blk.getPuyoColor() != cl) {
            return;
        }
        Point pt = new Point(x, y);
        if (ptlst.contains(pt)) {
            return;
        }
        ptlst.add(pt);
        checkNest(ptlst, x - 1, y, cl);
        checkNest(ptlst, x + 1, y, cl);
        checkNest(ptlst, x, y - 1, cl);
        checkNest(ptlst, x, y + 1, cl);
    }

    /**
     * ターン終了時にいろいろチェックする
     */
    private void checkTurnEnd() {
        enemyOjama.applyYokoku();
        modeNum = 0;
        BlockData blkdt = this.getBlockData();
        if (blkdt.getBlock(2, 2) > 0 || blkdt.getBlock(3, 2) > 0) {
            // ゲームオーバー
            feverGage.setGameOver();
            gameMode = GameMode.GameOverMode;
            return;
        }
        if (rensaNum > 0) {
            // 消した
            if (feverModeFlag) {
                if (feverGage.getFeverRestTime() > 0) {
                    gameMode = GameMode.FeverBombMode;
                    bombData = new ArrayList<BombPuyo>();
                    for (int x = 0; x < 6; x++) {
                        for (int y = 13; y >= 2; y--) {
                            BlockPuyo blk = getBlock(x, y);
                            if (blk == null) {
                                break;
                            }
                            bombData.add(new BombPuyo(x, y, blk.getPuyoColor()));
                        }
                    }
                } else {
                    // 戻る
                    gameMode = GameMode.FeverEndMode;
                }
                // ボイス
                if (voiceData != null) {
                    AudioClip vo = null;
                    if (feverRensaOut < 0) {
                        vo = voiceData.getVoice(VoiceDataIF.FEVER_FAIL);
                    } else if (feverRensaOut > 0) {
                        vo = voiceData.getVoice(VoiceDataIF.FEVER_SUCCESS);
                    }
                    if (vo != null) {
                        vo.play();
                    }
                }
            }
            if (this.getBlockData().getBlockNum(0) == 14 * 6) {
                // 全消し
                zenkesiCount = 40;
                PlaySE.playSE(PlaySE.ZENKESI);
                feverGage.zenkesiBonus();
                if (!feverModeFlag && feverGage.getFeverPoint() < 7) {
                    // 4連鎖の種を落とす
                    feverGage.dropFeverData(this, 4);
                    gameMode = GameMode.PatternFallMode;
                    return;
                }
            }
            if (!feverModeFlag) {
                if (feverGage.getFeverPoint() >= 7) {
                    // フィーバー突入
                    gameMode = GameMode.FeverStartMode;
                    if (voiceData != null) {
                        AudioClip clp = voiceData.getVoice(VoiceDataIF.FEVER_START);
                        if (clp != null) {
                            clp.play();
                        }
                    }
                } else {
                    makeNextPuyo(colorNum);
                }
            }
        } else if (feverModeFlag && feverGage.getFeverRestTime() == 0) {
            // フィーバー終了
            // 戻る
            gameMode = GameMode.FeverEndMode;
        } else {
            checkTurnBefore();
        }
    }

    /**
     * ターン開始前のチェック
     */
    private void checkTurnBefore() {
        if (rensaNum == 0) {
            if (getOjamaBar().getOjamaNum() > 0) {
                // おじゃまを落とすモード
                // おじゃまぷよを落とす
                lastDropOjama = getOjamaBar().dropOjama(this);
                makeThinkBlock();
                gameMode = GameMode.PatternFallMode;
                modeNum = 0;
                return;
            }
        } else if (feverModeFlag) {
            // フィーバーモード中で消した直後
            blockPuyo = new BlockPuyo[14][6];
            feverGage.dropFeverData(this);
            makeFeverBg();
            redrawAll();
            modeNum = 0;
            gameMode = GameMode.PatternFallMode;
            return;
        }
        makeNextPuyo(colorNum);
    }

    /**
     * 連鎖中におじゃまぷよを飛ばす
     */
    private void sendOjamaPuyo() {
        // 得点計算をする
        // まずは消えているぷよの数を取得する
        drawRensaNum = 20;
        Point startpt = null;
        int[] fadenum = new int[7];
        int fadeix = (int) (Math.random() * 6);
        for (int x = 0; x < 6; x++) {
            for (int y = 13; y >= 0; y--) {
                BlockPuyo blk = getBlock(x, y);
                if (blk == null) {
                    break;
                }
                if (blk.isFade()) {
                    fadenum[blk.getPuyoColor()]++;
                    if (blk.getPuyoColor() != BlockPuyo.PUYO_OJAMA) {
                        Point pt = new Point(x * 31 + 20 + drawX, (y - 2) * 28 + 118);
                        if (startpt == null || pt.y > startpt.y) {
                            startpt = pt;
                        }
                        // 消える効果
                        if (optPuyoFadeFlag) {
                            int dst1 = fadeix % 6;
                            int dst2 = (fadeix + 3) % 6;
                            fadeix++;
                            fadePuyoData.add(new FadePuyo(pt.x, pt.y, blk.getPuyoColor(), dst1 - x));
                            fadePuyoData.add(new FadePuyo(pt.x, pt.y, blk.getPuyoColor(), dst2 - x));
                        }
                    }
                }
            }
        }
        drawRensaPt = new Point(startpt.x - 20, startpt.y - 100);
        if (drawRensaPt.y < 80) {
            startpt.y = 80;
        }
        if (drawRensaPt.x > drawX + 80) {
            drawRensaPt.x = drawX + 80;
        }

        int addsc = countScore(fadenum, rensaNum, feverModeFlag);
        stockScore += addsc;
        int sendoja = 1;
        int ojamaRate = getOjamaRate();
        if (stockScore < ojamaRate) {
            // 最低でも1つ飛ばす
            stockScore = 0;
        } else {
            sendoja = stockScore / ojamaRate;
            stockScore %= ojamaRate;
        }
        scoreValue += addsc;

        getOjamaBar().sendOjamaPuyo(enemyOjama, sendoja, startpt);
    }

    public int countScore(int[] fadenum, int rensaNum, boolean feverModeFlag) {
        int clnum = 0;    // 複色ボーナス用
        int allnum = 0;    // 消したぷよの個数
        int samenum = 0;    // 同色ボーナス用
        for (int i = 1; i < 7; i++) {
            allnum += fadenum[i];
            if (i < 6 && fadenum[i] > 0) {
                clnum++;
                samenum += (fadenum[i] - 4);
            }
        }
        int[] rensabai = normalRensa;
        if (feverModeFlag) {
            rensabai = feverRensa;
        }
        int bai = samenum;
        if (rensaNum >= rensabai.length) {
            bai += rensabai[rensabai.length - 1];
        } else {
            bai += rensabai[rensaNum - 1];
        }
        // 複色ボーナスを足す
        switch (clnum) {
            case 2:
                bai += 2;
                break;
            case 3:
                bai += 6;
            case 4:
                bai += 10;
                break;
            case 5:
                bai += 18;
                break;
        }
        // スコアを計算する
        if (bai == 0) {
            bai = 1;
        }
        return allnum * bai * 10;
    }

    /**
     * メイン画面や背景を描画する
     *
     * @param gg
     */
    public void drawBack(Graphics2D gg) {
        Graphics2D g = (Graphics2D) scrImage.getGraphics();
        if (gameMode == GameMode.FeverBombMode) {
            // 特別にすべて再描画
            g.drawImage(feverBgImage, 0, 0, null);
            for (BombPuyo dt : bombData) {
                dt.draw(g);
            }
        } else {
            g.translate(6, 0);
            int sx = 7;
            if (drawX > 100) {
                sx = 207;
            }
            for (int x = 0; x < 6; x++) {
                Rectangle rc = redrawRect[x];
                if (rc == null) {
                    continue;
                }
                int wd = BlockPuyo.ICON_WIDTH;
                int xx = x * wd;
                int ngx = -1;
                if (x == 0) {
                    xx -= 6;
                    wd += 6;
                } else if (x == 2 || x == 3) {
                    // 真ん中
                    if (!feverModeFlag && rc.y < 30) {
                        rc.height += rc.y;
                        rc.y = 0;
                        //
                        int num = (timeCount >> 2) & 31;
                        if (num < 8) {
                            ngx = num * 32 + 33;
                        } else {
                            ngx = 1;
                        }
                    }
                } else if (x == 5) {
                    wd += 6;
                }
                if (feverModeFlag) {
                    g.drawImage(feverBgImage, xx, rc.y, xx + wd, rc.y + rc.height,
                            xx + 6, rc.y, xx + 6 + wd, rc.y + rc.height, null);
                } else if (bgImage != null) {
                    g.drawImage(bgImage, xx, rc.y, xx + wd, rc.y + rc.height,
                            sx + xx, rc.y, sx + xx + wd, rc.y + rc.height, null);
                } else {
                    g.clearRect(xx, rc.y, wd, rc.height);
                }
                // Xを書く
                if (ngx >= 0) {
                    g.drawImage(BlockPuyo.getPuyoImage(), xx, 0, xx + 31, 30,
                            ngx, 321, ngx + 31, 351, null);
                }
            }
            for (int x = 0; x < 6; x++) {
                Rectangle rc = redrawRect[x];
                if (rc == null) {
                    continue;
                }
                //g.clipRect(rc.x, rc.y, rc.width, rc.height);
                for (int y = 13; y >= 0; y--) {
                    if (blockPuyo[y][x] != null) {
                        blockPuyo[y][x].draw(g, rc, this);
                    }
                }
            }
        }
        g.dispose();

        // 次ぷよを書く
        int nx1 = 253;
        // y = 72
        int nx2 = nx1 + 26;
        int sx2 = 26;
        if (drawX > 100) {
            nx1 = 323;
            nx2 = nx1;
            sx2 = 0;
        }
        gg.drawImage(nextPuyoImage, nx1, 72, nx1 + 64, 152, 0, 0, 64, 80, null);
        gg.drawImage(nextPuyoImage, nx2, 152, nx2 + 38, 200, sx2, 80, sx2 + 38, 128, null);

        // フィーバーゲージを描く
        feverGage.drawBack(gg);

        // フレームを描く
        if (drawX > 100) {
            gg.drawImage(frameImage, 320, 0, 640, 480, 320, 1, 640, 481, null);
        } else {
            gg.drawImage(frameImage, 0, 0, 320, 480, 0, 1, 320, 481, null);
        }

        if (gameMode == GameMode.GameOverMode && !winnerFlag) {
            // ゲームオーバー
            BufferedImage loseimg = ImageData.getImage(ImageData.LOSE_IMAGE);
            double bai = Math.sin(modeNum * Math.PI / 80) * 0.3 + 1.0;
            int wd = (int) (loseimg.getWidth() * bai) / 2;
            int ht = (int) (loseimg.getHeight() * bai) / 2;
            gg.drawImage(loseimg, drawX + 99 - wd, 190 - ht, drawX + 99 + wd, 190 + ht,
                    0, 0, loseimg.getWidth(), loseimg.getHeight(), null);
        }

        if (transView != null) {
            gg.drawImage(scrImage, transView, null);
        } else {
            gg.drawImage(scrImage, drawX, 104, null);
        }
        if (feverModeFlag && gameMode != GameMode.GameOverMode) {
            // フィーバーの残り時間を描画する
            int sec = feverGage.getFeverRestTime() / 40;
            DigitImage.drawBig(gg, drawX + 79, 106, sec);
        }

        if (gameMode == GameMode.MoveMode) {
            movePuyo.draw(gg, drawX + 6, 104 - 56);
        }
        normalOjama.drawBack(gg);
        feverOjama.drawBack(gg);

        redrawRect = new Rectangle[6];
    }

    /**
     * 手前に表示するエリアを描画する
     *
     * @param g
     */
    public void drawFore(Graphics2D g) {
        DigitImage.drawScore(g, scoreValue, drawX > 100 ? 2 : 1);
        for (FadePuyo dt : fadePuyoData) {
            dt.draw(g);
        }
        // 開始時の描画
        if (gameMode == GameMode.ReadyGoMode) {
            BufferedImage readyimg = ImageData.getImage(ImageData.READY_IMAGE);
            BufferedImage goimg = ImageData.getImage(ImageData.GO_IMAGE);
            g.translate(drawX - 1, 104);
            if (modeNum < 20) {
                // Readyを書く1
                g.drawImage(readyimg, 0, modeNum * 15 - 250, 200, modeNum * 15 - 170, 0, 0, readyimg.getWidth(), readyimg.getHeight(), null);
            } else if (modeNum < 30) {
                // Readyを書く2
                g.drawImage(readyimg, 0, 45 + (modeNum & 1) * 10, 200, 125 + (modeNum & 1) * 10, 0, 0, readyimg.getWidth(), readyimg.getHeight(), null);
            } else if (modeNum < 50) {
                // Readyを書く3
                g.drawImage(readyimg, 0, 50, 200, 130, 0, 0, readyimg.getWidth(), readyimg.getHeight(), null);
            } else if (modeNum < 65) {
                // Readyの文字が伸縮する
                int half = 100;
                if (modeNum < 60) {
                    half = 100 - (modeNum - 50) * 20;
                } else {
                    half = 100 - (70 - modeNum) * 20;
                }
                if (half < 0) {
                    half = -half;
                }
                g.drawImage(readyimg, 100 - half, 50, 100 + half, 130, 0, 0, readyimg.getWidth(), readyimg.getHeight(), null);
            } else if (modeNum < 70) {
                // goの文字がでかくなっていく
                int wd = (modeNum - 65) * 15;
                g.drawImage(goimg, 100 - wd, 50, 100 + wd, 130, 0, 0, 150, 80, null);
            } else if (modeNum < 90) {
                // GOの文字
                g.drawImage(goimg, 25, 50, null);
            } else if (modeNum < 100) {
                // GOの文字がでかくなる
                if (drawX < 100 && modeNum == 90) {
                    // SEを鳴らす
                    // 左側だけ
                    PlaySE.playSE(PlaySE.PLAY_START);
                }
                int wd = (modeNum - 90) * 15 + 75;
                int ht = (modeNum - 90) * 8 + 80;
                Composite bak = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (100 - modeNum) * 0.1f));
                g.drawImage(goimg, 100 - wd, 90 - ht, 100 + wd, 90 + ht, 0, 0, 150, 80, null);
                g.setComposite(bak);
            } else {
                // 終了
                gameMode = GameMode.FallMode;
                timeCount = 0;
                modeNum = 0;
            }
            g.translate(-drawX + 1, -104);
        }
        feverGage.drawFore(g);
        normalOjama.drawBeam(g);
        feverOjama.drawBeam(g);
        // 連鎖文字
        if (drawRensaNum > 0) {
            DigitImage.drawRensa(g, drawRensaPt.x, drawRensaPt.y, rensaNum);
        }
        // 全消し
        if (zenkesiCount > 0) {
            zenkesiCount--;
            BufferedImage zenimg = ImageData.getImage(ImageData.ZENKESI_IMAGE);
            if (zenkesiCount > 10 || (zenkesiCount & 1) == 0) {
                g.drawImage(zenimg, drawX + 35, 160, null);
            }
        }
        // フィーバースタート
        if (gameMode == GameMode.FeverStartMode) {
            BufferedImage fevimg = ImageData.getImage(ImageData.FEVER_IMAGE);
            double bai = Math.sin(Math.PI * modeNum / 25) * 0.3 + 1.0;
            int wd = (int) (fevimg.getWidth() * bai / 2);
            int ht = (int) (fevimg.getHeight() * bai / 2);
            int xx = drawX + 99;
            int yy = 220 - modeNum * 4;
            g.drawImage(fevimg, xx - wd, yy - ht, xx + wd, yy + ht,
                    0, 0, fevimg.getWidth(), fevimg.getHeight(), null);
        }
        // WINNER表示
        if (winnerFlag) {
            BufferedImage winimg = ImageData.getImage(ImageData.WIN_IMAGE);
            int dy = (int) (Math.sin(timeCount * Math.PI / 40) * 20);
            int wd = 80;
            int ht = winimg.getHeight() * 160 / winimg.getWidth();
            g.drawImage(winimg, drawX + 99 - wd, 240 - ht + (int) dy, drawX + 99 + wd, 240 + dy,
                    0, 0, winimg.getWidth(), winimg.getHeight(), null);
        }
    }

    /**
     * おじゃまぷよの星を書く
     *
     * @param g
     */
    public void drawStar(Graphics2D g) {
        normalOjama.drawStar(g);
        feverOjama.drawStar(g);
    }

    /**
     * 各X座標のトップのY座標を得る
     *
     * @return
     */
    public int[] getTopY() {
        int[] ret = new int[6];
        for (int x = 0; x < 6; x++) {
            ret[x] = 14;
            for (int y = 13; y >= 0; y--) {
                if (blockPuyo[y][x] == null) {
                    break;
                }
                ret[x] = y;
            }
        }
        return ret;
    }

    public void pushKey(int ky) {
        keyFlag |= ky;
    }

    public void releaseKey(int ky) {
        keyFlag &= (~ky);
    }

    public int getKeyFlag() {
        return keyFlag;
    }

    /**
     * 回転中かチェックする
     *
     * @return
     */
    public boolean isTurning() {
        if (gameMode == GameMode.MoveMode) {
            return movePuyo.isTurning();
        }
        return false;
    }

    /**
     * 色数を返す
     *
     * @return
     */
    public int getColorNum() {
        return colorNum;
    }

    /**
     * 思考ルーチン用のブロックを作成する
     */
    public void makeThinkBlock() {
        int[][] blk = new int[14][6];
        for (int y = 0; y < 14; y++) {
            for (int x = 0; x < 6; x++) {
                if (blockPuyo[y][x] != null) {
                    blk[y][x] = blockPuyo[y][x].getPuyoColor();
                }
            }
        }
        thinkBlkData = new BlockData(blk);
    }

    /**
     * リプレイ用のキー情報を返す
     *
     * @return
     */
    public KeyRecord getKeyRecord() {
        return keyRecord;
    }

    /**
     * BGを作成する
     */
    public static void initBgImage(Image frm) {
        frameImage = frm;

        List<URI> flst = new ArrayList<>();
        try {
            URI uri = AbstractVoiceData.class.getResource("/image").toURI();
            Path myPath;
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                myPath = fileSystem.getPath("/image");
            } else {
                myPath = Paths.get(uri);
            }
            Stream<Path> walk = Files.walk(myPath, 1);
            walk.forEach(v -> {
                String fname = v.getFileName().toString().toLowerCase();
                if (fname.startsWith("bg") && fname.endsWith(".png")) {
                    flst.add(v.toUri());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        /*
        File imgdir = new File("image");
        String[] flst = imgdir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toUpperCase().startsWith("BG") && name.toUpperCase().endsWith(".PNG");
            }
        });
        */
        if (flst.size() > 0) {
            int ix = (int) (Math.random() * flst.size());
            try {
                bgImage = Toolkit.getDefaultToolkit().getImage(flst.get(ix).toURL());
                MediaTracker tr = new MediaTracker(new Canvas());
                tr.addImage(bgImage, 0);
                try {
                    tr.waitForAll();
                } catch (InterruptedException e) {
                }
                // イメージを作り直す
                BufferedImage bufimg = new BufferedImage(bgImage.getWidth(null), bgImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = bufimg.createGraphics();
                g2.drawImage(bgImage, 0, 0, null);
                bgImage = bufimg;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * リプレイ用データを設定する
     *
     * @param data
     */
    public void setReplayData(ReplayData data) {
        replayData = data;
    }

    /**
     * 敵のデータを設定する
     *
     * @param puyo
     */
    public void setEnemyPuyo(PuyoData puyo) {
        enemyPuyoData = puyo;
        enemyOjama = puyo.getOjamaBar();
    }

    /**
     * 現在有効なおじゃまバー
     *
     * @return
     */
    public OjamaBar getOjamaBar() {
        if (feverGage.isFeverMode()) {
            return feverOjama;
        }
        return normalOjama;
    }

    /**
     * キャラ特性のデータを設定する
     *
     * @param tsumo
     * @param nor
     * @param fev
     */
    public void setCharaData(String tsumo, String nor, String fev) {
        tsumoData = tsumo;
        String[] dt = nor.split(",");
        normalRensa = new int[dt.length];
        for (int i = 0; i < dt.length; i++) {
            normalRensa[i] = Integer.parseInt(dt[i]);
        }
        dt = fev.split(",");
        feverRensa = new int[dt.length];
        for (int i = 0; i < dt.length; i++) {
            feverRensa[i] = Integer.parseInt(dt[i]);
        }
    }

    /**
     * 音声データを設定する
     *
     * @param vo
     */
    public void setVoiceData(VoiceDataIF vo) {
        voiceData = vo;
    }

    /**
     * 色数を設定する
     *
     * @param i
     */
    public void setColorNum(int cl) {
        colorNum = cl;
    }

    /**
     * フィーバーモード中かどうかを返す
     *
     * @return
     */
    public boolean isFeverMode() {
        return feverModeFlag;
    }

    /**
     * ゲームオーバーかどうかを返す
     *
     * @return
     */
    public boolean isGameOver() {
        return gameMode == GameMode.GameOverMode;
    }

    /**
     * ブロックデータを返す
     *
     * @return
     */
    public BlockData getBlockData() {
        return thinkBlkData;
    }

    /**
     * 移動データを返す
     *
     * @return
     */
    public MoveData getMoveData() {
        if (gameMode == GameMode.MoveMode) {
            return movePuyo.getMoveData();
        }
        return null;
    }

    /**
     * いろいろデータ
     *
     * @return
     */
    public EnvData getThinkEnv() {
        EnvData ret = new EnvData(getThinkNext(), getThinkOjama(), feverGage.getFeverPoint(), getThinkFeverRest());
        return ret;
    }

    private int getThinkFeverRest() {
        if (!feverGage.isFeverMode()) {
            return -1;
        }
        return feverGage.getFeverRestTime() / 4;
    }

    /**
     * 思考ルーチン用に予告ぷよの数を返す
     *
     * @return
     */
    private int[] getThinkOjama() {
        int[] ret;
        if (feverGage.isFeverMode()) {
            ret = new int[3];
            ret[0] = feverOjama.getOjamaNum();
            ret[1] = feverOjama.getYokokuNum();
            ret[2] = normalOjama.getOjamaNum() + normalOjama.getYokokuNum();
        } else {
            ret = new int[2];
            ret[0] = normalOjama.getOjamaNum();
            ret[1] = normalOjama.getYokokuNum();
        }
        return ret;
    }

    /**
     * 思考ルーチン用の次ぷよデータを返す
     *
     * @return
     */
    private MoveData[] getThinkNext() {
        MoveData[] ret = new MoveData[2];
        ret[0] = nextPuyo[0].getMoveData();
        ret[1] = nextPuyo[1].getMoveData();
        return ret;
    }

    /**
     * 勝利者に設定する
     */
    public void setWinner() {
        winnerFlag = true;
        if (voiceData != null) {
            AudioClip vo = voiceData.getVoice(VoiceDataIF.WINNER_VOICE);
            if (vo != null) {
                vo.play();
            }
        }
    }

    static Color[] puyoBgColor = {
            new Color(222, 0, 0),
            new Color(0, 227, 0),
            new Color(0, 65, 214),
            new Color(222, 211, 0),
            new Color(240, 0, 231)
    };
    static int[] bigPuyoPt = {
            263, 224,
            327, 224,
            391, 224,
            369, 290,
            431, 290,
    };

    /**
     * フィーバー時のBGイメージを作る
     */
    private void makeFeverBg() {
        int pnum = (int) (Math.random() * colorNum);
        Graphics2D g = (Graphics2D) feverBgImage.getGraphics();
        Color cl = puyoBgColor[pnum];
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 200, 12 * 28);
        int sx = bigPuyoPt[pnum * 2];
        int sy = bigPuyoPt[pnum * 2 + 1];
        g.drawImage(BlockPuyo.getPuyoImage(), 68, 136, 132, 200,
                sx, sy, sx + 64, sy + 64, null);
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRect(60, 6 * 28 - 40, 80, 80);

        Composite bak = g.getComposite();
        AlphaComposite cmp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
        g.setComposite(cmp);
        g.setColor(new Color(cl.getRed(), cl.getGreen(), cl.getBlue(), 96));
        g.fillRect(0, 0, 200, 12 * 28);
        double add = Math.random() * Math.PI;
        for (int rd = 0; rd < 12; rd++) {
            double rad = Math.PI * rd / 6 + add;
            int[] xp = new int[4];
            int[] yp = new int[4];
            for (int i = 0; i < 2; i++) {
                double sub = 0.2 - i * 0.1;
                xp[0] = (int) (Math.sin(rad - sub) * 25 + 100);
                xp[1] = (int) (Math.sin(rad - sub) * 220 + 100);
                xp[2] = (int) (Math.sin(rad + sub) * 220 + 100);
                xp[3] = (int) (Math.sin(rad + sub) * 25 + 100);
                yp[0] = (int) (Math.cos(rad - sub) * 25 + 168);
                yp[1] = (int) (Math.cos(rad - sub) * 220 + 168);
                yp[2] = (int) (Math.cos(rad + sub) * 220 + 168);
                yp[3] = (int) (Math.cos(rad + sub) * 25 + 168);
                g.fillPolygon(xp, yp, 4);
            }
        }
        g.fillOval(20, 6 * 28 - 80, 160, 160);
        g.fillOval(50, 6 * 28 - 50, 100, 100);
        g.setComposite(bak);
    }

    /**
     * 下へ早く落とした時のスコア
     */
    public void addDropScore() {
        scoreValue++;
    }

    /**
     * 消えるぷよリストから相手に送るおじゃまぷよの大体の数を計算する
     *
     * @param fadeList
     * @param startRensaNum 計算開始時の連鎖数
     * @param feverModeFlag
     * @param mySend        自分が敵に送る場合はtrue、敵から送られる場合はfalse
     */
    public int[] getSendOjamaList(ArrayList<int[]> fadeList,
                                  int startRensaNum, boolean feverModeFlag, boolean mySend) {
        int[] rtn = new int[fadeList.size()];
        int[] fadenum;
        int rensaNum = 0;

        for (int[] numAry : fadeList) {
            fadenum = new int[7];
            for (int i = 0; i < numAry.length; i++) {
                fadenum[i % 5 + 1] = numAry[i];
            }
            rtn[rensaNum++] = getSendOjama(fadenum, startRensaNum++,
                    feverModeFlag, mySend);
        }

        return rtn;
    }


    /**
     * 消えるぷよから相手に送るおじゃまぷよの大体の数を計算する
     *
     * @param fadenum
     * @param rensaNum      現在の連鎖数
     * @param feverModeFlag
     * @param mySend        自分が敵に送る場合はtrue、敵から送られる場合はfalse
     */
    public int getSendOjama(int[] fadenum, int rensaNum, boolean feverModeFlag,
                            boolean mySend) {
        PuyoData data;
        if (mySend) {
            data = this;
        } else {
            data = enemyPuyoData;
        }
        int score = data.countScore(fadenum, rensaNum, feverModeFlag);
        int ojamaRate = data.getOjamaRate();
        if (score < ojamaRate) {
            return 1;
        } else {
            return score / ojamaRate;
        }
    }
}
