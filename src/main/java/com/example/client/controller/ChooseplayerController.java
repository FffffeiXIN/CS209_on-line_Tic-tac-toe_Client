package com.example.client.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
    private ChoiceBox<String> players;

    List<String> waiting;
    String myName;

    Socket socket;

    String choice;

    public ChooseplayerController(List<String> waiting, String name) throws IOException {
        this.waiting = waiting;
        myName = name;
        socket = new Socket("localhost", 8081);
    }

    @FXML
    public void initialize() {
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
                    OutputStream os = socket.getOutputStream();
                    //1 Create GameRoom/player name myname
                    String send_str;
                    if (choice.equals("Create GameRoom")) send_str = "Create ";
                    else send_str = choice + " ";
                    send_str += myName;
                    byte[] send_bytes = send_str.getBytes();
                    os.write(send_bytes);
                    os.flush();
//                    socket.shutdownOutput();

                    //进入等待界面
//                    text.setVisible(true);
//                    FXMLLoader fxmlLoader0 = new FXMLLoader();
//                    fxmlLoader0.setLocation(getClass().getClassLoader().getResource("com/example/client/waiting.fxml"));
//                    fxmlLoader0.setControllerFactory(t -> new WaitingController());
//                    Pane root0 = fxmlLoader0.load();
//                    Stage waitingStage = new Stage();
//                    waitingStage.setTitle(myName);
//                    waitingStage.setScene(new Scene(root0));
//                    waitingStage.setResizable(false);
//                    waitingStage.show();


//                    //接收报文
//                    InputStream is = socket.getInputStream();
//                    byte[] buf = new byte[1024];
//                    int readLen = 0;
//                    String response="";
//                    readLen = is.read(buf);
//                    response = new String(buf, 0, readLen);
//                    String[] info = response.split(" ");
//
//                    if(info[0].equals("gameStart")){
//                        //关掉旧界面
//                        Stage curStage = (Stage) config.getScene().getWindow();
//                        curStage.close();
//                        // 开启新界面
//                        FXMLLoader fxmlLoader = new FXMLLoader();
//                        fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/game.fxml"));
//                        fxmlLoader.setControllerFactory(t -> new GameController(socket,info[1]));
//                        Pane root = fxmlLoader.load();
//                        Stage nextStage = new Stage();
//                        nextStage.setTitle(myName);
//                        nextStage.setScene(new Scene(root));
//                        nextStage.setResizable(false);
//                        nextStage.show();
//
//                    }else {
//                        //抛出异常
//                    }

                    //关掉旧界面
                    Stage curStage = (Stage) config.getScene().getWindow();
                    curStage.close();
                    // 开启新界面
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getClassLoader().getResource("com/example/client/game.fxml"));
                    if (choice.equals("Create GameRoom")) {
                        fxmlLoader.setControllerFactory(t -> new GameController(socket, "player1"));
                    } else
                        fxmlLoader.setControllerFactory(t -> new GameController(socket, "player2"));
                    Pane root = fxmlLoader.load();
                    Stage nextStage = new Stage();
                    nextStage.setTitle(myName);
                    nextStage.setScene(new Scene(root));
                    nextStage.setResizable(false);
                    nextStage.show();


                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
