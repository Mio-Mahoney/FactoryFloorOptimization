import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class GUI extends JPanel {

    private int[][] factoryLayout; // 2D array representing box sizes
    private Color[] colors = {Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.darkGray};

    public GUI(int[][] layout) {
        this.factoryLayout = layout;
        setPreferredSize(new Dimension(1400, 1080)); // Set preferred size for the panel
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawFactoryLayout(g);
    }

    private void drawFactoryLayout(Graphics g) {
        int boxWidth = 75; // Default width of a box
        int boxHeight = 75; // Default height of a box

        int startX = (getWidth() - (boxWidth * factoryLayout[0].length)) / 2;
        int startY = (getHeight() - (boxHeight * factoryLayout.length)) / 2;

        boolean[][] drawn = new boolean[factoryLayout.length][factoryLayout[0].length]; // To track what has been drawn

        for (int i = 0; i < factoryLayout.length; i++) {
            for (int j = 0; j < factoryLayout[i].length; j++) {
                int size = factoryLayout[i][j];

                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(startX + j * boxWidth, startY + i * boxHeight, boxWidth, boxHeight);

                if (size > 0 && !drawn[i][j]) {
                    // Determine the dimensions of the current machine
                    int machineWidth = 1;
                    int machineHeight = 1;

                    // Look right to determine width
                    for (int k = j + 1; k < factoryLayout[i].length && factoryLayout[i][k] == size; k++) {
                        machineWidth++;
                    }

                    // Look down to determine height
                    for (int k = i + 1; k < factoryLayout.length && factoryLayout[k][j] == size; k++) {
                        machineHeight++;
                    }

                    // Draw the machine
                    g.setColor(colors[factoryLayout[i][j]]); // Random color
                    g.fillRect(startX + j * boxWidth, startY + i * boxHeight, boxWidth * machineWidth, boxHeight * machineHeight);
                    g.setColor(Color.BLACK);
                    g.drawRect(startX + j * boxWidth, startY + i * boxHeight, boxWidth * machineWidth, boxHeight * machineHeight);

                    // Mark the area as drawn
                    for (int m = i; m < i + machineHeight; m++) {
                        for (int n = j; n < j + machineWidth; n++) {
                            drawn[m][n] = true;
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        // Example factory layout: Each number represents the size factor of the box
        int[][] layout = {
                {0, 0, 0, 2, 2, 0}, //2x2 is one machine
                {0, 1, 0, 2, 2, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 3, 3, 3, 0, 0},
                {0, 3, 3, 3, 2, 2},//3x3 is one machine
                {0, 3, 3, 3, 2, 2},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 2, 2, 0, 0},
                {0, 4, 4, 4, 4, 0},
                {0, 4, 4, 4, 4, 0}, //4x4 is one machine
                {0, 4, 4, 4, 4, 0},
                {0, 4, 4, 4, 4, 1} //1x1 is one machine

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
