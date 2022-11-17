package com.example.client.controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ResultController {

    @FXML
    private Button close;

    @FXML
    private Label res;

    String result;

    @FXML
    void closeOnAction(ActionEvent event) {
        //关闭连接，关闭页面
    }

    public ResultController (String result){
        this.result = result;
    }

    @FXML
    public void initialize() {
        res.setText(result);
    }
}

