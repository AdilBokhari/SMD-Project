package com.example.project;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RestaurantListFrag extends Fragment {
    RecyclerView recyclerView;
    RestaurantAdapter adapter;
    List<Restaurant> restaurantList;
    DatabaseReference dbRef;
    FloatingActionButton fabAddResturant;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resturant_list, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.restaurantRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        restaurantList = new ArrayList<>();
        adapter = new RestaurantAdapter(getContext(), restaurantList);
        recyclerView.setAdapter(adapter);
        fabAddResturant = view.findViewById(R.id.fabAddNewRestaurant);
        dbRef = FirebaseDatabase.getInstance().getReference("restaurants");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                restaurantList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Restaurant restaurant = snap.getValue(Restaurant.class);
                    restaurantList.add(restaurant);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        fabAddResturant.setOnClickListener((v)->{
            addRestaurant();
        });
    }
    private void addRestaurant() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New Restaurant");

        View v = LayoutInflater.from(getContext()).inflate(R.layout.edit_add_restaurant_dialog, null);
        builder.setView(v);

        EditText etName = v.findViewById(R.id.etName);
        EditText etDesc = v.findViewById(R.id.etDes);
        EditText etImg = v.findViewById(R.id.etImageURL);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String imageUrl = etImg.getText().toString().trim();

            if (name.isEmpty() || desc.isEmpty() || imageUrl.isEmpty()) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("restaurants");
            String key = dbRef.push().getKey();

            Restaurant restaurant = new Restaurant(key, name, desc, imageUrl);
            dbRef.child(key).setValue(restaurant);

            Toast.makeText(getContext(), "Restaurant Added", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }

}
