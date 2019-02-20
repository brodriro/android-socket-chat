package com.demo.socketchat.di;

import com.demo.socketchat.ui.chat.ChatViewModel;
import com.demo.socketchat.ui.login.LoginViewModel;

import dagger.Component;


@PerActivity
@Component( dependencies = {ApplicationComponent.class})
public interface ViewModelsComponent {

    void inject(ChatViewModel chatViewModel);
    void inject(LoginViewModel loginViewModel);

}
