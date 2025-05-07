package com.example.project;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {
    private List<Restaurant> restaurantList;
    public RestaurantAdapter(List<Restaurant> restaurantList) {
        this.restaurantList = restaurantList;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameText, descText;
        public ImageView imageView;
        public ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.restaurantName);
            descText = view.findViewById(R.id.restaurantDesc);
            imageView = view.findViewById(R.id.restaurantImage);
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
    }
    @Override
    public int getItemCount() {
        return restaurantList.size();
    }
}
