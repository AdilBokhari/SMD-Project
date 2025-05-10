package com.example.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder> {

    private List<Order> orderList;
    private Context context;
    private OrderStatusChangeListener listener;

    public interface OrderStatusChangeListener {
        void onOrderStatusChange(Order order, String newStatus);
    }
    public AdminOrderAdapter(Context context, List<Order> orderList, OrderStatusChangeListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order, parent, false);
        return new AdminOrderViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.orderIdTextView.setText("Order #" + order.getId().substring(0, Math.min(8, order.getId().length())));
        holder.customerNameTextView.setText("Customer: " + order.getUserId());
        holder.restaurantNameTextView.setText(order.getRestaurantName());
        holder.orderStatusTextView.setText(order.getStatus());
        holder.orderTotalTextView.setText("$" + String.format("%.2f", order.getTotalAmount()));
        holder.customerAddressTextView.setText("Address: " + order.getUserAddress());
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
        setupStatusButtons(holder, order, position);
    }

    private void setupStatusButtons(AdminOrderViewHolder holder, Order order, int position) {
        holder.btnPreparing.setEnabled(true);
        holder.btnOutForDelivery.setEnabled(true);
        holder.btnDelivered.setEnabled(true);
        switch (order.getStatus()) {
            case Order.STATUS_PLACED:
                holder.btnOutForDelivery.setEnabled(false);
                holder.btnDelivered.setEnabled(false);
                break;
            case Order.STATUS_PREPARING:
                holder.btnPreparing.setEnabled(false);
                holder.btnDelivered.setEnabled(false);
                break;
            case Order.STATUS_OUT_FOR_DELIVERY:
                holder.btnPreparing.setEnabled(false);
                holder.btnOutForDelivery.setEnabled(false);
                break;
        }

        // Set click listeners
        holder.btnPreparing.setOnClickListener(v -> {
            if (listener != null && !order.getStatus().equals(Order.STATUS_PREPARING)) {
                listener.onOrderStatusChange(order, Order.STATUS_PREPARING);
                order.setStatus(Order.STATUS_PREPARING);
                notifyItemChanged(position);
            }
        });

        holder.btnOutForDelivery.setOnClickListener(v -> {
            if (listener != null && !order.getStatus().equals(Order.STATUS_OUT_FOR_DELIVERY)) {
                listener.onOrderStatusChange(order, Order.STATUS_OUT_FOR_DELIVERY);
                order.setStatus(Order.STATUS_OUT_FOR_DELIVERY);
                notifyItemChanged(position);
            }
        });

        holder.btnDelivered.setOnClickListener(v -> {
            if (listener != null && !order.getStatus().equals(Order.STATUS_DELIVERED)) {
                listener.onOrderStatusChange(order, Order.STATUS_DELIVERED);
                // Remove this item from the list as it's now delivered
                orderList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, orderList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, customerNameTextView, restaurantNameTextView, orderStatusTextView,
                orderTotalTextView, orderDateTextView, orderItemsTextView, customerAddressTextView;
        Button btnPreparing, btnOutForDelivery, btnDelivered;

        public AdminOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.adminOrderIdTextView);
            customerNameTextView = itemView.findViewById(R.id.customerNameTextView);
            restaurantNameTextView = itemView.findViewById(R.id.adminRestaurantNameTextView);
            orderStatusTextView = itemView.findViewById(R.id.adminOrderStatusTextView);
            orderTotalTextView = itemView.findViewById(R.id.adminOrderTotalTextView);
            orderDateTextView = itemView.findViewById(R.id.adminOrderDateTextView);
            orderItemsTextView = itemView.findViewById(R.id.adminOrderItemsTextView);
            customerAddressTextView = itemView.findViewById(R.id.customerAddressTextView);
            btnPreparing = itemView.findViewById(R.id.btnPreparing);
            btnOutForDelivery = itemView.findViewById(R.id.btnOutForDelivery);
            btnDelivered = itemView.findViewById(R.id.btnDelivered);
        }
    }
}