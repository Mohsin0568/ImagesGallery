package gallery.images.com.imagesgallery.ImagesGallery;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import gallery.images.com.imagesgallery.R;

import java.util.ArrayList;

public class AlbumImagesView extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private GalleryAlbumAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<AlbumsModel> albumsModels;
    private static int LOAD_IMAGES = 3 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_images_view);
//        getSupportActionBar().setTitle("Select Images");
//        setSupportActionBar(toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));

        // create an Object for Adapter
        mAdapter = new GalleryAlbumAdapter (AlbumImagesView.this,getGalleryAlbumImages ());

        // set the adapter object to the Recyclerview
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.SetOnItemClickListener(new GalleryAlbumAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View v, int position) {
                // do something with position


                Intent galleryAlbumsIntent = new Intent(AlbumImagesView.this, ShowAlbumImagesActivity.class);
                galleryAlbumsIntent.putExtra("position", position);

                galleryAlbumsIntent.putExtra("albumsList", getGalleryAlbumImages());
                startActivityForResult(galleryAlbumsIntent, LOAD_IMAGES);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == LOAD_IMAGES) {
            if(data != null) {
                setResult(2, data);
                finish();
            }
        }
    }

    private ArrayList<AlbumsModel> getGalleryAlbumImages() {
        final String[] columns = { MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        Cursor imagecursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                null, null, orderBy + " DESC");
        albumsModels = Utils.getAllDirectoriesWithImages(imagecursor);
        return albumsModels;

    }
}
