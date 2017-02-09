package gallery.images.com.imagesgallery;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.File;
import java.util.ArrayList;

import gallery.images.com.imagesgallery.ImagesGallery.AlbumImagesView;
import gallery.images.com.imagesgallery.util.ImageCache;
import gallery.images.com.imagesgallery.util.ImageFetcher;
import gallery.images.com.imagesgallery.util.Utils;
import gallery.images.com.imagesgallery.util.contentResolver.ImagePathProvider;


public class GridViewImages extends Fragment implements AdapterView.OnItemClickListener{

    Context context ;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager lmanager ;
    private static final String IMAGE_CACHE_DIR = "thumbs";
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    public static final int IMAGE_ACTIVITY_RESULT = 0 ;
    int MY_PERMISSIONS_REQUEST_READ_Images = 10 ;
    private ImageFetcher imageFetcher ;
    private boolean selectFlag = false, actionFlag = false;
    private MenuItem addImageMenu, selectImagesMenu, deleteImagesMenu, shareImagesMenu, backImageMenu, signInOut ;
    private ActionBar actionBar ;
    private int numColumns ;
    private int progressBarStatus = 0 ;
    private ProgressDialog progressBar ;

    private GridViewImagesAdapter_d mAdapter;
    ArrayList<String> allImagespath = new ArrayList<String>();
    ArrayList imagesPath ;
    private LinearLayout floatingButtonsLayout ;
    private FloatingActionButton selectAllImages, resetAllImages ;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    Uri capturedImageUri ;

    public GridViewImages() {
        super();
    }

    public static GridViewImages newInstance(String param1, String param2) {
        GridViewImages fragment = new GridViewImages();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(context, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f);

        imageFetcher = new ImageFetcher(context, mImageThumbSize);
        imageFetcher.clearCache();
        imageFetcher.flushCache();
        imageFetcher.closeCache();
        imageFetcher.clearCacheInternal();
        imageFetcher.flushCacheInternal();
        imageFetcher.closeCacheInternal();
        imageFetcher.setLoadingImage(R.drawable.empty_photo);
        imageFetcher.addImageCache(getFragmentManager(), cacheParams);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_grid_view_images, container, false);
        getAllImgagesPath();

        final GridView gridView = (GridView) v.findViewById(R.id.gridView);
        actionBar = ((MainActivity)context).getSupportActionBar();
        actionBar.setTitle("Images Gallery");
        floatingButtonsLayout = (LinearLayout) v.findViewById(R.id.floating_buttons_layout);
        selectAllImages = (FloatingActionButton) v.findViewById(R.id.select_all_images);
        resetAllImages = (FloatingActionButton) v.findViewById(R.id.reset_all_images);
        /*mRecyclerView = (RecyclerView) v.findViewById(R.id.gridImagesRecycler);
        mRecyclerView.setHasFixedSize(true);


        mRecyclerView.setLayoutManager(new GridLayoutManager(context,2));
*/
        mAdapter = new GridViewImagesAdapter_d(allImagespath, context, imageFetcher);
//        mAdapter = new GridViewImagesAdapter_d(context, allImagespath, mImageFetcher);

        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Before Honeycomb pause image loading on scroll to help with performance
                    if (!Utils.hasHoneycomb()) {
                        imageFetcher.setPauseWork(true);
                    }
                } else {
                    imageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });

