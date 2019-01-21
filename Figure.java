// Figure とそれを継承するクラス達
import java.util.HashMap;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import java.awt.*;
import java.*;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

// For sound
import javax.sound.sampled.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

// 画像を描画するクラスの雛形となるクラス
class Figure {
    // 位置に関するもの
    protected int x,y;
    // 画像データを格納する変数
    protected Image img;

    Figure() {
        this.x = 0; this.y = 0;
    }

    // 現在の座標を取得
    public int getX() { return this.x; }
    public int getY() { return this.y; }
    // setter
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    // x, yだけ画像を動かす
    public void move(int x, int y) {
        this.x += x; this.y += y;
    }
    // 画像の読み込みは渡されたファイル名をimgディレクトリ以下から探す
    public void setImg(String fname) {
        String path = new File("./img", fname).getPath();
        try {
            this.img = Toolkit.getDefaultToolkit().getImage(path);
        } catch (Exception e) {
            // 画像ファイルが存在しない。
            // 読み込み不可能なファイル形式である可能性がある。
            e.printStackTrace();
        }
    }
    public void draw(Graphics g) {
        // 設定された画像を描画する
        g.drawImage(img, this.x, this.y, null);
    }
}

// 鳩を定義するクラス
class PegeonClass extends Figure implements ActionListener {
    // *Effect が true のときに、エフェクトの文字を表示する
    private boolean changeEffect = false;
    private boolean beamEffect = false;

    // 進化、ビームのエフェクトの持続を管理する
    private Timer evolutionTimer;
    private Timer beamTimer;

    // 鳩の名前
    private String name;
    // 名前を表示するための Label
    private JLabel nameLabel;

    // 鳩の位置を決定する変数
    private double t;

    // 音声ファイル
    private soundThread crowsSound;
    private soundThread beamSound;
    // 鳩ビーム用画像
    private Image beam_img;

    private State state;

