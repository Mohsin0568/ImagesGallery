package gallery.images.com.imagesgallery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import gallery.images.com.imagesgallery.ImagesGallery.SelectableAdapter;
import gallery.images.com.imagesgallery.R;

import java.util.ArrayList;

public class GridViewImagesAdapter extends SelectableAdapter<GridViewImagesAdapter.ViewHolder> {

    public Context mContext;
    private ArrayList<String> mAlbumImages;
    public boolean mShowCheckBox;
    private ViewHolder.ClickListener clickListener;

    public GridViewImagesAdapter(Context context, ArrayList<String> galleryImagesList, ViewHolder.ClickListener clickListener) {
        this.mAlbumImages = galleryImagesList;
        this.mContext = context;
        this.clickListener = clickListener;

    }

    @Override
    public GridViewImagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.row_show_gallery_images, null);

        // create ViewHolder

        ViewHolder viewHolder = new ViewHolder(itemLayoutView,clickListener);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        final int pos = position;
        Glide.with(mContext)
                .load("file://"+mAlbumImages.get(position))
                .centerCrop()
                .placeholder(R.drawable.image_loading)
                .crossFade()
                .into (viewHolder.imgAlbum);
        viewHolder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
    }

    // Return the size arraylist
    @Override
    public int getItemCount() {
        return mAlbumImages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {

        private final RelativeLayout selectedOverlay;
        public ImageView imgAlbum;


        private ClickListener listener;

        public ViewHolder(View itemLayoutView,ClickListener listener) {
            super(itemLayoutView);

            this.listener = listener;
            imgAlbum = (ImageView) itemLayoutView.findViewById(R.id.img_album);
            selectedOverlay = (RelativeLayout) itemView.findViewById(R.id.selected_overlay);

            itemLayoutView.setOnClickListener(this);

            itemLayoutView.setOnLongClickListener (this);


        }
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(getAdapterPosition ());
            }
        }
        @Override
        public boolean onLongClick (View view) {
            if (listener != null) {
                return listener.onItemLongClicked(getAdapterPosition ());
            }
            return false;
        }

        public interface ClickListener {
            public void onItemClicked(int position);

            public boolean onItemLongClicked(int position);
        }
    }

    // method to access in activity after updating selection
    public ArrayList<String> getImagesPaths() {
        return mAlbumImages;
    }

    public void addImages(ArrayList<String> images){
        this.mAlbumImages.addAll(images);
        notifyDataSetChanged();
    }

    public void deleteAllImages(){
        this.mAlbumImages.clear();
        notifyDataSetChanged();
    }

    public void removeImages(ArrayList<String> images){
        this.mAlbumImages.remove(images);
        notifyDataSetChanged();
    }

}