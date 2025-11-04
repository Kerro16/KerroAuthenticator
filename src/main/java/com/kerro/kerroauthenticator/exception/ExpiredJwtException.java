package com.kerro.kerroauthenticator.exception;

public class ExpiredJwtException extends RuntimeException{
    public ExpiredJwtException(){
        super("Jwt is expired");
    }
}