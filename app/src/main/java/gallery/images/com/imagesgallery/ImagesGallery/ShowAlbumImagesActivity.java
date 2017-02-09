package gallery.images.com.imagesgallery.ImagesGallery;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import gallery.images.com.imagesgallery.R;

import java.io.File;
import java.util.ArrayList;

public class ShowAlbumImagesActivity extends AppCompatActivity implements ShowAlbumImagesAdapter.ViewHolder.ClickListener {

    private Toolbar toolbar;

    private RecyclerView mRecyclerView;
    private ShowAlbumImagesAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button btnSelection;
    private ArrayList<AlbumsModel> albumsModels;
    private int mPosition;
    public ArrayList<Uri> mShareImages = new ArrayList<Uri>();
    public ArrayList<String> shareImages = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_gallery_images);
        btnSelection = (Button) findViewById(R.id.btnShow);
//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle("Select Images");
//        setSupportActionBar(toolbar);
        if (toolbar != null) {
        }
        mPosition = (int)getIntent ().getIntExtra ("position",0);
        albumsModels = (ArrayList<AlbumsModel>) getIntent ().getSerializableExtra ("albumsList");
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);


        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));

        // create an Object for Adapter
        mAdapter = new ShowAlbumImagesAdapter (ShowAlbumImagesActivity.this,getAlbumImages (),this);

        // set the adapter object to the Recyclerview
        mRecyclerView.setAdapter(mAdapter);

        btnSelection.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int size = shareImages.size();
                Intent intent = new Intent().putStringArrayListExtra("urls", shareImages);
                setResult(3, intent);
                finish();
                }

        });

    }

    private Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = this.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return this.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }



    private ArrayList<AlbumImages> getAlbumImages() {
        Object[] abc = albumsModels.get(mPosition).folderImages.toArray ();

        Log.i("imagesLength", "" + abc.length);
        ArrayList<AlbumImages> paths = new ArrayList<AlbumImages>();
        int size = abc.length;
        for (int i = size-1; i >= 0; i--) {

            AlbumImages albumImages = new AlbumImages ();
            albumImages.setAlbumImages ((String) abc[i]);
            paths.add (albumImages);
        }
        return  paths;

    }

    @Override
    public void onItemClicked(int position) {
        toggleSelection(position);
    }

    @Override
    public boolean onItemLongClicked (int position) {
        toggleSelection(position);
        return true;
    }

    private void toggleSelection(int position) {
//        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();
        boolean isImageAvail = mAdapter.isImageAvailable(position);
        if(isImageAvail){
            mAdapter.toggleSelection(position);
        }
        else{
//            if(count == 3)
//            {
//                Toast.makeText(ShowAlbumImagesActivity.this,"You can select upto only 3 images",Toast.LENGTH_SHORT).show();
//            }
//            else
                mAdapter.toggleSelection(position);
        }
        Log.i("string path", "" + mAdapter.getAlbumImagesList().get(position).getAlbumImages());
        Uri uriPath = Uri.parse(mAdapter.getAlbumImagesList().get(position).getAlbumImages());
        String path = uriPath.getPath ();
        File imageFile = new File(path);
        Uri uri = getImageContentUri(imageFile);
        if(mAdapter.isSelected (position)) {
            mShareImages.add (uri);
            shareImages.add(mAdapter.getAlbumImagesList().get(position).getAlbumImages());
        }else {
            mShareImages.remove (uri);
            shareImages.remove(mAdapter.getAlbumImagesList().get(position).getAlbumImages());
        }
        Log.i("uri path", "" + mShareImages);
    }
}