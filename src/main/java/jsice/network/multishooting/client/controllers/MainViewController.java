package jsice.network.multishooting.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import jsice.network.multishooting.client.MainClient;
import jsice.network.multishooting.common.net.ClientMessage;
import jsice.network.multishooting.common.net.ClientMessageType;

import java.io.IOException;

public class MainViewController {

    private MainClient main;

    @FXML
    private TextField nameTextField;

    @FXML
    private void startGame() {
        String name = nameTextField.getText().trim();
        if (name.equals("")) name = "noname";
        try {
            main.getClient().send(new ClientMessage(ClientMessageType.Play, name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMain(MainClient main) {
        this.main = main;
    }
}
