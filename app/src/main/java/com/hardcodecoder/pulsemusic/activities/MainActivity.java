package com.hardcodecoder.pulsemusic.activities;

import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.TaskRunner;
import com.hardcodecoder.pulsemusic.loaders.LibraryLoader;
import com.hardcodecoder.pulsemusic.loaders.SortOrder;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;
import com.hardcodecoder.pulsemusic.themes.ThemeManager;
import com.hardcodecoder.pulsemusic.ui.AlbumFragment;
import com.hardcodecoder.pulsemusic.ui.ArtistFragment;
import com.hardcodecoder.pulsemusic.ui.ControlsFragment;
import com.hardcodecoder.pulsemusic.ui.HomeFragment;
import com.hardcodecoder.pulsemusic.ui.LibraryFragment;
import com.hardcodecoder.pulsemusic.ui.PlaylistCardFragment;


public class MainActivity extends MediaSessionActivity {

    public static final String TAG = "MainActivity";
    private static final String HOME = "HomeFragment";
    private static final String LIBRARY = "LibraryFragment";
    private static final String ALBUMS = "AlbumsFragment";
    private static final String PLAYLIST_CARDS = "PlaylistCardFragment";
    private static final String ARTIST = "ArtistFragment";
    private static final String ACTIVE = "ActiveFragment";

    private final FragmentManager fm = getSupportFragmentManager();
    private Fragment homeFrag = null;
    private Fragment libraryFrag = null;
    private Fragment playlistCardFrag = null;
    private Fragment controlsFrag = null;
    private final MediaController.Callback mCallback = new MediaController.Callback() {
        @Override
        public void onMetadataChanged(@Nullable MediaMetadata metadata) {
            showControlsFragment();
        }
    };

    private Fragment albumsFrag = null;
    private MediaController mController;
    private Fragment activeFrag = null;
    private MediaBrowser mMediaBrowser;
    private Fragment artistFrag = null;
    @StyleRes
    private int mCurrentTheme;
    @StyleRes
    private int mCurrentAccent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mCurrentTheme = ThemeManager.getThemeToApply();
        mCurrentAccent = ThemeManager.getAccentToApply();

        super.onCreate(null); // Pass null to prevent restoration of fragments on activity recreate

        setContentView(R.layout.activity_main);

        if (TrackManager.getInstance().getMainList() == null) {
            TaskRunner.getInstance().executeAsync(new LibraryLoader(getContentResolver(), SortOrder.TITLE_ASC), (data) -> {
                TrackManager.getInstance().setMainList(data);
                setUpMainContents(savedInstanceState);
            });
        } else setUpMainContents(savedInstanceState);
    }

    private void setUpMainContents(Bundle savedInstanceState) {
        if (savedInstanceState == null) switchFragment(homeFrag, HOME);
        else switchFragment(activeFrag, savedInstanceState.getString(ACTIVE, HOME));
        setUpBottomNavigationView();
    }

    private void setUpBottomNavigationView() {
        BottomNavigationView bottomNavigation = findViewById(R.id.google_bottom_nav);
        bottomNavigation.setOnNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            switch (id) {
                case R.id.nav_home:
                    if (activeFrag != homeFrag) {
                        switchFragment(homeFrag, HOME);
                    }
                    break;
                case R.id.nav_library:
                    if (activeFrag != libraryFrag) {
                        switchFragment(libraryFrag, LIBRARY);
                    }
                    break;
                case R.id.nav_playlist:
                    if (activeFrag != playlistCardFrag) {
                        switchFragment(playlistCardFrag, PLAYLIST_CARDS);
                    }
                    break;
                case R.id.nav_album:
                    if (activeFrag != albumsFrag) {
                        switchFragment(albumsFrag, ALBUMS);
                    }
                    break;
                case R.id.nav_artist:
                    if (activeFrag != artistFrag) {
                        switchFragment(artistFrag, ARTIST);
                    }
                    break;
            }
            return true;
        });
    }

    private void switchFragment(Fragment switchTo, String tag) {
        if (null == switchTo) {
            switch (tag) {

                case HOME:
                    homeFrag = new HomeFragment();
                    switchTo = homeFrag;
                    break;

                case LIBRARY:
                    libraryFrag = new LibraryFragment();
                    switchTo = libraryFrag;
                    break;

                case PLAYLIST_CARDS:
                    playlistCardFrag = new PlaylistCardFragment();
                    switchTo = playlistCardFrag;
                    break;

                case ALBUMS:
                    albumsFrag = new AlbumFragment();
                    switchTo = albumsFrag;
                    break;

                case ARTIST:
                    artistFrag = new ArtistFragment();
                    switchTo = artistFrag;
                    break;

                default:
                    Log.e(TAG, "SwitchTo fragment is not a member of defined fragments");
            }

            if (switchTo != null && activeFrag != null) {
                fm.beginTransaction()
                        .add(R.id.fragment_container, switchTo, tag)
                        .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                        .hide(activeFrag)
                        .show(switchTo)
                        .commit();
            } else if (switchTo != null) {
                fm.beginTransaction()
                        .add(R.id.fragment_container, switchTo, tag)
                        .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                        .show(switchTo)
                        .commit();
            }
        } else
            fm.beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                    .hide(activeFrag)
                    .show(switchTo)
                    .commit();
        activeFrag = switchTo;
    }

    /*private void connectToSession() {
        mMediaBrowser = new MediaBrowser(this, new ComponentName(MainActivity.this, PMS.class),
                // Which MediaBrowserService
                new MediaBrowser.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        try {
                            // Ah, here’s our Token again
                            MediaSession.Token token = mMediaBrowser.getSessionToken();

                            // This is what gives us access to everything
                            mController = new MediaController(MainActivity.this, token);

                            // Convenience method to allow you to use
                            // MediaControllerCompat.getMediaController() anywhere
                            setMediaController(mController);

                            //Register callback to receive metadata changes
                            mController.registerCallback(mCallback);

                            if (mController.getMetadata() != null)
                                showControlsFragment();
                        } catch (Exception e) {
                            Log.e(MainActivity.class.getSimpleName(), "Error creating controller", e);
                        }
                    }

                }, null); // optional Bundle
        mMediaBrowser.connect();
    }*/

    @Override
    public void onMediaServiceConnected(MediaController controller) {
        mController = controller;
        mMediaBrowser = getMediaBrowser();
        //Register callback to receive metadata changes
        mController.registerCallback(mCallback);

        if (mController.getMetadata() != null)
            showControlsFragment();
    }

    private void showControlsFragment() {
        if (controlsFrag == null) {
            controlsFrag = new ControlsFragment();
            findViewById(R.id.controls_fragment_container).setVisibility(View.VISIBLE);
            fm.beginTransaction()
                    .setCustomAnimations(R.anim.slide_up_enter, R.anim.slide_down_exit)
                    .replace(R.id.controls_fragment_container, controlsFrag)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        if ((mCurrentTheme != ThemeManager.getThemeToApply()) || (mCurrentAccent != ThemeManager.getAccentToApply())) {
            supportInvalidateOptionsMenu();
            recreate();
        }
        super.onStart();
        if (null != mController && mMediaBrowser.isConnected()) {
            mController.registerCallback(mCallback);
            if (null != mController.getMetadata())
                showControlsFragment();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mController)
            mController.unregisterCallback(mCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getMediaController() != null)
            getMediaController().unregisterCallback(mCallback);
        if (mMediaBrowser != null)
            mMediaBrowser.disconnect();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ACTIVE, activeFrag.getTag());
    }
}
