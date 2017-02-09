package gallery.images.com.imagesgallery;

import android.app.Application;
import android.content.Context;

/**
 * Created by mohsin on 29/01/17.
 */

public class ApplicationApp extends Application {

    public static Context context ;

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
