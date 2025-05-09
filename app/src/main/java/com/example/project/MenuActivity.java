package com.example.project;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MenuAdapter adapter;
    List<MenuItem> menuItemsList;
    DatabaseReference dbRef;
    private String restaurantId;
    FloatingActionButton fabAddItem;

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

        restaurantId = getIntent().getStringExtra("restaurantId");
        if (restaurantId == null || restaurantId.isEmpty()) return;
        recyclerView = findViewById(R.id.MenuRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        menuItemsList = new ArrayList<>();
        adapter = new MenuAdapter(menuItemsList);
        recyclerView.setAdapter(adapter);
        fabAddItem = findViewById(R.id.fabAddNewItem);

        dbRef = FirebaseDatabase.getInstance().getReference("menuItems");
        Query query = dbRef.orderByChild("restaurantId").equalTo(restaurantId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                menuItemsList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    MenuItem item = snap.getValue(MenuItem.class);
                    menuItemsList.add(item);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
        fabAddItem.setOnClickListener((v)->{
            addItem(restaurantId);
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
}