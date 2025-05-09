package com.example.project;

public class CartItem {
    private String id;
    private String menuItemId;
    private String userId;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private int quantity;
    private String restaurantId;

    public CartItem() {

    }

    public CartItem(String id, String menuItemId, String userId, String name, String description,
                    double price, String imageUrl, int quantity, String restaurantId) {
        this.id = id;
        this.menuItemId = menuItemId;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.restaurantId = restaurantId;
    }

    public static CartItem fromMenuItem(MenuItem menuItem, String userId, String cartItemId) {
        return new CartItem(
                cartItemId,
                menuItem.getId(),
                userId,
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getImageUrl(),
                1,
                menuItem.getRestaurantId()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public double getTotalPrice() {
        return price * quantity;
    }
}