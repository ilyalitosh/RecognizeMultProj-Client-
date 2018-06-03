package com.example.ilya.recognizemultproj;

/**
 * Created by ilya_ on 06.05.2018.
 */

public interface IActivity {

    void initComponents();

    void initListeners();

    void setBurgerButton(boolean value);

    void setActionBarTitle(int resId);

    void setActionBarSubtitle(String value);

    void startCameraActivity();

    void startServerActivity();

}
