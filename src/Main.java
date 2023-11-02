import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main extends JFrame {
    private JPanel panelMain;
    private JButton buttonActivate;
    private JLabel labelAbout;
    private JLabel labelAndroidVersion;
    private JLabel labelRandomWord;

    public Main() {
        buttonActivate.addActionListener(e -> Dictionary.launchDictionary(DB.getRandomWord()));
        labelRandomWord.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        labelRandomWord.setText(Utils.getUnderLinedText(Utils.randomWord));

        labelAbout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        labelAbout.setText(Utils.getUnderLinedText(labelAbout.getText()));

        labelAndroidVersion.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        labelAndroidVersion.setText(Utils.getUnderLinedText(labelAndroidVersion.getText()));

        labelAbout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new About();
            }
        });
        labelAndroidVersion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Utils.openLink("http://play.google.com/store/apps/details?id=com.meta4projects.fldfloatingdictionary");
            }
        });
        labelRandomWord.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Dictionary.launchDictionary(Utils.randomWord);
            }
        });
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.setContentPane(main.panelMain);
        main.setSize(800, 400);
        main.setPreferredSize(new Dimension(800, 400));
        main.setResizable(false);
        Utils.setAttributes(main);
    }
}