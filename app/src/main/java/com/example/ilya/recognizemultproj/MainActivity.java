package com.example.ilya.recognizemultproj;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private JavaCameraView camPreview;
    private Mat currentFrame;
    private View leftSideVerticalLine;
    private View rightSideVerticalLine;
    private View upSideHorizontalLine;
    private View downSideHorizontalLine;
    private int widthCameraPreview, heightCameraPreview;
    private Button buttonSend;
    private ProgressBar pbarLoadingResponseFromServer;
    private EditText inputFromServer;
    private SeekBar changeWidthRedFrame, changeHeightRedFrame;
    private RelativeLayout changingRedFrameSpace;
    private boolean appStart = true;
    private boolean isCanSend = false;
    private static CamClient camClient;
    private boolean isBurgerPressed = false;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case BaseLoaderCallback.SUCCESS:
                    camPreview.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setBurgerButton(true);
        setActionBarTitle(R.string.activity_name_camera);

        camClient = CamClient.getCamClientInstance();

        initComponents();

        initListeners();

    }

    private void initComponents(){
        camPreview = (JavaCameraView)findViewById(R.id.cam_preview);

        leftSideVerticalLine = findViewById(R.id.left_side_vertical_frame);
        rightSideVerticalLine = findViewById(R.id.right_side_vertical_frame);
        upSideHorizontalLine = findViewById(R.id.up_side_horizontal_frame);
        downSideHorizontalLine = findViewById(R.id.down_side_horizontal_frame);

        buttonSend = (Button)findViewById(R.id.button_send);

        changeHeightRedFrame = (SeekBar)findViewById(R.id.seek_bar_change_red_frame_height);
        changeWidthRedFrame = (SeekBar)findViewById(R.id.seek_bar_change_red_frame_width);
        changingRedFrameSpace = (RelativeLayout)findViewById(R.id.change_width_height_space);
        inputFromServer = (EditText)findViewById(R.id.output_response_from_server);
        pbarLoadingResponseFromServer = (ProgressBar)findViewById(R.id.progressbar_loading_response_from_server);

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
        camPreview.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                currentFrame = new Mat(new Size(width, height), CvType.CV_8UC4);
            }

            @Override
            public void onCameraViewStopped() {
                currentFrame.release();
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                if(appStart){
                    widthCameraPreview = camPreview.getWidth();
                    heightCameraPreview = camPreview.getHeight();
                    changeWidthRedFrame.setProgress((int)((upSideHorizontalLine.getWidth()+0.0)/widthCameraPreview * 120));
                    changeHeightRedFrame.setProgress((int)((leftSideVerticalLine.getHeight()+0.0)/heightCameraPreview * 120));
                    appStart = false;
                }
                currentFrame = inputFrame.rgba();
                if(isCanSend){
                    final Mat currentFrameForSending = inputFrame.gray();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int[] outLeftVert = new int[2];
                            int[] outRightVert = new int[2];
                            int[] outUpHoriz = new int[2];
                            int[] outDownHoriz = new int[2];
                            leftSideVerticalLine.getLocationOnScreen(outLeftVert);
                            rightSideVerticalLine.getLocationOnScreen(outRightVert);
                            upSideHorizontalLine.getLocationOnScreen(outUpHoriz);
                            downSideHorizontalLine.getLocationOnScreen(outDownHoriz);

                            double deltaWidth, deltaHeight;
                            deltaWidth = (widthCameraPreview + .0) / currentFrameForSending.cols();
                            deltaHeight = (heightCameraPreview + .0) / currentFrameForSending.rows();
                            int x1 = (int)(outLeftVert[0] / deltaWidth);
                            int y1 = (int)(outUpHoriz[1] / deltaHeight);
                            int x2 = (int)(outRightVert[0] / deltaWidth);
                            int y2 = (int)(outDownHoriz[1] / deltaHeight);

                            Mat subFrame = clipFrame(currentFrameForSending, x1, y1 - (getSupportActionBar().getHeight()), x2, y2 - (getSupportActionBar().getHeight()));

                            int iterator = 0;
                            byte startValue = -128;
                            byte[] dataToSend = new byte[270000];
                            for(int i = 0; i < subFrame.rows(); i++){
                                for(int j = 0; j < subFrame.cols(); j++){
                                    dataToSend[iterator] = (byte)(startValue + subFrame.get(i, j)[0]);
                                    iterator++;
                                }
                            }
                            try {
                                camClient.getDataOut().write(dataToSend);
                                while(true){
                                    final String input = camClient.getDataIn().readUTF();
                                    inputFromServer.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            pbarLoadingResponseFromServer.setVisibility(View.INVISIBLE);
                                            inputFromServer.setText(input);
                                        }
                                    });
                                    break;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    isCanSend = false;
                }
                return currentFrame;
            }
        });
        final RelativeLayout.LayoutParams leftSideVerticalLineParams = (RelativeLayout.LayoutParams)leftSideVerticalLine.getLayoutParams();
        final RelativeLayout.LayoutParams rightSideVerticalLineParams = (RelativeLayout.LayoutParams)rightSideVerticalLine.getLayoutParams();
        final RelativeLayout.LayoutParams upSideHorizontalLineParams = (RelativeLayout.LayoutParams)upSideHorizontalLine.getLayoutParams();
        final RelativeLayout.LayoutParams downSideHorizontalLineParams = (RelativeLayout.LayoutParams)downSideHorizontalLine.getLayoutParams();
        changeWidthRedFrame.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                upSideHorizontalLineParams.width = (int)(widthCameraPreview  * ((progress + 0.0)/120));
                downSideHorizontalLineParams.width = (int)(widthCameraPreview * ((progress + 0.0)/120));
                upSideHorizontalLine.setLayoutParams(upSideHorizontalLineParams);
                downSideHorizontalLine.setLayoutParams(downSideHorizontalLineParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        changeHeightRedFrame.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                leftSideVerticalLineParams.height = (int)(heightCameraPreview * ((progress + 0.0)/300));
                rightSideVerticalLineParams.height = (int)(heightCameraPreview * ((progress + 0.0)/300));
                leftSideVerticalLine.setLayoutParams(leftSideVerticalLineParams);
                rightSideVerticalLine.setLayoutParams(rightSideVerticalLineParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(camClient.isConnected()){
                    inputFromServer.setText("");
                    isCanSend = true;
                    pbarLoadingResponseFromServer.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(MainActivity.this, "Отсутствует подключение к серверу", Toast.LENGTH_SHORT).show();
                }
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
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

    private Mat clipFrame(Mat src, int x1, int y1, int x2, int y2){
        Mat subMat = src.submat(y1, y2, x1, x2);

        Mat scaledMat = new Mat(new Size(900, 300), CvType.CV_8UC1);
        Imgproc.resize(subMat, scaledMat, new Size(900, 300));

        return scaledMat;
    }

    private void setBurgerButton(boolean value){
        getSupportActionBar().setDisplayHomeAsUpEnabled(value);
    }

    private void setActionBarTitle(int resId) {
        getSupportActionBar().setTitle(resId);
    }

    private void startCameraActivity(){
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void startServerActivity(){
        Intent intent = new Intent(MainActivity.this, ServerActivity.class);
        startActivity(intent);
        finish();
    }

    private void requestPermissions(Activity activity){
        String[] permissionsStorage = {
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA
        };
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, permissionsStorage, 1);
        }
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

    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()){
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }else{
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, loaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(camPreview != null){
            camPreview.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(camPreview != null){
            camPreview.disableView();
        }
    }

}
