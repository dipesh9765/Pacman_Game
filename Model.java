package pacman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Model extends JPanel implements ActionListener {
    private final Font smallFont = new Font("Arial", Font.BOLD, 14);
    private final int blockSize = 24;
    private final int nBlocks = 15;
    private final int screenSize = nBlocks * blockSize;
    private final int MAX_GHOSTS = 12;

    /*
        TODO make combinations using this below @param
        TODO blueColor =1 ,leftBorder=1,topBorder =2,rightBorder =4,bottomBorder =8 ,whiteDots = 16
    */
    private final short[] blockData = {
            19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 28, 0, 17, 16, 16, 16, 16, 0, 0, 0, 20,
            0, 0, 0, 0, 0, 0, 17, 16, 16, 16, 16,    16, 16, 0, 20,
            19, 30, 0, 19, 18, 18, 16,16, 16, 16,24, 24, 0, 24, 20,
            21, 0, 0, 17, 16, 16, 16, 16, 16, 20,  0, 0, 0,  0,  21,
            17, 18, 18, 16, 16, 16, 16, 16, 16, 20,0, 0, 0, 0, 21,
            17, 16, 16, 16, 24, 16, 16, 16, 16, 20,0, 0, 0, 0, 21,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 18, 18, 18, 18, 20,
            17, 24, 24, 28, 0, 25, 24, 24, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 0, 0, 0, 0, 17, 16, 16, 24, 24, 16, 20,
            17, 18, 18, 22, 0, 19, 18, 18, 16, 16, 20, 0, 0, 17, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 20, 0, 0, 17, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 18, 18, 16, 20,
            25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
    };
    private final int[] validSpeeds = {1, 2, 3, 4, 6, 8};
    private Dimension d;
    private boolean inGame = false;
    private boolean died = false;
    private int nGhosts = 6;
    private int lives, score;
    private int[] dx, dy;
    private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed;
    private Image heart, ghost;
    private Image up, down, left, right;
    private int pacman_x, pacman_y, pacman_dx, pacman_dy;
    private int req_dx, req_dy;
    private int crntSpeed = 3;
    private short[] scrnData;
    private Timer timer;

    public Model() {

        Images();
        Initiate_Variable();
        addKeyListener(new TAdapter());
        setFocusable(true);
        startGame();
    }

    public static void main(String[] args) {

    }

    private void Images() {
        down = new ImageIcon("C:\\Users\\HP\\practise java\\src\\pacman\\images\\down.gif").getImage();
        up = new ImageIcon("C:\\Users\\HP\\practise java\\src\\pacman\\images\\up.gif").getImage();
        left = new ImageIcon("C:\\Users\\HP\\practise java\\src\\pacman\\images\\left.gif").getImage();
        right = new ImageIcon("C:\\Users\\HP\\practise java\\src\\pacman\\images\\right.gif").getImage();
        ghost = new ImageIcon("C:\\Users\\HP\\practise java\\src\\pacman\\images\\ghost.gif").getImage();
        heart = new ImageIcon("C:\\Users\\HP\\practise java\\src\\pacman\\images\\heart.png").getImage();

    }

    private void Initiate_Variable() {

        scrnData = new short[nBlocks * nBlocks];
        d = new Dimension(400, 400);
        ghost_x = new int[MAX_GHOSTS];
        ghost_dx = new int[MAX_GHOSTS];
        ghost_y = new int[MAX_GHOSTS];
        ghost_dy = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];

        timer = new Timer(40, this);
        timer.start();
    }

    private void playGame(Graphics2D g2d) {

        if (died) {

            death();

        } else {

            movePacman();
            drawPacman(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {

        String start = "Press SPACE to start";
        g2d.setColor(Color.yellow);
        g2d.drawString(start, (screenSize) / 4, 150);
    }

    private void drawScore(Graphics2D g) {
        g.setFont(smallFont);
        g.setColor(new Color(5, 181, 79));
        String s = "Score: " + score;
        g.drawString(s, screenSize / 2 + 96, screenSize + 16);

        for (int i = 0; i < lives; i++) {
            g.drawImage(heart, i * 28 + 8, screenSize + 1, this);
        }
    }

    private void checkMaze() {

        int i = 0;
        boolean finished = true;

        while (i < nBlocks * nBlocks && finished) {

            if ((scrnData[i]) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {

            score += 50;

            if (nGhosts < MAX_GHOSTS) {
                nGhosts++;
            }

            int max_Speed = 6;
            if (crntSpeed < max_Speed) {
                crntSpeed++;
            }

            initLevel();
        }
    }

    private void death() {

        lives--;

        if (lives == 0) {
            inGame = false;
        }

        continueLevel();
    }

    private void moveGhosts(Graphics2D g2d) {

        int pos;
        int count;

        for (int i = 0; i < nGhosts; i++) {
            if (ghost_x[i] % blockSize == 0 && ghost_y[i] % blockSize == 0) {
                pos = ghost_x[i] / blockSize + nBlocks * (ghost_y[i] / blockSize);

                count = 0;

                if ((scrnData[pos] & 1) == 0 && ghost_dx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((scrnData[pos] & 2) == 0 && ghost_dy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((scrnData[pos] & 4) == 0 && ghost_dx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((scrnData[pos] & 8) == 0 && ghost_dy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((scrnData[pos] & 15) == 15) {
                        ghost_dx[i] = 0;
                        ghost_dy[i] = 0;
                    } else {
                        ghost_dx[i] = -ghost_dx[i];
                        ghost_dy[i] = -ghost_dy[i];
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghost_dx[i] = dx[count];
                    ghost_dy[i] = dy[count];
                }

            }

            ghost_x[i] = ghost_x[i] + (ghost_dx[i] * ghostSpeed[i]);
            ghost_y[i] = ghost_y[i] + (ghost_dy[i] * ghostSpeed[i]);
            drawGhost(g2d, ghost_x[i] + 1, ghost_y[i] + 1);

            if (pacman_x > (ghost_x[i] - 12) && pacman_x < (ghost_x[i] + 12)
                    && pacman_y > (ghost_y[i] - 12) && pacman_y < (ghost_y[i] + 12)
                    && inGame) {

                died = true;
            }
        }
    }

    private void drawGhost(Graphics2D g2d, int x, int y) {
        g2d.drawImage(ghost, x, y, this);
    }

    private void movePacman() {

        int pos;
        short ch;

        if (pacman_x % blockSize == 0 && pacman_y % blockSize == 0) {
            pos = pacman_x / blockSize + nBlocks * (pacman_y / blockSize);
            ch = scrnData[pos];

            if ((ch & 16) != 0) {
                scrnData[pos] = (short) (ch & 15);
                score++;
            }

            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    pacman_dx = req_dx;
                    pacman_dy = req_dy;
                }
            }

            // Check for standstill
            if ((pacman_dx == -1 && pacman_dy == 0 && (ch & 1) != 0)
                    || (pacman_dx == 1 && pacman_dy == 0 && (ch & 4) != 0)
                    || (pacman_dx == 0 && pacman_dy == -1 && (ch & 2) != 0)
                    || (pacman_dx == 0 && pacman_dy == 1 && (ch & 8) != 0)) {
                pacman_dx = 0;
                pacman_dy = 0;
            }
        }
        int speedPacman = 6;
        pacman_x = pacman_x + speedPacman * pacman_dx;
        pacman_y = pacman_y + speedPacman * pacman_dy;
    }

    private void drawPacman(Graphics2D g2d) {

        if (req_dx == -1) {
            g2d.drawImage(left, pacman_x + 1, pacman_y + 1, this);
        } else if (req_dx == 1) {
            g2d.drawImage(right, pacman_x + 1, pacman_y + 1, this);
        } else if (req_dy == -1) {
            g2d.drawImage(up, pacman_x + 1, pacman_y + 1, this);
        } else {
            g2d.drawImage(down, pacman_x + 1, pacman_y + 1, this);
        }
    }

    private void drawMaze(Graphics2D g2d) {

        short i = 0;
        int x, y;

        for (y = 0; y < screenSize; y += blockSize) {
            for (x = 0; x < screenSize; x += blockSize) {

                g2d.setColor(new Color(0, 72, 251));
                g2d.setStroke(new BasicStroke(5));

                if ((blockData[i] == 0)) {
                    g2d.fillRect(x, y, blockSize, blockSize);
                }

                if ((scrnData[i] & 1) != 0) {
                    g2d.drawLine(x, y, x, y + blockSize - 1);
                }

                if ((scrnData[i] & 2) != 0) {
                    g2d.drawLine(x, y, x + blockSize - 1, y);
                }

                if ((scrnData[i] & 4) != 0) {
                    g2d.drawLine(x + blockSize - 1, y, x + blockSize - 1,
                            y + blockSize - 1);
                }

                if ((scrnData[i] & 8) != 0) {
                    g2d.drawLine(x, y + blockSize - 1, x + blockSize - 1,
                            y + blockSize - 1);
                }

                if ((scrnData[i] & 16) != 0) {
                    g2d.setColor(new Color(255, 255, 255));
                    g2d.fillOval(x + 10, y + 10, 6, 6);
                }

                i++;
            }
        }
    }

    private void startGame() {

        lives = 3;
        score = 0;
        initLevel();
        nGhosts = 6;
        crntSpeed = 3;
    }

    private void initLevel() {

        int i;
        for (i = 0; i < nBlocks * nBlocks; i++) {
            scrnData[i] = blockData[i];
        }

        continueLevel();
    }

    private void continueLevel() {

        int dx = 1;
        int random;

        for (int i = 0; i < nGhosts; i++) {

            ghost_y[i] = 4 * blockSize; //start position
            ghost_x[i] = 4 * blockSize;
            ghost_dy[i] = 0;
            ghost_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (crntSpeed + 1));

            if (random > crntSpeed) {
                random = crntSpeed;
            }

            ghostSpeed[i] = validSpeeds[random];
        }

        pacman_x = 7 * blockSize;  //start position
        pacman_y = 11 * blockSize;
        pacman_dx = 0;    //reset direction move
        pacman_dy = 0;
        req_dx = 0;        // reset direction controls
        req_dy = 0;
        died = false;
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);

        if (inGame) {
            playGame(g2d);
        } else {
            showIntroScreen(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    //controls
    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
                    req_dx = 0;
                    req_dy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                }
            } else {
                if (key == KeyEvent.VK_SPACE) {
                    inGame = true;
                    startGame();
                }
            }
        }
    }

}
