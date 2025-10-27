import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class PingPongGame extends JFrame {

    public PingPongGame() {
        setTitle("Ping Pong Game – Power-Up Edition");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String[] modes = {"2 Players", "Computer (Easy)", "Computer (Medium)", "Computer (Hard)"};
        String choice = (String) JOptionPane.showInputDialog(null,
                "Choose Game Mode:",
                "Ping Pong Game",
                JOptionPane.PLAIN_MESSAGE,
                null, modes, modes[0]);

        if (choice != null) {
            GamePanel panel = new GamePanel(choice);
            add(panel);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        } else {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PingPongGame::new);
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH = 800;
    private final int HEIGHT = 500;
    private final int PADDLE_WIDTH = 15;
    private final int BASE_PADDLE_HEIGHT = 80;
    private final int BALL_SIZE = 20;

    private int paddle1Y = HEIGHT / 2 - BASE_PADDLE_HEIGHT / 2;
    private int paddle2Y = HEIGHT / 2 - BASE_PADDLE_HEIGHT / 2;
    private int paddle1Height = BASE_PADDLE_HEIGHT;
    private int paddle2Height = BASE_PADDLE_HEIGHT;

    private int ballX = WIDTH / 2 - BALL_SIZE / 2;
    private int ballY = HEIGHT / 2 - BALL_SIZE / 2;
    private int ballXSpeed = 4;
    private int ballYSpeed = 3;
    private int paddle1Speed = 0;
    private int paddle2Speed = 0;

    private int score1 = 0;
    private int score2 = 0;

    private boolean reverseP1 = false;
    private boolean reverseP2 = false;

    private boolean powerUpActive = false;
    private int powerUpX, powerUpY;
    private int powerUpSize = 20;
    private String currentPowerUp = "";
    private int powerUpTimer1 = 0;
    private int powerUpTimer2 = 0;
    private int powerUpSide = 0;

    private Timer timer;
    private Random random = new Random();

    private String mode;
    private boolean gameOver = false;

    public GamePanel(String mode) {
        this.mode = mode;

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        timer = new Timer(10, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
            if (mode.startsWith("Computer")) moveAI();
            checkCollision();
            handlePowerUps();
        }
        repaint();
    }

    private void move() {
        paddle1Y += paddle1Speed;
        paddle2Y += paddle2Speed;
        ballX += ballXSpeed;
        ballY += ballYSpeed;

        if (paddle1Y < 0) paddle1Y = 0;
        if (paddle1Y > HEIGHT - paddle1Height) paddle1Y = HEIGHT - paddle1Height;
        if (paddle2Y < 0) paddle2Y = 0;
        if (paddle2Y > HEIGHT - paddle2Height) paddle2Y = HEIGHT - paddle2Height;
    }

    private void moveAI() {
        int aiSpeed;
        switch (mode) {
            case "Computer (Easy)":
                aiSpeed = 3;
                break;
            case "Computer (Medium)":
                aiSpeed = 5;
                break;
            case "Computer (Hard)":
                aiSpeed = 7;
                break;
            default:
                aiSpeed = 4;
        }

        if (ballY + BALL_SIZE / 2 > paddle2Y + paddle2Height / 2) paddle2Y += aiSpeed;
        else paddle2Y -= aiSpeed;
    }

    private void checkCollision() {
        if (ballY <= 0 || ballY >= HEIGHT - BALL_SIZE) ballYSpeed = -ballYSpeed;

        Rectangle ballRect = new Rectangle(ballX, ballY, BALL_SIZE, BALL_SIZE);
        Rectangle leftPaddle = new Rectangle(20, paddle1Y, PADDLE_WIDTH, paddle1Height);
        Rectangle rightPaddle = new Rectangle(WIDTH - 35, paddle2Y, PADDLE_WIDTH, paddle2Height);

        if (ballRect.intersects(leftPaddle)) ballXSpeed = Math.abs(ballXSpeed);
        if (ballRect.intersects(rightPaddle)) ballXSpeed = -Math.abs(ballXSpeed);

        if (ballX < 0) {
            score2++;
            checkWin();
            resetBall();
        }
        if (ballX > WIDTH - BALL_SIZE) {
            score1++;
            checkWin();
            resetBall();
        }

        if (powerUpActive && ballRect.intersects(new Rectangle(powerUpX, powerUpY, powerUpSize, powerUpSize))) {
            applyPowerUp(powerUpSide);
            powerUpActive = false;
        }
    }

    private void checkWin() {
        if (score1 == 10 || score2 == 10) gameOver = true;
    }

    private void resetBall() {
        ballX = WIDTH / 2 - BALL_SIZE / 2;
        ballY = HEIGHT / 2 - BALL_SIZE / 2;
        ballXSpeed = -ballXSpeed;
        ballYSpeed = random.nextInt(6) - 3;

        if (random.nextInt(100) < 40) spawnPowerUp();
    }

    private void spawnPowerUp() {
        powerUpSide = random.nextBoolean() ? 1 : 2;
        if (powerUpSide == 1)
            powerUpX = random.nextInt(WIDTH / 2 - 100) + 50;
        else
            powerUpX = random.nextInt(WIDTH / 2 - 100) + WIDTH / 2 + 50;

        powerUpY = random.nextInt(HEIGHT - 100) + 50;

        String[] types = {"speed", "grow", "reverse"};
        currentPowerUp = types[random.nextInt(types.length)];
        powerUpActive = true;
    }

    private void applyPowerUp(int side) {
        if (side == 1) {
            switch (currentPowerUp) {
                case "speed":
                    ballXSpeed += (ballXSpeed > 0 ? 2 : -2);
                    break;
                case "grow":
                    paddle1Height = 120;
                    powerUpTimer1 = 300;
                    break;
                case "reverse":
                    reverseP2 = true;
                    powerUpTimer2 = 300;
                    break;
            }
        } else {
            switch (currentPowerUp) {
                case "speed":
                    ballXSpeed += (ballXSpeed > 0 ? 2 : -2);
                    break;
                case "grow":
                    paddle2Height = 120;
                    powerUpTimer2 = 300;
                    break;
                case "reverse":
                    reverseP1 = true;
                    powerUpTimer1 = 300;
                    break;
            }
        }
    }

    private void handlePowerUps() {
        if (powerUpTimer1 > 0) {
            powerUpTimer1--;
            if (powerUpTimer1 == 0) {
                paddle1Height = BASE_PADDLE_HEIGHT;
                reverseP1 = false;
            }
        }
        if (powerUpTimer2 > 0) {
            powerUpTimer2--;
            if (powerUpTimer2 == 0) {
                paddle2Height = BASE_PADDLE_HEIGHT;
                reverseP2 = false;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        g.drawLine(WIDTH / 2, 0, WIDTH / 2, HEIGHT);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("PLAYER 1", 50, 25);
        g.drawString(mode.startsWith("Computer") ? "COMPUTER" : "PLAYER 2", WIDTH - 160, 25);

        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString(score1 + "   " + score2, WIDTH / 2 - 30, 30);

        g.fillRect(20, paddle1Y, PADDLE_WIDTH, paddle1Height);
        g.fillRect(WIDTH - 35, paddle2Y, PADDLE_WIDTH, paddle2Height);

        g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

        if (powerUpActive) {
            switch (currentPowerUp) {
                case "speed": g.setColor(Color.RED); break;
                case "grow": g.setColor(Color.GREEN); break;
                case "reverse": g.setColor(Color.YELLOW); break;
            }
            g.fillRect(powerUpX, powerUpY, powerUpSize, powerUpSize);
            g.setColor(Color.WHITE);
        }

        if (reverseP1) g.drawString("Controls Reversed!", 50, 60);
        if (reverseP2) g.drawString("Controls Reversed!", WIDTH - 230, 60);

        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String winner;
            int xPosition;

            if (score1 == 10) {
                winner = "Player 1 Wins!";
                xPosition = 80;
            } else {
                winner = mode.startsWith("Computer") ? "Computer Wins!" : "Player 2 Wins!";
                xPosition = WIDTH - 300;
            }

            g.drawString(winner, xPosition, HEIGHT / 2 - 20);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("Press R to Restart", WIDTH / 2 - 100, HEIGHT / 2 + 30);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameOver) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W: paddle1Speed = reverseP1 ? 6 : -6; break;
                case KeyEvent.VK_S: paddle1Speed = reverseP1 ? -6 : 6; break;
                case KeyEvent.VK_UP: if (!mode.startsWith("Computer")) paddle2Speed = reverseP2 ? 6 : -6; break;
                case KeyEvent.VK_DOWN: if (!mode.startsWith("Computer")) paddle2Speed = reverseP2 ? -6 : 6; break;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_R) {
            restartGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!mode.startsWith("Computer")) {
            if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_S) paddle1Speed = 0;
            if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) paddle2Speed = 0;
        } else {
            if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_S) paddle1Speed = 0;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private void restartGame() {
        score1 = 0;
        score2 = 0;
        gameOver = false;
        paddle1Y = HEIGHT / 2 - BASE_PADDLE_HEIGHT / 2;
        paddle2Y = HEIGHT / 2 - BASE_PADDLE_HEIGHT / 2;
        resetBall();
    }
}
