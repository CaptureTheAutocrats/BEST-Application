package com.best;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.best.fragments.AddProductFragment;
import com.best.fragments.HomeFragment;
import com.best.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);
        loadFragment(new HomeFragment()); // default fragment

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            if ( item.getItemId() == R.id.nav_home ){
                selected = new HomeFragment();
            }
            else if ( item.getItemId() == R.id.nav_add ) {
                selected = new AddProductFragment();
            }
            else if ( item.getItemId() == R.id.nav_profile ){
                selected = new ProfileFragment();
            }
            return loadFragment(selected);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
