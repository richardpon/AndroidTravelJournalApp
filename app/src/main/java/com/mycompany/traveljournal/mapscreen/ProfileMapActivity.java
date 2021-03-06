package com.mycompany.traveljournal.mapscreen;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mycompany.traveljournal.R;
import com.mycompany.traveljournal.common.LocationOnConnectListener;
import com.mycompany.traveljournal.common.LocationService;
import com.mycompany.traveljournal.detailsscreen.DetailActivity;
import com.mycompany.traveljournal.helpers.Util;
import com.mycompany.traveljournal.models.Post;
import com.mycompany.traveljournal.models.User;
import com.mycompany.traveljournal.service.JournalApplication;
import com.mycompany.traveljournal.service.JournalCallBack;
import com.mycompany.traveljournal.service.JournalService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ekucukog on 6/13/2015.
 */
public class ProfileMapActivity extends ActionBarActivity implements
        GoogleMap.OnMapLongClickListener{

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private final static String TAG = "ProfileMapActivityDebug";
    private JournalService client;
    private LatLng m_location;
    private String m_userID;
    //private User m_user;

    private ArrayList<Marker> markers = null;
    private ArrayList<Post> currentPosts = null;
    private HashMap markersToPosts = new HashMap();


    private int animationIndex =0;
    private ArrayList<Marker> sortedMarkers = null;
    private ArrayList<Boolean> shown= null;
    private ArrayList<Polyline> polylines = null;

    private Toolbar toolbarForMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        setToolbar();

        m_userID = getIntent().getStringExtra("user_id");
        client = JournalApplication.getClient();

        markers = new ArrayList<>();
        currentPosts = new ArrayList<>();
        polylines = new ArrayList<>();

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                    map.setInfoWindowAdapter(new CustomWindowAdapter(ProfileMapActivity.this, getLayoutInflater()));
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
            // Attach long click listener to the map here
            map.setOnMapLongClickListener(this);

            // Map is ready
            Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            getPostsForUser();

        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void getPostsForUser(){
        client.getPostsForUser(m_userID, null, 20, new JournalCallBack<List<Post>>() {
            @Override
            public void onSuccess(List<Post> resultPosts) {
                Log.d(TAG, "success getting posts: " + resultPosts.toString());

                currentPosts.addAll(resultPosts);
                if(currentPosts.size()>0){

                    m_location = new LatLng(currentPosts.get(0).getLatitude(), currentPosts.get(0).getLongitude());
                    //make sure map camera goes to target location
                    setTargetLocation(m_location);
                }

                for (int i = 0; i < currentPosts.size(); i++) {

                    Post post = currentPosts.get(i);

                    if (post == null) {
                        Log.d(TAG, "post is null");
                    } else {
                        Log.d(TAG, "post is: " + post.toString());

                        LatLng point = new LatLng(post.getLatitude(), post.getLongitude());

                        Log.d(TAG, "Returned point: " + point.toString());
                        putSinglePin(post);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "Failed to get posts");
            }
        });
    }

    private void setTargetLocation(LatLng location) {

        if(location!=null){
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, Util.ZOOM_MEDIUM);
            map.animateCamera(cameraUpdate);
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

        markers.add(marker);
        markersToPosts.put(marker, post);
        Log.d(TAG, "Marked pin at point: " + point.toString());
    }

    public ArrayList<Marker> getSortedMarkers(){

        ArrayList<Marker> sorted = new ArrayList<>();

        for(Marker marker: markers){

            //insert into right position
            int i =0;
            for(; i<sorted.size(); i++){
                Post currentPost = (Post)markersToPosts.get(marker);
                Post sortedPost = (Post)markersToPosts.get(sorted.get(i));
                if(currentPost.getCreatedAt().before(sortedPost.getCreatedAt())){
                    break;
                }
            }
            sorted.add(i, marker);
        }

        //Log.d(TAG, "sorted");
        return sorted;
    }

    @Override
    public void onMapLongClick(LatLng point) {
        //Toast.makeText(this, "Long Press to " + point.toString(), Toast.LENGTH_LONG).show();
        doAnimation();
    }

    private void doAnimation(){

        sortedMarkers = getSortedMarkers();

        if(sortedMarkers!=null){

            // Handler allows us to repeat a code block after a specified delay
            final android.os.Handler handler = new android.os.Handler();
            final long start = SystemClock.uptimeMillis();
            final long durationForPic = 2000;//2 sec
            final long durationTransition = 15;//15ms

            shown = new ArrayList<>();
            for(int i=0; i<sortedMarkers.size(); i++){
                shown.add(new Boolean(false));
            }
            for(int i=0; i<polylines.size(); i++){
                polylines.get(i).remove();
            }
            polylines = new ArrayList<>();
            animationIndex =0;

            handler.post(new Runnable() {
                @Override
                public void run() {

                    if(animationIndex >= sortedMarkers.size()){
                        Log.d(TAG, "end of animation");

                    }
                    else if(shown.get(animationIndex)==false){
                        //pic not shown before

                        sortedMarkers.get(animationIndex).showInfoWindow();
                        shown.remove(animationIndex);
                        shown.add(animationIndex, new Boolean(true));
                        // Post this event again 1s from now.
                        handler.postDelayed(this, durationForPic);
                    }
                    else{
                        //pic has been shown, switch to next photo
                        sortedMarkers.get(animationIndex).hideInfoWindow();

                        //if not last marker, then
                        //draw a line between this and next point
                        if( animationIndex < sortedMarkers.size()-1 ){

                            Post current = (Post)markersToPosts.get(sortedMarkers.get(animationIndex));
                            Post next = (Post)markersToPosts.get(sortedMarkers.get(animationIndex+1));

                            LatLng currentPoint = new LatLng(current.getLatitude(), current.getLongitude());
                            LatLng nextPoint = new LatLng(next.getLatitude(), next.getLongitude());

                            Polyline polyline = map.addPolyline(new PolylineOptions()
                                    .add(currentPoint)
                                    .add(nextPoint)
                                    .color(Color.CYAN)
                                    .width(12));

                            polylines.add(polyline);
                            Log.d(TAG, "drawing a line here");
                        }

                        // Post this event again 15ms from now.
                        handler.postDelayed(this, durationTransition);
                        animationIndex ++;
                    }
                }
            });
        }
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
