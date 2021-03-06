import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;

// コマンド一覧を表示するウィンドウ
class CommandListWindowClass extends JFrame{
    CommandListWindowClass(int basex, int basey, int x, int y) {
        // ウィンドウの表示位置とサイズを指定
        this.setBounds(basex, basey, x, y);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // ウィンドウに載せる部品を登録
        //HTMLの中身をtextに移植
        StringBuffer text = new StringBuffer();
        try{
            //こちらが
            URL url = getClass().getResource("media.html");
            // FileReader html_reader = new FileReader(url);
            BufferedReader tmp = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while((line = tmp.readLine()) != null){
                text.append(line + "\n");
            }
            tmp.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        //HTMLをはっつける
        try{
            JEditorPane html = new JEditorPane("text/html",text.toString());
	        html.setEditable(false);
            html.getDocument();
            this.getContentPane().add(new JScrollPane(html));

        }
        catch(Exception err){System.out.println(err);}
        // 可視化
        this.setTitle("コマンド一覧");
        this.setVisible(true);
    }
}
