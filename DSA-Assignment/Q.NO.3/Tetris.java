import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.Random;

public class Tetris extends JPanel {
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 20;
    private final int CELL_SIZE = 30;
    private Block currentBlock;
    private Queue<Block> blockQueue;
    private Stack<Block[]> gameBoard;
    private Timer timer;
    private boolean isGameOver;
    private int score;

    public Tetris() {
        setPreferredSize(new Dimension(BOARD_WIDTH * CELL_SIZE, BOARD_HEIGHT * CELL_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(new TAdapter());

        blockQueue = new LinkedList<>();
        gameBoard = new Stack<>();
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            gameBoard.push(new Block[BOARD_WIDTH]);
        }

        score = 0;
        generateNewBlock();
        timer = new Timer(500, new GameCycle());
        timer.start();
    }

    private void generateNewBlock() {
        currentBlock = new Block();
        blockQueue.add(currentBlock);
    }

    private void moveBlockDown() {
        if (!isCollision(currentBlock, 1, 0)) {
            currentBlock.moveDown();
        } else {
            placeBlock();
            checkCompletedRows();
            score += 10; // Increment score for placing a block
            generateNewBlock();
            if (isCollision(currentBlock, 0, 0)) {
                isGameOver = true;
                timer.stop();
            }
        }
    }

    private void placeBlock() {
        for (Point p : currentBlock.shape) {
            gameBoard.get(currentBlock.y + p.y)[currentBlock.x + p.x] = currentBlock;
        }
    }

    private void checkCompletedRows() {
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            boolean isRowCompleted = true;
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (gameBoard.get(y)[x] == null) {
                    isRowCompleted = false;
                    break;
                }
            }
            if (isRowCompleted) {
                gameBoard.remove(y);
                gameBoard.push(new Block[BOARD_WIDTH]);
                score += 100; // Increment score for completing a row
            }
        }
    }

    private boolean isCollision(Block block, int deltaY, int deltaX) {
        for (Point p : block.shape) {
            int newY = block.y + p.y + deltaY;
            int newX = block.x + p.x + deltaX;
            if (newY < 0 || newY >= BOARD_HEIGHT || newX < 0 || newX >= BOARD_WIDTH || gameBoard.get(newY)[newX] != null) {
                return true;
            }
        }
        return false;
    }

    private void drawGame(Graphics g) {
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (gameBoard.get(y)[x] != null) {
                    g.setColor(gameBoard.get(y)[x].color);
                    g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        for (Point p : currentBlock.shape) {
            g.setColor(currentBlock.color);
            g.fillRect((currentBlock.x + p.x) * CELL_SIZE, (currentBlock.y + p.y) * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + score, 10, 20);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGame(g);
        if (isGameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over", BOARD_WIDTH * CELL_SIZE / 2 - 50, BOARD_HEIGHT * CELL_SIZE / 2);
        }
    }

    private class GameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            moveBlockDown();
            repaint();
        }
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (!isGameOver) {
                if (key == KeyEvent.VK_LEFT && !isCollision(currentBlock, 0, -1)) {
                    currentBlock.moveLeft();
                } else if (key == KeyEvent.VK_RIGHT && !isCollision(currentBlock, 0, 1)) {
                    currentBlock.moveRight();
                } else if (key == KeyEvent.VK_UP) {
                    currentBlock.rotate();
                    if (isCollision(currentBlock, 0, 0)) {
                        currentBlock.rotateBack();
                    }
                } else if (key == KeyEvent.VK_DOWN) {
                    moveBlockDown();
                }
                repaint();
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris");
        Tetris game = new Tetris();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Block {
    public Point[] shape;
    public int x, y;
    public Color color;

    public Block() {
        Random random = new Random();
        int type = random.nextInt(7);
        switch (type) {
            case 0: shape = new Point[] {new Point(0, 0), new Point(1, 0), new Point(0, 1), new Point(1, 1)}; break; // O
            case 1: shape = new Point[] {new Point(0, 0), new Point(-1, 0), new Point(1, 0), new Point(2, 0)}; break; // I
            case 2: shape = new Point[] {new Point(0, 0), new Point(-1, 0), new Point(1, 0), new Point(0, 1)}; break; // T
            case 3: shape = new Point[] {new Point(0, 0), new Point(-1, 0), new Point(0, 1), new Point(1, 1)}; break; // S
            case 4: shape = new Point[] {new Point(0, 0), new Point(1, 0), new Point(0, 1), new Point(-1, 1)}; break; // Z
            case 5: shape = new Point[] {new Point(0, 0), new Point(-1, 0), new Point(1, 0), new Point(1, 1)}; break; // L
            case 6: shape = new Point[] {new Point(0, 0), new Point(-1, 0), new Point(1, 0), new Point(-1, 1)}; break; // J
        }
        x = 4;
        y = 0;
        color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public void moveDown() {
        y++;
    }

    public void moveLeft() {
        x--;
    }

    public void moveRight() {
        x++;
    }

    public void rotate() {
        for (Point p : shape) {
            int temp = p.x;
            p.x = -p.y;
            p.y = temp;
        }
    }

    public void rotateBack() {
        for (Point p : shape) {
            int temp = p.x;
            p.x = p.y;
            p.y = -temp;
        }
    }
}