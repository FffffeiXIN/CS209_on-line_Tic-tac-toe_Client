package com.example.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

public class WaitingController {
//    @FXML
//    private ProgressIndicator ima;

    @FXML
    private Label res;

    String result;

    @FXML
    void closeOnAction(ActionEvent event) {
        //关闭连接，关闭页面
    }

    public WaitingController (){
        this.result = "aaa";
    }

    @FXML
    public void initialize() {
        res.setText(result);
    }

}