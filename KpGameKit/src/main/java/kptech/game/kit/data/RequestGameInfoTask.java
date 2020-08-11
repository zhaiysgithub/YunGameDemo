package kptech.game.kit.data;

import android.content.Context;
import android.os.AsyncTask;

import kptech.game.kit.GameInfo;
import kptech.game.kit.constants.SharedKeys;
import kptech.game.kit.utils.Logger;
import kptech.game.kit.utils.ProferencesUtils;

public class RequestGameInfoTask extends AsyncTask<String,Void,GameInfo> {
    private static final Logger logger = new Logger("RequestGameInfoTask") ;

    private Context mContext;
    private IRequestCallback mCallback;

    public RequestGameInfoTask(Context context){
        this.mContext = context;
    }

    public RequestGameInfoTask setRequestCallback(IRequestCallback callback){
        this.mCallback = callback;
        return this;
    }

    @Override
    protected GameInfo doInBackground(String... args) {
        try {
            String corpId = args[0];
            String pkg = args[1];
            String pass = ProferencesUtils.getString(mContext, SharedKeys.KEY_GAME_APP_PAAS, null);
            GameInfo info = RequestTask.queryGameInfo(corpId, pkg, pass);
            return info;
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(GameInfo game) {
        if(mCallback!=null){
            mCallback.onResult(game, 1);
        }
    }
}
