package com.example.histoquiz.activities;
import com.example.histoquiz.R;
import com.example.histoquiz.adapters.FriendsAdapter;
import com.example.histoquiz.adapters.MyAccountAdapter;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Objects;

public class MyAccountActivity extends AppCompatActivity {

    protected int tabSelectedIconColor, tabUnselectedIconColor;
    protected TabLayout tabLayout;
    protected TabItem tabPerfil, tabAmigos, tabDesempenho;
    protected ViewPager2 viewPager;
    protected MyAccountAdapter myAccountAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_minha_conta);
        initGui();
        tabLayout.selectTab(tabLayout.getTabAt(0));
        tabSelectorManager();
    }


    protected void initGui(){
        tabLayout = findViewById(R.id.tabBar);
        tabPerfil = findViewById(R.id.tabPerfil);
        tabAmigos = findViewById(R.id.tabAmigos);
        tabDesempenho = findViewById(R.id.tabDesempenho);
        viewPager = findViewById(R.id.viewPager);
        tabSelectedIconColor = ContextCompat.getColor(MyAccountActivity.this, R.color.darkerPurple);
        tabUnselectedIconColor = ContextCompat.getColor(MyAccountActivity.this, R.color.white);
        myAccountAdapter = new MyAccountAdapter(getSupportFragmentManager(), getLifecycle(), tabLayout.getTabCount(), this);
        viewPager.setAdapter(myAccountAdapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position){
                case 0:
                    tab.setIcon(R.drawable.ic_id_card);
                    tab.setText(getString(R.string.perfil));
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(tabSelectedIconColor, PorterDuff.Mode.SRC_IN);
                    break;
                case 1:
                    tab.setIcon(R.drawable.ic_user_friends);
                    tab.setText(getString(R.string.amigos));
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(tabSelectedIconColor, PorterDuff.Mode.SRC_IN);
                    break;
                case 2:
                    tab.setIcon(R.drawable.ic_chart_pie);
                    tab.setText(getString(R.string.desempenho));
                    Objects.requireNonNull(tab.getIcon()).setColorFilter(tabSelectedIconColor, PorterDuff.Mode.SRC_IN);
                    break;
            }
        }).attach();
        Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(0)).getIcon()).setColorFilter(tabSelectedIconColor, PorterDuff.Mode.SRC_IN);
        Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(1)).getIcon()).setColorFilter(tabUnselectedIconColor, PorterDuff.Mode.SRC_IN);
        Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(2)).getIcon()).setColorFilter(tabUnselectedIconColor, PorterDuff.Mode.SRC_IN);
    }


    protected void tabSelectorManager(){
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                Objects.requireNonNull(tab.getIcon()).setColorFilter(tabSelectedIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Objects.requireNonNull(tab.getIcon()).setColorFilter(tabUnselectedIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}