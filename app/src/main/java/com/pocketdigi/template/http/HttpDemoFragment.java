package com.pocketdigi.template.http;

import android.Manifest;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;

import com.pocketdigi.core.SFragment;
import com.pocketdigi.plib.core.PLog;
import com.pocketdigi.plib.core.PToast;
import com.pocketdigi.plib.http.PDownFileRequest;
import com.pocketdigi.plib.http.PHttp;
import com.pocketdigi.plib.http.PRequest;
import com.pocketdigi.plib.http.PResponseListener;
import com.pocketdigi.plib.http.DownProgressListener;
import com.pocketdigi.plib.http.PUploadRequest;
import com.pocketdigi.plib.http.UploadListener;
import com.pocketdigi.plib.util.RuntimeUtil;
import com.pocketdigi.template.R;
import com.pocketdigi.template.client.Client;
import com.pocketdigi.template.databinding.FragmentHttpBinding;
import com.pocketdigi.template.model.Person;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

/**
 * Http Demo
 * Created by Exception on 16/6/14.
 */
@EFragment(R.layout.fragment_http)
public class HttpDemoFragment extends SFragment {
    FragmentHttpBinding dataBinding;
    PDownFileRequest downFileRequest;
    PUploadRequest uploadRequest;
    public static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    @AfterViews
    public void afterViews() {
         dataBinding = DataBindingUtil.bind(getView());
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }

    @Click
    public void btnGet() {
        Client.get(new PResponseListener<String>() {
            @Override
            public void onResponse(PRequest request, String response) {
                dataBinding.setOutput(response);
            }

            @Override
            public void onError(PRequest request, Exception e) {

            }
        });
    }

    @Click
    public void btnPOST(){
        Client.post(new PResponseListener<String>() {
            @Override
            public void onResponse(PRequest request, String response) {
                dataBinding.setOutput(response);
            }

            @Override
            public void onError(PRequest request, Exception e) {

            }
        });
    }

    @Click
    public void btnPOSTJSON() {
        Person person=new Person();
        person.setAge(10);
        person.setName("王大头");
        person.setPhone("12343344");
        Client.postObject(person, new PResponseListener<String>() {
            @Override
            public void onResponse(PRequest request, String response) {
                dataBinding.setOutput(response);
            }

            @Override
            public void onError(PRequest request, Exception e) {

            }
        });
    }

    @Click
    public void btnDownload() {
        String savePath = RuntimeUtil.getContextFilesDir(getActivity()) + "/WindowsXP_SP2.exe";
        if(downFileRequest!=null)
            downFileRequest.cancel();
        downFileRequest = Client.downloadFile("http://speed.myzone.cn/WindowsXP_SP2.exe", savePath, new DownProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("已下载:").append(bytesRead / 1024.0f).append("KB\n");
                stringBuilder.append("总大小:").append(contentLength / 1024.0f).append("KB\n");
                stringBuilder.append("是否下载完:").append(done);
                dataBinding.setOutput(stringBuilder.toString());
            }
        });
    }

    @Click
    public void btnDownloadCancel(){
        if(downFileRequest!=null) {
            downFileRequest.cancel();
            PToast.show("下载已取消");
        }
    }

    @Click
    public void btnUpload() {
        uploadRequest = Client.upload("/sdcard/hello.txt", new UploadListener<String>() {
            @Override
            public void onUpload(long uploadBytes, long totalLength) {
                System.out.println("已上传:" + uploadBytes + "总长:" + totalLength);
            }

            @Override
            public void onResponse(PRequest request, String response) {
                System.out.println("上传结束");
                dataBinding.setOutput(response);
            }

            @Override
            public void onError(PRequest request, Exception e) {

            }
        });
    }

    @Click
    public void btnUploadCancel(){
        if(uploadRequest!=null)
            uploadRequest.cancel();
    }

}