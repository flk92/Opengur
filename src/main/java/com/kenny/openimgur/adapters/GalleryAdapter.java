package com.kenny.openimgur.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kenny.openimgur.R;
import com.kenny.openimgur.activities.SettingsActivity;
import com.kenny.openimgur.classes.ImgurAlbum;
import com.kenny.openimgur.classes.ImgurBaseObject;
import com.kenny.openimgur.classes.ImgurPhoto;
import com.kenny.openimgur.classes.OpengurApp;
import com.kenny.openimgur.util.FileUtil;
import com.kenny.openimgur.util.ImageUtil;
import com.kenny.openimgur.util.LogUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import org.apache.commons.collections15.list.SetUniqueList;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * Created by kcampagna on 7/27/14.
 */
public class GalleryAdapter extends ImgurBaseAdapter<ImgurBaseObject> {
    public static final int MAX_ITEMS = 200;

    private int mUpVoteColor;

    private int mDownVoteColor;

    private boolean mAllowNSFWThumb;

    private boolean mShowPoints = true;

    private String mThumbnailQuality;

    public GalleryAdapter(Context context, SetUniqueList<ImgurBaseObject> objects) {
        super(context, objects, true);
        mUpVoteColor = mResources.getColor(R.color.notoriety_positive);
        mDownVoteColor = mResources.getColor(R.color.notoriety_negative);
        SharedPreferences pref = OpengurApp.getInstance(context).getPreferences();
        mAllowNSFWThumb = pref.getBoolean(SettingsActivity.KEY_NSFW_THUMBNAILS, false);
        mThumbnailQuality = pref.getString(SettingsActivity.KEY_THUMBNAIL_QUALITY, ImgurPhoto.THUMBNAIL_GALLERY);
    }

    public GalleryAdapter(Context context, SetUniqueList<ImgurBaseObject> objects, boolean showPoints) {
        this(context, objects);
        mShowPoints = showPoints;
    }

    @Override
    protected DisplayImageOptions getDisplayOptions() {
        return ImageUtil.getDisplayOptionsForGallery().build();
    }

    /**
     * Returns a list of objects for the viewing activity. This will return a max of 200 items to avoid memory issues.
     * 100 before and 100 after the currently selected position. If there are not 100 available before or after, it will go to as many as it can
     *
     * @param position The position of the selected items
     * @return
     */
    public ArrayList<ImgurBaseObject> getItems(int position) {
        List<ImgurBaseObject> objects;
        int size = getCount();

        if (position - MAX_ITEMS / 2 < 0) {
            objects = getAllItems().subList(0, size > MAX_ITEMS ? position + (MAX_ITEMS / 2) : size);
        } else {
            objects = getAllItems().subList(position - (MAX_ITEMS / 2), position + (MAX_ITEMS / 2) <= size ? position + (MAX_ITEMS / 2) : size);
        }

        return new ArrayList<>(objects);
    }

    /**
     * Removes an item from the adapter given an id
     *
     * @param id The id of the item
     * @return If the item was removed
     */
    public boolean removeItem(String id) {
        List<ImgurBaseObject> items = getAllItems();
        boolean removed = false;

        for (ImgurBaseObject obj : items) {
            if (obj.getId().equals(id)) {
                removeItem(obj);
                removed = true;
                break;
            }
        }

        return removed;
    }

    public void setAllowNSFW(boolean allowNSFW) {
        mAllowNSFWThumb = allowNSFW;
        notifyDataSetChanged();
    }

    public void setThumbnailQuality(String quality) {
        if (!mThumbnailQuality.equals(quality)) {
            LogUtil.v(TAG, "Updating thumbnail quality to " + quality);
            // Clear any memory cache we may have for the new thumbnail
            mImageLoader.clearMemoryCache();
            mThumbnailQuality = quality;
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GalleryHolder holder;
        ImgurBaseObject obj = getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.gallery_item, parent, false);
            holder = new GalleryHolder(convertView);
        } else {
            holder = (GalleryHolder) convertView.getTag();
        }

