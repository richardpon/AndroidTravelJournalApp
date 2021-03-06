package com.mycompany.traveljournal.createscreen;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.mycompany.traveljournal.R;
import com.mycompany.traveljournal.base.PostsListActivity;
import com.mycompany.traveljournal.common.ProgressBarListener;
import com.mycompany.traveljournal.helpers.BitmapScaler;
import com.mycompany.traveljournal.helpers.DeviceDimensionsHelper;
import com.mycompany.traveljournal.helpers.Util;
import com.mycompany.traveljournal.profilescreen.ProfileActivity;

/**
 * Created by sjayaram on 6/5/2015.
 */
public class CreatePostActivity extends PostsListActivity{

    CreatePostFragment createPostFragment;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    private final String CREATE_FRAGMENT_TAG = "createPostFragment";

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    public void setUpFragment() {
        setUpCameraIntent();
        createPostFragment =  CreatePostFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.flContainer, createPostFragment, CREATE_FRAGMENT_TAG);
        ft.commit();
    }

    public void setUpFragmentFromTag(){
        createPostFragment = (CreatePostFragment)getSupportFragmentManager().findFragmentByTag(CREATE_FRAGMENT_TAG);
    }

    public void setUpCameraIntent(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Util.getPhotoFileUri(photoFileName)); // set the image file name
        // Start the image capture intent to take photo
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri takenPhotoUri = Util.getPhotoFileUri(photoFileName);
                createPostFragment.setPhotoPath(takenPhotoUri);

            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
}
