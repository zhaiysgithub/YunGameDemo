package com.kptech.kputils.http.loader;

import android.text.TextUtils;

import com.kptech.kputils.cache.DiskCacheEntity;
import com.kptech.kputils.common.util.IOUtil;
import com.kptech.kputils.http.RequestParams;
import com.kptech.kputils.http.request.UriRequest;

/**
 * Author: wyouflf
 * Time: 2014/05/30
 */
/*package*/ class StringLoader extends Loader<String> {

    private String charset = "UTF-8";
    private String resultStr = null;

    @Override
    public Loader<String> newInstance() {
        return new StringLoader();
    }

    @Override
    public void setParams(final RequestParams params) {
        if (params != null) {
            String charset = params.getCharset();
            if (!TextUtils.isEmpty(charset)) {
                this.charset = charset;
            }
        }
    }

    @Override
    public String load(final UriRequest request) throws Throwable {
        request.sendRequest();
        resultStr = IOUtil.readStr(request.getInputStream(), charset);
        return resultStr;
    }

    @Override
    public String loadFromCache(final DiskCacheEntity cacheEntity) throws Throwable {
        if (cacheEntity != null) {
            return cacheEntity.getTextContent();
        }

        return null;
    }

    @Override
    public void save2Cache(UriRequest request) {
        saveStringCache(request, resultStr);
    }
}
