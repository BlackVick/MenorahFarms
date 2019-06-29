package com.blackviking.menorahfarms;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.blackviking.menorahfarms.Common.Common;

import io.paperdb.Paper;


public class Splash extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        String localUser = Paper.book().read(Common.USER_ID);

        if (!TextUtils.isEmpty(localUser)){

            //jump to page

        } else {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent loginIntent = new Intent(Splash.this, SignIn.class);
                    startActivity(loginIntent);
                    finish();
                }
            }, 0);

        }

    }

}
