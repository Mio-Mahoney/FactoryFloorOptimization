import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class GUI extends JPanel {

    private int[][] factoryLayout; // 2D array representing box sizes
    private Color[] colors = {Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.yellow};
    private int Size;
    private int numCols;
    private int numRows;
    private int boxWidth;
    private int boxHeight;

    public GUI(int[][] layout, int size) {
        this.factoryLayout = layout;
        this.Size = size;
        setPreferredSize(new Dimension(1400, 1400)); // Set preferred size for the panel
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        numRows = factoryLayout.length;
        numCols = factoryLayout[0].length;

        boxHeight = getHeight() / numCols;
        boxWidth = getWidth() / numRows;

        drawGrid(g);
        drawFactoryLayout(g);
    }


    private void drawGrid(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                int x = col * boxWidth;
                int y = row * boxHeight;
                g.drawRect(x, y, boxWidth, boxHeight);
            }

        }
    }

    public void drawFactoryLayout(Graphics g) {
        for (int row = 0; row < factoryLayout.length; row++) {
            for (int col = 0; col < factoryLayout[row].length; col++) {
                int type = factoryLayout[row][col];
                if (type > 0 && type < colors.length) {
                    g.setColor(colors[type]);
                    g.fillRect(col * boxWidth, row * boxHeight, boxWidth, boxHeight);
                }
            }
        }
    }
//    private void drawFactoryLayout(Graphics g) {
//        int boxLength = getHeight()/Size; // Default width of a box
//
//        int startX = (getWidth() - (boxLength * factoryLayout[0].length)) / 2;
//        int startY = (getHeight() - (boxLength * factoryLayout.length)) / 2;
//
//        boolean[][] drawn = new boolean[factoryLayout.length][factoryLayout[0].length]; // To track what has been drawn
//
//        for (int i = 0; i < factoryLayout.length; i++) {
//            for (int j = 0; j < factoryLayout[i].length; j++) {
//                int type = factoryLayout[i][j];
//
//                g.setColor(Color.LIGHT_GRAY);
//                g.drawRect(startX + j * boxLength, startY + i * boxLength, boxLength, boxLength);
//
//                if (size > 0 && !drawn[i][j]) {
//                    // Determine the dimensions of the current machine
//                    int machineWidth = 1;
//                    int machineHeight = 1;
//
//                    // Look right to determine width
//                    for (int k = j + 1; k < factoryLayout[i].length && factoryLayout[i][k] == size; k++) {
//                        machineWidth++;
//                    }
//
//                    // Look down to determine height
//                    for (int k = i + 1; k < factoryLayout.length && factoryLayout[k][j] == size; k++) {
//                        machineHeight++;
//                    }
//
//                    // Draw the machine
//                    g.setColor(colors[factoryLayout[i][j]]); // Random color
//                    g.fillRect(startX + j * boxLength, startY + i * boxLength, boxLength * machineWidth, boxLength * machineHeight);
//                    g.setColor(Color.BLACK);
//                    g.drawRect(startX + j * boxLength, startY + i * boxLength, boxLength * machineWidth, boxLength * machineHeight);
//
//                    // Mark the area as drawn
//                    for (int m = i; m < i + machineHeight; m++) {
//                        for (int n = j; n < j + machineWidth; n++) {
//                            drawn[m][n] = true;
//                        }
//                    }
//                }
//            }
//        }
//    }

    public static void main(String[] args) {
        // Example factory layout: Each number represents the size factor of the box
//        int[][] layout = {
//                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 2, 2, 0, 0, 0}, //2x2 is one machine
//                {0, 0, 0, 1, 0, 2, 2, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 3, 3, 3, 0, 0, 0, 0},
//                {0, 0, 0, 3, 3, 3, 2, 2, 0, 0},//3x3 is one machine
//                {0, 0, 0, 3, 3, 3, 2, 2, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 2, 2, 0, 0, 0, 0},
//                {0, 0, 0, 4, 4, 4, 4, 0, 0, 0},
//                {0, 0, 0, 4, 4, 4, 4, 0, 0, 0}, //4x4 is one machine
//                {0, 0, 0, 4, 4, 4, 4, 0, 0, 0},
//                {0, 0, 0, 4, 4, 4, 4, 1, 0, 0}, //1x1 is one machine
//                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
//        };

        FactorySet factort1 = new FactorySet(48, 30);
        int[][]layout = factort1.getMachines();

        System.out.println(factort1);
        System.out.println(factort1.score);
        JFrame frame = new JFrame("Factory Layout");
        GUI factoryLayout = new GUI(layout, factort1.FACTORY_SIZE);
        frame.add(factoryLayout);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center the frame on the screen
        frame.setVisible(true);
    }
}
