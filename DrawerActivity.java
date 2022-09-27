package io.github.qijaz221.hairsaloon;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import io.github.qijaz221.hairsaloon.comments.ui.CommentsFragment;
import io.github.qijaz221.hairsaloon.comments.ui.CommentsSearchFragment;
import io.github.qijaz221.hairsaloon.constants.Constants;
import io.github.qijaz221.hairsaloon.dialogs.SignOutDialog;
import io.github.qijaz221.hairsaloon.init.InitActivity;
import io.github.qijaz221.hairsaloon.profile.UserProfileActivity;
import io.github.qijaz221.hairsaloon.reusable.BaseSingleFragmentToolBarActivity;
import io.github.qijaz221.hairsaloon.settings.Settings;
import io.github.qijaz221.hairsaloon.settings.ui.BaseSettingsActivity;
import io.github.qijaz221.hairsaloon.user.CurrentUserManager;
import io.github.qijaz221.hairsaloon.user.UsersListFragment;
import io.github.qijaz221.hairsaloon.view.CircleTransform;


public class DrawerActivity extends BaseSingleFragmentToolBarActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = DrawerActivity.class.getSimpleName();
    private Handler mHandler;

    @Override
    protected int getLayoutResId() {
        return R.layout.drawer_activity;
    }


    @Override
    protected Fragment createFragmentOne() {
        return CommentsFragment.newInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkFirstRun();
        CurrentUserManager.getInstance(this).init(this);
        //setContentView(R.layout.activity_drawer);
        /*if (!PermissionUtils.hasPermission(this, Manifest.permission.RECORD_AUDIO)) {
            PermissionUtils.requestPermissionFromActivity(this, Manifest.permission.RECORD_AUDIO, 99);
        }*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        updateNavHeader();
        mHandler = new Handler();

    }



    public void checkFirstRun() {
        Log.d(TAG, Build.MODEL);
        if (Settings.get(this).isFirstRun() ||
                !Settings.get(this).isUserLoggedIn()) {
            startActivity(new Intent(this, InitActivity.class));
            finish();
            //WelcomeDialog.show(this);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, BaseSettingsActivity.class));
                return true;
            case R.id.shut_down:
                //ExitDialog.show(this);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_feed:
                switchFragmentDelayed(R.id.mainFragmentContainer,
                        CommentsFragment.newInstance(), 400);
                break;
            case R.id.nav_people:
                switchFragmentDelayed(R.id.mainFragmentContainer,
                        UsersListFragment.newInstance(null, -1,
                                UsersListFragment.REQUEST_SEARCH, null), 400);
                break;
            case R.id.nav_my_friends:
                if (CurrentUserManager.getInstance(this).getCurrentUser() != null) {
                    switchFragmentDelayed(R.id.mainFragmentContainer,
                            UsersListFragment.newInstance(CurrentUserManager
                                            .getInstance(this).getCurrentUser().getUserId(),
                                    UsersListFragment.TYPE_FRIENDS,
                                    UsersListFragment.REQUEST_NORMAL,
                                    null), 400);
                }
                break;
            case R.id.nav_my_followers:
                if (CurrentUserManager.getInstance(this).getCurrentUser() != null) {
                    switchFragmentDelayed(R.id.mainFragmentContainer,
                            UsersListFragment.newInstance(CurrentUserManager.getInstance(this).getCurrentUser().getUserId(),
                                    UsersListFragment.TYPE_FOLLOWERS,
                                    UsersListFragment.REQUEST_NORMAL,
                                    null), 400);
                }
                break;
            case R.id.nav_my_profile:
                if (CurrentUserManager.getInstance(this).getCurrentUser() != null) {
                    Intent intent = new Intent(this, UserProfileActivity.class);
                    intent.putExtra(Constants.KEY_USER_ID, CurrentUserManager.getInstance(this).getCurrentUser().getUserId());
                    intent.putExtra(Constants.KEY_USER_IMAGE_URL, CurrentUserManager.getInstance(this).getCurrentUser().getProfilePictureUrl());
                    intent.putExtra(Constants.KEY_USER_NAME, CurrentUserManager.getInstance(this).getCurrentUser().getDisplayName());
                    startActivity(intent);
                }
                break;
            case R.id.nav_my_posts:
                switchFragmentDelayed(R.id.mainFragmentContainer,
                        CommentsSearchFragment.newInstance(CommentsSearchFragment.REQUEST_MY_POSTS),
                        400);
                break;
            case R.id.nav_find_posts:
                switchFragmentDelayed(R.id.mainFragmentContainer,
                        CommentsSearchFragment.newInstance(CommentsSearchFragment.REQUEST_SEARCH),
                        400);
                break;
            case R.id.nav_logout:
                startDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new SignOutDialog(DrawerActivity.this).show();
                    }
                });
                break;
        }
        return true;
    }

    protected void startDelayed(Runnable runnable) {
        mHandler.postDelayed(runnable, 200);
    }

    protected void switchFragmentDelayed(final int res, final Fragment fragment, long delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switchFragment(res, fragment);
            }
        }, delay);
    }


    public void updateNavHeader() {
        NavigationView view = (NavigationView) findViewById(R.id.nav_view);
        if (Settings.get(this).isUserLoggedIn()) {
            ImageView user = (ImageView) view.getHeaderView(0).findViewById(R.id.imageView);
            ImageView userCover = (ImageView) view.getHeaderView(0).findViewById(R.id.header_bg);
            Picasso.with(this).load(Settings.get(this).getUserImageUrl()).fit().centerCrop().
                    transform(new CircleTransform()).placeholder(R.drawable.user_place_holder).into(user);


            Picasso.with(this).
                    load(Settings.get(this).getUserCoverUrl())
                    .placeholder(R.drawable.default_cover_low)
                    .fit().centerCrop().into(userCover);
            TextView userName = (TextView) view.getHeaderView(0).findViewById(R.id.nav_header_title);
            userName.setText(Settings.get(this).getUserName());
            TextView accountName = (TextView) view.getHeaderView(0).findViewById(R.id.nav_header_subtitle);
            accountName.setText(Settings.get(this).getAccountName());
            view.getMenu().findItem(R.id.nav_logout).setVisible(true);
        }
    }


}
