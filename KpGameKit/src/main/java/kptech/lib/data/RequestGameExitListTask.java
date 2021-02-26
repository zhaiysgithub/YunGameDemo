package kptech.lib.data;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import kptech.game.kit.GameInfo;
import kptech.game.kit.utils.Logger;

public class RequestGameExitListTask extends AsyncTask<String,Void,List<GameInfo>> {

    private IRequestCallback<List<GameInfo>> mCallback;

    public RequestGameExitListTask(Context context){


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
            Logger.error("RequestGameExitListTask",e.getMessage());
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
