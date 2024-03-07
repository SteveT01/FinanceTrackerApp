package com.example.financetrackerapp.model;

// Importing the Serializable interface to allow AmountModel objects to be serialized.
import java.io.Serializable;

// Declaration of the AmountModel class that implements the Serializable interface.
public class AmountModel implements Serializable {
    // Unique identifier for the transaction.
    String amountId, userId, title, amount, details, image,dateTime; // Identifier of the user associated with this transaction with title, value, date and details of transactions with image associated.

    // Default constructor for creating an instance without setting any data initially.
    public AmountModel() {
    }

    // Constructor with parameters to initialize a new instance of AmountModel with specific values.
    public AmountModel(String amountId, String userId, String title, String amount, String details, String image, String dateTime) {
        this.amountId = amountId; // Sets the transaction's unique identifier.
        this.userId = userId; // Sets the user's identifier
        this.title = title; // Sets the title or name of the transaction.
        this.amount = amount; // Sets tne monetary value of the transaction.
        this.details = details; // Sets additional details about the transaction.
        this.image = image; // Sets the URL or local path to an image associated with the transaction.
        this.dateTime = dateTime; // Sets the date and time of the transaction.
    }

    // Getter and setter methods for each field, providing controlled access to the fields of the class.

    public String getAmountId() {
        return amountId;
    }

    public void setAmountId(String amountId) {
        this.amountId = amountId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    // Additional method to get a formatted version of the date part from the dateTime string.
    public String getFormattedDate() {
        // Assumes dateTime is in "dd-MM-yyyy, HH:mm:ss" format and splits it to return only the date part.
        return dateTime.split(",")[0]; // Extracting "dd-MM-yyyy" part
    }

}
