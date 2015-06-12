package com.mycompany.traveljournal.mapscreen;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mycompany.traveljournal.R;
import com.mycompany.traveljournal.detailsscreen.DetailActivity;
import com.mycompany.traveljournal.detailsscreen.DetailFragment;
import com.mycompany.traveljournal.helpers.Util;
import com.mycompany.traveljournal.mainscreen.MainActivity;
import com.mycompany.traveljournal.models.Post;
import com.mycompany.traveljournal.service.JournalApplication;
import com.mycompany.traveljournal.service.JournalCallBack;
import com.mycompany.traveljournal.service.JournalService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ekucukog on 6/10/2015.
 */
public class SingleMapFragment extends Fragment {

    private SupportMapFragment mapFragment;
    private static View view;
    private GoogleMap map;
    private final static String TAG = "SingleMapFragmentDebug";
    private JournalService client;

    private Post m_post;
    private String m_postID;
    private LatLng m_location;

    public static SingleMapFragment newInstance(String postId) {
        SingleMapFragment singleMapFragment = new SingleMapFragment();
        Bundle args = new Bundle();
        args.putString("post_id", postId);
        singleMapFragment.setArguments(args);
        return singleMapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_postID = getArguments().getString("post_id", "");
        client = JournalApplication.getClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = (View) inflater.inflate(R.layout.fragment_single_map, container, false);

        mapFragment = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map_fragment));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                    map.setInfoWindowAdapter(new CustomWindowAdapter(getActivity(), getActivity().getLayoutInflater()));
                }
            });
        } else {
            Toast.makeText(getActivity(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    /*public void setData(String postID){
        m_postID = postID;
    }*/

    public void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {

            // Map is ready
            Toast.makeText(getActivity(), "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();

            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(final Marker mark) {

                    mark.showInfoWindow();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mark.showInfoWindow();

                        }
                    }, 200);

                    return true;
                }
            });

            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    //Call detail page from here, NO!
                    /*Intent i = new Intent(SingleMapActivity.this, DetailActivity.class);
                    i.putExtra("post_id", post.getPostID());
                    startActivity(i);*/
                }
            });

            client.getPostWithId(m_postID,new JournalCallBack<List<Post>>() {
                @Override
                public void onSuccess(List<Post> posts) {
                    m_post = posts.get(0);
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
            Toast.makeText(getActivity(), "Error - Map was null!!", Toast.LENGTH_SHORT).show();
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
}