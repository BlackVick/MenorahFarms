package com.blackviking.menorahfarms.Common;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


public class CheckInternet extends AsyncTask<String,Void,Integer>{
    Context context;

    //create async response
    public interface AsyncResponse {
        void processFinish(Integer output);
    }

    //initializing response
    public AsyncResponse delegate = null;

    //constructor for response and context
    public CheckInternet(Context context, AsyncResponse delegate) {
        this.context=context;
        this.delegate = delegate;
    }

    public  boolean isConnected()
    {
        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Service.CONNECTIVITY_SERVICE);

        if (connectivityManager!=null)
        {
            NetworkInfo info=connectivityManager.getActiveNetworkInfo();
            if (info!=null)
            {
                if (info.getState()==NetworkInfo.State.CONNECTED)
                {
                    return true;
                }
            }
        }
        return false;
    }



    @Override
    protected Integer doInBackground(String... params) {

        Integer result=0;
        try {
            Socket socket=new Socket();
            SocketAddress socketAddress=new InetSocketAddress("8.8.8.8",53);
            socket.connect(socketAddress,1500);
            socket.close();
            result=1;
        } catch (IOException e) {
            e.printStackTrace();
            result=0;
        }

        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (isConnected())
        {
            if (result==1)
            {
                delegate.processFinish(result);
            }

            if(result==0)
            {
                delegate.processFinish(result);
            }
        }
        else
        {
            delegate.processFinish(2);
        }
        super.onPostExecute(result);
    }
}
