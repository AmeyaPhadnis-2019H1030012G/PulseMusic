package com.hardcodecoder.pulsemusic.activities.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.activities.TrackPickerActivity;
import com.hardcodecoder.pulsemusic.adapters.PlaylistDataAdapter;
import com.hardcodecoder.pulsemusic.helper.RecyclerViewGestureHelper;
import com.hardcodecoder.pulsemusic.interfaces.PlaylistItemListener;
import com.hardcodecoder.pulsemusic.interfaces.SimpleGestureCallback;
import com.hardcodecoder.pulsemusic.model.MusicModel;

import java.util.ArrayList;
import java.util.List;

public abstract class AdvancePlaylist extends BasePlaylistActivity implements PlaylistItemListener, SimpleGestureCallback {

    protected PlaylistDataAdapter mAdapter = null;
    private ItemTouchHelper mItemTouchHelper = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setShuffleButtonAction(v -> {
            if (mAdapter != null) shuffleTrackAndPlay(mAdapter.getPlaylistTracks());
        });
        setUpDynamicButton(R.string.playlist_add_more, R.drawable.ic_playlist_add, v ->
                startActivityForResult(new Intent(this, TrackPickerActivity.class), TrackPickerActivity.REQUEST_CODE));
    }

    protected void setUpData(@Nullable final List<MusicModel> dataList, int scrollTo) {
        if (null == dataList || dataList.isEmpty()) {
            MaterialTextView textView = findViewById(R.id.no_tracks_found);
            textView.setText(getEmptyListStyledText());
        } else {
            findViewById(R.id.no_tracks_found).setVisibility(View.GONE);
            RecyclerView recyclerView = (RecyclerView) ((ViewStub) findViewById(R.id.stub_playlist_tracks_rv)).inflate();
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            mAdapter = new PlaylistDataAdapter(dataList, getLayoutInflater(), this, this);
            recyclerView.setAdapter(mAdapter);

            ItemTouchHelper.Callback itemTouchHelperCallback = new RecyclerViewGestureHelper(mAdapter);
            mItemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
            mItemTouchHelper.attachToRecyclerView(recyclerView);
            if (scrollTo > -1) recyclerView.scrollToPosition(scrollTo);
        }
    }

    protected void onReceiveData(ArrayList<MusicModel> receivedData) {
    }

    @Override
    public void onItemClick(int position) {
        if (mAdapter != null) setTrackAndPlay(mAdapter.getPlaylistTracks(), position);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder holder) {
        mItemTouchHelper.startDrag(holder);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == TrackPickerActivity.REQUEST_CODE) {
            Object object;
            if (null != data && null != (object = data.getSerializableExtra(TrackPickerActivity.ID_PICKED_TRACKS))) {
                ArrayList<MusicModel> selectedTracks = (ArrayList<MusicModel>) object;
                if (selectedTracks.size() > 0)
                    onReceiveData(selectedTracks);
            }
        }
    }
}