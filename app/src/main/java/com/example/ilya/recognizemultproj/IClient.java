package com.example.ilya.recognizemultproj;

import android.widget.EditText;

/**
 * Created by ilya_ on 06.05.2018.
 */

public interface IClient {

    void connectToServer(String ip, int port);

    boolean isConnected();

    void getInput(EditText inputFromServer);

}
