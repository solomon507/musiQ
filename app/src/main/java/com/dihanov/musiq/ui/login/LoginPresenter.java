package com.dihanov.musiq.ui.login;

import com.dihanov.musiq.R;
import com.dihanov.musiq.config.Config;
import com.dihanov.musiq.di.app.App;
import com.dihanov.musiq.interfaces.MainViewFunctionable;
import com.dihanov.musiq.models.User;
import com.dihanov.musiq.service.LastFmApiClient;
import com.dihanov.musiq.util.AppLog;
import com.dihanov.musiq.util.Connectivity;
import com.dihanov.musiq.util.Constants;
import com.dihanov.musiq.util.HelperMethods;

import javax.inject.Inject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by dimitar.dihanov on 2/5/2018.
 */

public class LoginPresenter implements LoginContract.Presenter {
    private LastFmApiClient lastFmApiClient;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private LoginContract.View login;

    @Inject
    public LoginPresenter(LastFmApiClient lastFmApiClient){
        this.lastFmApiClient = lastFmApiClient;
    }

    @Override
    public void takeView(LoginContract.View view) {
        login = view;
    }

    @Override
    public void leaveView() {
        login = null;
    }

    @Override
    public void authenticateUser(String username, String password, LoginContract.View login, boolean rememberMe) {
        if (checkConnection()){
            HelperMethods.setLayoutChildrenEnabled(true, login.findViewById(R.id.login_layout));
            return;
        }

        lastFmApiClient.getLastFmApiService()
                .getMobileSessionToken(Constants.AUTH_MOBILE_SESSION_METHOD, username, password, Config.API_KEY, HelperMethods.generateAuthSig(username, password), Config.FORMAT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<User>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        HelperMethods.setLayoutChildrenEnabled(false, login.findViewById(R.id.login_layout));
                        compositeDisposable.add(d);
                        login.showProgressBar();
                        HelperMethods.showTooltip((MainViewFunctionable)login, login.getBirdIcon(), login.getString((R.string.logging_in_text)));
                    }

                    @Override
                    public void onNext(User user) {
                        App.getSharedPreferences().edit().putString(Constants.USER_SESSION_KEY, user.getSession().getKey()).apply();
                    }

                    @Override
                    public void onError(Throwable e) {
                        HelperMethods.showTooltip((MainViewFunctionable)login, login.getBirdIcon(), login.getString((R.string.error_username_password)));
                        HelperMethods.setLayoutChildrenEnabled(true, login.findViewById(R.id.login_layout));
                        login.hideProgressBar();
                        AppLog.log(this.getClass().toString(), e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        persistUserInfo(username, password, rememberMe);
                        compositeDisposable.clear();
                        login.redirectToMain();

                    }
                });
    }


    private void persistUserInfo(String username, String password, boolean rememberMe) {
        App.getSharedPreferences().edit().putString(Constants.USERNAME, username)
                .putString(Constants.PASSWORD, password)
                .putBoolean(Constants.REMEMBER_ME, rememberMe)
                .apply();
    }

    private boolean checkConnection() {
        if(!Connectivity.isConnected(App.getAppContext())){
            HelperMethods.showNetworkErrorTooltip(login, login.getBirdIcon());
            return true;
        }
        return false;
    }

    public void setLastFmApiClient(LastFmApiClient lastFmApiClient) {
        this.lastFmApiClient = lastFmApiClient;
    }
}
