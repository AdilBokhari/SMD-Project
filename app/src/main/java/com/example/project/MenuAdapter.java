package com.example.project;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    private List<MenuItem> MenuList;
    onEditDel menu;
    Context c;
    SharedPreferences sharedPref;
    String userRole;
    FirebaseAuth mAuth;

    public MenuAdapter(List<com.example.project.MenuItem> MenuList, Context context) {
        this.MenuList = MenuList;
        menu = (onEditDel)context;
        this.c = context;
        mAuth = FirebaseAuth.getInstance();
    }

    public interface onEditDel{
        void onEdit(MenuItem r);
        void onDelete(String ID, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameText, descText, priceText;
        public ImageView imageView;
        ImageButton ibEdit, ibDel;
        Button btnAddToCart;

        public ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.menuItemName);
            descText = view.findViewById(R.id.menuItemDesc);
            priceText = view.findViewById(R.id.menuItemPrice);
            imageView = view.findViewById(R.id.menuItemImage);
            ibEdit = view.findViewById(R.id.ibEdit);
            ibDel = view.findViewById(R.id.ibDel);
            btnAddToCart = view.findViewById(R.id.btnAddToCart);
        }
    }

    @Override
    public MenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MenuItem r = MenuList.get(position);
        holder.nameText.setText(r.getName());
        holder.descText.setText(r.getDescription());
        holder.priceText.setText("$" + String.format("%.2f", r.getPrice()));
        Glide.with(holder.itemView.getContext()).load(r.getImageUrl()).into(holder.imageView);
        holder.ibDel.setOnClickListener(v -> {
            new AlertDialog.Builder(c)
                    .setTitle("Confirmation")
                    .setMessage("Are you sure you want to delete this Menu Item?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        String menuItemId = r.getId();
                        menu.onDelete(menuItemId, position); // call fragment method
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        });
        sharedPref = c.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userRole = sharedPref.getString("userRole", "user"); // Default to "user" if not found
        if ("admin".equals(userRole)) {
            holder.ibDel.setVisibility(View.VISIBLE);
            holder.ibEdit.setVisibility(View.VISIBLE);
            holder.btnAddToCart.setVisibility(View.GONE);
        } else {
            holder.ibDel.setVisibility(View.GONE);
            holder.ibEdit.setVisibility(View.GONE);
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                holder.btnAddToCart.setVisibility(View.VISIBLE);
            } else {
                holder.btnAddToCart.setVisibility(View.GONE);
            }
        }
        holder.ibEdit.setOnClickListener(v -> {
            editItem(r);
        });
        holder.btnAddToCart.setOnClickListener(v -> {
            addToCart(r);
        });
    }
    @Override
    public int getItemCount() {
        return MenuList.size();
    }
    public void editItem(MenuItem item){
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Edit Item");
        View v = LayoutInflater.from(c).inflate(R.layout.edit_add_item_dialog, null);
        builder.setView(v);
        EditText etName = v.findViewById(R.id.etName);
        EditText etDesc = v.findViewById(R.id.etDes);
        EditText etImg = v.findViewById(R.id.etImageURL);
        EditText etPrice = v.findViewById(R.id.etPrice);
        etName.setText(item.getName());
        etDesc.setText(item.getDescription());
        etImg.setText(item.getImageUrl());
        etPrice.setText(String.valueOf(item.getPrice()));
        builder.setPositiveButton("Update", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String img = etImg.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            if (name.isEmpty() || desc.isEmpty() || img.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(c, "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }
            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(c, "Invalid price", Toast.LENGTH_SHORT).show();
                return;
            }
            item.setName(name);
            item.setDescription(desc);
            item.setImageUrl(img);
            item.setPrice(price);
            menu.onEdit(item);
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }
    private void addToCart(MenuItem menuItem) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(c, "Please log in to add items to cart", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("carts");
        String cartItemId = cartRef.push().getKey();
        if (cartItemId == null) {
            Toast.makeText(c, "Failed to generate cart item ID", Toast.LENGTH_SHORT).show();
            return;
        }
        CartItem cartItem = CartItem.fromMenuItem(menuItem, userId, cartItemId);
        cartRef.child(userId).child(cartItemId).setValue(cartItem)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(c, "Added to cart", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(c, "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}