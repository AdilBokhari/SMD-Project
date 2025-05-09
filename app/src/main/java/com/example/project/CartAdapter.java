package com.example.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private Context context;
    private String userId;
    private CartItemListener listener;

    public interface CartItemListener {
        void onCartUpdated();
    }

    public CartAdapter(Context context, List<CartItem> cartItems, String userId, CartItemListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.userId = userId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);

        holder.nameTextView.setText(cartItem.getName());
        holder.descTextView.setText(cartItem.getDescription());
        holder.priceTextView.setText("$" + String.format("%.2f", cartItem.getPrice()));
        holder.quantityTextView.setText(String.valueOf(cartItem.getQuantity()));
        holder.totalTextView.setText("$" + String.format("%.2f", cartItem.getTotalPrice()));

        Glide.with(context).load(cartItem.getImageUrl()).into(holder.imageView);

        holder.btnIncrease.setOnClickListener(v -> {
            int newQuantity = cartItem.getQuantity() + 1;
            updateCartItemQuantity(cartItem, newQuantity);
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (cartItem.getQuantity() > 1) {
                int newQuantity = cartItem.getQuantity() - 1;
                updateCartItemQuantity(cartItem, newQuantity);
            } else {
                removeCartItem(cartItem, position);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            removeCartItem(cartItem, position);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    private void updateCartItemQuantity(CartItem cartItem, int newQuantity) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("carts")
                .child(userId)
                .child(cartItem.getId())
                .child("quantity");

        cartRef.setValue(newQuantity)
                .addOnSuccessListener(aVoid -> {
                    cartItem.setQuantity(newQuantity);
                    notifyDataSetChanged();
                    if (listener != null) {
                        listener.onCartUpdated();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeCartItem(CartItem cartItem, int position) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("carts")
                .child(userId)
                .child(cartItem.getId());

        cartRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    cartItems.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, cartItems.size());
                    if (listener != null) {
                        listener.onCartUpdated();
                    }
                    Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show();
                });
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView, descTextView, priceTextView, quantityTextView, totalTextView;
        Button btnIncrease, btnDecrease, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cartItemImage);
            nameTextView = itemView.findViewById(R.id.cartItemName);
            descTextView = itemView.findViewById(R.id.cartItemDesc);
            priceTextView = itemView.findViewById(R.id.cartItemPrice);
            quantityTextView = itemView.findViewById(R.id.cartItemQuantity);
            totalTextView = itemView.findViewById(R.id.cartItemTotal);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}