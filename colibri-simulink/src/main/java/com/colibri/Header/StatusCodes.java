package com.colibri.Header;
// This class provides the different status codes which is sent after the reception of the message
public enum StatusCodes {
    OK(200),
    ERROR_STRUCTURE(300), ERROR_SYNTACTIC(400),
    ERROR_SEMANTIC(500), ERROR_CONNECTION(600), ERROR_PROCESSING(700),
    ERROR_PERMISSION(800);

    private int code;

    StatusCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
