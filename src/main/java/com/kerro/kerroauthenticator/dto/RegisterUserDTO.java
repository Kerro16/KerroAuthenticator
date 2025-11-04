package com.kerro.kerroauthenticator.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class RegisterUserDTO {

    @NotEmpty(message = "The username is required")
    private String username;

    @NotEmpty(message = "The email is required")
    @Email(message = "The email address is invalid")
    private String email;

    @NotEmpty(message = "The password is required")
    @Size(min = 6, message = "The password must be at least 6 character long")
    private String password;

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }


    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public RegisterUserDTO() {
        //Empty constructor
    }
}
