package com.example.project;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity implements MenuAdapter.onEditDel {

    RecyclerView recyclerView;
    MenuAdapter adapter;
    List<MenuItem> menuItemsList;
    DatabaseReference dbRef;
    private String restaurantId;
    FloatingActionButton fabAddItem;
    SharedPreferences sharedPref;
    String userRole;
    Restaurant restaurant;
    Button btnViewCart;

    ImageView ivRes;
    TextView tvName, tvItems;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.menu), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvName = findViewById(R.id.restaurantName);
        ivRes = findViewById(R.id.ivRestaurant);
        btnViewCart = findViewById(R.id.btnViewCart);
        tvItems = findViewById(R.id.tvItems);

        Intent intent = getIntent();
        restaurantId = intent.getStringExtra("restaurantId");
        if (restaurantId == null || restaurantId.isEmpty()) return;
        recyclerView = findViewById(R.id.MenuRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        menuItemsList = new ArrayList<>();
        adapter = new MenuAdapter(menuItemsList, this);
        recyclerView.setAdapter(adapter);
        fabAddItem = findViewById(R.id.fabAddNewItem);

        dbRef = FirebaseDatabase.getInstance().getReference("menuItems");
        Query query = dbRef.orderByChild("restaurantId").equalTo(restaurantId);

        DatabaseReference restaurantsRef = FirebaseDatabase.getInstance().getReference("restaurants");

        restaurantsRef.child(restaurantId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    restaurant = snapshot.getValue(Restaurant.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MenuActivity.this, "Failed to load restaurant info.", Toast.LENGTH_SHORT).show();
            }
        });

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                menuItemsList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    MenuItem item = snap.getValue(MenuItem.class);
                    menuItemsList.add(item);
                }
                adapter.notifyDataSetChanged();

                // ✅ Show/hide views based on list state
                if (menuItemsList.isEmpty()) {
                    tvItems.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvItems.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                tvName.setText(restaurant.getName().trim());

                String imageUrl = restaurant.getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(MenuActivity.this).load(imageUrl).into(ivRes);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        sharedPref = this.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userRole = sharedPref.getString("userRole", "user"); // Default to "user" if not found

        if ("admin".equals(userRole)) {
            fabAddItem.setVisibility(View.VISIBLE);  // Make the floating button visible if role is admin
        } else {
            fabAddItem.setVisibility(View.GONE);  // Hide the floating button otherwise
        }

        fabAddItem.setOnClickListener((v)->{
            addItem(restaurantId);
        });

        btnViewCart.setOnClickListener(v -> {
            Intent mainIntent = new Intent(MenuActivity.this, MainActivity.class);
            mainIntent.putExtra("openCartFragment", true);
            startActivity(mainIntent);
        });
    }

    private void addItem(String restaurantId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Menu Item");

        View v = LayoutInflater.from(this).inflate(R.layout.edit_add_item_dialog, null);
        builder.setView(v);

        EditText etName = v.findViewById(R.id.etName);
        EditText etDesc = v.findViewById(R.id.etDes);
        EditText etImg = v.findViewById(R.id.etImageURL);
        EditText etPrice = v.findViewById(R.id.etPrice);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String imageUrl = etImg.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();

            if (name.isEmpty() || desc.isEmpty() || imageUrl.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter a valid price", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("menuItems");
            String key = dbRef.push().getKey();

            MenuItem menuItem = new MenuItem(key, name, desc, price, imageUrl, restaurantId);
            dbRef.child(key).setValue(menuItem);

            Toast.makeText(this, "Menu Item Added", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    @Override
    public void onEdit(MenuItem r) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("menuItems")
                .child(r.getId());

        dbRef.setValue(r)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onDelete(String ID, int position) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("menuItems");
        dbRef.child(ID).removeValue();

        menuItemsList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void onAdd() {
        counter++;
        if(counter>0){
            btnViewCart.setText("View Cart ("+counter+")");
        }
    }
}