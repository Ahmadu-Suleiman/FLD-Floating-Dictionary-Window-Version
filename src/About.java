import javax.swing.*;
import java.awt.*;

public class About extends JFrame {

    private JPanel aboutPanel;

    public About() {
        setContentPane(aboutPanel);
        setSize(400, 300);
        setPreferredSize(new Dimension(400, 400));
        setResizable(false);
        Utils.setAttributes(this);
    }
}
