package com.example.client.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class WelcomeController {

    @FXML
    private Button startButton;

    @FXML
    private TextField Name;

    @FXML
    void startButtonOnAction(ActionEvent event) {
        startButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                //发送socket 处理数据
                try {
                    //关掉旧界面
                    Stage curStage = (Stage) startButton.getScene().getWindow();
                    curStage.close();

                    //填充新界面内容
                    // 要发送的消息
                    String sendMsg = "开始游戏";

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
                    List<String> waiting = new ArrayList<>();
                    for (int i = 0; i < roomsInfo.length; i++) {
                        String[] eachInfo = roomsInfo[i].split(" ");
                        if (eachInfo.length != 1) waiting.add(eachInfo[1]);
                    }
                    System.out.println(waiting.toString());

                    // 开启新界面
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/chooseplayer.fxml"));
                    fxmlLoader.setControllerFactory(t -> {
                        try {
                            return new ChooseplayerController(waiting, Name.getText());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    Pane root = fxmlLoader.load();
                    Stage nextStage = new Stage();
                    nextStage.setTitle(Name.getText());
                    nextStage.setScene(new Scene(root));
                    nextStage.setResizable(false);
                    nextStage.show();
                    // 关闭socket
                    socket.close();

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

}
