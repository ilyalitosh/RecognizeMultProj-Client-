package com.example.ilya.recognizemultproj;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by ilya_ on 01.11.2017.
 */

public class CamClient {

    private static Socket socket;
    private static InputStream in;
    private static OutputStream out;
    private static DataInputStream dataIn;
    private static DataOutputStream dataOut;
    private static CamClient camClient;
    private static String ip;
    private static int port;

    public DataOutputStream getDataOut(){
        return dataOut;
    }

    public DataInputStream getDataIn(){
        return dataIn;
    }

    private CamClient(){

    }

    public static CamClient getCamClientInstance(){
        if(camClient == null){
            camClient = new CamClient();
        }
        return camClient;
    }

    public void connectToServer(String ip, int port){
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 5000);
            this.ip = ip;
            this.port = port;
            initStreams();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isConnectionAlive;
    public boolean isConnected(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket testConnection = new Socket();
                    testConnection.connect(new InetSocketAddress(ip, port), 500);
                    testConnection.close();
                    isConnectionAlive = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    isConnectionAlive = false;
                }
            }
        });
        if(socket == null){
            return false;
        }else{
            try {
                t.start();
                t.join();
                return isConnectionAlive;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void initStreams(){
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            dataIn = new DataInputStream(in);
            dataOut = new DataOutputStream(out);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("возможно сокет отключился");
        }
    }

    public void getInput(EditText inputFromServer){
        try {
            while(dataIn.readLine() != null){
                inputFromServer.setText(dataIn.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void encodeFrame(Bitmap bmFrame, byte[] frameData, int width, int height){
        int pixelInt;
        int iterator = 0;
        int startValue = -128;
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                pixelInt = bmFrame.getPixel(j, i);
                frameData[iterator] = (byte)(startValue + Color.red(pixelInt));
                frameData[iterator + 1] = (byte)(startValue + Color.green(pixelInt));
                frameData[iterator + 2] = (byte)(startValue + Color.blue(pixelInt));
                iterator += 3;
            }
        }
    }

}
