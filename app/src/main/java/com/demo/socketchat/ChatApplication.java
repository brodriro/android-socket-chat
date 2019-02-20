package com.demo.socketchat;

import android.app.Application;

import com.demo.socketchat.di.ApplicationComponent;
import com.demo.socketchat.di.DaggerApplicationComponent;

public class ChatApplication extends Application {


    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationComponent = DaggerApplicationComponent.builder().build();
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }


}
