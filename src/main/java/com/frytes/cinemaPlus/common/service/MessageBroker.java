package com.frytes.cinemaPlus.common.service;

import java.util.concurrent.CompletableFuture;

public interface MessageBroker {
    CompletableFuture<Void> send(String topic, String key, Object data);
}