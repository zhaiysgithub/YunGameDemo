package com.kptech.kputils.http.loader;

import android.text.TextUtils;

import org.json.JSONArray;
import com.kptech.kputils.cache.DiskCacheEntity;
import com.kptech.kputils.common.util.IOUtil;
import com.kptech.kputils.http.RequestParams;
import com.kptech.kputils.http.request.UriRequest;

/**
 * Author: wyouflf
 * Time: 2014/06/16
 */
/*package*/ class JSONArrayLoader extends Loader<JSONArray> {

    private String charset = "UTF-8";
    private String resultStr = null;

    @Override
    public Loader<JSONArray> newInstance() {
        return new JSONArrayLoader();
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
    public JSONArray load(final UriRequest request) throws Throwable {
        request.sendRequest();
        resultStr = IOUtil.readStr(request.getInputStream(), charset);
        return new JSONArray(resultStr);
    }

    @Override
    public JSONArray loadFromCache(final DiskCacheEntity cacheEntity) throws Throwable {
        if (cacheEntity != null) {
            String text = cacheEntity.getTextContent();
            if (!TextUtils.isEmpty(text)) {
                return new JSONArray(text);
            }
        }

        return null;
    }

    @Override
    public void save2Cache(UriRequest request) {
        saveStringCache(request, resultStr);
    }
}
