import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liuzc on 2016/3/4.
 */
public class mainForm extends JFrame {

    private JTextField textField_roomID;
    private JButton Button_connect;
    private JButton Button_disconn;
    private JCheckBox CheckBox_scrollerOn;
    private JCheckBox CheckBox_scrollerTop;
    private JPanel mainPanel;
    private JTabbedPane tabbedPane1;
    private JTextArea textArea_log;
    private JTextField TextField_posx;
    private JTextField TextField_posy;
    private JTextField TextField_width;
    private JTextField TextField_height;
    private JButton Button_browse;
    private JTextField textField_fontpath;

    private static mainForm form = null;
    private danmuku_handler handler = null;
    private right_scroller scroller = null;

    public mainForm(String title) {
        super(title);
        setContentPane(mainPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        // 关闭事件
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (handler != null) {
                    handler.stop();
                }
                if (scroller != null){
                    scroller.close();
                }
            }
        });

        // 连接按钮
        Button_connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomID = textField_roomID.getText();
                if (!roomID.matches("\\d+")){
                    log("请输入正确的房间号");
                } else {
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    if (ge.getDefaultScreenDevice().isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
                        handler = new danmuku_handler();
                        handler.start(roomID, true, form);
                        if (CheckBox_scrollerOn.isSelected()) {
                            scroller = new right_scroller(
                                    TextField_posx.getText().matches("\\d+") ? Integer.valueOf(TextField_posx.getText()) : -1,
                                    TextField_posy.getText().matches("\\d+") ? Integer.valueOf(TextField_posy.getText()) : -1,
                                    TextField_width.getText().matches("\\d+") ? Integer.valueOf(TextField_width.getText()) : -1,
                                    TextField_height.getText().matches("\\d+") ? Integer.valueOf(TextField_height.getText()) : -1,
                                    CheckBox_scrollerTop.isSelected(),
                                    textField_fontpath.getText());
                        }
                        Button_connect.setEnabled(false);
                    } else {
                        log("你的系统不支持透明窗口，已关闭侧边栏");
                    }
                }
            }
        });

        // 断开按钮
        Button_disconn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (handler != null){
                    handler.stop();
                    if (scroller != null) scroller.close();
                    log("已断开");
                    changeTitle("B站直播弹幕姬");
                    Button_connect.setEnabled(true);
                }
            }
        });

        // 浏览字体按钮
        Button_browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "ttf & ttc file", "ttf", "ttc");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(null);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    textField_fontpath.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        setVisible(true);

    }

    public void log(String comment){
        log(-1, "弹幕姬", comment);
    }

    public void log(int type, String speaker, String comment){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
                textArea_log.append(dateFormat.format(new Date()) + " " + speaker + "：" + comment + "\n");
                if (CheckBox_scrollerOn.isSelected()) addDM(type, speaker, comment);
            }
        });
    }

    public void addDM(int type, String speaker, String comment){
        if (scroller != null){
            if (type != -1) {
                scroller.addDM(type, speaker, comment);
            }
        }
    }

    public void changeTitle(String str){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setTitle(str);
            }
        });
    }

    public static void main(String[] args) {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        form = new mainForm("B站直播弹幕姬");
    }

}
