package com.example.ilya.recognizemultproj;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by ilya_ on 24.12.2017.
 */

public class ServerActivity extends AppCompatActivity {

    private EditText inputIpServer;
    private EditText inputPortServer;
    private Button buttonConnectToServer;
    private boolean isBurgerPressed = false;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CamClient camClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_activity);
        setBurgerButton(true);
        setActionBarTitle(R.string.activity_name_server);
        camClient = CamClient.getCamClientInstance();
        if(camClient.isConnected()){
            setActionBarSubtitle("Подключен");
        }else{
            setActionBarSubtitle("Не подключен");
        }

        initComponents();
        initListeners();

    }

    private void initComponents(){
        inputIpServer = (EditText)findViewById(R.id.input_ip_server);
        inputPortServer = (EditText)findViewById(R.id.input_port_server);
        buttonConnectToServer = (Button)findViewById(R.id.button_connect_to_server);

        navigationView = (NavigationView)findViewById(R.id.navigation_view);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, null, R.string.navigation_bar_open, R.string.navigation_bar_close){
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                isBurgerPressed = false;
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                isBurgerPressed = true;
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
    }

    private void initListeners(){
        buttonConnectToServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Thread connectionThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        camClient.connectToServer(inputIpServer.getText().toString(),
                                                                    Integer.parseInt(inputPortServer.getText().toString()));
                    }
                });
                connectionThread.start();
                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        try {
                            connectionThread.join();
                            if(camClient.isConnected()){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setActionBarSubtitle("Подключен");
                                        Toast.makeText(ServerActivity.this, "Успешно", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setActionBarSubtitle("Не подключен");
                                        Toast.makeText(ServerActivity.this, "Не удалось подключиться к серверу", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();*/
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.navigation_menu_camera:
                        startCameraActivity();
                        break;
                    case R.id.navigation_menu_server:
                        startServerActivity();
                        break;
                }
                return false;
            }
        });
    }

    private void setBurgerButton(boolean value){
        getSupportActionBar().setDisplayHomeAsUpEnabled(value);
    }

    private void setActionBarTitle(int resId){
        getSupportActionBar().setTitle(resId);
    }

    private void setActionBarSubtitle(String value){
        getSupportActionBar().setSubtitle(value);
    }

    private void startCameraActivity(){
        Intent intent = new Intent(ServerActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void startServerActivity(){
        Intent intent = new Intent(ServerActivity.this, ServerActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                if(isBurgerPressed){
                    drawerLayout.closeDrawer(GravityCompat.START);
                }else{
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
