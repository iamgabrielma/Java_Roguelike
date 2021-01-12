import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {

        GamePanel panel;

        GameFrame(){
            System.out.println("[Main]: Creating GameFrame");
            panel = new GamePanel();
            this.add(panel);
            this.setTitle("Rogue");
            this.setVisible(true); // Calling setVisible(true) on a JFrame which is already visible works for you because this ends up calling validate() internally, which in turn revalidates all subcomponents in the frame: https://stackoverflow.com/a/21187039
            this.setResizable(false); // Ensures that the graphical interface looks the way you intend by preventing the user from re-sizing it
            //this.setBackground(Color.black);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit the application once X is clicked
            this.pack(); // This sets/fits/adjust the frame to the preferred size of the subcomponent, in this case the panel
            this.setLocationRelativeTo(null); // Appears in the middle of the screen rather than corner
            System.out.println("[Main]: Loaded!");
        }
}
