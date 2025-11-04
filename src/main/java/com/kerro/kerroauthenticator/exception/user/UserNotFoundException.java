package com.kerro.kerroauthenticator.exception.user;

public class UserNotFoundException extends UserException {
    public UserNotFoundException(Long id){
        super(id != null
                ? "Cannot find user with ID:" + id
                :"No users found in the system"
        );
    }
}