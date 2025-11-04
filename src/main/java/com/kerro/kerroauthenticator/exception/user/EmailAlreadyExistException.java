package com.kerro.kerroauthenticator.exception.user;

public class EmailAlreadyExistException extends UserException{

    public EmailAlreadyExistException(String email) {
        super("Email already exists: " + email);
    }
}