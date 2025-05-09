package com.example.project;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    private List<MenuItem> MenuList;
    public MenuAdapter(List<com.example.project.MenuItem> MenuList) {
        this.MenuList = MenuList;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameText, descText;
        public ImageView imageView;
        public ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.menuItemName);
            descText = view.findViewById(R.id.menuItemDesc);
            imageView = view.findViewById(R.id.menuItemImage);
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
    }
    @Override
    public int getItemCount() {
        return MenuList.size();
    }
}
