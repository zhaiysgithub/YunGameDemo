package com.kptech.kputils.http.body;


import com.kptech.kputils.http.ProgressHandler;

/**
 * Created by wyouflf on 15/8/13.
 */
public interface ProgressBody extends RequestBody {
    void setProgressHandler(ProgressHandler progressHandler);
}
