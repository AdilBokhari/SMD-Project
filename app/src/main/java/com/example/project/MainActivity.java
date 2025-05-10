package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity implements RestaurantAdapter.onResturantClicklistener {

    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewPagerAdapter adapter;

    FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();

        new TabLayoutMediator(
                tabLayout,
                viewPager2,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position) {
                            case 0:
                                tab.setText("Food");
                                BadgeDrawable badge = tab.getOrCreateBadge();
                                tab.setIcon(R.drawable.food_icon);
                                break;
                            case 1:
                                tab.setText("Cart");
                                BadgeDrawable badge1 = tab.getOrCreateBadge();
                                tab.setIcon(R.drawable.cart_icon);
                                break;
                            case 2:
                                tab.setText("Profile");
                                BadgeDrawable badge2 = tab.getOrCreateBadge();
                                tab.setIcon(R.drawable.profile_icon);
                                break;
                        }
                    }
                }
        ).attach();

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                BadgeDrawable badge = tabLayout.getTabAt(position).getOrCreateBadge();
                badge.setVisible(false);
                badge.setNumber(0);
            }
        });

        // Check if we should open the Cart fragment
        if (getIntent().getBooleanExtra("openCartFragment", false)) {
            viewPager2.setCurrentItem(1); // Index 1 is the Cart fragment
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("openCartFragment", false)) {
            viewPager2.setCurrentItem(1);
        }
    }

    public void init()
    {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewpager2);
        adapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(adapter);
        manager = getSupportFragmentManager();
    }

    @Override
    public void onResturantClick(String restaurantId) {
        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
        intent.putExtra("restaurantId", restaurantId);
        startActivity(intent);
    }
}