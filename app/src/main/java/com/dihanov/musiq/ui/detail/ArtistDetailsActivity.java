package com.dihanov.musiq.ui.detail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dihanov.musiq.R;
import com.dihanov.musiq.models.Artist;
import com.dihanov.musiq.models.ArtistTopTags;
import com.dihanov.musiq.models.SpecificArtist;
import com.dihanov.musiq.models.Tag;
import com.dihanov.musiq.util.Constants;
import com.google.gson.Gson;
import com.veinhorn.tagview.TagView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import dagger.android.support.DaggerAppCompatActivity;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by dimitar.dihanov on 9/29/2017.
 */

public class ArtistDetailsActivity extends DaggerAppCompatActivity implements ArtistDetailsActivityContract.View {
    private static final String TAG_RETAINED_ARTIST = "artist";
    private static final String TAG_RETAINED_ALBUMS = "albums";
    private static final String TAG_RETAINED_TAGS = "tags";

    private String serializedArtist;
    private String serializedAlbumList;
    private String serializedTags;
    private Artist artist;

    @Inject
    ArtistDetailsActivityPresenter presenter;

    @BindView(R.id.artist_details_collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;

    @BindView(R.id.artist_details_toolbar)
    Toolbar toolbar;

    @BindView(R.id.artist_details_image)
    ImageView artistImage;

    @BindView(R.id.artist_details_tabs)
    TabLayout tabLayout;

    @BindView(R.id.artist_details_viewpager)
    ViewPager viewPager;

    @BindView(R.id.artist_details_name)
    TextView artistTitle;

    @BindView(R.id.artist_details_progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.artist_details_bird)
    TextView bird;

    TagView firstTag, secondTag, thirdTag, fourthTag, fifthTag;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            this.serializedArtist = savedInstanceState.getString(TAG_RETAINED_ARTIST);
            this.serializedAlbumList = savedInstanceState.getString(TAG_RETAINED_ALBUMS);
            this.serializedTags = savedInstanceState.getString(TAG_RETAINED_TAGS);
        } else {
            Intent receiveIntent = getIntent();
            String artistSerialized = receiveIntent.getStringExtra(Constants.ARTIST);
            String albumSerialized = receiveIntent.getStringExtra(Constants.ALBUM);
            String tagsSerialized = receiveIntent.getStringExtra(Constants.TAGS);
            this.serializedArtist = artistSerialized;
            this.serializedAlbumList = albumSerialized;
            this.serializedTags = tagsSerialized;
        }

        initTags();
        deserializeArtistInfo();
        initCollapsingToolbar();
        setSupportActionBar(toolbar);
        this.presenter.takeView(this);

        initViewPager();
        initArtistImage();
        setArtistTitle(artist.getName());
    }

    @SuppressLint("ResourceType")
    private void initTags() {
        firstTag = (TagView) findViewById(R.id.first_tag);
        secondTag = (TagView) findViewById(R.id.second_tag);
        thirdTag = (TagView) findViewById(R.id.third_tag);
        fourthTag = (TagView) findViewById(R.id.fourth_tag);
        fifthTag = (TagView) findViewById(R.id.fifth_tag);

        TagView[] tags = new TagView[]{
                firstTag, secondTag, thirdTag, fourthTag, fifthTag
        };

        List<Tag> tagText = new Gson().fromJson(this.serializedTags, ArtistTopTags.class).getToptags().getTag();

        //this is very ugly, however since there is no lambda what can you do
        List<String> firstFive = new ArrayList<String>(){
            {
                for (int i = 0; i < 5; i++) {
                    this.add(tagText.get(i).getName());
                }
            }
        };

        //sorted by tag name length
        Collections.sort(firstFive, new Comparator<String>() {

            @Override
            public int compare(String s, String t1) {
                return Integer.compare(t1.length(), s.length());
            }
        });

        for (int i = 0; i < tags.length; i++) {
            TagView currTag = tags[i];
            currTag.setText(firstFive.get(i));
            currTag.setTagColor(Color.parseColor(getString(R.color.colorAccent)));
        }
    }

    private void deserializeArtistInfo() {
        this.artist = new Gson().fromJson(serializedArtist, SpecificArtist.class).getArtist();
    }

    private void initArtistImage() {
        Glide.with(this).load(this.artist.getImage().get(Constants.IMAGE_XLARGE).getText()).crossFade(2000).into(this.artistImage);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(TAG_RETAINED_ARTIST, serializedArtist);
        outState.putString(TAG_RETAINED_ALBUMS, serializedAlbumList);
        outState.putString(TAG_RETAINED_TAGS, serializedTags);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.presenter.leaveView();
    }

    private void initViewPager() {
        ArtistDetailsViewPagerAdapter viewPagerAdapter = new ArtistDetailsViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        //need to call this as calligraphy doesnt change the fonts of the tablayout, since there is no exposes property,
        //in the xml, and the fonts are set programatically
        Constants.changeTabsFont(this, tabLayout);
    }

    private void initCollapsingToolbar() {
        Constants.setToolbarFont(collapsingToolbar, this);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.artist_details_appbar);
        appBarLayout.setExpanded(true);


        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(" ");
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = false;
                }
            }
        });
    }

    @Override
    public String getArtistBiography() {
        return this.artist.getBio().getContent();
    }

    @Override
    public String getSerialiedAlbums() {
        return this.serializedAlbumList;
    }

    @Override
    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    @Override
    public Artist getArtist() {
        return this.artist;
    }

    @Override
    public void showProgressBar() {
        this.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressbar() {
        this.progressBar.setVisibility(View.GONE);
    }

    @Override
    public View getBirdIcon() {
        return this.bird;
    }


    public void setArtistTitle(String artistTitle) {
        this.artistTitle.setText(artistTitle);
    }
}