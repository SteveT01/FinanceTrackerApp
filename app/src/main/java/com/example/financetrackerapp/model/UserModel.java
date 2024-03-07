package com.example.financetrackerapp.model;

// Declaration of the UserModel class.
public class UserModel {
    // User's details.
    String name, email, password, image;

    // Default constructor.
    public UserModel() {
    }

    // Constructor with parameters to initialize a new instance of UserModel with specific values.
    public UserModel(String name, String email, String password, String image) {
        this.name = name; // Sets the user's name.
        this.email = email; // Sets the user's email
        this.password = password; // Sets the user's password
        this.image = image; // Sets the user's profile image path or URL.
    }

    // Getter method for the user's name.
    public String getName() {
        return name;
    }

    // Setter method for the user's name. Allows changing the name of the user after object creation.
    public void setName(String name) {
        this.name = name;
    }

    // Getter method for the user's email.
    public String getEmail() {
        return email;
    }

    // Setter method for the user's email. Allows changing the email of the user.
    public void setEmail(String email) {
        this.email = email;
    }

    // Getter method for the user's password.
    public String getPassword() {
        return password;
    }

    // Setter method for the user's password. Allows changing the password of the user.
    public void setPassword(String password) {
        this.password = password;
    }

    // Getter method for the user's profile image path or URL.
    public String getImage() {
        return image;
    }

    // Setter method for the user's profile image. Allows changing the profile image of the user.
    public void setImage(String image) {
        this.image = image;
    }
}
