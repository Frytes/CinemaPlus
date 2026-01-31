package com.frytes.cinemaPlus.notification.service;

import com.frytes.cinemaPlus.content.dto.SocketSeatMessage;

public interface SeatSyncService {
    void distribute(SocketSeatMessage message);
}