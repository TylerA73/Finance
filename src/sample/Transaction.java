package sample;

import java.time.LocalDate;

/**
 * Transaction
 *
 * Description: Created to represent records from the transactions table
 */
public class Transaction {

    private int id;
    private String description;
    private double amount;
    private LocalDate date;
    private Type type;

    public Transaction(String description, double amount, LocalDate date, Type type) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.type = type;
    }

    public Transaction(int id, String description, double amount, LocalDate date, Type type) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.type = type;
    }

    public void setId(int id) {this.id = id;}

    public int getId() {return this.id;}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
