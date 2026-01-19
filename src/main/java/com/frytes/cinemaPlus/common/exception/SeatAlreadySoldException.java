package com.frytes.cinemaPlus.common.exception;

public class SeatAlreadySoldException extends RuntimeException {
    public SeatAlreadySoldException(String message) {
        super(message);
    }
}
