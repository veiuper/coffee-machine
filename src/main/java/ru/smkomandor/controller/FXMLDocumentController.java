package ru.smkomandor.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import ru.smkomandor.db.DataAccessor;
import ru.smkomandor.model.Good;
import ru.smkomandor.model.Basket;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class FXMLDocumentController implements Initializable {
    @FXML
    private TableView<Good> goodTableView;
    @FXML
    private TextField textFieldSearchBar;
    @FXML
    private TableView<Basket.Row> basketTableView;
    @FXML
    private TableView<Basket> basketTotalTableView;
    @FXML
    private Button buttonPay;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        applyStructuralChanges();
        fillElementsWithData();
    }

    @FXML
    private Stage handleButtonPayAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader((getClass().getResource("/FXMLPay.fxml")));
        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setScene(new Scene(loader.load()));
        FXMLPayController controller = loader.getController();
        controller.initData(Basket.get().getAmount());
        stage.showAndWait();
        if (Basket.get().getCount() == 0) {
            clear();
        }
        return stage;
    }

    private void applyStructuralChanges() {
        goodTableView.getColumns().clear();
        TableColumn<Good, Integer> goodIdColumn = new TableColumn<>("Id");
        goodIdColumn.setVisible(false);
        TableColumn<Good, String> goodNameColumn = new TableColumn<>("Good");
        TableColumn<Good, BigDecimal> goodCostColumn = new TableColumn<>("Price");
        goodIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        goodNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        goodCostColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        goodTableView.getColumns().addAll(goodIdColumn, goodNameColumn, goodCostColumn);
        goodTableView.setRowFactory(tv -> {
            TableRow<Good> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                Good selectedItem = goodTableView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    Basket.add(selectedItem);
                    basketTableView.getItems().clear();
                    basketTableView.getItems().addAll(Basket.get().getRows());
                    basketTotalTableView.getItems().clear();
                    basketTotalTableView.getItems().addAll(Basket.get());
                }
            });
            return row;
        });
        textFieldSearchBar.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldString, String newString) {
                try {
                    goodTableView.getItems().clear();
                    goodTableView.getItems().addAll(DataAccessor.getDataAccessor().getGoods(newString));
                    goodTableView.sort();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        basketTableView.getColumns().clear();
        TableColumn<Basket.Row, Integer> basketRowIdColumn = new TableColumn<>("Id");
        basketRowIdColumn.setVisible(false);
        TableColumn<Basket.Row, Integer> basketRowLineNumberColumn = new TableColumn<>("Number");
        TableColumn<Basket.Row, String> basketRowNameColumn = new TableColumn<>("Name");
        TableColumn<Basket.Row, BigDecimal> basketRowCostColumn = new TableColumn<>("Cost");
        TableColumn<Basket.Row, Integer> basketRowCountColumn = new TableColumn<>("Count");
        TableColumn<Basket.Row, BigDecimal> basketRowLineAmountColumn = new TableColumn<>("Amount");
        basketRowIdColumn.setSortable(false);
        basketRowLineNumberColumn.setSortable(false);
        basketRowNameColumn.setSortable(false);
        basketRowCostColumn.setSortable(false);
        basketRowCountColumn.setSortable(false);
        basketRowLineAmountColumn.setSortable(false);
        basketRowIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        basketRowLineNumberColumn.setCellValueFactory(new PropertyValueFactory<>("lineNumber"));
        basketRowNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        basketRowCostColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        basketRowCountColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        basketRowLineAmountColumn.setCellValueFactory(new PropertyValueFactory<>("lineAmount"));
        basketTableView.getColumns().addAll(
                basketRowIdColumn,
                basketRowLineNumberColumn,
                basketRowNameColumn,
                basketRowCostColumn,
                basketRowCountColumn,
                basketRowLineAmountColumn
        );
        addButtonToTable();
        basketTotalTableView.getStyleClass().add("hide-header");
        basketTotalTableView.getColumns().clear();
        TableColumn<Basket, String> basketTotalTextColumn = new TableColumn<>("Text");
        TableColumn<Basket, Integer> basketTotalCountColumn = new TableColumn<>("Count");
        TableColumn<Basket, BigDecimal> basketTotalAmountColumn = new TableColumn<>("Amount");
        basketTotalTextColumn.setCellValueFactory(basket -> new SimpleObjectProperty<>("Total"));
        basketTotalCountColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        basketTotalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        basketTotalTableView.getColumns().addAll(
                basketTotalTextColumn,
                basketTotalCountColumn,
                basketTotalAmountColumn
        );
        basketTotalTableView.getItems().add(Basket.get());
        basketTotalTableView.prefHeightProperty()
                .bind(Bindings.size(basketTotalTableView.getItems())
                        .multiply(basketTotalTableView.getFixedCellSize()).add(27));

    }

    private void fillElementsWithData() {
        try {
            goodTableView.getItems().clear();
            goodTableView.getItems().addAll(DataAccessor.getDataAccessor().getAllGoods());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addButtonToTable() {
        TableColumn<Basket.Row, Void> buttonColumn = new TableColumn("Delete");

        Callback<TableColumn<Basket.Row, Void>, TableCell<Basket.Row, Void>> cellFactory;
        cellFactory = new Callback<TableColumn<Basket.Row, Void>, TableCell<Basket.Row, Void>>() {
            @Override
            public TableCell<Basket.Row, Void> call(final TableColumn<Basket.Row, Void> param) {
                final TableCell<Basket.Row, Void> cell = new TableCell<Basket.Row, Void>() {

                    private final Button button = new Button("Delete");

                    {
                        button.setOnAction((ActionEvent event) -> {
                            Basket.Row row = getTableView().getItems().get(getIndex());
                            Basket.delete(row);
                            basketTableView.getItems().clear();
                            basketTableView.getItems().addAll(Basket.get().getRows());
                            basketTotalTableView.getItems().clear();
                            basketTotalTableView.getItems().addAll(Basket.get());
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(button);
                        }
                    }
                };
                return cell;
            }
        };
        buttonColumn.setCellFactory(cellFactory);
        basketTableView.getColumns().add(buttonColumn);
    }

    private void clear() {
        basketTableView.getItems().clear();
        basketTotalTableView.getItems().clear();
        basketTotalTableView.getItems().addAll(Basket.get());
    }
}
