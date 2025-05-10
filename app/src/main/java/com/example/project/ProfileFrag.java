package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileFrag extends Fragment {

    FirebaseAuth mAuth;
    EditText emailEditText, passwordEditText, nameEditText, addressEditText;
    Button loginButton, signUpButton, logoutButton, btnAdminOrders;
    TextView userEmailTextView, ordersTitle;
    LinearLayout loginLayout, profileLayout;
    RecyclerView ordersRecyclerView;
    OrderAdapter orderAdapter;
    List<Order> orderList;
    Handler handler = new Handler();
    Runnable nameSaveRunnable;
    Runnable addressSaveRunnable;
    DatabaseReference ordersRef;
    ValueEventListener ordersListener;

    public ProfileFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        init(view);
        loginButton.setOnClickListener(v -> loginUser());
        signUpButton.setOnClickListener(v -> registerUser());
        logoutButton.setOnClickListener(v -> logoutUser());
        btnAdminOrders.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AdminOrdersActivity.class));
        });
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(nameSaveRunnable);
            }
            @Override
            public void afterTextChanged(Editable s) {
                nameSaveRunnable = () -> updateUserName();
                handler.postDelayed(nameSaveRunnable, 1000);
            }
        });
        addressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(addressSaveRunnable);
            }
            @Override
            public void afterTextChanged(Editable s) {
                addressSaveRunnable = () -> updateUserAddress();
                handler.postDelayed(addressSaveRunnable, 1000);
            }
        });
        updateUI(mAuth.getCurrentUser());
        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            loginLayout.setVisibility(View.GONE);
            profileLayout.setVisibility(View.VISIBLE);
            userEmailTextView.setText(user.getEmail());
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                nameEditText.setText(displayName);
            } else {
                nameEditText.setText("");
                nameEditText.setHint("Enter your name");
            }
            loadUserAddress(user.getUid());
            loadUserOrders(user.getUid());
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String role = snapshot.child("role").getValue(String.class);
                        if ("admin".equals(role)) {
                            btnAdminOrders.setVisibility(View.VISIBLE);
                        } else {
                            btnAdminOrders.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    btnAdminOrders.setVisibility(View.GONE);
                }
            });
        } else {
            loginLayout.setVisibility(View.VISIBLE);
            profileLayout.setVisibility(View.GONE);
            ordersRecyclerView.setVisibility(View.GONE);
            ordersTitle.setVisibility(View.GONE);
            btnAdminOrders.setVisibility(View.GONE);

            if (ordersRef != null && ordersListener != null) {
                ordersRef.removeEventListener(ordersListener);
            }
        }
    }

    private void loadUserAddress(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String address = dataSnapshot.child("address").getValue(String.class);
                    if (address != null && !address.isEmpty()) {
                        addressEditText.setText(address);
                    } else {
                        addressEditText.setText("");
                        addressEditText.setHint("Enter your address");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load address: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserOrders(String userId) {
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        Query query = ordersRef.orderByChild("userId").equalTo(userId);

        ordersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null) {
                        orderList.add(order);
                    }
                }

                // Sort orders by timestamp (newest first)
                orderList.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));

                orderAdapter.notifyDataSetChanged();

                if (orderList.isEmpty()) {
                    ordersRecyclerView.setVisibility(View.GONE);
                    ordersTitle.setVisibility(View.GONE);
                } else {
                    ordersRecyclerView.setVisibility(View.VISIBLE);
                    ordersTitle.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to load orders: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        query.addValueEventListener(ordersListener);
    }

    private void updateUserName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String newName = nameEditText.getText().toString().trim();
            if (TextUtils.isEmpty(newName)) {
                return;
            }
            if (newName.equals(user.getDisplayName())) {
                return;
            }
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DatabaseReference userRef = FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(user.getUid())
                                    .child("name");
                            userRef.setValue(newName);

                            Toast.makeText(getActivity(), "Name updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Update failed: " +
                                    task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void updateUserAddress() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String newAddress = addressEditText.getText().toString().trim();
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("address");
            userRef.setValue(newAddress)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getActivity(), "Address updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Failed to update address: " +
                                e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), "Email and password required", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
                        updateUI(user);
                        String userId = user.getUid();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                        userRef.get().addOnCompleteListener(databaseTask -> {
                            if (databaseTask.isSuccessful() && databaseTask.getResult() != null) {
                                String role = (String) databaseTask.getResult().child("role").getValue();
                                if (role != null) {
                                    SharedPreferences sharedPref = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("userRole", role);
                                    editor.apply();
                                }
                            } else {
                                SharedPreferences sharedPref = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("userRole", "user");
                                editor.apply();
                            }
                        });

                    } else {
                        Toast.makeText(getActivity(), "Login failed: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), "Email and password required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            String name = "";
                            String role = "user";
                            String address = "";
                            User newUser = new User(uid,name,role,address);
                            DatabaseReference userRef = FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(uid);
                            userRef.setValue(newUser);
                        }
                        Toast.makeText(getActivity(), "Account created", Toast.LENGTH_SHORT).show();
                        updateUI(user);
                    } else {
                        Toast.makeText(getActivity(), "Sign up failed: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("userRole");
        editor.apply();
        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        updateUI(null);
    }

    private void init(View view) {
        mAuth = FirebaseAuth.getInstance();
        loginLayout = view.findViewById(R.id.loginLayout);
        emailEditText = view.findViewById(R.id.editTextEmail);
        passwordEditText = view.findViewById(R.id.editTextPassword);
        loginButton = view.findViewById(R.id.buttonLogin);
        signUpButton = view.findViewById(R.id.buttonSignUp);
        profileLayout = view.findViewById(R.id.profileLayout);
        userEmailTextView = view.findViewById(R.id.textViewUserEmail);
        nameEditText = view.findViewById(R.id.editTextName);
        addressEditText = view.findViewById(R.id.editTextAddress);
        logoutButton = view.findViewById(R.id.buttonLogout);
        btnAdminOrders = view.findViewById(R.id.buttonAdminOrders);
        ordersTitle = view.findViewById(R.id.ordersTitle);
        ordersRecyclerView = view.findViewById(R.id.ordersRecyclerView);
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(getActivity(), orderList);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ordersRecyclerView.setAdapter(orderAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ordersRef != null && ordersListener != null) {
            ordersRef.removeEventListener(ordersListener);
        }
        handler.removeCallbacks(nameSaveRunnable);
        handler.removeCallbacks(addressSaveRunnable);
    }
}