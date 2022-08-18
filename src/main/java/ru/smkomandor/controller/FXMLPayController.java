package ru.smkomandor.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.smkomandor.db.DataAccessor;
import ru.smkomandor.model.Basket;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class FXMLPayController implements Initializable {
    private BigDecimal total;
    @FXML
    private Label labelSum;
    @FXML
    private TextField textFieldSum;
    @FXML
    private Button buttonPay;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void initData(BigDecimal total) {
        this.total = total;
        labelSum.setText("Amount to be paid: " + total);
    }

    public void handleButtonPayAction(ActionEvent actionEvent) throws SQLException {
        BigDecimal bigDecimal = BigDecimal.valueOf(Double.parseDouble(textFieldSum.getText()));
        if (bigDecimal.equals(total)) {
            DataAccessor.getDataAccessor().save();
            Basket.clear();
            Stage stage = (Stage) buttonPay.getScene().getWindow();
            stage.close();
        }
    }
}
