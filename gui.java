import javax.swing.*;
import java.awt.*;

public class GUI extends JPanel {

    private int[][] factoryLayout; // 2D array representing box sizes

    public GUI(int[][] layout) {
        this.factoryLayout = layout;
        setPreferredSize(new Dimension(800, 600)); // Set preferred size for the panel
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawFactoryLayout(g);
    }

    private void drawFactoryLayout(Graphics g) {
        int boxWidth = 50; // Default width of a box
        int boxHeight = 50; // Default height of a box

        int startX = (getWidth() - (boxWidth * factoryLayout[0].length)) / 2;
        int startY = (getHeight() - (boxHeight * factoryLayout.length)) / 2;

        for (int i = 0; i < factoryLayout.length; i++) {
            //For every row
            for (int j = 0; j < factoryLayout[i].length; j++) {
                //For every column
                int size = factoryLayout[i][j];
                g.setColor(Color.getHSBColor((float) Math.random(), 1.0f, 1.0f)); // Random color
                g.fillRect(startX + j * boxWidth, startY + i * boxHeight, boxWidth * size, boxHeight * size);
                g.setColor(Color.BLACK);
                g.drawRect(startX + j * boxWidth, startY + i * boxHeight, boxWidth * size, boxHeight * size);
            }
        }
    }

    public static void main(String[] args) {
        // Example factory layout: Each number represents the size factor of the box
        int[][] layout = {
                {0, 0, 2, 2, 0}, //2x2 is one machine
                {1, 0, 2, 2, 0},
                {0, 0, 0, 0, 0},
                {3, 3, 3, 0, 0},
                {3, 3, 3, 2, 0},
                {3, 3, 3, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 3, 3, 3, 0}, //3x3 is one machine
                {1, 3, 3, 3, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 1} //1x1 is one machine

        };

        JFrame frame = new JFrame("Factory Layout");
        GUI factoryLayout = new GUI(layout);
        frame.add(factoryLayout);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center the frame on the screen
        frame.setVisible(true);
    }
}