        // Get the appropriate photo to display
        if (obj.isNSFW() && !mAllowNSFWThumb) {
            holder.image.setImageResource(R.drawable.ic_nsfw);
            holder.itemType.setVisibility(View.GONE);
        } else if (obj instanceof ImgurPhoto) {
            ImgurPhoto photoObject = ((ImgurPhoto) obj);
            String photoUrl;

            // Check if the link is a thumbed version of a large gif
            if (photoObject.hasMP4Link() && photoObject.isLinkAThumbnail() && ImgurPhoto.IMAGE_TYPE_GIF.equals(photoObject.getType())) {
                photoUrl = photoObject.getThumbnail(mThumbnailQuality, true, FileUtil.EXTENSION_GIF);
            } else {
                photoUrl = ((ImgurPhoto) obj).getThumbnail(mThumbnailQuality, false, null);
            }

            displayImage(holder.image, photoUrl);

            if(photoObject.isAnimated()) {
                holder.itemType.setVisibility(View.VISIBLE);
                holder.itemType.setImageResource(R.drawable.ic_gif_24dp);
                holder.itemType.setBackgroundColor(mResources.getColor(R.color.black_55));
            }else{
                holder.itemType.setVisibility(View.GONE);
            }
        } else if (obj instanceof ImgurAlbum) {
            ImgurAlbum album = ((ImgurAlbum) obj);
            displayImage(holder.image, album.getCoverUrl(mThumbnailQuality));
            int albumImageId;

            switch (album.getAlbumImageCount()) {
                case 1:
                    albumImageId = R.drawable.numeric_1_box_24dp;
                    break;

                case 2:
                    albumImageId = R.drawable.numeric_2_box_24dp;
                    break;

                case 3:
                    albumImageId = R.drawable.numeric_3_box_24dp;
                    break;

                case 4:
                    albumImageId = R.drawable.numeric_4_box_24dp;
                    break;

                case 5:
                    albumImageId = R.drawable.numeric_5_box_24dp;
                    break;

                case 6:
                    albumImageId = R.drawable.numeric_6_box_24dp;
                    break;

                case 7:
                    albumImageId = R.drawable.numeric_7_box_24dp;
                    break;

                case 8:
                    albumImageId = R.drawable.numeric_8_box_24dp;
                    break;

                case 9:
                    albumImageId = R.drawable.numeric_9_box_24dp;
                    break;

                default:
                    albumImageId = R.drawable.numeric_9_plus_box_24dp;
                    break;
            }

            holder.itemType.setImageResource(albumImageId);
            holder.itemType.setVisibility(View.VISIBLE);
            holder.itemType.setBackground(null);
        } else {
            String url = ImgurBaseObject.getThumbnail(obj.getId(), obj.getLink(), mThumbnailQuality);
            displayImage(holder.image, url);
            holder.itemType.setVisibility(View.GONE);
        }

        if (mShowPoints) {
            int totalPoints = obj.getUpVotes() - obj.getDownVotes();
            holder.score.setText(mResources.getQuantityString(R.plurals.points, totalPoints, totalPoints));
            holder.score.setVisibility(View.VISIBLE);
        } else {
            holder.score.setVisibility(View.GONE);
        }

        if (obj.isFavorited() || ImgurBaseObject.VOTE_UP.equals(obj.getVote())) {
            holder.score.setTextColor(mUpVoteColor);
        } else if (ImgurBaseObject.VOTE_DOWN.equals(obj.getVote())) {
            holder.score.setTextColor(mDownVoteColor);
        } else {
            holder.score.setTextColor(Color.WHITE);
        }

        return convertView;
    }

    static class GalleryHolder extends ImgurViewHolder {
        @Bind(R.id.image)
        ImageView image;

        @Bind(R.id.score)
        TextView score;

        @Bind(R.id.itemType)
        ImageView itemType;

        public GalleryHolder(View view) {
            super(view);
        }
    }
}
