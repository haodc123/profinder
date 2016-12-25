package conghaodng.demo.profinder.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import conghaodng.demo.profinder.BuildConfig;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;

/**
 *
 */
public class MyDiskCache {
	private DiskLruCache mDiskCache;
    private CompressFormat mCompressFormat = CompressFormat.JPEG;
    private int mCompressQuality = 80;
    private static final int VALUE_COUNT = 1;
    private final int IO_BUFFER_SIZE = 8 * 1024;
    private final String TAG = "Disk Cache";
    private Context ct;

    public MyDiskCache(Context context,String uniqueName, int diskCacheSize) {
        try {
        		this.ct = context;
                final File diskCacheDir = getDiskCacheDir(context, uniqueName );
                mDiskCache = DiskLruCache.open(diskCacheDir, Constants.APP_VERSION_CODE, VALUE_COUNT, diskCacheSize );
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void put( String key, Bitmap data ) {
        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskCache.edit(key);
            if ( editor == null ) {
                return;
            }
            if( writeBitmapToFile( data, editor ) ) {               
                mDiskCache.flush();
                editor.commit();
                if ( BuildConfig.DEBUG ) {
                   Log.d( TAG, "image put on disk cache " + key );
                }
            } else {
                editor.abort();
                if ( BuildConfig.DEBUG ) {
                    Log.d( TAG, "ERROR on: image put on disk cache " + key );
                }
            }   
        } catch (IOException e) {
            if ( BuildConfig.DEBUG ) {
                Log.d( TAG, "ERROR on: image put on disk cache " + key );
            }
            try {
                if ( editor != null ) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }           
        }
    }

    public Bitmap getBitmap( String key ) {
        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        try {

            snapshot = mDiskCache.get( key );
            if ( snapshot == null ) {
                return null;
            }
            final InputStream in = snapshot.getInputStream( 0 );
            if ( in != null ) {
                final BufferedInputStream buffIn = 
                new BufferedInputStream( in, IO_BUFFER_SIZE );
                bitmap = BitmapFactory.decodeStream( buffIn );   
            }   
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        if ( BuildConfig.DEBUG ) {
            Log.d( TAG, bitmap == null ? "" : "image read from disk " + key);
        }
        return bitmap;
    }
    
    private boolean writeBitmapToFile( Bitmap bitmap, DiskLruCache.Editor editor )
        throws IOException, FileNotFoundException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream( editor.newOutputStream( 0 ), IO_BUFFER_SIZE );
            return bitmap.compress( mCompressFormat, mCompressQuality, out );
        } finally {
            if ( out != null ) {
                out.close();
            }
        }
    }

    private File getDiskCacheDir(Context context, String uniqueName) {

    // Check if media is mounted or storage is built-in, if so, try and use external cache dir
    // otherwise use internal cache dir
        final String cachePath =
            Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                    !isExternalStorageRemovable() ?
                    getExternalCacheDir(context).getPath() :
                    context.getCacheDir().getPath();

        File f = new File(cachePath + File.separator + uniqueName);
        Log.d(TAG, f.getAbsolutePath().toString());
        return f;
    }

    public boolean containsKey( String key ) {

        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get( key );
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        return contained;

    }

    public void clearCache() {
        if ( BuildConfig.DEBUG ) {
            Log.d( TAG, "disk cache CLEARED");
        }
        try {
            mDiskCache.delete();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return mDiskCache.getDirectory();
    }
	
	public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }
    public static File getExternalCacheDir(Context context) {
        if (hasExternalCacheDir()) {
            return context.getExternalCacheDir();
        }
        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/PrF_cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }
    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }
}
