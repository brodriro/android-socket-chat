package com.demo.socketchat.di;

import android.app.Application;

import com.demo.socketchat.controllers.SocketHelper;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component( modules = {ChatModule.class})
public interface ApplicationComponent {

    void inject(Application application);

    SocketHelper getSocketHelper();

}
