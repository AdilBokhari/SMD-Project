package com.example.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;
    private Context context;
    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.orderIdTextView.setText("Order #" + order.getId().substring(0, 8));
        holder.restaurantNameTextView.setText(order.getRestaurantName());
        holder.orderStatusTextView.setText(order.getStatus());
        holder.orderTotalTextView.setText("$" + String.format("%.2f", order.getTotalAmount()));
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        String dateString = sdf.format(new Date(order.getTimestamp()));
        holder.orderDateTextView.setText(dateString);
        switch (order.getStatus()) {
            case Order.STATUS_PLACED:
                holder.orderStatusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case Order.STATUS_PREPARING:
                holder.orderStatusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case Order.STATUS_OUT_FOR_DELIVERY:
                holder.orderStatusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                break;
            case Order.STATUS_DELIVERED:
                holder.orderStatusTextView.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                break;
        }
        StringBuilder itemsText = new StringBuilder();
        for (int i = 0; i < order.getItems().size(); i++) {
            CartItem item = order.getItems().get(i);
            itemsText.append(item.getQuantity()).append("x ").append(item.getName());
            if (i < order.getItems().size() - 1) {
                itemsText.append(", ");
            }
        }
        holder.orderItemsTextView.setText(itemsText.toString());
    }
    @Override
    public int getItemCount() {
        return orderList.size();
    }
    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, restaurantNameTextView, orderStatusTextView,
                orderTotalTextView, orderDateTextView, orderItemsTextView;
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            restaurantNameTextView = itemView.findViewById(R.id.restaurantNameTextView);
            orderStatusTextView = itemView.findViewById(R.id.orderStatusTextView);
            orderTotalTextView = itemView.findViewById(R.id.orderTotalTextView);
            orderDateTextView = itemView.findViewById(R.id.orderDateTextView);
            orderItemsTextView = itemView.findViewById(R.id.orderItemsTextView);
        }
    }
}