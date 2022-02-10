package com.czesak;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MainForm extends JFrame implements Runnable {
    private JPanel mainPanel;
    private JPanel jPanel1;
    private JTextField ipTextField;
    private JButton startButton;
    private JButton restartButton;

    private JGamePanel gamePanel;
    ServerSocket serverSocket;
    Socket socket;
    DataOutputStream dos;
    DataInputStream dis;
    Thread thread;

    public MainForm(String title) throws HeadlessException {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        restartButton.setEnabled(false);
        startButton.addActionListener(actionEvent -> buttonStartClick());
        restartButton.addActionListener(actionEvent -> buttonRestartClick());
    }

    private void createUIComponents() {
        jPanel1 = new JGamePanel();
        gamePanel = (JGamePanel) jPanel1;
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mousePressedEvent(e);
            }
        });
    }

    @Override
    public void run() {
        while (Config.threadRunning) {
            if (Config.errors >= 10)
                Config.comError = true;
            if (!Config.yourTurn && !Config.comError) {
                try {
                    int space = dis.readInt();
                    if ((space > -1) && (space < 9)) {
                        if (Config.circle)
                            Config.board[space] = "X";
                        else
                            Config.board[space] = "O";
                        Config.yourTurn = true;
                        if (checkWin(false) || checkTie()) {
                            restartButton.setEnabled(true);
                            Config.yourTurn = false;
                        }
                    } else if (space == 999) {
                        Config.reset();
                        Config.yourTurn = true;
                        gamePanel.repaint();
                    }
                } catch (IOException e) {
                    Config.errors++;
                }
            }
            gamePanel.repaint();
            if (!Config.circle && !Config.accepted)
                listenForServerRequest();
        }
    }

    private void buttonStartClick() {
        Config.ip = ipTextField.getText().trim();
        if (!connectToServer())
            initializeServer();
        thread = new Thread(this, "lab12game");
        Config.threadRunning = true;
        thread.start();
        startButton.setEnabled(false);
        if (Config.circle)
            setTitle("Kółko - krzyżyk: [O]");
        else
            setTitle("Kółko - krzyżyk: [X]");
    }

    private void buttonRestartClick() {
        Config.reset();
        Toolkit.getDefaultToolkit().sync();
        try {
            dos.writeInt(999);
            dos.flush();
        } catch (IOException e1) {
            Config.errors++;
        }
        Config.yourTurn = false;
        gamePanel.repaint();
    }

    private void mousePressedEvent(MouseEvent e) {
        if (Config.accepted) {
            if (Config.yourTurn) {
                if (!Config.comError && !Config.won && !Config.enemyWon) {
                    int x = 3 * e.getX() / Config.WIDTH;
                    int y = 3 * e.getY() / Config.HEIGHT;
                    int position = x + 3 * y;
                    if (Config.board[position] == null) {
                        if (!Config.circle)
                            Config.board[position] = "X";
                        else
                            Config.board[position] = "O";
                        Config.yourTurn = false;
                        Toolkit.getDefaultToolkit().sync();
                        try {
                            dos.writeInt(position);
                            dos.flush();
                        } catch (IOException e1) {
                            Config.errors++;
                        }
                        if (checkWin(true) || checkTie())
                            restartButton.setEnabled(true);
                        gamePanel.repaint();
                    }
                }
            }
        }
    }

    private void initializeServer() {
        try {
            serverSocket = new ServerSocket(Config.port, 8, InetAddress.getByName(Config.ip));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Config.yourTurn = true;
        Config.circle = false;
    }

    private void listenForServerRequest() {
        try {
            socket = serverSocket.accept();
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            Config.accepted = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean connectToServer() {
        try {
            socket = new Socket(Config.ip, Config.port);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            Config.accepted = true;
            gamePanel.repaint();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean checkTie() {
        for (String s : Config.board)
            if (s == null)
                return false;
        Config.tie = true;
        return true;
    }

    public boolean checkWin(boolean myWin) {
        String str;
        if (myWin) {
            if (Config.circle)
                str = "O";
            else
                str = "X";
        } else {
            if (Config.circle)
                str = "X";
            else
                str = "O";
        }
        for (int[] win : Config.wins) {
            if (Config.board[win[0]] == null || Config.board[win[1]] == null || Config.board[win[2]] == null)
                continue;
            if (Config.board[win[0]].equals(str) && Config.board[win[1]].equals(str) && Config.board[win[2]].equals(str)) {
                Config.line[0] = win[0];
                Config.line[1] = win[2];
                if (myWin)
                    Config.won = true;
                else
                    Config.enemyWon = true;
                return true;
            }
        }
        return false;
    }
}
