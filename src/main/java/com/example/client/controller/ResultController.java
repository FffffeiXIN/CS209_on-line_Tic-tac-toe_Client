package com.example.client.controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class ResultController {

    @FXML
    private Button close;

    @FXML
    private Label res;

    String result;

    Stage pre_stage;

    Socket socket;

    @FXML
    void closeOnAction(ActionEvent event) throws IOException {
        //关闭页面,关闭连接
        Stage curStage = (Stage) close.getScene().getWindow();
        curStage.close();
        pre_stage.close();
        socket.close();
    }

    public ResultController (String result , Stage stage, Socket socket){
        this.result = result;
        pre_stage = stage;
        this.socket = socket;
    }

    @FXML
    public void initialize() {
        res.setText(result);
    }
}

