package com.pocketdigi.template.client;

import android.os.Build;

import com.google.gson.Gson;
import com.pocketdigi.plib.core.PLog;
import com.pocketdigi.plib.http.PRequest;
import com.pocketdigi.plib.http.PResponseListener;
import com.pocketdigi.plib.util.RuntimeUtil;
import com.pocketdigi.template.MainActivity;

import java.util.Map;
import java.util.Set;

import okhttp3.Request;
import security.Security;
import security.TimestampUtils;

/**
 * 自动签名的Request
 * 所有公共参数放header里，因为像post,put方法，如果传的是json字符串，公共参数不好处理
 * header里的参数不参与签名，但有些参数如token,timestamp，需要参与签名，特殊处理
 * 参数按key排序，所以用TreeMap
 * 签名方法:url+(requestBody json||所有post参数(key+value))+timestamp+token,调用Security.generateSignCode(String str)方法，生成sign,加到header里，header加timestamp
 * jni里的generateSignCode方法会判断apk签名的hashcode来确认是否被二次打包，请自行修改src/main/jni/security.c里的KEY_HASHCODE_RELEASE和KEY_HASHCODE_DEBUG
 * 参与签名的md5 key也在src/main/jni/security.c里，务必修改
 * Created by Exception on 16/6/17.
 */
public class SignPRequest<T> extends PRequest<T> {
    public SignPRequest(@METHOD String method, String url, PResponseListener<T> responseListener, Class<T> responseType) {
        super(method, url, responseListener, responseType);
    }

    public SignPRequest(String url, PResponseListener<T> responseListener, Class<T> responseType) {
        super(url, responseListener, responseType);
    }

    public SignPRequest(String url) {
        super(url);
    }

    @Override
    public Request buildRequest() {
        addCommonHeader();
        //加timestamp
        long timeStamp = TimestampUtils.getInstance().getTimeStamp();
        addHeader("timestamp",String.valueOf(timeStamp));
        //加sign
        StringBuilder stringBuilder=new StringBuilder(url);
        //拼参数
        if (postObject != null) {
            //post json
            Gson gson=new Gson();
            String json = gson.toJson(postObject);
            stringBuilder.append(json);
        }else if(params!=null) {
            Set<Map.Entry<String, String>> paramEntries = params.entrySet();
            for (Map.Entry<String, String> entry : paramEntries) {
                stringBuilder.append(entry.getKey()).append(entry.getValue());
            }
        }
        //加时间戳
        stringBuilder.append(timeStamp);
        //加token
        stringBuilder.append("tokenValue");
        //签名
        String signStr=stringBuilder.toString();
        String signCode = Security.generateSignCode(signStr, MainActivity.getInstance());
        PLog.d(this,"sign str:"+signStr);
        PLog.d(this,"sign code:"+signCode);
        addHeader("sign",signCode);
        return super.buildRequest();
    }

    /**
     * 添加通用参数
     */
    private void addCommonHeader(){
        addHeader("sv", Build.VERSION.RELEASE);
        addHeader("v", RuntimeUtil.getCurrentVersionName());
        addHeader("token", "tokenValue");
    }
}
