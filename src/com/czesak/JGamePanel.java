package com.czesak;

import javax.swing.*;
import java.awt.*;

public class JGamePanel extends JPanel {

    private final Font font = new Font("Verdana", Font.BOLD, 28);

    public JGamePanel() {
        super(true);
        setFocusable(true);
        requestFocus();
        setBackground(Color.WHITE);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        render(g);
    }

    private void drawBoard(Graphics g){
        g.setColor(Color.WHITE);
        g.fillRect(0,0,Config.WIDTH,Config.HEIGHT);
        Graphics2D g2 = (Graphics2D)g;
        g.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g.drawLine(0, Config.HEIGHT/3, Config.WIDTH, Config.HEIGHT/3);
        g.drawLine(0, 2*Config.HEIGHT/3, Config.WIDTH, 2*Config.HEIGHT/3);
        g.drawLine(Config.WIDTH/3, 0, Config.WIDTH/3, Config.HEIGHT);
        g.drawLine(2*Config.WIDTH/3, 0, 2*Config.WIDTH/3, Config.HEIGHT);
    }

    private void drawSymbol(Graphics g, char chr, int x, int y, Color col){
        Graphics2D g2 = (Graphics2D)g;
        g2.setStroke(new BasicStroke(4));
        g2.setColor(col);
        switch(chr){
            case 'O':
                g2.drawOval(x-(Config.WIDTH/3-20)/2, y-(Config.HEIGHT/3-20)/2,
                        Config.WIDTH/3-20, Config.HEIGHT/3-20);
                break;
            case 'X':
                g2.drawLine(x-(Config.WIDTH/3-20)/2, y-(Config.HEIGHT/3-20)/2,
                        x+(Config.WIDTH/3-20)/2, y+(Config.HEIGHT/3-20)/2);
                g2.drawLine(x+(Config.WIDTH/3-20)/2, y-(Config.HEIGHT/3-20)/2,
                        x-(Config.WIDTH/3-20)/2, y+(Config.HEIGHT/3-20)/2);
                break;
        }
    }

    void render(Graphics g) {
        drawBoard(g);
        g.setFont(font);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        if (Config.comError) {
            g.setColor(Color.RED);
            int stringWidth = g2.getFontMetrics().stringWidth(Config.comErrorString);
            g.drawString(Config.comErrorString, Config.WIDTH / 2 - stringWidth / 2, Config.HEIGHT / 2);
            return;
        }
        if (Config.accepted) {
            for (int i = 0; i < Config.board.length; i++) {
                if (Config.board[i] != null) {
                    char chr = Config.board[i].charAt(0);
                    Color col;
                    if(chr=='O'){
                        if(Config.circle) col = Color.BLUE; else col = Color.RED;
                    }else{
                        if(!Config.circle) col = Color.BLUE; else col = Color.RED;
                    }
                    drawSymbol(g, chr, (2 * (i % 3) + 1) * Config.WIDTH/6,
                            (2 * (i / 3) +1) * Config.HEIGHT/6, col);
                }
            }
            if (Config.won || Config.enemyWon) {
                g2.setStroke(new BasicStroke(10));
                g.setColor(Color.GREEN);
                g.drawLine(
                        (2 * (Config.line[0] % 3) + 1) * Config.WIDTH/6,
                        (2 * (Config.line[0] / 3) +1) * Config.HEIGHT/6,
                        (2 * (Config.line[1] % 3) + 1) * Config.WIDTH/6,
                        (2 * (Config.line[1] / 3) +1) * Config.HEIGHT/6);
                g.setColor(Color.RED);
                if (Config.won) {
                    int stringWidth = g2.getFontMetrics().stringWidth(Config.wonString);
                    g.drawString(Config.wonString, Config.WIDTH / 2 - stringWidth / 2, Config.HEIGHT / 2);
                } else if (Config.enemyWon) {
                    int stringWidth = g2.getFontMetrics().stringWidth(Config.enemyWonString);
                    g.drawString(Config.enemyWonString, Config.WIDTH / 2 - stringWidth / 2, Config.HEIGHT / 2);
                }
            }
            if (Config.tie) {
                g.setColor(Color.BLACK);
                int stringWidth = g2.getFontMetrics().stringWidth(Config.tieString);
                g.drawString(Config.tieString, Config.WIDTH / 2 - stringWidth / 2, Config.HEIGHT / 2);
            }
            if(!Config.yourTurn && !Config.won && !Config.enemyWon && !Config.tie) {
                g.setColor(Color.RED);
                int stringWidth = g2.getFontMetrics().stringWidth(Config.waitingOpString);
                g.drawString(Config.waitingOpString, Config.WIDTH / 2 - stringWidth / 2, Config.HEIGHT / 2);

            }
        } else {
            if(Config.threadRunning) {
                g.setColor(Color.RED);
                int stringWidth = g2.getFontMetrics().stringWidth(Config.waitingString);
                g.drawString(Config.waitingString, Config.WIDTH / 2 - stringWidth / 2, Config.HEIGHT / 2);
            }
        }
    }

}
