package com.example.project;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

public class Order {
    private String id;
    private String userId;
    private String restaurantId;
    private String restaurantName;
    private List<CartItem> items;
    private double totalAmount;
    private String status;
    private long timestamp;
    private String userAddress;

    public static final String STATUS_PLACED = "Order Placed";
    public static final String STATUS_PREPARING = "Preparing";
    public static final String STATUS_OUT_FOR_DELIVERY = "Out for Delivery";
    public static final String STATUS_DELIVERED = "Delivered";

    public Order() {

    }

    public Order(String id, String userId, String restaurantId, String restaurantName,
                 List<CartItem> items, double totalAmount, String userAddress) {
        this.id = id;
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = STATUS_PLACED;
        this.timestamp = new Date().getTime();
        this.userAddress = userAddress;
    }
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("userId", userId);
        result.put("restaurantId", restaurantId);
        result.put("restaurantName", restaurantName);
        result.put("items", items);
        result.put("totalAmount", totalAmount);
        result.put("status", status);
        result.put("timestamp", timestamp);
        result.put("userAddress", userAddress);
        return result;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }
}