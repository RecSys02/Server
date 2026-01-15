package com.tourai.develop.kafka.enumType;

public enum KafkaTopic {

    TEMP("temp"),
    USER_SIGNED_UP("user.signed-up"),
    EMAIL_SEND("email.send");


    private final String topic;

    KafkaTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }
}
