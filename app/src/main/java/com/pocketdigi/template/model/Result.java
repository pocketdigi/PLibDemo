package com.pocketdigi.template.model;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

/**
 *
 * Created by fhp on 15/1/13.
 */
public abstract class Result<T> {
    /**
     * IO错误
     */
    public static final String ERROR_CODE_IO = "-1";
    String code;
    String errorMsg;
    boolean success;

    T data;
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getErrorMsg() {
        return errorMsg;
    }


    public boolean isSuccess() {
        return success;
    }

    public T getData(){
        return data;
    }


}