//        mRecyclerView.setAdapter(mAdapter);

        gridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {
                        if (mAdapter.getNumColumns() == 0) {
                            numColumns = (int) Math.floor(
                                    gridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                            if (numColumns > 0) {
                                final int columnWidth =
                                        (gridView.getWidth() / numColumns) - mImageThumbSpacing;
                                mAdapter.setNumColumns(numColumns);
                                mAdapter.setItemHeight(columnWidth);
                                if (BuildConfig.DEBUG) {
                                    Log.d("Images Log", "onCreateView - numColumns set to " + numColumns);
                                }
                                if (Utils.hasJellyBean()) {
                                    gridView.getViewTreeObserver()
                                            .removeOnGlobalLayoutListener(this);
                                } else {
                                    gridView.getViewTreeObserver()
                                            .removeGlobalOnLayoutListener(this);
                                }
                            }
                        }
                    }
                });
        selectAllImages.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                mAdapter.selectAllImages();
                selectFlag = true;
                actionFlag = true;
                actionBar.setTitle(allImagespath.size()+"  Selected");
                getActivity().invalidateOptionsMenu();
            }
        });

        resetAllImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.resetallImages();
                selectFlag = false;
                getActivity().invalidateOptionsMenu();
                floatingButtonsLayout.setVisibility(View.INVISIBLE);
            }
        });

        return v ;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context ;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id){
        if(selectFlag == false) {
            Intent intent = new Intent(context, FullViewImages.class);
            intent.putStringArrayListExtra("imagesList", allImagespath);
            intent.putExtra(FullViewImages.EXTRA_IMAGE, (int) id);
            if (Utils.hasJellyBean()) {
                ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
                getActivity().startActivity(intent, options.toBundle());
            } else {
                startActivity(intent);
            }
        }
        else{
            mAdapter.toggleSelection(position);
            actionBar.setTitle(mAdapter.getSelectedCount()+"  Selected");
            if(mAdapter.getSelectedCount() == 1){
                actionFlag = true;
                getActivity().invalidateOptionsMenu();
            }
            else if(mAdapter.getSelectedCount() == 0){
                actionFlag = false;
                getActivity().invalidateOptionsMenu();
            }
        }
    }

    public void getAllImgagesPath(){
        allImagespath.clear();
        Cursor c = getActivity().managedQuery(ImagePathProvider.CONTENT_URI,null,null,null,"PATH");
        if(c.moveToFirst()){
            do{
                String names[] = c.getColumnNames();
                String path = c.getString(c.getColumnIndex("path"));
                File f = new File(path);
                if(f.exists())
                    allImagespath.add(path);
            }while(c.moveToNext());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        ContentValues values = new ContentValues();
        if(requestCode == IMAGE_ACTIVITY_RESULT && null != data){
            imagesPath = data.getIntegerArrayListExtra("urls");
            ArrayList<String> newImages = new ArrayList<String>();
            Uri uri;
            int i = 0 ;
            for(Object x : imagesPath){
                if(!(allImagespath.contains(x))) {
                    values.put(ImagePathProvider.PATH, x.toString());
                    uri = getActivity().getContentResolver().insert(ImagePathProvider.CONTENT_URI, values);
                    i++;
                    newImages.add(x.toString());
                }
            }
            if(i>0){
                mAdapter.addImages(newImages);
            }
        }
        else if(requestCode == REQUEST_IMAGE_CAPTURE){
            String capturedPath = getRealPathFromURI(context, capturedImageUri) ;
            values.put(ImagePathProvider.PATH, capturedPath);
            getActivity().getContentResolver().insert(ImagePathProvider.CONTENT_URI, values);

        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            System.out.println("What the f " + contentUri);
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
    public void onResume(){
        super.onResume();
        getAllImgagesPath();
        if(allImagespath.size() == 0){
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            NoImagesFragment noImagesFragment = new NoImagesFragment();
            transaction.replace(R.id.fragment_body, noImagesFragment);
            transaction.commit();
        }
        imageFetcher.setExitTasksEarly(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause(){
        super.onPause();
        imageFetcher.setPauseWork(false);
        imageFetcher.setExitTasksEarly(true);
        imageFetcher.flushCache();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        imageFetcher.closeCache();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        addImageMenu = menu.findItem(R.id.add_images);
        selectImagesMenu = menu.findItem(R.id.select_images);
        deleteImagesMenu = menu.findItem(R.id.delete_images);
        shareImagesMenu = menu.findItem(R.id.share_images);
        deleteImagesMenu.setVisible(false);
        shareImagesMenu.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_images) {
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.add_image_options_dialog);
            dialog.setCancelable(true);
            dialog.show();
            Button fromCamera = (Button) dialog.findViewById(R.id.add_image_from_camera);
            Button fromGallery = (Button) dialog.findViewById(R.id.add_image_from_gallery);
            fromCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "Image File name");
                    capturedImageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                    if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                    dialog.cancel();
                }
            });
            fromGallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_Images);
                    }
                    else{
                        Intent intent = new Intent(context,AlbumImagesView.class);
                        startActivityForResult(intent, IMAGE_ACTIVITY_RESULT);
                    }
                    dialog.cancel();
                }
            });
            return true;
        }
        else if(id == R.id.select_images){
            selectFlag = true;
            getActivity().invalidateOptionsMenu();
            floatingButtonsLayout.setVisibility(View.VISIBLE);
            selectFlag = true ;
        }
        else if(id == android.R.id.home){
            mAdapter.removeSelection();
            selectFlag = false;
            actionFlag = false;
            floatingButtonsLayout.setVisibility(View.INVISIBLE);
            getActivity().invalidateOptionsMenu();
        }
        else if(id == R.id.delete_images){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setMessage("Deleting Images from Favorito will not remove Images from Phone memory.");
            alertDialog.setPositiveButton("Ok, Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteImagesFromGrid();
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog dialog = alertDialog.create();
            dialog.show();
        }
        else if(id == R.id.share_images){
            shareImages();
            actionFlag = false;
            selectFlag = false;
            mAdapter.removeSelection();
            getActivity().invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(selectFlag && actionFlag){
            deleteImagesMenu.setVisible(true);
            shareImagesMenu.setVisible(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            selectImagesMenu.setVisible(false);
            addImageMenu.setVisible(false);
        }
        else if(selectFlag){
            actionBar.setTitle("Select Images");
            deleteImagesMenu.setVisible(false);
            shareImagesMenu.setVisible(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            selectImagesMenu.setVisible(false);
            addImageMenu.setVisible(false);
        }
        else{
            actionBar.setTitle("Images Gallery");
            deleteImagesMenu.setVisible(false);
            shareImagesMenu.setVisible(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            selectImagesMenu.setVisible(true);
            addImageMenu.setVisible(true);
        }
        super.onPrepareOptionsMenu(menu);
    }

    public void shareImages(){
        SparseBooleanArray selectedImages = mAdapter.getSelectedIds();
        int size = selectedImages.size();
        ArrayList<Uri> uris = new ArrayList<Uri>();
        for(int i = 0 ; i < size ; i++){
            uris.add(Uri.parse(allImagespath.get(selectedImages.keyAt(i)-numColumns)));
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share images..."));
    }

    public void deleteImagesFromGrid(){
        SparseBooleanArray selectedImages = mAdapter.getSelectedIds();
        int size = selectedImages.size();
        ArrayList<String> dimages = new ArrayList<String>();
        startProgressBar();
        for(int i = 0 ; i < size ; i++){
            getActivity().getContentResolver().delete(ImagePathProvider.CONTENT_URI, "path=?", new String[]{allImagespath.get(selectedImages.keyAt(i)-numColumns)});
            progressBarStatus = ((i+1)/size)*100;
            dimages.add(allImagespath.get(selectedImages.keyAt(i)-numColumns));
        }
        allImagespath.clear();
        getAllImgagesPath();
        actionFlag = false;
        selectFlag = false;
        mAdapter.removeSelection();
        getActivity().invalidateOptionsMenu();
        mAdapter.deleteImages(dimages);
        if(allImagespath.size() == 0){
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            NoImagesFragment noImagesFragment = new NoImagesFragment();
            transaction.replace(R.id.fragment_body, noImagesFragment);
            transaction.commit();
        }
    }

    public void startProgressBar(){
        progressBar = new ProgressDialog(context);
        progressBar.setCancelable(false);
        progressBar.setMessage("Deleting Files");
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(progressBarStatus <= 100){
                    try{
                        Thread.sleep(100);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    progressBar.setProgress(progressBarStatus);
                }
            }
        }).start();
        progressBar.dismiss();
    }

}

