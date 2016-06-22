package com.pocketdigi.template.client;

import com.pocketdigi.plib.http.DownProgressListener;
import com.pocketdigi.plib.http.PDownFileRequest;
import com.pocketdigi.plib.http.PHttp;
import com.pocketdigi.plib.http.PRequest;
import com.pocketdigi.plib.http.PResponseListener;
import com.pocketdigi.plib.http.PUploadRequest;
import com.pocketdigi.plib.http.UploadListener;
import com.pocketdigi.template.datamodel.BaiduImageResult;
import com.pocketdigi.template.model.Person;

/**
 * Created by Exception on 16/6/4.
 */
public class Client {
    public static final String API_PREFIX="http://192.168.199.204/wechat/index.php/Api/Index/";

    public static PDownFileRequest downloadFile(String url, String savePath, DownProgressListener listener) {
        PDownFileRequest pDownFileRequest = new PDownFileRequest(url, savePath,listener);
        PHttp.getInstance().addRequest(pDownFileRequest);
        return pDownFileRequest;
    }

    public static void postObject(Person person,PResponseListener<String> listener) {
        PRequest<String> request=new SignPRequest<>(PRequest.POST,  API_PREFIX+"add", listener,String.class);
        request.setPostObject(person);
        PHttp.getInstance().addRequest(request);
    }

    public static void post(PResponseListener<String> listener) {
        PRequest<String> request=new SignPRequest<>(PRequest.POST, API_PREFIX+"add", listener,String.class);
        request.addParam("p1","value1");
        request.addParam("p2","value2");
        PHttp.getInstance().addRequest(request);
    }
    public static void get(PResponseListener<String> listener) {
        PRequest<String> request=new SignPRequest<>(API_PREFIX+"add", listener,String.class);
        PHttp.getInstance().addRequest(request);
    }

    public static PUploadRequest upload(String filePath,UploadListener<String> listener) {
        PUploadRequest<String> request=new PUploadRequest<>(API_PREFIX+"upload","file",filePath, listener,String.class);
        PHttp.getInstance().addRequest(request);
        return request;
    }


    public static void getBaiduImageList(PResponseListener<BaiduImageResult> listener,int page){
        PRequest<BaiduImageResult> request=new PRequest<>("http://image.baidu.com/data/imgs?col=美女&tag=小清新&sort=0&pn="+page+"&rn=10&p=channel&from=1", listener,BaiduImageResult.class);
        PHttp.getInstance().addRequest(request);
    }

}
