package com.wifosoft.wumbum.model;


import android.os.Parcel;
import android.os.Parcelable;


import com.wifosoft.wumbum.filter.FilterMode;
import com.wifosoft.wumbum.sort.SortingMode;
import com.wifosoft.wumbum.sort.SortingOrder;

import java.io.Serializable;

/**
 * Created by dnld on 2/4/16.
 */
public class AlbumSettings implements Serializable, Parcelable {

    String coverPath;
    int sortingMode, sortingOrder;
    boolean pinned;
    String password = "";
    FilterMode filterMode = FilterMode.ALL;

    public boolean hasPassword(){
        if (password == null){return false;}
        if (password.isEmpty()) {return false;}
        if( password.equals("")) {return false;}
        return true;
    }



    public static AlbumSettings getDefaults() {
        return new AlbumSettings(null, SortingMode.DATE.getValue(), 1, 0 , "");
    }

    public static AlbumSettings getFavorite() {
        return new AlbumSettings(null, SortingMode.DATE.getValue(), 1, 0, "" , FilterMode.FAVORITE);
    }

    public AlbumSettings(String cover, int sortingMode, int sortingOrder, int pinned, String password) {
        this.coverPath = cover;
        this.sortingMode = sortingMode;
        this.sortingOrder = sortingOrder;
        this.pinned = pinned == 1;
        this.password = password;
    }

    public AlbumSettings(String cover, int sortingMode, int sortingOrder, int pinned, String password, FilterMode filterMode) {
        this.coverPath = cover;
        this.sortingMode = sortingMode;
        this.sortingOrder = sortingOrder;
        this.pinned = pinned == 1;
        this.password = password;
        this.filterMode = filterMode;
    }

    public SortingMode getSortingMode() {
        return SortingMode.fromValue(sortingMode);
    }

    public SortingOrder getSortingOrder() {
        return SortingOrder.fromValue(sortingOrder);
    }
    public String getPassword() {
        return this.password;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.coverPath);
        dest.writeInt(this.sortingMode);
        dest.writeInt(this.sortingOrder);
        dest.writeByte(this.pinned ? (byte) 1 : (byte) 0);
        dest.writeString(this.password);
        dest.writeInt(this.filterMode == null ? -1 : this.filterMode.ordinal());
    }

    /** This is the constructor used by CREATOR. */
    protected AlbumSettings(Parcel in) {
        this.coverPath = in.readString();
        this.sortingMode = in.readInt();
        this.sortingOrder = in.readInt();
        this.pinned = in.readByte() != 0;
        this.password = in.readString();
        int tmpFilterMode = in.readInt();
        this.filterMode = tmpFilterMode == -1 ? null : FilterMode.values()[tmpFilterMode];
    }

    /** It is a non-null static field that must be in parcelable. */
    public static final Parcelable.Creator<AlbumSettings> CREATOR = new Parcelable.Creator<AlbumSettings>() {

        @Override
        public AlbumSettings createFromParcel(Parcel source) {
            return new AlbumSettings(source);
        }

        @Override
        public AlbumSettings[] newArray(int size) {
            return new AlbumSettings[size];
        }
    };
}