package com.example.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.CartItemListener {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private TextView emptyCartText, loginPromptText, cartTotalPrice;
    private Button btnCheckout;
    private View cartSummaryLayout;
    private FirebaseAuth mAuth;
    private DatabaseReference cartRef;
    private ValueEventListener cartListener;

    public CartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.cartRecyclerView);
        emptyCartText = view.findViewById(R.id.emptyCartText);
        loginPromptText = view.findViewById(R.id.loginPromptText);
        cartTotalPrice = view.findViewById(R.id.cartTotalPrice);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        cartSummaryLayout = view.findViewById(R.id.cartSummaryLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartItems = new ArrayList<>();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            adapter = new CartAdapter(getContext(), cartItems, userId, this);
            recyclerView.setAdapter(adapter);
            loadCartItems(userId);
            showCartUI();

            btnCheckout.setOnClickListener(v -> {
                //place order here
            });
        } else {
            showLoginPrompt();
        }
    }

    private void loadCartItems(String userId) {
        cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId);

        cartListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear();

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    CartItem cartItem = itemSnapshot.getValue(CartItem.class);
                    if (cartItem != null) {
                        cartItems.add(cartItem);
                    }
                }

                adapter.notifyDataSetChanged();

                if (cartItems.isEmpty()) {
                    showEmptyCart();
                } else {
                    hideEmptyCart();
                }

                updateCartTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load cart items: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        cartRef.addValueEventListener(cartListener);
    }

    private void updateCartTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        cartTotalPrice.setText("$" + String.format("%.2f", total));
    }

    private void showCartUI() {
        recyclerView.setVisibility(View.VISIBLE);
        cartSummaryLayout.setVisibility(View.VISIBLE);
        loginPromptText.setVisibility(View.GONE);
    }

    private void showEmptyCart() {
        recyclerView.setVisibility(View.GONE);
        emptyCartText.setVisibility(View.VISIBLE);
        btnCheckout.setEnabled(false);
    }

    private void hideEmptyCart() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyCartText.setVisibility(View.GONE);
        btnCheckout.setEnabled(true);
    }

    private void showLoginPrompt() {
        recyclerView.setVisibility(View.GONE);
        emptyCartText.setVisibility(View.GONE);
        cartSummaryLayout.setVisibility(View.GONE);
        loginPromptText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCartUpdated() {
        updateCartTotal();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cartRef != null && cartListener != null) {
            cartRef.removeEventListener(cartListener);
        }
        cartItems.clear();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            if (cartRef != null && cartListener != null) {
                cartRef.removeEventListener(cartListener);
            }
            cartItems.clear();
            if (adapter != null) adapter.notifyDataSetChanged();
            adapter = new CartAdapter(getContext(), cartItems, userId, this);
            recyclerView.setAdapter(adapter);
            loadCartItems(userId);
            showCartUI();
        } else {
            showLoginPrompt();
        }
    }
}
