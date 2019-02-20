package com.demo.socketchat.di;

import com.demo.socketchat.controllers.SocketHelper;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ChatModule {

    @Singleton
    @Provides
    SocketHelper provideSocket(){
        return new SocketHelper();
    }

    //Provides
}
