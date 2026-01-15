package com.tourai.develop.kafka.enumType;

public enum KafkaConsumerGroup {

    EMAIL_SERVICE("email-service"),
    LOG_SERVICE("log-service"),
    TEMP_SERVICE("temp-service");

    private final String groupId;
    // Kafka 에서 공식 개념 이름이 "Consumer Group Id"라서 groupId 가 정석..


    KafkaConsumerGroup(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }
}
