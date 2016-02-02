package com.laquysoft.motivetto;

/**
 * Created by joaobiriba on 28/01/16.
 */
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import android.graphics.Bitmap;
import android.util.Log;

public class TileServer {

    private static final String LOG_TAG = TileServer.class.getSimpleName();

    public class TilePair
    {
        private final Bitmap bitmap;
        private final int seekTime;

        public TilePair(Bitmap aBitmap, int aSeekTime)
        {
            bitmap   = aBitmap;
            seekTime = aSeekTime;
        }

        public Bitmap bitmap()   { return bitmap; }
        public int seekTime() { return seekTime; }
    }

    Bitmap original, scaledImage;
    int rows, columns, width, tileSize;
    HashSet<TilePair> slices;
    ArrayList<TilePair> unservedSlices;
    Random random;

    public TileServer(Bitmap original, int rows, int columns, int tileSize) {
        super();
        this.original = original;
        this.rows = rows;
        this.columns = columns;
        this.tileSize = tileSize;

        random = new Random();
        slices = new HashSet<TilePair>();
        sliceOriginal();
    }

    protected void sliceOriginal() {
        int fullWidth = tileSize * rows;
        int fullHeight = tileSize * columns;
        scaledImage = Bitmap.createScaledBitmap(original, fullWidth, fullHeight, true);

        int x, y;
        Bitmap bitmap;
        TilePair tilepair;
        for (int colI=0; colI<3; colI++) {
            for (int rowI=0; rowI<3; rowI++) {
                x = rowI * tileSize;
                y = colI * tileSize;
                bitmap = Bitmap.createBitmap(scaledImage, x, y, tileSize, tileSize);
                int seekTime = slices.size() * 3;
                Log.d(LOG_TAG, "sliceOriginal: " + seekTime);
                tilepair = new TilePair(bitmap,seekTime);
                slices.add(tilepair);
            }
        }
        unservedSlices = new ArrayList<TilePair>();
        unservedSlices.addAll(slices);
    }

    public TilePair serveRandomSlice() {
        if (unservedSlices.size() > 0) {
            int randomIndex = random.nextInt(unservedSlices.size());
            TilePair tilePair = unservedSlices.remove(randomIndex);
            return tilePair;
        }
        return null;
    }


}