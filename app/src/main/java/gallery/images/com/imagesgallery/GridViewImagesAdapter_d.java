package gallery.images.com.imagesgallery;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;

import gallery.images.com.imagesgallery.util.ImageFetcher;

/**
 * Created by mohsin on 30/08/16.
 */
public class GridViewImagesAdapter_d extends BaseAdapter{
    ArrayList<String> imagesList ;
    Context context ;
    ImageView overlayImage ;
    GridView.LayoutParams mGridViewLayoutParams;
    ImageFetcher imageFetcher ;
    public SparseBooleanArray mSelectedImages ;
    RelativeLayout selectedOverlay ;
    HashMap<Integer,RelativeLayout> allRelatives = new HashMap<Integer,RelativeLayout>();

    private int mItemHeight = 0;
    private int mNumColumns = 0;
    private int mActionBarHeight = 0;


    public GridViewImagesAdapter_d(ArrayList<String> imagesList, Context context, ImageFetcher imageFetcher){
        this.imagesList = imagesList ;
        this.context = context ;
        this.imageFetcher = imageFetcher ;
        mSelectedImages = new SparseBooleanArray();
        mGridViewLayoutParams = new GridView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public int getCount() {
        if (getNumColumns() == 0) {
            return 0;
        }
        else {
            return imagesList.size() + mNumColumns;
        }
    }

    @Override
    public Object getItem(int position) {
        return position < mNumColumns ?
                null : imagesList.get(position - mNumColumns);
    }

    @Override
    public long getItemId(int position) {
        return position < mNumColumns ? 0 : position - mNumColumns;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position < mNumColumns) {
            if (convertView == null) {
                convertView = new View(context);
            }
            // Set empty view with height of ActionBar
            convertView.setLayoutParams(new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, mActionBarHeight));
            return convertView;
        }
        ImageView imageView ;
        imageView = new RecyclingImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(mGridViewLayoutParams);
        overlayImage = new ImageView(context);
        overlayImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_tick_circle_green));
        imageFetcher.loadImage(imagesList.get(position-mNumColumns), imageView);
        RelativeLayout rlayout = new RelativeLayout(context);
        rlayout.addView(imageView);
        RelativeLayout inlayout = new RelativeLayout(context);
        inlayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        inlayout.setLayoutParams(lp);
        inlayout.addView(overlayImage);
        if(mSelectedImages.get(position))
            inlayout.setVisibility(View.VISIBLE);
        else
            inlayout.setVisibility(View.INVISIBLE);
        rlayout.addView(inlayout);
        allRelatives.put(position, inlayout);

       /* View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.row_show_gallery_images, null);
        selectedOverlay = (RelativeLayout) itemLayoutView.findViewById(R.id.selected_overlay);
        imageView = (ImageView) itemLayoutView.findViewById(R.id.img_album);
        imageFetcher.loadImage(imagesList.get(position-mNumColumns), imageView);
        allRelatives.put(position,selectedOverlay);*/
        return rlayout;
    }

    public void toggleSelection(int position){
        selectView(position, !mSelectedImages.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value) {
            mSelectedImages.put(position, value);
            allRelatives.get(position).setVisibility(View.VISIBLE);
        }
        else {
            mSelectedImages.delete(position);
            allRelatives.get(position).setVisibility(View.INVISIBLE);
        }
//        notifyDataSetChanged();
    }

    public void removeSelection() {
        mSelectedImages = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectAllImages(){
        resetallImages();
        int size = allRelatives.size();
        for(int i = 2 ; i < size+2 ; i++){
            allRelatives.get(i).setVisibility(View.VISIBLE);
        }
        int i = 2 ;
        for(String x : imagesList){
            mSelectedImages.put(i, true);
            i++;
        }
    }

    public void resetallImages(){
        int size = allRelatives.size();
        for(int i = 2 ; i < size+2 ; i++){
            allRelatives.get(i).setVisibility(View.INVISIBLE);
        }
        mSelectedImages = new SparseBooleanArray();
    }

    public int getSelectedCount() {
        return mSelectedImages.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedImages;
    }

    public void addImages(ArrayList<String> newImages){
        imagesList.addAll(newImages);
        notifyDataSetChanged();
    }

    public void deleteImages(ArrayList<String> deleteImages){
        imagesList.removeAll(deleteImages);
        notifyDataSetChanged();
    }

    public void setItemHeight(int height) {
        if (height == mItemHeight) {
            return;
        }
        mItemHeight = height;
        mGridViewLayoutParams =
                new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight);
        imageFetcher.setImageSize(height);
        notifyDataSetChanged();
    }

    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
    }

    public int getNumColumns() {
        return mNumColumns;
    }

}
