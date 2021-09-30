package com.kptech.kputils.http.loader;

import com.kptech.kputils.cache.DiskCacheEntity;
import com.kptech.kputils.http.request.UriRequest;

/**
 * Author: wyouflf
 * Time: 2014/10/17
 */
/*package*/ class IntegerLoader extends Loader<Integer> {
    @Override
    public Loader<Integer> newInstance() {
        return new IntegerLoader();
    }

    @Override
    public Integer load(UriRequest request) throws Throwable {
        request.sendRequest();
        return request.getResponseCode();
    }

    @Override
    public Integer loadFromCache(final DiskCacheEntity cacheEntity) throws Throwable {
        return null;
    }

    @Override
    public void save2Cache(UriRequest request) {

    }
}
