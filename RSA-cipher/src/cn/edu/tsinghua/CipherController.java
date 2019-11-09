package cn.edu.tsinghua;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CipherController {

    private RSA rsaCipher;

    @FXML
    private TextArea pTextArea;

    @FXML
    private TextArea qTextArea;

    @FXML
    private TextArea nTextArea;

    @FXML
    private TextArea dTextArea;

    @FXML
    private TextArea plainTextArea;

    @FXML
    private TextArea encryptedTextArea;

    @FXML
    private TextField eTextField;

    @FXML
    private ComboBox<Integer> bitsBox;

    @FXML
    private void initialize() {
        bitsBox.getItems().addAll(256, 512, 768, 1024);
        bitsBox.getSelectionModel().select(0);
    }

    @FXML
    private void clickGenKey(ActionEvent event) {
        int bits = bitsBox.getSelectionModel().getSelectedItem();
        long e = Long.parseLong(eTextField.getText());
        rsaCipher = new RSA(bits, e);
        if (rsaCipher != null) {
            pTextArea.setText(rsaCipher.getP());
            qTextArea.setText(rsaCipher.getQ());
            nTextArea.setText(rsaCipher.getN());
            dTextArea.setText(rsaCipher.getD());
        }
    }

    @FXML
    private void clickEncrypt(ActionEvent event) {
        if (rsaCipher == null)
            return;
        String plainText = plainTextArea.getText();
        String encryptedText = rsaCipher.encrypt(plainText);
        encryptedTextArea.setText(encryptedText);
    }

    @FXML
    private void clickDecrypt(ActionEvent event) {
        if (rsaCipher == null)
            return;
        String encryptedText = encryptedTextArea.getText();
        String plainText = rsaCipher.decrypt(encryptedText);
        plainTextArea.setText(plainText);
    }
}
