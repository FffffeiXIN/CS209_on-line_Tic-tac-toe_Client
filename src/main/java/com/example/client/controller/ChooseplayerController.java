package com.example.client.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ChooseplayerController {
    @FXML
    private Button config;
    @FXML
    private Button refresh;
    @FXML
    private ChoiceBox<String> players;
    List<String> waiting;
    String myName;
    Socket socket;
    String choice;

    String win;
    String lose;
    String draw;
    @FXML
    private Label showdraw;

    @FXML
    private Label showlose;

    @FXML
    private Label showwin;

    public ChooseplayerController(List<String> waiting, String name, String win, String lose, String draw) throws IOException {
        this.waiting = waiting;
        myName = name;
        this.win = win;
        this.lose = lose;
        this.draw = draw;
    }

    @FXML
    public void initialize() {
        showwin.setText("Win: " + win);
        showlose.setText("Lose: " + lose);
        showdraw.setText("Draw: " + draw);
        players.setValue("choose players");
        for (int i = 0; i < waiting.size(); i++) {
            if (!waiting.get(i).equals(myName)) {
                players.getItems().add(waiting.get(i));
            }
        }
        players.getItems().add("Create GameRoom");

    }

    @FXML
    void configOnAction(ActionEvent event) {
        config.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouseEvent) {
                //拿到选择: 玩家 or 创房间
                choice = players.getValue();
                //发送报文，确定玩家
                try {
                    //发送报文
                    socket = new Socket("localhost", 8081);
                    OutputStream os = socket.getOutputStream();
                    //1 Create GameRoom/player name myname
                    String send_str;
                    if (choice.equals("Create GameRoom")) send_str = "Create ";
                    else send_str = choice + " ";
                    send_str += myName;
                    byte[] send_bytes = send_str.getBytes();
                    if (choice.equals("Create GameRoom")) {
                        os.write(send_bytes);
                        os.flush();
                    }


                    //关掉旧界面
                    Stage curStage = (Stage) config.getScene().getWindow();
                    curStage.close();
                    // 开启新界面
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/game.fxml"));
                    if (choice.equals("Create GameRoom")) {
                        fxmlLoader.setControllerFactory(t -> new GameController(socket, "player1", send_bytes, myName, win, lose, draw));
                        System.out.println("请等待玩家进入房间，待玩家加入后请先下棋：");
                    } else {
                        fxmlLoader.setControllerFactory(t -> new GameController(socket, "player2", send_bytes, myName, win, lose, draw));
                        System.out.println("进入游戏房间");
                    }

                    Pane root = fxmlLoader.load();
                    Stage nextStage = new Stage();
                    nextStage.setTitle(myName);
                    nextStage.setScene(new Scene(root));
                    nextStage.setResizable(false);
                    nextStage.setOnCloseRequest(event -> {
                        try {
                            //发消息说我走了
                            String exit_str;
                            exit_str = "exit";
                            System.out.println(exit_str);
                            byte[] send_bytes1 = exit_str.getBytes();
                            os.write(send_bytes1);
                            os.flush();
                            //关闭socket
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    nextStage.show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @FXML
    void refreshOnAction(ActionEvent event) throws IOException {
        // 要发送的消息
        String sendMsg = myName + " 1";

        // 获取服务器的地址
        InetAddress addr = InetAddress.getByName("localhost");

        // 创建packet包对象，封装要发送的包数据和服务器地址和端口号
        DatagramPacket packet = new DatagramPacket(sendMsg.getBytes(),
                sendMsg.getBytes().length, addr, 8080);

        // 创建Socket对象
        DatagramSocket socket = new DatagramSocket();

        // 发送消息到服务器
        socket.send(packet);

        //接收消息
        byte[] bytes = new byte[1024];
        DatagramPacket receive_packet = new DatagramPacket(bytes, bytes.length);
        socket.receive(receive_packet);
        String receiveMsg = new String(receive_packet.getData(), 0, receive_packet.getLength());

        //处理信息 报文格式：房间信息1\r\n房间信息2…… 房间信息：full /available /available p1
        String[] roomsInfo = receiveMsg.split("\r\n");
        List<String> waiting_list = new ArrayList<>();
        for (int i = 0; i < roomsInfo.length; i++) {
            String[] eachInfo = roomsInfo[i].split(" ");
            if (eachInfo.length != 1) waiting_list.add(eachInfo[1]);
        }
        waiting = waiting_list;
        players = new ChoiceBox<>();
        for (int i = 0; i < waiting.size(); i++) {
            if (!waiting.get(i).equals(myName)) {
                players.getItems().add(waiting.get(i));
            }
        }
        players.getItems().add("Create GameRoom");
        Stage curStage = (Stage) refresh.getScene().getWindow();
        curStage.close();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/chooseplayer.fxml"));
        fxmlLoader.setControllerFactory(t -> {
            try {
                return new ChooseplayerController(waiting, myName, win, lose, draw);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Pane root = fxmlLoader.load();
        Stage nextStage = new Stage();
        nextStage.setTitle(myName);
        nextStage.setScene(new Scene(root));
        nextStage.setResizable(false);
        nextStage.show();
    }
}
