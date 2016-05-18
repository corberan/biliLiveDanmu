import javax.smartcardio.TerminalFactory;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.parser.Element;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by liuzc on 2016/3/4.
 */
public class right_scroller extends JFrame {
    private JTextPane jTextPane = null;
    private JScrollPane jScrollPane = null;
    private String bodyRule = null;

    public right_scroller(int x, int y, int wid, int hgt, boolean isTop, String fontfilePath){
        super("弹幕姬侧边栏");
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();

        if (wid < 0) wid = 250;
        if (hgt < 0) hgt = screensize.height - insets.top - insets.bottom;

        if (x < 0) x = screensize.width - insets.right - wid;
        if (y < 0) y = insets.top;

        setLocation(x, y);
        setSize(wid, hgt);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        if (isTop) setAlwaysOnTop(true);
//        getRootPane().setDoubleBuffered(true);
        setOpacity(0.99f);
        setBackground(new Color(0, 0, 0, 0));
//        setLocationRelativeTo(null);

        Font font = new Font(null, Font.TRUETYPE_FONT, 16);
        if (fontfilePath.length() > 0){
            File fontFile = new File(fontfilePath);
            if (fontFile.exists()) {
                InputStream is = null;
                BufferedInputStream bis = null;
                try {
                    is = new FileInputStream(fontFile);
                    bis = new BufferedInputStream(is);
                    font = Font.createFont(Font.TRUETYPE_FONT, bis);
                    font = font.deriveFont(16f);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        if (is != null)
                            is.close();
                        if (bis != null)
                            bis.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        jTextPane = new JTextPane();
        jTextPane.setContentType("text/html");
        jTextPane.setFont(font);
//        jTextPane.setBorder(BorderFactory.createLineBorder(Color.red));
        jTextPane.setBounds(x, y, wid, hgt);
        jTextPane.setEditable(false);
        jTextPane.setFocusable(false);
        jTextPane.setBackground(new Color(0, 0, 0, 144));
//        jTextPane.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt; }";
        HTMLDocument doc = (HTMLDocument)jTextPane.getStyledDocument();
        doc.getStyleSheet().addRule(bodyRule);


        jScrollPane = new JScrollPane();
        jScrollPane.setViewportView(jTextPane);
        jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane.setBackground(new Color(0, 0, 0, 0));
        getContentPane().add(jScrollPane);

        setVisible(true);
    }

    public void addDM(int type, String speaker, String comment){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String html = "";
                if (type == 0){
                    html += "<span style=\"color:#4fc1e9;\">" + speaker + "：</span>";
                }else if (type == 1){
                    html += "<span style=\"color:#ff8f34;\">" + speaker + "：</span>";
                }
                html += "<span style=\"color:#FFFFFF;\">" + comment + "</span>";
                html += "<br/>";

                HTMLDocument doc = (HTMLDocument)jTextPane.getStyledDocument();
                try {
                    repaint();
                    doc.insertAfterEnd(doc.getCharacterElement(doc.getLength()), html);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
//                System.out.println(jTextPane.getText());
                jScrollPane.getViewport().setViewPosition(new Point(0, jScrollPane.getVerticalScrollBar().getMaximum()));//jTextPane.getCaretPosition()));//jScrollPane.getVerticalScrollBar().getMaximum()));
            }
        });
    }

    public void close(){
        dispose();
    }

}
