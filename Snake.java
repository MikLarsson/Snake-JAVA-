
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;



public class Snake extends JPanel implements ActionListener, KeyListener{

    public FileOutputStream fos;
    public DataOutputStream dos;
    public FileInputStream fis;
    public DataInputStream dis;

    public JFrame jframe;
    public Dimension dim;
    public Timer timer = new Timer(20,this);

    public static double stopWatch;

    public int score, highScore, ticks, direction, tailLength;
    public int fadeColor;

    public static final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3, SCALE = 30;

    public boolean newHighScore;

    public static Point head, cherry;

    public Random random;

    public static ArrayList<Point> snakeParts = new ArrayList<>();


    public static boolean gameover, paused;

    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 1050;


    public Snake(){
        File f = new File("highscore.dat");
        if(f.exists() && !f.isDirectory()) {
            try {
                fis = new FileInputStream("highscore.dat");
                dis = new DataInputStream(fis);
                highScore = dis.readInt();
                fis.close();
                dis.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            highScore = 0;
            try {
                fos = new FileOutputStream("highscore.dat");
                dos = new DataOutputStream(fos);
                dos.writeInt(0);
                fos.close();
                dos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        dim = Toolkit.getDefaultToolkit().getScreenSize();
        jframe = new JFrame("Snake");
        jframe.setVisible(true);
        jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jframe.setSize(WINDOW_WIDTH+(jframe.getInsets().right+jframe.getInsets().left),
                            WINDOW_HEIGHT+(jframe.getInsets().top+jframe.getInsets().bottom));
        jframe.setLocation(dim.width/2 - jframe.getWidth()/2, dim.height/2 - jframe.getHeight()/2);
        jframe.add(this);
        jframe.addKeyListener(this);
        start();
    }

    public void start(){
        ticks = 0;
        score = 0;
        stopWatch = 0;
        tailLength = 2;
        fadeColor = 254;

        newHighScore = false;
        gameover = false;
        paused = false;

        random = new Random();
        head = new Point(0,0);

        direction = DOWN;
        snakeParts.clear();
        assignCherryCoordinates();

        for(int i = 0; i < tailLength; i++){
            snakeParts.add(new Point(head.x,head.y));
        }

        timer.start();
    }

    public void assignCherryCoordinates(){
        do {
            cherry = new Point(random.nextInt(WINDOW_WIDTH / SCALE), random.nextInt((WINDOW_HEIGHT / SCALE)-2)+2);
        }while(badCherryCoordinates());
    }

    public boolean badCherryCoordinates(){
        if(collisionWithCherry())
            return true;

        for(Point p : snakeParts){
            if(p.x == cherry.x && p.y == cherry.y)
                return true;
        }

        return false;
    }

    public boolean collisionWithCherry(){
        if(head.x == cherry.x && head.y == cherry.y)
            return true;
        return false;
    }

    public boolean collisionWithItself(){
        for(Point p : snakeParts){
            if(head.x == p.x && head.y==p.y)
                return true;
        }
        return false;
    }

    public void update() {
        //Everything is updating as long as the game isn't over or paused.
        if (!gameover && !paused) {

            ticks++;
            stopWatch += 0.02;

            //The snake is allowed to update its position 10 times per second.
            if (ticks % 5 == 0 && head != null) {

                snakeParts.add(new Point(head.x, head.y));

                /*
                Depending on the direction of the snake, the head is moved accordingly
                as long as it's not outside the parameters of the game.
                 */
                switch(direction){
                    case UP:
                        if (head.y - 1 >= 0) {
                            head = new Point(head.x, head.y - 1);
                        } else {
                            gameover = true;
                        }
                        break;
                    case DOWN:
                        if (head.y + 1 < WINDOW_HEIGHT / SCALE) {
                            head = new Point(head.x, head.y + 1);
                        } else {
                            gameover = true;
                        }
                        break;
                    case LEFT:
                        if (head.x - 1 >= 0) {
                            head = new Point(head.x - 1, head.y);
                        } else {
                            gameover = true;
                        }
                        break;
                    case RIGHT:
                        if (head.x + 1 < WINDOW_WIDTH / SCALE) {
                            head = new Point(head.x + 1, head.y);
                        } else {
                            gameover = true;
                        }
                        break;
                }

                //Check if the snake is colliding with its body. If true then its game over.
                if (collisionWithItself()) {
                    gameover = true;
                }

                //Increase score, add one extra body part and assign new cherry coordinates if the snake eats a cherry.
                if (collisionWithCherry()) {
                    score += 10;
                    if(score > highScore){
                        highScore = score;
                        newHighScore = true;
                    }
                    snakeParts.add(0, new Point(snakeParts.get(snakeParts.size() - 1).x, snakeParts.get(snakeParts.size() - 1).y));
                    assignCherryCoordinates();
                }

                if(!gameover) {
                    snakeParts.remove(0);
                }else{
                    if(highScore > score){
                        try {
                            fos = new FileOutputStream("highscore.dat");
                            dos = new DataOutputStream(fos);
                            dos.writeInt(highScore);
                            fos.close();
                            dos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }

            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //Paints all the components: Snake, cherry, score, time,
        repaint();

        //Updates all the components: Snake, cherry, score, time,
        update();
    }


    @Override
    public void keyPressed(KeyEvent e) {
        if(!gameover) {

            //As long as the game is running the player is allowed to pause the game.
            if (e.getKeyCode() == KeyEvent.VK_P){
                paused = !paused;
            }

            //If the game isn't paused, the player is allowed to change the direction of the snake.
            if(!paused) {
                if ((e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) &&
                        snakeParts.get(snakeParts.size() - 1).getY() != head.y - 1) {
                    direction = UP;
                }

                if ((e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) &&
                        snakeParts.get(snakeParts.size() - 1).getY() != head.y + 1) {
                    direction = DOWN;
                }

                if ((e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) &&
                        snakeParts.get(snakeParts.size() - 1).getX() != head.x - 1) {
                    direction = LEFT;
                }

                if ((e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) &&
                        snakeParts.get(snakeParts.size() - 1).getX() != head.x + 1) {
                    direction = RIGHT;

                }
            }
        }else{
            //If its game over the player is only allowed to press SPACE or ESC.
            if(e.getKeyCode() == KeyEvent.VK_SPACE)
                start();
            if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
                jframe.dispose();
        }
    }


    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    protected void paintComponent(Graphics g){
        //Calls paintComponent in the JPanel class.
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0,0,1200,1050);

        if(fadeColor != 0) {
            if(paused){
                drawPauseText(g);
            }

            drawSnake(g);
            drawCherry(g);
            drawTimeAndScore(g);


            if(gameover)
                fadeColor-=2;

        }else{
            drawGameOver(g);
        }
    }

    public void drawTimeAndScore(Graphics g){
        //Draw the score and time on the screen
        int halfTimeWidth, halfHighScoreWidth;
        g.setColor(new Color(255,255,255,fadeColor));
        g.setFont(new Font("Helvetica", Font.PLAIN, 32));
        g.drawString("Score: "+score, 15,45);
        halfTimeWidth = g.getFontMetrics().stringWidth("Time: "+(double)Math.round(stopWatch*10)/10);
        g.drawString("Time: "+ (double)Math.round(stopWatch*10)/10, (WINDOW_WIDTH-halfTimeWidth)/2, 45);

        if(!newHighScore) {
            halfHighScoreWidth = g.getFontMetrics().stringWidth("Highscore: " + highScore);
            g.drawString("Highscore: " + highScore, WINDOW_WIDTH-halfHighScoreWidth-15, 45);
        }else {
            halfHighScoreWidth = g.getFontMetrics().stringWidth("Highscore: " + score);
            g.setColor(new Color(255,255,0,fadeColor));
            g.drawString("Highscore: " + score, WINDOW_WIDTH-halfHighScoreWidth-15, 45);
        }
    }

    public void drawCherry(Graphics g){
        //Draw the cherry
        g.setColor(new Color(255,0,0,fadeColor));
        g.fillOval(cherry.x * SCALE + 5, cherry.y * SCALE + 5, SCALE-10, SCALE-10);
    }

    public void drawSnake(Graphics g){
        Color snakeGreen = new Color(112,146,86,fadeColor);
        g.setColor(snakeGreen);
        for (Point point : snakeParts) {
            g.fillRect(point.x * SCALE + 1, point.y * SCALE + 1,
                    SCALE - 1, SCALE - 1);
        }

        //Draw snake head
        drawSnakeHead(g);
    }

    public void drawSnakeHead(Graphics g){
        if(!gameover)
            g.fillRect(head.x * SCALE + 1, head.y * SCALE + 1, SCALE - 1, SCALE - 1);
        g.setColor(new Color(0,0,0,fadeColor));
        if(direction == DOWN) {
            g.fillOval(head.x * SCALE + 5, head.y * SCALE + 21, 8, 8);
            g.fillOval(head.x * SCALE + 17, head.y * SCALE + 21, 8, 8);
            g.setColor(new Color(255,0,0,fadeColor));
            g.drawLine(head.x * SCALE + 15, head.y * SCALE + 30,
                    head.x * SCALE + 15, head.y * SCALE + 40);

            g.drawLine(head.x * SCALE + 15, head.y * SCALE + 40,
                    head.x * SCALE + 7, head.y * SCALE + 50);

            g.drawLine(head.x * SCALE + 15, head.y * SCALE + 40,
                    head.x * SCALE + 22, head.y * SCALE + 50);
        }else if(direction == UP){
            g.fillOval(head.x * SCALE + 5, head.y * SCALE + 1, 8, 8);
            g.fillOval(head.x * SCALE + 17, head.y * SCALE + 1, 8, 8);

            g.setColor(new Color(255,0,0,fadeColor));
            g.drawLine(head.x * SCALE + 15, head.y * SCALE,
                    head.x * SCALE + 15, head.y * SCALE - 10);

            g.drawLine(head.x * SCALE + 15, head.y * SCALE - 10,
                    head.x * SCALE + 7, head.y * SCALE - 20);

            g.drawLine(head.x * SCALE + 15, head.y * SCALE - 10,
                    head.x * SCALE + 22, head.y * SCALE - 20);
        }else if(direction == RIGHT){
            g.fillOval(head.x * SCALE + 21, head.y * SCALE + 5, 8, 8);
            g.fillOval(head.x * SCALE + 21, head.y * SCALE + 17, 8, 8);

            g.setColor(new Color(255,0,0,fadeColor));
            g.drawLine(head.x * SCALE + 30, head.y * SCALE + 15,
                    head.x * SCALE + 40, head.y * SCALE + 15);

            g.drawLine(head.x * SCALE + 40, head.y * SCALE + 15,
                    head.x * SCALE + 48, head.y * SCALE + 8);

            g.drawLine(head.x * SCALE + 40, head.y * SCALE + 15,
                    head.x * SCALE + 48, head.y * SCALE + 22);
        }else{
            g.fillOval(head.x * SCALE + 1, head.y * SCALE + 5, 8, 8);
            g.fillOval(head.x * SCALE + 1, head.y * SCALE + 17, 8, 8);

            g.setColor(new Color(255,0,0,fadeColor));
            g.drawLine(head.x * SCALE, head.y * SCALE + 15,
                    head.x * SCALE-10, head.y * SCALE + 15);

            g.drawLine(head.x * SCALE - 10, head.y * SCALE + 15,
                    head.x * SCALE - 17, head.y * SCALE + 7);

            g.drawLine(head.x * SCALE - 10, head.y * SCALE + 15,
                    head.x * SCALE - 17, head.y * SCALE + 22);
        }
    }

    public void drawPauseText(Graphics g){
        g.setColor(Color.WHITE);
        int textWidthPaused;
        g.setFont(new Font("Helvetica", Font.BOLD, 96));
        textWidthPaused = g.getFontMetrics().stringWidth("Paused");
        g.drawString("Paused", (WINDOW_WIDTH-textWidthPaused)/2, (WINDOW_HEIGHT)/2);
    }

    public void drawGameOver(Graphics g){
        int textWidthGameOver, textWidthScore, textWidthInstructions;
        int textHeightGameOver, textHeightScore;

        g.setColor(Color.WHITE);

        g.setFont(new Font("Helvetica", Font.PLAIN, 128));

        textWidthGameOver = g.getFontMetrics().stringWidth("Game Over");
        textHeightGameOver = g.getFontMetrics().getHeight();

        g.drawString("Game Over", (WINDOW_WIDTH-textWidthGameOver)/2,(WINDOW_HEIGHT-textHeightGameOver)/2);
        g.setFont(new Font("Helvetica", Font.PLAIN, 64));

        if(score == 0)
            textWidthScore = g.getFontMetrics().stringWidth("Score: X");
        else if(score > 0 && score < 100)
            textWidthScore = g.getFontMetrics().stringWidth("Score: XX");
        else if(score > 100 && score < 1000)
            textWidthScore = g.getFontMetrics().stringWidth("Score: XXX");
        else
            textWidthScore = g.getFontMetrics().stringWidth("Score: XXXX");

        textHeightScore = g.getFontMetrics().getHeight();
        g.drawString("Score: "+score, (WINDOW_WIDTH-textWidthScore)/2, (WINDOW_HEIGHT-textHeightGameOver)/2+textHeightScore);

        g.setFont(new Font("Helvetica", Font.PLAIN, 32));

        textWidthInstructions = g.getFontMetrics().stringWidth("[Press SPACE to play again or ESC to quit]");

        g.drawString("[Press SPACE to play again or ESC to quit]",
                (WINDOW_WIDTH-textWidthInstructions)/2, (WINDOW_HEIGHT-textHeightGameOver)/2+textHeightScore*2);
    }



    public static void main(String[] args){
        new Snake();
    }
}
