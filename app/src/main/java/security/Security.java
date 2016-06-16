package security;

import android.content.Context;

/**
 * Created by fhp on 16/5/13.
 */
public class Security {
    static{
        System.loadLibrary("security");
    }
    /**返回apk签名的hashcode**/
    public native static int getApkSignHashCode(Context context);
    /**生成sign代码**/
    public native static String generateSignCode(String str,Context context);
}
