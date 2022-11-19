package com.example.client.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class GameController {
    Socket player;
    private static final int PLAY_1 = 1;
    private static final int PLAY_2 = -1;
    private static final int EMPTY = 0;
    private static final int BOUND = 90;
    private static final int OFFSET = 15;

    @FXML
    private Pane base_square;

    @FXML
    private Button config;

    @FXML
    private Button exit;
    @FXML
    private Label info;

    @FXML
    private Rectangle game_panel;
    String win;
    String lose;
    String draw;

    private static int[][] chessBoard = new int[3][3];
    private static boolean[][] flag = new boolean[3][3];
    private int myRole;
    private String info_mess;
    private byte[] send;
    private String myName;

    public GameController(Socket socket, String myRole, byte[] send_byte, String name, String win, String lose, String draw) {
        myName = name;
        this.win = win;
        this.lose = lose;
        this.draw = draw;
        send = send_byte;
        player = socket;
        if (myRole.equals("player1")) {
            this.myRole = PLAY_1;
            info_mess = "请按准备按钮，并等待其他玩家进入房间做好准备";
        } else {
            this.myRole = PLAY_2;
        }
    }

    @FXML
    private Button start;

    int x;
    int y;

    @FXML
    void configOnAction(ActionEvent event) {
        config.setOnMouseClicked(event1 -> {
            try {
                //发送行动位置报文
                OutputStream os = player.getOutputStream();
                String send_str;
                send_str = x + " " + y;
                System.out.println(send_str);
                byte[] send_bytes = send_str.getBytes();

                os.write(send_bytes);
                os.flush();

                //接收结果报文
                InputStream is = player.getInputStream();
                byte[] buf = new byte[1024];
                int readLen = 0;
                String response = "";
                readLen = is.read(buf);
                response = new String(buf, 0, readLen);
                String[] res = response.split(" ");
                //到时候还加状态码，先这样
                if (res[0].equals("Yes")) {
                    System.out.println("游戏结束，恭喜你赢了");
                    win = String.valueOf(Integer.parseInt(win)+ 1);
                    // 开启新界面
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/result.fxml"));
                    Stage curStage = (Stage) config.getScene().getWindow();
                    fxmlLoader.setControllerFactory(t -> new ResultController(myName + ": 恭喜你赢了!", curStage, player, myName, win, lose, draw));
                    Pane root = fxmlLoader.load();
                    Stage nextStage = new Stage();
                    nextStage.setTitle("对战结果");
                    nextStage.setScene(new Scene(root));
                    nextStage.setResizable(false);
                    nextStage.show();

                }
                //如果加上的状态码要改长度 或者让状态码和这个合并也行
                else if (res.length == 4 && res[3].equals("full")) {
                    draw = String.valueOf(Integer.parseInt(draw)+ 1);
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/result.fxml"));
                    Stage curStage = (Stage) config.getScene().getWindow();
                    fxmlLoader.setControllerFactory(t -> new ResultController(myName + ": 平局", curStage, player, myName, win, lose, draw));
                    Pane root = fxmlLoader.load();
                    Stage nextStage = new Stage();
                    nextStage.setTitle("对战结果");
                    nextStage.setScene(new Scene(root));
                    nextStage.setResizable(false);
                    nextStage.show();
                    System.out.println("游戏结束，平局");
                }

                //再接收一遍对手的结果
                else {
                    System.out.println("等待对手下棋：");
                    byte[] buf2 = new byte[1024];
                    int readLen2 = 0;
                    String response2 = "";
                    readLen2 = is.read(buf2);
                    response2 = new String(buf2, 0, readLen2);

                    if (response2.equals("oppExit")) {
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/result.fxml"));
                        Stage curStage = (Stage) config.getScene().getWindow();
                        fxmlLoader.setControllerFactory(t -> new ResultController(myName + ": 对手掉线", curStage, player, myName, win, lose, draw));
                        Pane root = fxmlLoader.load();
                        Stage nextStage = new Stage();
                        nextStage.setTitle("通知");
                        nextStage.setScene(new Scene(root));
                        nextStage.setResizable(false);
                        nextStage.show();
                        System.out.println("对手掉线");
                    } else {
                        String[] res2 = response2.split(" ");
                        //到时候还加状态码，先这样
                        int oppox = Integer.parseInt(res2[1]);
                        int oppoy = Integer.parseInt(res2[2]);
                        System.out.println(oppox + " " + oppoy);
                        chessBoard[oppox][oppoy] = -myRole;
                        drawChess();
                        if (res2[0].equals("Yes")) {
                            lose = String.valueOf(Integer.parseInt(lose)+ 1);
                            //赢了弹窗 游戏结束
//                            //关掉旧界面
//                            Stage curStage = (Stage) start.getScene().getWindow();
//                            curStage.close();
                            // 开启新界面
                            FXMLLoader fxmlLoader = new FXMLLoader();
                            fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/result.fxml"));
                            Stage curStage = (Stage) config.getScene().getWindow();
                            fxmlLoader.setControllerFactory(t -> new ResultController(myName + ": 很遗憾，对手赢了", curStage, player, myName, win, lose, draw));
                            Pane root = fxmlLoader.load();
                            Stage nextStage = new Stage();
                            nextStage.setTitle("对战结果");
                            nextStage.setScene(new Scene(root));
                            nextStage.setResizable(false);
                            nextStage.show();
                            System.out.println("游戏结束，对手赢了");
                        }
                        //如果加上的状态码要改长度 或者让状态码和这个合并也行
                        else if (res2.length == 4 && res2[3].equals("full")) {
                            draw = String.valueOf(Integer.parseInt(draw)+ 1);
                            FXMLLoader fxmlLoader = new FXMLLoader();
                            fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/result.fxml"));
                            Stage curStage = (Stage) config.getScene().getWindow();
                            fxmlLoader.setControllerFactory(t -> new ResultController(myName + ": 平局", curStage, player, myName, win, lose, draw));
                            Pane root = fxmlLoader.load();
                            Stage nextStage = new Stage();
                            nextStage.setTitle("对战结果");
                            nextStage.setScene(new Scene(root));
                            nextStage.setResizable(false);
                            nextStage.show();
                            System.out.println("游戏结束，平局");
                        } else {
                            System.out.println("请下棋：");
                        }
                    }
                }
            } catch (IOException e) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/result.fxml"));
                Stage curStage = (Stage) config.getScene().getWindow();
                fxmlLoader.setControllerFactory(t -> new ResultController(myName + ": 服务器异常", curStage, player, myName, win, lose, draw));
                Pane root = null;
                try {
                    root = fxmlLoader.load();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                Stage nextStage = new Stage();
                nextStage.setTitle("通知");
                nextStage.setScene(new Scene(root));
                nextStage.setResizable(false);
                nextStage.show();
                System.out.println("服务器异常");
//                throw new RuntimeException(e);
            }
        });
    }

    @FXML
    void startOnAction(ActionEvent event) {
        start.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    if (myRole == PLAY_2) {
                        OutputStream os = player.getOutputStream();
                        os.write(send);
                        os.flush();
                        System.out.println("等待对手下棋：");
                    }
                    InputStream is = player.getInputStream();
                    byte[] buf = new byte[1024];
                    int readLen = 0;
                    String response = "";
                    readLen = is.read(buf);
                    response = new String(buf, 0, readLen);
                    if (response.equals("oppExit")) {
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/result.fxml"));
                        Stage curStage = (Stage) config.getScene().getWindow();
                        fxmlLoader.setControllerFactory(t -> new ResultController(myName + ": 对手掉线", curStage, player, myName, win, lose, draw));
                        Pane root = fxmlLoader.load();
                        Stage nextStage = new Stage();
                        nextStage.setTitle("通知");
                        nextStage.setScene(new Scene(root));
                        nextStage.setResizable(false);
                        nextStage.show();
                        System.out.println("对手掉线");
                    } else {
                        if (!response.equals("ok to start")) {
                            String[] pos = response.split(" ");
                            int x = Integer.parseInt(pos[1]);
                            int y = Integer.parseInt(pos[2]);
                            System.out.println(x + " " + y);
                            chessBoard[x][y] = -myRole;
                            drawChess();
                            System.out.println("请下棋：");
                        } else {
                            info.setVisible(false);
                        }
                        start.setDisable(true);
                    }
                } catch (IOException e) {
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/result.fxml"));
                    Stage curStage = (Stage) config.getScene().getWindow();
                    fxmlLoader.setControllerFactory(t -> new ResultController(myName + ": 服务器异常", curStage, player, myName, win, lose, draw));
                    Pane root = null;
                    try {
                        root = fxmlLoader.load();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    Stage nextStage = new Stage();
                    nextStage.setTitle("通知");
                    nextStage.setScene(new Scene(root));
                    nextStage.setResizable(false);
                    nextStage.show();
                    System.out.println("服务器异常");
//                    throw new RuntimeException(e);
                }
            }
        });
    }

    @FXML
    void exitOnAction(ActionEvent event) {
        exit.setOnMouseClicked(event1 -> {
            try {
                //发消息说我走了
                OutputStream os = player.getOutputStream();
                String exit_str;
                exit_str = "exit";
                System.out.println(exit_str);
                byte[] send_bytes = exit_str.getBytes();
                os.write(send_bytes);
                os.flush();
                //关闭socket
                player.close();
                //关闭页面
                Stage curStage = (Stage) exit.getScene().getWindow();
                curStage.close();
            } catch (IOException e) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/result.fxml"));
                Stage curStage = (Stage) config.getScene().getWindow();
                fxmlLoader.setControllerFactory(t -> new ResultController(myName + ": 服务器异常", curStage, player, myName, win, lose, draw));
                Pane root = null;
                try {
                    root = fxmlLoader.load();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                Stage nextStage = new Stage();
                nextStage.setTitle("通知");
                nextStage.setScene(new Scene(root));
                nextStage.setResizable(false);
                nextStage.show();
                System.out.println("服务器异常");
//                throw new RuntimeException(e);
            }
        });
    }

    @FXML
    public void initialize() {
        info.setText(info_mess);
        game_panel.setOnMouseClicked(event -> {
            int x = (int) (event.getX() / BOUND);
            int y = (int) (event.getY() / BOUND);
            //渲染当前步骤 如果有效才执行下面发报文等的操作
            if (chessBoard[x][y] == EMPTY) {
                chessBoard[x][y] = myRole;
                this.x = x;
                this.y = y;
                drawChess();
            }
        });
    }

    private void drawChess() {
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
//                if (flag[i][j]) {
//                    // This square has been drawing, ignore.
//                    continue;
//                }
                switch (chessBoard[i][j]) {
                    case PLAY_1:
                        drawCircle(i, j);
                        break;
                    case PLAY_2:
                        drawLine(i, j);
                        break;
                    case EMPTY:
                        // do nothing
                        break;
                    default:
                        System.err.println("Invalid value!");
                }
            }
        }
    }

    private void drawCircle(int i, int j) {
        Circle circle = new Circle();
        base_square.getChildren().add(circle);
        circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
        circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
        circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
        circle.setStroke(Color.RED);
        circle.setFill(Color.TRANSPARENT);
        flag[i][j] = true;
    }

    private void drawLine(int i, int j) {
        Line line_a = new Line();
        Line line_b = new Line();
        base_square.getChildren().add(line_a);
        base_square.getChildren().add(line_b);
        line_a.setStartX(i * BOUND + OFFSET * 1.5);
        line_a.setStartY(j * BOUND + OFFSET * 1.5);
        line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
        line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_a.setStroke(Color.BLUE);

        line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
        line_b.setStartY(j * BOUND + OFFSET * 1.5);
        line_b.setEndX(i * BOUND + OFFSET * 1.5);
        line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_b.setStroke(Color.BLUE);
        flag[i][j] = true;
    }
}
