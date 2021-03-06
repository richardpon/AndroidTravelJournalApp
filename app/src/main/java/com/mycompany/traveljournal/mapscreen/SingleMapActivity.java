package com.mycompany.traveljournal.mapscreen;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mycompany.traveljournal.R;
import com.mycompany.traveljournal.helpers.Util;
import com.mycompany.traveljournal.models.Post;
import com.mycompany.traveljournal.service.JournalApplication;
import com.mycompany.traveljournal.service.JournalCallBack;
import com.mycompany.traveljournal.service.JournalService;

import java.util.List;

public class SingleMapActivity extends ActionBarActivity {

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private final static String TAG = "SingleMapActivityDebug";
    private JournalService client;

    private Post m_post;
    private String m_postID;
    private LatLng m_location;

    private Toolbar toolbarForMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        setToolbar();

        m_postID = getIntent().getStringExtra("post_id");
        client = JournalApplication.getClient();

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                    map.setInfoWindowAdapter(new CustomWindowAdapter(SingleMapActivity.this, getLayoutInflater()));
                }
            });
        } else {
            Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setToolbar() {
        toolbarForMap = (Toolbar) findViewById(R.id.toolbarmap);
        if (toolbarForMap != null) {
            setSupportActionBar(toolbarForMap);

            // Set the home icon on toolbar
            ActionBar actionbar = getSupportActionBar();
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_up_menu);
        }
    }

    public void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {

            // Map is ready
            Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();

        client.getPostWithId(m_postID,new JournalCallBack<Post>() {
                @Override
                public void onSuccess(Post post) {
                    m_post = post;
                    if(m_post!=null){
                        m_location = new LatLng(m_post.getLatitude(), m_post.getLongitude());
                        //make sure map camera goes to target location
                        setTargetLocation(m_location);
                        putSinglePin(m_post);
                    }
                    else{
                        Log.d(TAG, "post is null");
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Failed to get post");
                }
            });

        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setTargetLocation(LatLng location) {

        if(location!=null){
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, Util.ZOOM_HIGH);
            map.animateCamera(cameraUpdate);
        }
        else{
            Log.d(TAG, "location is null");
        }
    }

    private void putSinglePin(Post post){

        Log.d(TAG, "Will put pin for post: " + post.toString());

        // Define color of marker icon
        BitmapDescriptor defaultMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);

        LatLng point = new LatLng(post.getLatitude(), post.getLongitude());

        String imageUrl = post.getImageUrl();
        String caption = post.getCaption();

        // Creates and adds marker to the map
        Marker marker = map.addMarker(new MarkerOptions()
                .position(point)
                .title(caption)
                .snippet(imageUrl)
                .icon(defaultMarker));

        //marker.showInfoWindow();
        //markers.add(marker);
        //markersToPosts.put(marker, post);
        Log.d(TAG, "Marked pin at point: " + point.toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

}
