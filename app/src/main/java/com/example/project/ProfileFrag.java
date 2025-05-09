package com.example.project;

import android.content.Context;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileFrag extends Fragment {

    FirebaseAuth mAuth;
    EditText emailEditText, passwordEditText, nameEditText;
    Button loginButton, signUpButton, logoutButton;
    TextView userEmailTextView;
    LinearLayout loginLayout, profileLayout;

    Handler handler = new Handler();
    Runnable nameSaveRunnable;

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
                handler.postDelayed(nameSaveRunnable, 1000); // 1 second delay
            }
        });

        // Check if user is already logged in
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
        } else {
            loginLayout.setVisibility(View.VISIBLE);
            profileLayout.setVisibility(View.GONE);
        }
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
                            Toast.makeText(getActivity(), "Name updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Update failed: " +
                                    task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
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

                        // Retrieve the role from Firebase Database or set a default role
                        String userId = user.getUid();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                        userRef.get().addOnCompleteListener(databaseTask -> {
                            if (databaseTask.isSuccessful() && databaseTask.getResult() != null) {
                                String role = (String) databaseTask.getResult().child("role").getValue();
                                if (role != null) {
                                    // Store the role in SharedPreferences
                                    SharedPreferences sharedPref = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("userRole", role);
                                    editor.apply(); // Save the role
                                }
                            } else {
                                // If role doesn't exist in DB, store a default role
                                SharedPreferences sharedPref = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("userRole", "user"); // Default role
                                editor.apply(); // Save the role
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
                            String name = ""; // optional: set from nameEditText if desired
                            String imageUrl = ""; // optional: set default image URL
                            String role = "user"; // default role

                            // Create User object
                            User newUser = new User(uid, name, imageUrl, role);

                            // Save to Firebase Realtime Database
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
        editor.remove("userRole"); // Removes the key-value pair
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
        logoutButton = view.findViewById(R.id.buttonLogout);
    }
}