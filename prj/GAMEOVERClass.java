import java.util.HashMap;
import java.util.Random;
import javax.swing.*;
import java.awt.*;
import java.*;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

class GAMEOVERClass extends JFrame implements KeyListener {
    public TitleClass titlewindow;

    public GAMEOVERClass(){
        JPanel p = new JPanel();
        JLabel label = new JLabel();
        ImageIcon icon = new ImageIcon(getClass().getResource("/img/GAMEOVER.jpg"));
        label.setIcon(icon);
        p.add(label);
        Container contentPane = getContentPane();
        contentPane.add(p, BorderLayout.CENTER);

        this.setBounds(150, 150, 680, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

        this.setTitle("Pegeon QUEST");
        addKeyListener(this);

        // エンド画面の音声再生
        Thread sound = new Thread(new soundThread("mottoissyoni.wav"));
        sound.start();
    }

    public void keyPressed(KeyEvent e){
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                this.setVisible(false);
		titlewindow = new TitleClass();
                break;
        }
        repaint();
    }

    public void keyReleased(KeyEvent e){

    }

    public void keyTyped(KeyEvent e){

    }

    public static void main(String[] args){
        new GAMEOVERClass();
    }
}
