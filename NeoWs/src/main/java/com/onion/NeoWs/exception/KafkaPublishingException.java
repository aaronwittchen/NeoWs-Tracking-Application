package com.onion.NeoWs.exception;

public class KafkaPublishingException extends RuntimeException {
    public KafkaPublishingException(String message) {
        super(message);
    }
    
    public KafkaPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}