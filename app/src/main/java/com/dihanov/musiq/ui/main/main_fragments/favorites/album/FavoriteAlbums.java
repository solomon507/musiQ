package com.dihanov.musiq.ui.main.main_fragments.favorites.album;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dihanov.musiq.R;
import com.dihanov.musiq.di.app.App;
import com.dihanov.musiq.ui.adapters.AlbumDetailsAdapter;
import com.dihanov.musiq.ui.main.MainActivity;
import com.dihanov.musiq.ui.main.main_fragments.ViewPagerCustomizedFragment;
import com.dihanov.musiq.util.Constants;
import com.dihanov.musiq.util.HelperMethods;

import java.util.ArrayList;
import java.util.HashSet;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dimitar.dihanov on 11/2/2017.
 */

public class FavoriteAlbums extends ViewPagerCustomizedFragment implements FavoriteAlbumsContract.View{
    public static final String TITLE = "favorite albums";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Inject
    FavoriteAlbumsContract.Presenter favoriteFragmentsPresenter;

    private MainActivity mainActivity;


    public static FavoriteAlbums newInstance() {
        Bundle args = new Bundle();
        FavoriteAlbums albumResultFragment = new FavoriteAlbums();
        albumResultFragment.setArguments(args);
        return albumResultFragment;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mainActivity = (MainActivity)getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.artist_search_fragment, container, false);
        ButterKnife.bind(this, view);

        //very important to call this - this enables us to use the below method(onCreateOptionsMenu), and allows us
        //to receive calls from MainActivity's onCreateOptionsMenu
        setHasOptionsMenu(true);

        initRecyclerView();
        this.favoriteFragmentsPresenter.takeView(this);
        this.favoriteFragmentsPresenter.loadFavoriteAlbums(
                App.getSharedPreferences().getStringSet(Constants.FAVORITE_ALBUMS_KEY, new HashSet<>()),
                this);

        return view;
    }

    private void initRecyclerView() {
        RecyclerView.LayoutManager layoutManager = null;
        //check if tablet --> 3 columns instead of 2;
        if (HelperMethods.isTablet(mainActivity)){
            layoutManager = new GridLayoutManager(mainActivity, 3);
            recyclerView.addItemDecoration(new FavoriteAlbums.GridSpacingItemDecoration(3, HelperMethods.dpToPx(10, mainActivity), true));
        } else {
            layoutManager = new GridLayoutManager(mainActivity, 2);
            recyclerView.addItemDecoration(new FavoriteAlbums.GridSpacingItemDecoration(2, HelperMethods.dpToPx(10, mainActivity), true));
        }

        if(layoutManager == null){
            return;
        }

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new AlbumDetailsAdapter(mainActivity,
                new ArrayList<>(),
                favoriteFragmentsPresenter,
                true));
    }

    @Override
    public Context getContext() {
        return this.mainActivity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.favoriteFragmentsPresenter.leaveView();
    }

    @Override
    public RecyclerView getRecyclerView() {
        return this.recyclerView;
    }

    @Override
    public void setRecyclerViewAdapter(RecyclerView.Adapter<?> adapter) {
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(this.getContext(), GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
}
