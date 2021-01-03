package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class Controller implements Initializable {
    /*
     *  FXML injected values
     */
    @FXML
    private Button add;
    @FXML
    private Button edit;
    @FXML
    private Button delete;

    @FXML
    private TableView tv;

    @FXML
    private Label totAmt;

    @FXML
    private TableColumn descCol;
    @FXML
    private TableColumn amtCol;
    @FXML
    private TableColumn dateCol;
    @FXML
    private TableColumn<Transaction, String> typeCol;

    @FXML
    private DatePicker fromDate;
    @FXML
    private DatePicker toDate;

    @FXML
    private BarChart<String, Double> chart;

    /*
     * Global values
     */
    private Database db = new Database();
    private ObservableList<Transaction> transactions;
    private ObservableList<Type> types;
    private Transaction selectedTrans;

    /**
     * Handles the click event of the add button
     * @param event
     */
    @FXML
    public void addClick(ActionEvent event) {

        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Add Transaction");
        dialog.setHeaderText("Enter transaction information, and then click \"Submit\"");

        Label descLbl = new Label("Description: ");
        Label valLbl = new Label("Value: ");
        Label dateLbl = new Label("Date: ");
        Label typeLbl = new Label("Type: ");

        TextField descTxt = new TextField();
        TextField valTxt = new TextField();
        DatePicker dateTxt = new DatePicker();
        ComboBox<Type> typeBox = new ComboBox<Type>();
        typeBox.getItems().addAll(types);

        // Tell the combobox how to interpret a Type as a value
        Callback<ListView <Type>, ListCell<Type>> cb = lv -> new ListCell<Type>() {
            @Override
            protected void updateItem(Type type, boolean empty) {
                super.updateItem(type, empty);
                setText(empty ? "" : type.getDescription());
            }
        };
        typeBox.setCellFactory(cb);
        typeBox.setButtonCell(cb.call(null));

        GridPane grid = new GridPane();
        grid.add(descLbl, 1, 1);
        grid.add(descTxt, 2, 1);
        grid.add(valLbl, 1, 2);
        grid.add(valTxt, 2, 2);
        grid.add(dateLbl, 1, 3);
        grid.add(dateTxt, 2, 3);
        grid.add(typeLbl, 1, 4);
        grid.add(typeBox, 2, 4);

        dialog.getDialogPane().setContent(grid);

        ButtonType btnOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(btnOk);

        // Create a new Transaction using the values input by the user in the dialog
        dialog.setResultConverter(new Callback<ButtonType, Transaction>() {
            @Override
            public Transaction call(ButtonType buttonType) {
                // If the OK button was clicked, use the values entered for the Transaction
                if (buttonType == btnOk) {
                    return new Transaction(
                            descTxt.getText(), Double.parseDouble(valTxt.getText()),
                            dateTxt.getValue(), typeBox.getValue()
                    );
                }

                // If OK was not clicked, just return null
                return null;
            }
        });

        // If the dialog returned a new Transaction, create a new database record with it
        // then fetch all of the transactions within that range
        // If a Transaction was not returned, then just do nothing
        Optional<Transaction> transaction = dialog.showAndWait();
        if (transaction.isPresent()) {
            // Try to insert a new transaction into the database
            // If there is a problem, just notify the user
            try {
                db.insertTransaction(transaction.get());
                transactions.clear();
                transactions.addAll(db.findTransactions(toDate.getValue(), fromDate.getValue()));
            } catch (Exception e) {
                createAlert(Alert.AlertType.ERROR, "Transaction could not be created.");
            }
        }
    }

    /**
     * Handles the click event of the edit button
     * @param event
     */
    @FXML
    public void editClick(ActionEvent event) {

        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Edit Transaction");
        dialog.setHeaderText("Change transaction information, and then click \"Submit\"");

        Label descLbl = new Label("Description: ");
        Label valLbl = new Label("Value: ");
        Label dateLbl = new Label("Date: ");
        Label typeLbl = new Label("Type: ");

        TextField descTxt = new TextField();
        TextField valTxt = new TextField();
        DatePicker dateTxt = new DatePicker();
        ComboBox<Type> typeBox = new ComboBox<Type>();
        typeBox.getItems().addAll(types);

        descTxt.setText(selectedTrans.getDescription());
        valTxt.setText(Double.toString(selectedTrans.getAmount()));
        dateTxt.setValue(selectedTrans.getDate());

        // When we open the edit dialog, we want to search for the value in the combobox
        // that matches the value of the type of the selected transaction
        // then set the value to the value in the list of types
        // If we don't, the value will be listed as a memory address
        Type selectedType = selectedTrans.getType();
        for (int i = 0; i < typeBox.getItems().size(); i++) {
            if (typeBox.getItems().get(i).getId() == selectedType.getId()) {
                typeBox.setValue(typeBox.getItems().get(i));
                break;
            }
        }

        // Tell the combobox how to interpret a Type
        Callback<ListView <Type>, ListCell<Type>> cb = lv -> new ListCell<Type>() {
            @Override
            protected void updateItem(Type type, boolean empty) {
                super.updateItem(type, empty);
                setText(empty ? "" : type.getDescription());
            }
        };
        typeBox.setCellFactory(cb);
        typeBox.setButtonCell(cb.call(null));

        GridPane grid = new GridPane();
        grid.add(descLbl, 1, 1);
        grid.add(descTxt, 2, 1);
        grid.add(valLbl, 1, 2);
        grid.add(valTxt, 2, 2);
        grid.add(dateLbl, 1, 3);
        grid.add(dateTxt, 2, 3);
        grid.add(typeLbl, 1, 4);
        grid.add(typeBox, 2, 4);

        dialog.getDialogPane().setContent(grid);

        ButtonType btnOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(btnOk);

        // Update the selected Transaction with the new values, and return that Transaction
        dialog.setResultConverter(new Callback<ButtonType, Transaction>() {
            @Override
            public Transaction call(ButtonType buttonType) {
                // If OK was clicked, set the values of the selected Transaction to the new values
                if (buttonType == btnOk) {
                    selectedTrans.setDescription(descTxt.getText());
                    selectedTrans.setAmount(Double.parseDouble(valTxt.getText()));
                    selectedTrans.setDate(dateTxt.getValue());
                    selectedTrans.setType(typeBox.getValue());

                    // return the updated Transaction
                    return selectedTrans;
                }

                // If OK is not clicked, then just don't update that transaction
                return null;
            }
        });

        // If the updated Transaction is not, try to update that transaction in the database
        // If it is null, just don't do anything to it
        Optional<Transaction> transaction = dialog.showAndWait();
        if (transaction.isPresent()) {
            // Try to update the transaction
            // If there is a problem updating, notify the user
            try {
                db.updateTransaction(transaction.get());
                transactions.clear();
                transactions.addAll(db.findTransactions(toDate.getValue(), fromDate.getValue()));
                selectedTrans = null;
                delete.setDisable(true);
                edit.setDisable(true);
            } catch (Exception e) {
                createAlert(Alert.AlertType.ERROR, "\"" + selectedTrans.getDescription() + "\" could not be updated.");
            }
        }
    }

    /**
     * Handles the click event of the delete button
     * @param event
     */
    @FXML
    public void deleteClick(ActionEvent event) {

        // We want to confirm that the user intended on clicking the delete button first
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete transaction \"" + selectedTrans.getDescription() + "\" ?",
                  ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        // If they click "Yes", it means they meant to delete the transaction
        // We will go ahead and delete it from the database, and retrieve an updated
        // list of transactions
        // If they click "No", then they did not mean to, or have changed their mind
        // We will just go ahead and close the alert, and do nothing with that transaction
        if (alert.getResult() == ButtonType.YES) {
            // We will try to delete the transaction from the database
            // If there is a problem, we will notify the user that there was an issue
            try {
                db.deleteTransaction(selectedTrans);
                transactions.clear();
                transactions.addAll(db.findTransactions(toDate.getValue(), fromDate.getValue()));
                selectedTrans = null;
                delete.setDisable(true);
                edit.setDisable(true);
            } catch (Exception e) {
                createAlert(Alert.AlertType.ERROR, "\"" + selectedTrans.getDescription() + "\" could not be deleted.");
            }
        } else {
            selectedTrans = null;
            delete.setDisable(true);
            edit.setDisable(true);
        }

    }

    /**
     * Handes the change event of the DatePicker
     * @param event
     */
    @FXML
    private void dateChange(ActionEvent event) {
        // Try to fetch a list of transactions using the new date(s)
        // If there is a problem, notify the user
        try {
            transactions.clear();
            transactions.addAll(db.findTransactions(toDate.getValue(), fromDate.getValue()));
        } catch (Exception e) {
            createAlert(Alert.AlertType.ERROR, "\"" + selectedTrans.getDescription() + "\" could not be fetched.");
        }
    }

    /**
     * Handles the mouse click event of a selected row
     * @param event
     */
    @FXML
    private void valueSelected(MouseEvent event) {
        delete.setDisable(false);
        edit.setDisable(false);
        selectedTrans = (Transaction)tv.getSelectionModel().getSelectedItem();
    }

    /**
     * Add a given list to the TableView
     * @param list
     */
    private void addListToTable(ObservableList<Transaction> list) {
        // Change the total text to be the calculated total
        double total = getTotal(transactions);
        totAmt.setText(Double.toString(total));

        // Clear the TableView of items, then add the new list
        tv.getItems().clear();
        tv.getItems().addAll(list);

        // If the total is positive, highlight the total in green
        // If the total is negative, make it red
        // If the total is 0, make it orange
        if (total > 0d) {
            totAmt.setTextFill(Color.GREEN);
        } else if (total < 0d) {
            totAmt.setTextFill(Color.RED);
        } else {
            totAmt.setTextFill(Color.ORANGE);
        }

        // Populate the chart with the list values
        populateChart(list);
    }

    /**
     * Handles the population of the BarChart of values
     * @param list
     */
    private void populateChart(ObservableList<Transaction> list) {

        double incomeTot, essentialTot, nonEssentialTot, fixedTot;
        incomeTot = essentialTot = nonEssentialTot = fixedTot = 0d;

        // Loop through the list of Transactions
        // We need to determine the type of transaction is it
        // Add the transaction amount to its respective total type
        for (int i = 0; i < list.size(); i++) {
            switch (list.get(i).getType().getId()) {
                case 1:
                    incomeTot += list.get(i).getAmount();
                    break;
                case 2:
                    essentialTot += list.get(i).getAmount();
                    break;
                case 3:
                    nonEssentialTot += list.get(i).getAmount();
                    break;
                case 4:
                    fixedTot += list.get(i).getAmount();
                    break;
                default:
            }
        }

        // Update the values in the BarChart
        chart.getData().get(0).getData().get(0).setYValue(incomeTot);
        chart.getData().get(0).getData().get(1).setYValue(essentialTot);
        chart.getData().get(0).getData().get(2).setYValue(nonEssentialTot);
        chart.getData().get(0).getData().get(3).setYValue(fixedTot);
    }

    /**
     * Calculates the total amount of the transactions
     * @param transactions
     * @return double total
     */
    private double getTotal(ObservableList<Transaction> transactions) {
        List<Double> nums = new ArrayList<Double>();

        // Loop through the list of transactions to extract the amounts
        // If the type is not income, make it negative
        // If it is, leave it positive
        transactions.forEach((t) -> {
            if (t.getType().getId() != 1) {
                nums.add(-t.getAmount());
            } else {
                nums.add(t.getAmount());
            }
        });

        // Return the sum of the amounts
        return nums.stream().reduce(0d, (sub, n) -> {
            return sub + n;
        });
    }

    /**
     * Handles the creation of an Alert
     * @param type
     * @param message
     */
    private void createAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.showAndWait();
        selectedTrans = null;
        delete.setDisable(true);
        edit.setDisable(true);
    }

    /**
     * Handles the initialization of the Controller
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Initialize the values of the DatePickers
        // Make the value of the from date the first day of the current month
        // Make the value of the to date the current date
        fromDate.setValue(LocalDate.now().withDayOfMonth(1));
        toDate.setValue(LocalDate.now());

        // We need to tell the columns with property to refer to in the Transaction class
        // for Type, we need to specify that it use the description in the Type class
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeCol.setCellValueFactory(data -> {
            return new SimpleStringProperty(data.getValue().getType().getDescription());
        });

        // Initialize the BarChart
        XYChart.Data incomeData = new XYChart.Data("Income", 0.0);
        XYChart.Data essentialData = new XYChart.Data("Essential", 0.0);
        XYChart.Data nonEssentialData = new XYChart.Data("Non-Essential", 0.0);
        XYChart.Data fixedData = new XYChart.Data("Fixed", 0.0);

        XYChart.Series currentSeries = new XYChart.Series();
        currentSeries.getData().addAll(incomeData, essentialData, nonEssentialData, fixedData);
        currentSeries.setName("Current");
        chart.getData().add(currentSeries);

        types = FXCollections.observableArrayList();
        transactions = FXCollections.observableArrayList();

        // We want to attempt to fetch a list of transactions from the database when the app launches
        // If there is a problem, notify the user
        try {
            transactions.addListener(new ListChangeListener<Transaction>() {
                @Override
                public void onChanged(Change<? extends Transaction> change) {
                    addListToTable(transactions);
                }
            });
            types.addAll(db.findAllTypes());
            transactions.addAll(db.findTransactions(toDate.getValue(), fromDate.getValue()));
        } catch (Exception e) {
            createAlert(Alert.AlertType.ERROR, "\"" + selectedTrans.getDescription() + "\" could not be fetched.");
        }
    }

}
