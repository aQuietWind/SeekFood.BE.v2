package com.seek.food.config.Data;

public class QueueData {
    private String name;
    private String routingKey;

    public QueueData() {
    }

    public QueueData(String name, String routingKey) {
        this.name = name;
        this.routingKey = routingKey;
    }

    /**
     * 获取
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 设置
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取
     * @return routingKey
     */
    public String getRoutingKey() {
        return routingKey;
    }

    /**
     * 设置
     * @param routingKey
     */
    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String toString() {
        return "QueueData{name = " + name + ", routingKey = " + routingKey + "}";
    }
}
