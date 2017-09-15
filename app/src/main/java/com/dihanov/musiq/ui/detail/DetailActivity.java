package com.dihanov.musiq.ui.detail;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dihanov.musiq.R;
import com.dihanov.musiq.ui.detail.fragment.DetailFragment;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasFragmentInjector;

/**
 * Created by mertsimsek on 25/05/2017.
 */

public class DetailActivity extends AppCompatActivity implements HasFragmentInjector, DetailView {

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentAndroidInjector;

    @Inject
    DetailPresenter detailPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        detailPresenter.loadDetail();

        if (savedInstanceState == null)
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, DetailFragment.newInstance())
                    .commitAllowingStateLoss();
    }

    @Override
    public void onDetailLoaded() {
        Log.v("TEST", "Detail is loaded");
    }

    @Override
    public AndroidInjector<android.app.Fragment> fragmentInjector() {
        return fragmentAndroidInjector;
    }
}
