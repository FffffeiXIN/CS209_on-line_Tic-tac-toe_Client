package com.example.client.controller;

//import entity.Result;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import java.net.URL;
import java.util.ResourceBundle;

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
    private Label info;

    @FXML
    private Rectangle game_panel;

//    private static boolean TURN = false;

    private static int[][] chessBoard = new int[3][3];
    private static boolean[][] flag = new boolean[3][3];
//    private int win = 0;
    private int myRole;
    private String info_mess;

    public GameController(Socket socket, String myRole) {
        player = socket;
        if (myRole.equals("player1")){
            this.myRole = PLAY_1;
            info_mess = "请按准备按钮，并等待其他玩家进入房间做好准备";
        }
        else {
            this.myRole = PLAY_2;
        }
    }

    @FXML
    private Button start;

    int x;
    int y;

    @FXML
    void configOnAction(ActionEvent event) {
        config.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>(){

            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    //发送行动位置报文
                    OutputStream os = player.getOutputStream();
                    String send_str;
                    send_str = x + " " + y;
                    System.out.println(send_str);
                    byte[] send_bytes = send_str.getBytes();

                    os.write(send_bytes);
                    os.flush();
//                    player.shutdownOutput();


                    //接收结果报文
                    InputStream is = player.getInputStream();
                    byte[] buf = new byte[1024];
                    int readLen = 0;
                    String response="";
                    readLen = is.read(buf);
                    response = new String(buf, 0, readLen);
                    String[] res = response.split(" ");
                    //到时候还加状态码，先这样
                    if (res[0].equals("Yes")){
                        System.out.println("我赢了");
                        // 开启新界面
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/result.fxml"));
                        fxmlLoader.setControllerFactory(t -> new ResultController("恭喜你赢了!"));
                        Pane root = fxmlLoader.load();
                        Stage nextStage = new Stage();
                        nextStage.setTitle("对战结果");
                        nextStage.setScene(new Scene(root));
                        nextStage.setResizable(false);
                        nextStage.show();
                    }
//                    InputStream is = player.getInputStream();
//                    ObjectInputStream ois = new ObjectInputStream(is);
//                    Result my_res = (Result) ois.readObject();
                    //操作成功
//                    if (my_res.state_code == 200) {
//                        //判断胜负
//                        //赢了弹窗
//                    } else {
//                        //有异常 比如点击已有棋子区域
//                    }


//                byte[] buf = new byte[1024];
//                int readLen = 0;
//                String response="";
//                readLen = is.read(buf);
//                response = new String(buf, 0, readLen);

                    //再接收一遍对手的结果
                    else {
                        byte[] buf2 = new byte[1024];
                        int readLen2 = 0;
                        String response2="";
                        readLen2 = is.read(buf2);
                        response2 = new String(buf2, 0, readLen2);
                        String[] res2 = response2.split(" ");
                        //到时候还加状态码，先这样
                        int oppox = Integer.parseInt(res2[1]);
                        int oppoy = Integer.parseInt(res2[2]);
                        chessBoard[oppox][oppoy] = -myRole;
                        drawChess();
                        if (res2[0].equals("Yes")){
                            //赢了弹窗 游戏结束
                            System.out.println("对手赢了");
//                            //关掉旧界面
//                            Stage curStage = (Stage) start.getScene().getWindow();
//                            curStage.close();
                            // 开启新界面
                            FXMLLoader fxmlLoader = new FXMLLoader();
                            fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/result.fxml"));
                            fxmlLoader.setControllerFactory(t -> new ResultController("很遗憾，对手赢了"));
                            Pane root = fxmlLoader.load();
                            Stage nextStage = new Stage();
                            nextStage.setTitle("对战结果");
                            nextStage.setScene(new Scene(root));
                            nextStage.setResizable(false);
                            nextStage.show();
                        }
                    }


//                    Result oppo_res = (Result) ois.readObject();
//                    chessBoard = oppo_res.chessboard;
//                    drawChess();
//                    TURN = !TURN;
//                    if (oppo_res.state_code == 200 && oppo_res.win) {
//                        //对手赢了
//                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @FXML
    void startOnAction(ActionEvent event) {
        start.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    InputStream is = player.getInputStream();
                    byte[] buf = new byte[1024];
                    int readLen = 0;
                    String response="";
                    readLen = is.read(buf);
                    response = new String(buf, 0, readLen);
                    if(!response.equals("ok to start")){
                        String[] pos = response.split(" ");
                        int x = Integer.parseInt(pos[1]);
                        int y = Integer.parseInt(pos[2]);
                        chessBoard[x][y] = -myRole;
                        drawChess();
                    }
                    else {
                        info.setVisible(false);
                    }
//                    player.shutdownInput();
//                    InputStream init = player.getInputStream();
//                    ObjectInputStream init_ois = new ObjectInputStream(init);
//                    Result init_res = (Result) init_ois.readObject();
//                    chessBoard = init_res.chessboard;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @FXML
    public void initialize(){
        info.setText(info_mess);
        game_panel.setOnMouseClicked(event -> {
//            int x = (int) (event.getX() / BOUND);
//            int y = (int) (event.getY() / BOUND);
            int x = (int) (event.getX() / BOUND);
            int y = (int) (event.getY() / BOUND);
            //渲染当前步骤 如果有效才执行下面发报文等的操作
            if (chessBoard[x][y] == EMPTY) {
                chessBoard[x][y] = myRole;
                this.x = x;
                this.y = y;
                drawChess();
//                TURN = !TURN;
//                try {
//                    //发送行动位置报文
//                    OutputStream os = player.getOutputStream();
//                    String send_str;
//                    send_str = x + " " + y;
//                    System.out.println(send_str);
//                    byte[] send_bytes = send_str.getBytes();
//
//                    os.write(send_bytes);
//                    os.flush();
////                    player.shutdownOutput();
//
//
//                    //接收结果报文
//                    InputStream is = player.getInputStream();
//                    byte[] buf = new byte[1024];
//                    int readLen = 0;
//                    String response="";
//                    readLen = is.read(buf);
//                    response = new String(buf, 0, readLen);
//                    String[] res = response.split(" ");
//                    //到时候还加状态码，先这样
//                    if (res[0].equals("Yes")){
//                        System.out.println("我赢了");
//                        // 开启新界面
//                        FXMLLoader fxmlLoader = new FXMLLoader();
//                        fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/result.fxml"));
//                        fxmlLoader.setControllerFactory(t -> new ResultController("恭喜你赢了!"));
//                        Pane root = fxmlLoader.load();
//                        Stage nextStage = new Stage();
//                        nextStage.setTitle("对战结果");
//                        nextStage.setScene(new Scene(root));
//                        nextStage.setResizable(false);
//                        nextStage.show();
//                    }
////                    InputStream is = player.getInputStream();
////                    ObjectInputStream ois = new ObjectInputStream(is);
////                    Result my_res = (Result) ois.readObject();
//                    //操作成功
////                    if (my_res.state_code == 200) {
////                        //判断胜负
////                        //赢了弹窗
////                    } else {
////                        //有异常 比如点击已有棋子区域
////                    }
//
//
////                byte[] buf = new byte[1024];
////                int readLen = 0;
////                String response="";
////                readLen = is.read(buf);
////                response = new String(buf, 0, readLen);
//
//                    //再接收一遍对手的结果
//                    else {
//                        byte[] buf2 = new byte[1024];
//                        int readLen2 = 0;
//                        String response2="";
//                        readLen2 = is.read(buf2);
//                        response2 = new String(buf2, 0, readLen2);
//                        String[] res2 = response2.split(" ");
//                        //到时候还加状态码，先这样
//                        int oppox = Integer.parseInt(res2[1]);
//                        int oppoy = Integer.parseInt(res2[2]);
//                        chessBoard[oppox][oppoy] = -myRole;
//                        drawChess();
//                        if (res2[0].equals("Yes")){
//                            //赢了弹窗 游戏结束
//                            System.out.println("对手赢了");
////                            //关掉旧界面
////                            Stage curStage = (Stage) start.getScene().getWindow();
////                            curStage.close();
//                            // 开启新界面
//                            FXMLLoader fxmlLoader = new FXMLLoader();
//                            fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/result.fxml"));
//                            fxmlLoader.setControllerFactory(t -> new ResultController("很遗憾，对手赢了"));
//                            Pane root = fxmlLoader.load();
//                            Stage nextStage = new Stage();
//                            nextStage.setTitle("对战结果");
//                            nextStage.setScene(new Scene(root));
//                            nextStage.setResizable(false);
//                            nextStage.show();
//                        }
//                    }
//
//
////                    Result oppo_res = (Result) ois.readObject();
////                    chessBoard = oppo_res.chessboard;
////                    drawChess();
////                    TURN = !TURN;
////                    if (oppo_res.state_code == 200 && oppo_res.win) {
////                        //对手赢了
////                    }
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
            }
        });
    }

//    private boolean refreshBoard(int x, int y) {
//        if (chessBoard[x][y] == EMPTY) {
//            chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
//            drawChess();
//            return true;
//        }
//        return false;
//    }

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
