package gallery.images.com.imagesgallery;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import gallery.images.com.imagesgallery.util.contentResolver.ImagePathProvider;

public class AddImages extends AppCompatActivity {

    ArrayList<String> allImagespath = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_images);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        getAllImgagesPath();
        boolean addFlag = false;

        if(Intent.ACTION_SEND.equals(action) && type != null){
            addSingleImage(intent);
            addFlag = true;
        }
        else if (Intent.ACTION_ATTACH_DATA.equals(action) && type != null){
            addSingleImage(intent);
            addFlag = true;
        }
        else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null){
            addMultipleImages(intent);
            addFlag = true;
        }
        else{
            Toast.makeText(this, "Invalid Data type", Toast.LENGTH_SHORT).show();
        }
        if(addFlag){
            Toast.makeText(this, "Images Added successfully", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    public void addSingleImage(Intent intent){
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        addImage(uri.getPath());
    }

    public void addMultipleImages(Intent intent){
        ArrayList<Uri> images = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        for(Uri image : images){
            addImage(image.getPath());
        }
    }

    public void addImage(String url){
        if(!(allImagespath.contains(url))) {
            ContentValues values = new ContentValues();
            values.put(ImagePathProvider.PATH, url);
            Uri uri = getContentResolver().insert(ImagePathProvider.CONTENT_URI, values);
        }
    }

    public void getAllImgagesPath(){
        allImagespath.clear();
        Cursor c = managedQuery(ImagePathProvider.CONTENT_URI,null,null,null,"PATH");
        if(c.moveToFirst()){
            do{
                String names[] = c.getColumnNames();
                String path = c.getString(c.getColumnIndex("path"));
                File f = new File(path);
                if(f.exists()) {
                    allImagespath.add(path);
                }
            }while(c.moveToNext());
        }
    }

}
