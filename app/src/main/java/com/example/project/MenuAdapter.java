package com.example.project;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    private List<MenuItem> MenuList;
    onEditDel menu;
    Context c;
    SharedPreferences sharedPref;
    String userRole;
    public MenuAdapter(List<com.example.project.MenuItem> MenuList, Context context) {
        this.MenuList = MenuList;
        menu = (onEditDel)context;
        this.c = context;
    }

    public interface onEditDel{
        void onEdit(MenuItem r);
        void onDelete(String ID, int position);
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameText, descText;
        public ImageView imageView;
        ImageButton ibEdit, ibDel;
        public ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.menuItemName);
            descText = view.findViewById(R.id.menuItemDesc);
            imageView = view.findViewById(R.id.menuItemImage);
            ibEdit = view.findViewById(R.id.ibEdit);
            ibDel = view.findViewById(R.id.ibDel);
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
        } else {
            holder.ibDel.setVisibility(View.GONE);
            holder.ibEdit.setVisibility(View.GONE);
        }
        holder.ibEdit.setOnClickListener(v -> {
            editItem(r);
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

            menu.onEdit(item); // Call activity to update Firebase
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }
}
