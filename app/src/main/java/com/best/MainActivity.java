package com.best;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.best.fragments.AddProductFragment;
import com.best.fragments.CartFragment;
import com.best.fragments.OrdersAsSellerFragment;
import com.best.fragments.OrdersFragment;
import com.best.fragments.ProductsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();
    private Fragment currentFragment = null;
    SessionManager sessionManager;
    RadioGroup productFilterGroup;
    FrameLayout fragmentContainer;
    LinearLayout productFilterLayout;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav           = findViewById(R.id.bottom_nav);
        productFilterGroup  = findViewById(R.id.product_filter_group);
        fragmentContainer   = findViewById(R.id.fragment_container);
        productFilterLayout = findViewById(R.id.product_filter_layout);

        // Load default fragment
        switchFragment(R.id.nav_products);

        bottomNav.setOnItemSelectedListener(item -> {
            switchFragment(item.getItemId());
            return true;
        });

        sessionManager = new SessionManager(getApplicationContext());

        productFilterGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, final int checkedId) {
                if( checkedId == R.id.radio_my_products ){
                    Global.show_my_products = 1;
                }else{
                    Global.show_my_products = 0;
                }
            }
        });
    }

    // Inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    // Handle item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if ( item.getItemId() == R.id.action_profile ) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }
        else if ( item.getItemId() == R.id.action_logout ) {
            sessionManager.clear();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    // Create and keep fragments in memory.
    // Use add(), show(), and hide() instead of replace().

    public void switchFragment(int itemId) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Fragment fragment = fragmentMap.get(itemId);

        if (itemId == R.id.nav_products) {
            productFilterLayout.setVisibility(View.VISIBLE);
        } else {
            productFilterLayout.setVisibility(View.GONE);
        }

        if (fragment == null) {

            // Lazy creation and caching
            if ( itemId == R.id.nav_products ) {
                fragment = new ProductsFragment();
            }
            else if ( itemId == R.id.nav_cart ) {
                fragment = new CartFragment();
            }
            else if ( itemId == R.id.nav_orders ) {
                fragment = new OrdersFragment();
            }
            else if ( itemId == R.id.nav_orders ) {
                fragment = new OrdersFragment();
            }
            else if ( itemId == R.id.nav_orders_as_seller ) {
                fragment = new OrdersAsSellerFragment();
            }
            else if ( itemId == R.id.nav_add ) {
                fragment = new AddProductFragment();
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

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        new AlertDialog.Builder(this)
//                .setMessage("Are you sure you want to exit?")
//                .setCancelable(false)
//                .setPositiveButton("No", null)
//                .setNeutralButton("Yes", new DialogInterface.OnClickListener(){
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        MainActivity.super.onBackPressed();
//                    }
//                })
//                .show();
    }
}
