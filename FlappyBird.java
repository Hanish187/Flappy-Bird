import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.io.*;
import java.util.Random;
import javax.swing.*;
public class FlappyBird extends JPanel implements ActionListener, KeyListener{
    
    int boardWidth = 360;
    int boardHeight = 640;

    //Images

    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    //bird class

    int birdX = boardWidth/8;
    int birdY = boardWidth/2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird{
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img)
        {
            this.img = img;
        }
    }

     //pipe class
     int pipeX = boardWidth;
     int pipeY = 0;
     int pipeWidth = 64;
     int pipeHeight= 512;

      class Pipe{
        int x = pipeX; 
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img ;
        boolean passed = false;


        Pipe(Image img) 
        {
            this.img = img;
        }
      }

      //game logic
      Bird bird;
      int velocityX = -4;
      int velocityY = 0;
      int gravity = 1;

      ArrayList<Pipe> pipes;
      Random random = new Random();

      Timer gameLoop;
      Timer placePipeTimer;
      boolean gameOver = false;
      double score = 0;
      double heightscore = 0;
      final String scoreFile = "higestscore.txt";
      
    FlappyBird()
    {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);


        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();
        
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        loadingheightscore();

//place pipes timer
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                placePipes();
            }
        });
        placePipeTimer.start();
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();
    }
    void placePipes()
    {
        int randomPipeY = (int) (pipeY - pipeHeight/4-Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;

        Pipe topPipe =  new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        draw(g);
    }


    public void draw(Graphics g)
    {
        g.drawImage(backgroundImg,0,0,this.boardWidth, this.boardHeight, null);

        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        for(int i=0;i<pipes.size();i++)
        {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x,pipe.y, pipe.width, pipe.height, null);
        }
        g.setColor(Color.white);

        g.setFont(new Font("Arial",Font.PLAIN,32));

        if(gameOver)
        {
            g.drawString("Game Over: " + String.valueOf((int) score),10, 35);
            g.drawString("Higest Score"+ (int)heightscore, 10,75);
        }
        else{
            g.drawString(String.valueOf((int) score), 10, 35);
            g.drawString("Highest Score: "+  (int) heightscore, 10,75);
        }
    }



    public void move(){
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);


        for(int i=0;i<pipes.size();i++)
        {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;


            if(!pipe.passed && bird.x > pipe.x + pipe.width){
                score += 0.5;
                pipe.passed  = true;
            }

            if(collision(bird, pipe))
            {
                gameOver = true;
            }
        }

        if(bird.y > boardHeight)
        {
            gameOver = true;
        }
    }
    boolean collision(Bird a, Pipe b){
        return a.x < b.x + b.width &&
        a.x + a.width > b.x &&
        a.y < b.y + b.height &&
        a.y + a.height > b.y;  
    }
    @Override
    public void actionPerformed(ActionEvent e){
        move();
        repaint();
        if(gameOver){
            if(score>heightscore)
            {
                heightscore = score;
                saveHighestScore();
            }
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e){
        if(e.getKeyCode() == KeyEvent.VK_SPACE){
            velocityY = -9;
            if (gameOver) 
            {
                if(score > heightscore){
                    heightscore = score;
                }
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placePipeTimer.start();   
            }
        }
    }


    void loadingheightscore()
{
    try{
        File file = new File(scoreFile);
        if(file.exists()){
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            if(line != null){
                heightscore = Double.parseDouble(line);
            }
            reader.close();
        }
    }
    catch(IOException e){
        e.printStackTrace();
    }
}
void saveHighestScore(){
    try{
        BufferedWriter writer = new BufferedWriter(new FileWriter(scoreFile));
        writer.write(String.valueOf(heightscore));
        writer.close();
    }
    catch(IOException e){
        e.printStackTrace();
    }
}
    
    @Override
    public void keyTyped(KeyEvent e){
    }
    @Override
    public void keyReleased(KeyEvent e){}
}
