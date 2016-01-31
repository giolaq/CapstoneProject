package com.laquysoft.motivetto.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by joaobiriba on 30/01/16.
 */
public class ParcelableSpotifyObject implements Parcelable {

    public String mName;
    public String mFatherName;
    public String mArtistName;
    public String largeThumbnailUrl;
    public String smallThumbnailUrl;
    public String previewUrl;

    public ParcelableSpotifyObject(String name, String mFatherName, String mArtistName, String largeThumbnailUrl,
                                   String smallThumbnailUrl, String previewUrl) {
        this.mName = name;
        this.mFatherName = mFatherName;
        this.mArtistName = mArtistName;
        this.largeThumbnailUrl = largeThumbnailUrl;
        this.smallThumbnailUrl = smallThumbnailUrl;
        this.previewUrl = previewUrl;
    }

    private ParcelableSpotifyObject(Parcel in) {
        mName = in.readString();
        mFatherName = in.readString();
        mArtistName = in.readString();
        largeThumbnailUrl = in.readString();
        smallThumbnailUrl = in.readString();
        previewUrl = in.readString();
    }


    public static final Creator<ParcelableSpotifyObject> CREATOR = new Creator<ParcelableSpotifyObject>() {
        @Override
        public ParcelableSpotifyObject createFromParcel(Parcel in) {
            return new ParcelableSpotifyObject(in);
        }

        @Override
        public ParcelableSpotifyObject[] newArray(int size) {
            return new ParcelableSpotifyObject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeString(this.mFatherName);
        dest.writeString(this.mArtistName);
        dest.writeString(this.largeThumbnailUrl);
        dest.writeString(this.smallThumbnailUrl);
        dest.writeString(this.previewUrl);
    }
}