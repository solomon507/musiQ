package com.dihanov.musiq.ui.login;

import android.content.Context;

import com.dihanov.musiq.ui.BasePresenter;
import com.dihanov.musiq.ui.BaseView;

/**
 * Created by dimitar.dihanov on 2/5/2018.
 */

public interface LoginContract {
    interface View extends BaseView<Presenter> {
        void showProgressBar();

        android.view.View getBirdIcon();
    }

    interface Presenter extends BasePresenter<View> {
        void authenticateUser(String username, String password, Context context, boolean rememberMe);
    }
}