    //　各餌の摂取状態を管理するためのクラス
    public class State{
        // 餌をやった量を表示するかを決める。表示するときは true
        private visible = true;
        // 各餌の上げた回数
        private int java, food, report;
        // 各餌の進化するために必要な餌の数
        private final int javaLimit = 5, reportLimit = 4, foodLimit = 3;
        // 進化している場合は true
        private boolean isEvolved = false;
        State() {
            this.java = 0;
            this.food = 0;
            this.report = 0;
        }
        // draw function draw the state
        private void draw(Graphics g, int x, int y) {
            if ( visible ) {
                g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));
                g.drawString("Java:" + this.java, x, y);
                g.drawString("Food:" + this.food, x, y + 30);
                g.drawString("Report:" + this.report, x, y + 30 * 2);
            }
        }
    }
    public State getState() { return state; }
    // 餌番号を受け取って、受け取った番号に対応する変数に＋１する
    // *Limitを超えたら、進化させる。
    // すでに進化しているのなら、進化させない。
    public void setstate(int feednum) {
        // まず、食べた餌の量をインクリメントする
        if( feednum == 1 ) {
            this.state.java++;
        } else if( feednum == 2){
            this.state.report++;
        } else if( feednum == 3 ){
            this.state.food++;
        }
        if( !state.isEvolved ) {
            // 進化するかを判定
            if (this.state.java >= state.javaLimit) {
                setImg("pegeon_digital.png"); state.isEvolved = this.changeEffect = true;
            }
            if (this.state.food >= state.foodLimit) {
                setImg("pegeon_very-big.jpg"); state.isEvolved = this.changeEffect = true;
            }
            if (this.state.report >= state.reportLimit) {
                setImg("pegeon_mukimuki.jpg");  state.isEvolved = this.changeEffect = true;
            }
            if( state.isEvolved ){
                evolutionTimer = new Timer(800, this);
                evolutionTimer.start();
            }
        }
    }

    PegeonClass(int x, int y) {
        state = new State();
        this.setX(x);
        this.setY(y);
        this.setImg("pegeon_small.png");
        this.crowsSound = new soundThread("koke.wav"); // 鳩の鳴き声を探す必要あり
        this.beamSound = new soundThread("fm_shot4.wav");

        this.name = null;
        this.t = 0;

        // 鳩ビーム用画像の読み込み
        String path = new File("./img", "pegeon_very-big_beam.jpg").getPath();
        try {
            this.beam_img = Toolkit.getDefaultToolkit().getImage(path);
        } catch (Exception e) {
            // 画像ファイルが存在しない。
            // 読み込み不可能なファイル形式である可能性がある。
            e.printStackTrace();
        }
    }
    // setter
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    @Override
    public void draw(Graphics g) {
        if( this.beamEffect == true ) {
            drawPegeonbeam(g);
        } else {
            // System.out.println("State\n java: " + this.getState().java+ ", food: " + this.getState().food + ", report: " + this.getState().report );
            // 鳩を動かす
            int futureX = Math.abs((int)(400 * Math.cos(this.t) + 100));
            int futureY = Math.abs((int)(400 * Math.sin(this.t) + 50));

            // System.out.println( "x:" + this.getX() +  " y:" + this.getY());
            if( t > Math.PI * 2) { t = 0; }
            else { t += 0.005; }

            // 鳩が進む方向に鳩の頭を向ける
            if ( this.getX() < futureX ) {
                // 左右反転させて描画
                int width = img.getWidth(null);
                int height = img.getHeight(null);
                this.setX(futureX); this.setY(futureY);
                g.drawImage(img, this.getX(), this.getY(), -1 * width, height, null);
            } else {
                this.setX(futureX); this.setY(futureY);
                super.draw(g);
            }
            if( getName() != null ) {
                // もし鳩に名前がつけられていれば実行
                g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 32)); // set Font
                g.drawString(this.name, this.x + 50, this.y + 20); // draw pegeon name
            }
        }
        if( this.changeEffect == true ) {
            evolution(g);
        }

        state.draw(g, getX() + 50, getY() + 40);
    }
    // crow は鳩の鳴き声を鳴らします。
    public void crow() {
        Thread sound = new Thread(this.crowsSound);
        sound.start();
    }

    // 進化時のエフェクト
    private void evolution(Graphics g) {
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 120));
        g.drawString("EVOLUTION!!", 20, 100);
    }

    // 鳩ビームのエフェクト
    private void drawPegeonbeam(Graphics g) {
        // 鳩ビーム時のエフェクトを描画する
        // 鳩ビームの画像を描画
        g.drawImage(this.beam_img, 100, 200, null);

        // write string
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 80));
        g.drawString("（・⊝・）BEAM!!", 80, 250);
    }

    // 鳩ビーム開始用メソッド
    public void beam() {
        this.beamEffect = true;
        this.beamTimer = new Timer(1000, this);
        this.beamTimer.start();

        Thread sound = new Thread(this.beamSound);
        sound.start();
    }

    public void actionPerformed(ActionEvent e) {
        if( e.getSource().equals(beamTimer) ){
            // System.out.println("beamTimer off");
            this.beamEffect = false;
            this.beamTimer.stop();
        } else if ( e.getSource().equals(evolutionTimer) ) {
            // System.out.println("evolutionTimer off");
            this.changeEffect = false;
            this.evolutionTimer.stop();
        }
    }
}

// 鳩の餌のクラス
class PegeonFeedClass extends Figure {
    private int kind;
    PegeonFeedClass(int x, int y, int kind) {
        String feedName = "";
        this.setX(x); this.setY(y);
        this.kind = kind;
        if( kind == 1) feedName = "feed_java.jpg";
        if( kind == 2) feedName = "feed_report.jpg";
        if( kind == 3) feedName = "feed_food.jpg";

        this.setImg(feedName);
    }
}

// 背景を定義するクラス
class SceneClass extends Figure {
    SceneClass() {
        this.setX(0); this.setY(0);
        this.setImg("bg.jpg");
    }
}
class soundThread implements Runnable {
    File file;
    soundThread(String fname) {
        this.file = new File("./sound", fname);
    }
    @Override
    public void run() {
        AudioInputStream audioIn;
        Clip clip;
        try {
            audioIn = AudioSystem.getAudioInputStream(this.file);
            AudioFormat af = audioIn.getFormat();

            DataLine.Info dataLine = new DataLine.Info(Clip.class, af);

            clip = (Clip)AudioSystem.getLine(dataLine);

            clip.open(audioIn);
            clip.start();
            Thread.sleep(10000); // スレッドが止まると音の再生が止まってしまうので、スレッドをスリープさせる
            clip.close();
        } catch (Exception e) {
            // オーディオファイルが存在しない。
            // 読み込み不可能な形式である可能性がある。
            e.printStackTrace();
        }

    }
}
