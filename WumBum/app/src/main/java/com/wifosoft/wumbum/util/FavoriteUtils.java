package com.wifosoft.wumbum.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.util.Log;

import com.wifosoft.wumbum.model.Media;
import com.wifosoft.wumbum.util.preferences.Prefs;

public class FavoriteUtils {
    public FavoriteUtils() {
        super();
    }

    public static Set<String> listMediaToSet(List<Media> medias) {
        Set res = new HashSet<>();
        for (Media media : medias) {
            res.add(media.getPath());
        }
        return res;
    }

    // This four methods are used for maintaining favorites.
    public static void saveFavorites(Set<String> favorites) {
        Prefs.setFavoriteMedias(favorites);
    }

    public static void addFavorite(Media media) {
        Set<String> favorites = FavoriteUtils.getFavorites();
        favorites.add(media.getPath());
        FavoriteUtils.saveFavorites(favorites);
    }

    public static void removeFavorite(Media media) {
        Set<String> favorites = FavoriteUtils.getFavorites();
        favorites.remove(media.getPath());
        FavoriteUtils.saveFavorites(favorites);
    }

    public static Set<String> getFavorites() {
        return Prefs.getFavoriteMedias();
    }

    public static boolean isFavorite(Media media) {
        Set<String> favorites = FavoriteUtils.getFavorites();
        System.out.println(favorites);
        System.out.println(media.getPath());
        return favorites.contains(media.getPath());
    }
}
