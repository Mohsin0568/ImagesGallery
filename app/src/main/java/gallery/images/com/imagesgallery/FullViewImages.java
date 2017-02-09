package gallery.images.com.imagesgallery;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.graphics.Matrix;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import gallery.images.com.imagesgallery.util.ImageCache;
import gallery.images.com.imagesgallery.util.ImageFetcher;
import gallery.images.com.imagesgallery.util.Utils;
import gallery.images.com.imagesgallery.util.contentResolver.ImagePathProvider;

public class FullViewImages extends AppCompatActivity implements View.OnClickListener {

    private static final String IMAGE_CACHE_DIR = "images";
    public static final String EXTRA_IMAGE = "extra_image";

    private ImagePagerAdapter mAdapter;
    private ImageFetcher mImageFetcher;
    private ViewPager mPager;
    List<String> allImagesPath ;
    ScaleGestureDetector gesture ;
    private Matrix matrix = new Matrix();
    private ImageDetailFragment detailFragment ;
    private MenuItem addImageMenu, selectImagesMenu, deleteImagesMenu, shareImagesMenu, backImageMenu, setImageAs ;
    ActionBar actionBar ;
    boolean showActionBar = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_view_images);
        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras == null){
                allImagesPath = new ArrayList<String>();
            }
            else{
                allImagesPath = extras.getStringArrayList("imagesList");
            }
        }
        else{
            allImagesPath = (ArrayList<String>) savedInstanceState.getSerializable("imagesList");
        }
        // Fetch screen height and width, to use as our max size when loading images as this
        // activity runs full screen
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        // For this sample we'll use half of the longest width to resize our images. As the
        // image scaling ensures the image is larger than this, we should be left with a
        // resolution that is appropriate for both portrait and landscape. For best image quality
        // we shouldn't divide by 2, but this will use more memory and require a larger memory
        // cache.
        final int longest = (height > width ? height : width) / 2;

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(this, longest);
        mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
        mImageFetcher.setImageFadeIn(false);

        // Set up ViewPager and backing adapter
//        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), Images.imageUrls.length);
        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), allImagesPath.size());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setPageMargin((int) getResources().getDimension(R.dimen.horizontal_page_margin));
        mPager.setOffscreenPageLimit(2);

        // Set up activity to go full screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Enable some additional newer visibility and ActionBar features to create a more
        // immersive photo viewing experience
        if (Utils.hasHoneycomb()) {
            actionBar = getSupportActionBar();

            // Hide title text and set home as up
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
//            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE );
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            actionBar.hide();

            // Hide and show the ActionBar as the visibility changes
           mPager.setOnSystemUiVisibilityChangeListener(
                    new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int vis) {
                            if ((vis & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0) {
                                actionBar.hide();
                            } else {
                                actionBar.show();
                            }
                        }
                    });

            // Start low profile mode and hide ActionBar

        }
        final int extraCurrentItem = getIntent().getIntExtra(EXTRA_IMAGE, -1);
        if(extraCurrentItem != -1){
            mPager.setCurrentItem(extraCurrentItem);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        actionBar.hide();
    }

    public ImageFetcher getImageFetcher() {
        return mImageFetcher;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int currentItem = mPager.getCurrentItem();
        switch (item.getItemId()){
            case android.R.id.home:
                super.onBackPressed();
                break;

            case R.id.delete_images:
               deleteImage(currentItem);
                break;

            case R.id.share_images:
                shareCurrentImage(allImagesPath.get(currentItem));
                break;

            case R.id.set_as_image:
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                intent.setDataAndType(Uri.parse(allImagesPath.get(currentItem)), "image/*");
                intent.putExtra("jpg", "image/*");
                startActivityForResult(Intent.createChooser(intent,
                        "Set Image As"), 1);

            }

        return super.onOptionsItemSelected(item);
    }

    public void deleteImage(int currentItem){
        getContentResolver().delete(ImagePathProvider.CONTENT_URI, "path=?", new String[]{allImagesPath.get(currentItem)});
        mPager.setAdapter(null);
        allImagesPath.remove(currentItem);
        mAdapter.setmSize(allImagesPath.size());
        mPager.setAdapter(mAdapter);
        if(allImagesPath.size() == 0)
            super.onBackPressed();
        else if(currentItem == (allImagesPath.size()-1))
            mPager.setCurrentItem(currentItem - 1);
        else
            mPager.setCurrentItem(currentItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        addImageMenu = menu.findItem(R.id.add_images);
        selectImagesMenu = menu.findItem(R.id.select_images);
        deleteImagesMenu = menu.findItem(R.id.delete_images);
        shareImagesMenu = menu.findItem(R.id.share_images);
        setImageAs = menu.findItem(R.id.set_as_image);
        setImageAs.setVisible(true);
        addImageMenu.setVisible(false);
        selectImagesMenu.setVisible(false);
        deleteImagesMenu.setVisible(true);
        shareImagesMenu.setVisible(true);
        return true;
    }

    public void onClick(View v){
        final int vis = mPager.getSystemUiVisibility();
        if(showActionBar){
            actionBar.show();
            showActionBar = false;
        }
        else{
            actionBar.hide();
            showActionBar = true;
        }
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {
        private int mSize;
        ArrayList<Fragment> allFragments = new ArrayList<Fragment>();

        public ImagePagerAdapter(FragmentManager fm, int size) {
            super(fm);
            mSize = size;
        }

        public void setmSize(int mSize){
            this.mSize = mSize ;
        }

        @Override
        public int getCount() {
            return mSize;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment frag = ImageDetailFragment.newInstance(allImagesPath.get(position));
            allFragments.add(frag);
            return frag ;
        }
    }

    public void shareCurrentImage(String imageUrl){
        Uri uri = Uri.parse(imageUrl);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share images..."));
    }
}