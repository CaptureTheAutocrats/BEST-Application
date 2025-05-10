package com.best;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.best.fragments.AddProductFragment;
import com.best.fragments.CartFragment;
import com.best.fragments.ProductsFragment;
import com.best.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();
    private Fragment currentFragment = null;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        // Load default fragment
        switchFragment(R.id.nav_products);

        bottomNav.setOnItemSelectedListener(item -> {
            switchFragment(item.getItemId());
            return true;
        });

    }

    // Create and keep fragments in memory.
    // Use add(), show(), and hide() instead of replace().

    private void switchFragment(int itemId) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Fragment fragment = fragmentMap.get(itemId);
        if (fragment == null) {
            // Lazy creation and caching
            if ( itemId == R.id.nav_products ) {
                fragment = new ProductsFragment();
            } else if ( itemId == R.id.nav_cart ) {
                fragment = new CartFragment();
            } else if ( itemId == R.id.nav_add ) {
                fragment = new AddProductFragment();
            } else if ( itemId == R.id.nav_profile ) {
                fragment = new ProfileFragment();
            }
            if (fragment != null) {
                fragmentMap.put(itemId, fragment);
                transaction.add(R.id.fragment_container, fragment);
            }
        }

        // Hide current fragment
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }

        // Show selected fragment
        if (fragment != null) {
            transaction.show(fragment);
            currentFragment = fragment;
        }

        transaction.commit();
    }
}
