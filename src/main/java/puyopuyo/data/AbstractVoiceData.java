package puyopuyo.data;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.stream.Stream;

public class AbstractVoiceData implements VoiceDataIF {

    /**
     * ボイスリスト
     */
    protected HashMap<Integer, AudioClip> voiceList;

    protected AbstractVoiceData(final String prefix) {
        voiceList = new HashMap<Integer, AudioClip>();
        try {
            URI uri = AbstractVoiceData.class.getResource("/voice").toURI();
            Path myPath;
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                myPath = fileSystem.getPath("/voice");
            } else {
                myPath = Paths.get(uri);
            }
            Stream<Path> walk = Files.walk(myPath, 1);
            walk.forEach(v -> {
                String fname = v.getFileName().toString().toLowerCase();
                if (fname.startsWith(prefix.toLowerCase()) && fname.endsWith(".wav")) {
                    // 対象
                    int ix = fname.indexOf(".wav");
                    int n = Integer.parseInt(fname.substring(ix - 2, ix));
                    try {
                        AudioClip clp = Applet.newAudioClip(v.toUri().toURL());
                        voiceList.put(n, clp);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        /*
        File[] flst = new File("voice").listFiles(
                new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        if (name.toLowerCase().startsWith(prefix.toLowerCase())) {
                            return name.toLowerCase().endsWith(".wav");
                        }
                        return false;
                    }
                });
        if (flst != null) {
            for (File fl : flst) {
                try {
                    String nm = fl.getName().toLowerCase();
                    int ix = nm.indexOf(".wav");
                    int n = Integer.parseInt(nm.substring(ix - 2, ix));
                    AudioClip clp = Applet.newAudioClip(fl.toURI().toURL());
                    voiceList.put(n, clp);
                } catch (NumberFormatException e) {
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        */
    }

    /**
     * 連鎖用のボイスを取得する
     */
    public AudioClip[] getRensaVoice(int[] rensa) {
        AudioClip[] ret = new AudioClip[rensa.length];
        String txt = getVoiceIndex(rensa.length).toUpperCase();
        for (int i = 0; i < ret.length; i++) {
            char ch = txt.charAt(i);
            int ix = ch - '0';
            if (ch == 'F' || (i > 0 && i == ret.length - 1)) {
                ix = getFinishIndex(i + 1, rensa[i]);
            } else if (ch >= 'A') {
                ix = ch - 'A' + 6;
            }
            ret[i] = getVoice(ix);
        }
        return ret;
    }

    /**
     * 連鎖数に応じた連鎖ボイス文字列を取得する
     *
     * @param num 連鎖数
     * @return
     */
    protected String getVoiceIndex(int num) {
        char[] ch = new char[num];
        for (int i = 0; i < num; i++) {
            char c = (char) ('0' + i);
            if (c > '5') {
                c = '5';
            }
            ch[i] = c;
        }
        return new String(ch);
    }

    /**
     * フィニッシュボイスのインデックスを取得する
     *
     * @param rensanum 連鎖数
     * @param clnum    消した数
     * @return
     */
    protected int getFinishIndex(int rensanum, int clnum) {
        int n = (rensanum - 1) / 2;
        if (n > 4) {
            n = 4;
        }
        return n + 6;
    }

    /**
     * ボイスを取得する
     */
    public AudioClip getVoice(int type) {
        return voiceList.get(type);
    }
}
