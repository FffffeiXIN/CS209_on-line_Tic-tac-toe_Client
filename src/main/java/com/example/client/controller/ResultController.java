package com.example.client.controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ResultController {

    @FXML
    private Label showdraw;

    @FXML
    private Label showlose;

    @FXML
    private Label showwin;

    private String myname;

    @FXML
    private Button close;

    @FXML
    private Label res;


    String result;

    Stage pre_stage;

    Socket socket;
    String win;
    String lose;
    String draw;
    boolean error = false;

    @FXML
    void closeOnAction(ActionEvent event) throws IOException {
        if (error){
            Stage curStage = (Stage) close.getScene().getWindow();
            curStage.close();
            pre_stage.close();
            socket.close();
        }
        else {
            //发送报文更改数据库
            OutputStream os = socket.getOutputStream();
            String close_str;
            close_str =myname+" "+ win+ " "+lose+" "+draw;
            System.out.println(close_str);
            byte[] send_bytes = close_str.getBytes();
            os.write(send_bytes);
            os.flush();
            //关闭页面,关闭连接
            Stage curStage = (Stage) close.getScene().getWindow();
            curStage.close();
            pre_stage.close();
            socket.close();
        }

    }

    public ResultController (String result , Stage stage, Socket socket, String name, String win, String lose, String draw,boolean error){
        this.result = result;
        pre_stage = stage;
        this.socket = socket;
        this.win = win;
        this.lose = lose;
        this.draw = draw;
        myname = name;
        this.error = error;
    }

    @FXML
    public void initialize() {
        res.setText(result);
        showwin.setText("Win: "+win);
        showlose.setText("Lose: "+ lose);
        showdraw.setText("Draw: "+draw);
    }
}

