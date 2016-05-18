import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;

/**
 * Created by liuzc on 2016/3/3.
 */
public class test extends JFrame {

    private static JLabel jLabel;

    public test() {
        super("Simple Translucency Demo");

//        setLocation(800, 500);
        setSize(300, 200);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setAlwaysOnTop(true);

        getRootPane().setDoubleBuffered(false);
//        setOpacity(0.2f);
        setBackground(new Color(0, 0, 0, 96));

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private static class changeWaitaWhile extends Thread {
        public void run(){
            try{
                sleep(2000);
                jLabel.setText("<html><span style=\"color:#4fc1e9;\">啦啦啦啦</span></html>");
                
            }catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                if (ge.getDefaultScreenDevice().isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
                    JFrame jframe = new test();
                    jLabel = new JLabel("<html><span style=\"color:#4fc1e9;\">lalalalalla</span></html>");
                    Container container = jframe.getContentPane();
                    container.add(jLabel);
//                    (new changeWaitaWhile()).start();
                }
            }
        });
    }
}
