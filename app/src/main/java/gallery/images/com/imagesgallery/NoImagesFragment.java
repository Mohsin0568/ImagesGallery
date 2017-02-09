package gallery.images.com.imagesgallery;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.jar.Manifest;

import gallery.images.com.imagesgallery.ImagesGallery.AlbumImagesView;
import gallery.images.com.imagesgallery.util.contentResolver.ImagePathProvider;

public class NoImagesFragment extends Fragment {

    Context context ;
    Button addFromGallery, addFromCamera ;
    public final int RESULT_GET_IMAGES = 2 ;
    List imagesPath ;
    CardView cardView ;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Uri capturedImageUri ;
    int MY_PERMISSIONS_REQUEST_READ_Images = 10 ;
    public NoImagesFragment() {

    }

    public static NoImagesFragment newInstance(String param1, String param2) {
        NoImagesFragment fragment = new NoImagesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity)context).getSupportActionBar().setTitle("Add Favorito Images");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_no_images, container, false);
        addFromGallery = (Button) v.findViewById(R.id.add_from_gallery);
        addFromCamera = (Button) v.findViewById(R.id.take_from_camera);
        Typeface face = Typeface.createFromAsset(context.getAssets(),"fonts/script.otf");
        addFromGallery.setTypeface(face);
        addFromCamera.setTypeface(face);
        addFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagesGallery();
            }
        });
        addFromCamera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                takeImageFromCamera() ;
            }
        });
        return v;
    }

    public void openImagesGallery(){
        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_Images);
        }
        else{
            Intent intent = new Intent(context,AlbumImagesView.class);
            startActivityForResult(intent, RESULT_GET_IMAGES);
        }
    }

    public void takeImageFromCamera(){
        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_Images);
        }
        else{
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Image File name");
            capturedImageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
            if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            /*File photoImage = null ;
            try{
                photoImage = createImageFile();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            if(photoImage != null){
                Uri photoUri = FileProvider.getUriForFile(context, "gallery.images.com.imagesgallery", photoImage);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }*/
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        System.out.println("Persmissions length is " + permissions.length + "  " + grantResults.length);
        if( grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            System.out.println("Inside inside if condition");
            Intent intent = new Intent(context,AlbumImagesView.class);
            startActivityForResult(intent, RESULT_GET_IMAGES);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        ContentValues values = new ContentValues();
        boolean imagesPage = false ;
        if(requestCode == RESULT_GET_IMAGES && null != data ){
            imagesPath = data.getIntegerArrayListExtra("urls");
            Uri uri;
            int i = 0 ;
            for(Object x : imagesPath){
                values.put(ImagePathProvider.PATH, x.toString());
                uri = getActivity().getContentResolver().insert(ImagePathProvider.CONTENT_URI, values);
                i++;
            }
            if(i > 0){
                imagesPage = true ;
            }
        }
        else if(requestCode == REQUEST_IMAGE_CAPTURE){
            //capturedImageUri = data.getData();
            String capturedPath = getRealPathFromURI(context, capturedImageUri) ;
            values.put(ImagePathProvider.PATH, capturedPath);
            getActivity().getContentResolver().insert(ImagePathProvider.CONTENT_URI, values);
            //System.out.println("URL after capturing image from camera is " + getRealPathFromURI(context, capturedImageUri));
            imagesPage = true ;
        }

        if(imagesPage){
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            GridViewImages gridViewImages = new GridViewImages();
            transaction.replace(R.id.fragment_body, gridViewImages);
            transaction.commit();
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        String mCurrentPhotoPath = image.getAbsolutePath();
        System.out.println("Image capture current path is " + mCurrentPhotoPath);
        return image;
    }
}
