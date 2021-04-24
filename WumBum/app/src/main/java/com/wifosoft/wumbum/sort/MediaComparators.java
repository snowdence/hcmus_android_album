package com.wifosoft.wumbum.sort;


import com.wifosoft.wumbum.model.Media;

import java.util.Comparator;

public class MediaComparators {

    public static Comparator<Media> getComparator(AlbumSettings settings) {
        return getComparator(settings.getSortingMode(), settings.getSortingOrder());
    }

    public static Comparator<Media> getComparator(SortingMode sortingMode, SortingOrder sortingOrder) {
        return sortingOrder == SortingOrder.ASCENDING
                ? getComparator(sortingMode) : reverse(getComparator(sortingMode));
    }


    public static Comparator<Media> getComparator(SortingMode sortingMode) {
        switch (sortingMode) {
            case NAME: return getNameComparator();
            case DATE: default: return getDateComparator();
            case SIZE: return getSizeComparator();
            case TYPE: return getTypeComparator();
        }
    }

    private static <T> Comparator<T> reverse(Comparator<T> comparator) {
        return (o1, o2) -> comparator.compare(o2, o1);
    }

    private static Comparator<Media> getDateComparator() {
        return (f1, f2) -> f1.getDateModified().compareTo(f2.getDateModified());
    }

    private static Comparator<Media> getNameComparator() {
        return (f1, f2) -> f1.getPath().compareTo(f2.getPath());
    }

    private static Comparator<Media> getSizeComparator() {
        return (f1, f2) -> Long.compare(f1.getSize(), f2.getSize());
    }

    private static Comparator<Media> getTypeComparator() {
        return (f1, f2) -> f1.getMimeType().compareTo(f2.getMimeType());
    }


}
