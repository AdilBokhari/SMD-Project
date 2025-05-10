package com.example.project;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersActivity extends AppCompatActivity implements AdminOrderAdapter.OrderStatusChangeListener {

    private RecyclerView recyclerView;
    private AdminOrderAdapter adapter;
    private List<Order> orderList;
    private TextView emptyOrdersText;
    private DatabaseReference ordersRef;
    private ValueEventListener ordersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Orders");
        }

        recyclerView = findViewById(R.id.adminOrdersRecyclerView);
        emptyOrdersText = findViewById(R.id.emptyOrdersText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        adapter = new AdminOrderAdapter(this, orderList, this);
        recyclerView.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null && !Order.STATUS_DELIVERED.equals(order.getStatus())) {
                        orderList.add(order);
                    }
                }

                adapter.notifyDataSetChanged();

                if (orderList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyOrdersText.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyOrdersText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminOrdersActivity.this,
                        "Failed to load orders: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        ordersRef.addValueEventListener(ordersListener);
    }

    @Override
    public void onOrderStatusChange(Order order, String newStatus) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("orders")
                .child(order.getId());

        orderRef.child("status").setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Order status updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update order status: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ordersRef != null && ordersListener != null) {
            ordersRef.removeEventListener(ordersListener);
        }
    }
}