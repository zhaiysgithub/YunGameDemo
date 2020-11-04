package kptech.game.kit.data;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import kptech.game.kit.GameInfo;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.constants.Urls;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;

public class RequestGameExitListTask extends AsyncTask<String,Void,List<GameInfo>> {
    private static final Logger logger = new Logger("RequestGameExitListTask") ;

    private Context mContext;
    private IRequestCallback<List<GameInfo>> mCallback;

    public RequestGameExitListTask(Context context){
        this.mContext = context;
    }

    public RequestGameExitListTask setRequestCallback(IRequestCallback callback){
        this.mCallback = callback;
        return this;
    }

    @Override
    protected List<GameInfo> doInBackground(String... args) {
        try {
            String corpId = args[0];
            String gameId = args[1];
            List<GameInfo> info = RequestTask.queryGameExitList(corpId, gameId);
            return info;
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<GameInfo> games) {
        if(mCallback!=null){
            mCallback.onResult(games, 1);
        }
    }


}
