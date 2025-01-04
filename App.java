import javax.swing.*;
public class App {
    public static void main(String[] args) {
        int boardWidth = 360;
        int boardHeigth = 640;

        JFrame  frame = new JFrame();

        frame.setSize(boardWidth,boardHeigth);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        FlappyBird  flappyBird = new FlappyBird();
        frame.add(flappyBird);
        frame.pack();
        flappyBird.requestFocus();
        frame.setVisible(true);
    }
}