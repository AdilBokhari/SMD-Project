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
public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {
    private List<Restaurant> restaurantList;
    onResturantClicklistener click;
    String restaurantId;
    RestaurantListFrag frag;
    Context c;
    SharedPreferences sharedPref;
    String userRole;
    public interface onResturantClicklistener {
        void onResturantClick(String resturantId);
    }

    public interface onEditDel{
        void onEdit(Restaurant r);
        void onDelete(String ID, int position);
    }
    public RestaurantAdapter(Context context,
                             List<Restaurant> restaurantList, RestaurantListFrag fragRes) {
        this.restaurantList = restaurantList;
        this.c = context;
        click = (onResturantClicklistener) context;
        this.frag = fragRes;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameText, descText;
        public ImageView imageView;
        ImageButton ibEdit, ibDel;
        public ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.restaurantName);
            descText = view.findViewById(R.id.restaurantDesc);
            imageView = view.findViewById(R.id.restaurantImage);
            ibEdit = view.findViewById(R.id.ibEdit);
            ibDel = view.findViewById(R.id.ibDel);
        }
    }
    @Override
    public RestaurantAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Restaurant r = restaurantList.get(position);
        holder.nameText.setText(r.getName());
        holder.descText.setText(r.getDescription());
        Glide.with(holder.itemView.getContext()).load(r.getImageUrl()).into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            restaurantId = r.getId();
            click.onResturantClick(restaurantId);
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
        holder.ibDel.setOnClickListener(v -> {
            new AlertDialog.Builder(c)
                    .setTitle("Confirmation")
                    .setMessage("Are you sure you want to delete this restaurant?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        String restaurantId = r.getId();
                        frag.onDelete(restaurantId, position); // call fragment method
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        });
        holder.ibEdit.setOnClickListener(v -> {
            editRestaurant(r);
        });
    }
    @Override
    public int getItemCount() {
        return restaurantList.size();
    }
    public void editRestaurant(Restaurant restaurant){
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Edit Restaurant");

        View v = LayoutInflater.from(c).inflate(R.layout.edit_add_restaurant_dialog, null);
        builder.setView(v);

        EditText etName = v.findViewById(R.id.etName);
        EditText etDesc = v.findViewById(R.id.etDes);
        EditText etImg = v.findViewById(R.id.etImageURL);

        etName.setText(restaurant.getName());
        etDesc.setText(restaurant.getDescription());
        etImg.setText(restaurant.getImageUrl());

        builder.setPositiveButton("Update", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String img = etImg.getText().toString().trim();

            if (name.isEmpty() || desc.isEmpty() || img.isEmpty()) {
                Toast.makeText(c, "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }

            restaurant.setName(name);
            restaurant.setDescription(desc);
            restaurant.setImageUrl(img);

            frag.onEdit(restaurant); // Call fragment to update Firebase
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }
}