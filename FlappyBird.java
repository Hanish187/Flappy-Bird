import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.io.*;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    // Constants
    final int BOARD_WIDTH = 360;
    final int BOARD_HEIGHT = 640;
    final int BIRD_WIDTH = 34;
    final int BIRD_HEIGHT = 24;
    final int PIPE_WIDTH = 64;
    final int PIPE_HEIGHT = 512;
    final int PIPE_GAP = BOARD_HEIGHT / 4;
    final String SCORE_FILE = "highestscore.txt";

    // Images
    Image backgroundImg, birdImg, topPipeImg, bottomPipeImg;

    // Bird properties
    class Bird {
        int x, y, width, height;
        Image img;

        Bird(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }

    Bird bird;

    // Pipe properties
    class Pipe {
        int x, y, width, height;
        Image img;
        boolean passed;

        Pipe(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
            this.passed = false;
        }
    }

    ArrayList<Pipe> pipes;

    // Game state variables
    Timer gameLoop, pipeSpawner;
    int birdVelocityY = 0;
    final int GRAVITY = 1;
    final int PIPE_SPEED = -4;
    boolean gameOver = false, gameStarted = false;
    double score = 0, highScore = 0;

    public FlappyBird() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Initialize objects
        bird = new Bird(BOARD_WIDTH / 8, BOARD_HEIGHT / 2, BIRD_WIDTH, BIRD_HEIGHT, birdImg);
        pipes = new ArrayList<>();
        loadHighScore();

        // Timers
        gameLoop = new Timer(1000 / 60, this);
        pipeSpawner = new Timer(1500, e -> spawnPipes());
    }

    void spawnPipes() {
        int randomY = (int) (-PIPE_HEIGHT / 4 - Math.random() * (PIPE_HEIGHT / 2));
        pipes.add(new Pipe(BOARD_WIDTH, randomY, PIPE_WIDTH, PIPE_HEIGHT, topPipeImg));
        pipes.add(new Pipe(BOARD_WIDTH, randomY + PIPE_HEIGHT + PIPE_GAP, PIPE_WIDTH, PIPE_HEIGHT, bottomPipeImg));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImg, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, null);
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        if (gameOver) {
            g.drawString("Game Over!", BOARD_WIDTH / 4, BOARD_HEIGHT / 2 - 50);
            g.drawString("Score: " + (int) score, BOARD_WIDTH / 4, BOARD_HEIGHT / 2);
            g.drawString("High Score: " + (int) highScore, BOARD_WIDTH / 4, BOARD_HEIGHT / 2 + 50);
        } else {
            g.drawString("Score: " + (int) score, 10, 30);
            g.drawString("High Score: " + (int) highScore, 10, 60);
        }
    }

    void moveBird() {
        birdVelocityY += GRAVITY;
        bird.y += birdVelocityY;
        bird.y = Math.max(bird.y, 0);

        if (bird.y > BOARD_HEIGHT) {
            gameOver = true;
        }
    }

    void movePipes() {
        for (Pipe pipe : pipes) {
            pipe.x += PIPE_SPEED;
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
            }
            if (checkCollision(bird, pipe)) {
                gameOver = true;
            }
        }
        pipes.removeIf(pipe -> pipe.x + pipe.width < 0);
    }

    boolean checkCollision(Bird bird, Pipe pipe) {
        return bird.x < pipe.x + pipe.width &&
               bird.x + bird.width > pipe.x &&
               bird.y < pipe.y + pipe.height &&
               bird.y + bird.height > pipe.y;
    }

    void resetGame() {
        gameOver = false;
        score = 0;
        bird.y = BOARD_HEIGHT / 2;
        birdVelocityY = 0;
        pipes.clear();
    }

    void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SCORE_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Double.parseDouble(line);
            }
        } catch (IOException e) {
            System.out.println("High score file not found.");
        }
    }

    void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SCORE_FILE))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            moveBird();
            movePipes();
        } else {
            if (score > highScore) {
                highScore = score;
                saveHighScore();
            }
            gameLoop.stop();
            pipeSpawner.stop();
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameStarted) {
                gameStarted = true;
                gameLoop.start();
                pipeSpawner.start();
            }
            if (!gameOver) {
                birdVelocityY = -10;
            } else {
                resetGame();
                gameLoop.start();
                pipeSpawner.start();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird game = new FlappyBird();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
