package psai.develop.khoondaan;

import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * Created by psai on 11/2/2015.
 */
public class Application extends android.app.Application {

    private static Application mInstance;
    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        // register with parse
        ParseUtils.registerParse(this);
    }

    public static synchronized Application getInstance() {
        return mInstance;
    }
}