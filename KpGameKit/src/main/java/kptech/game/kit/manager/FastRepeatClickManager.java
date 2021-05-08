package kptech.game.kit.manager;

/**
 * 预防快速点击管理类
 */
public class FastRepeatClickManager {

    private long lastClickTime = 0;
    //两次触发时间间隔 500ms
    private static final long DIFF = 800;
    //上一次触发 view 的id
    private int lastViewId = -1;

    private FastRepeatClickManager() {
    }

    private static class RepeatClickManagerHolder {
        private static final FastRepeatClickManager INSTANCE = new FastRepeatClickManager();
    }

    public static FastRepeatClickManager getInstance() {
        return RepeatClickManagerHolder.INSTANCE;
    }

    public boolean isFastDoubleClick() {
        return isFastDoubleClick(-1, DIFF);
    }

    public boolean isFastDoubleClick(int viewId) {
        return isFastDoubleClick(viewId, DIFF);
    }

    public boolean isFastDoubleClick(int viewId, long sDiff) {
        long currentTime = System.currentTimeMillis();
        long diffTime = currentTime - lastClickTime;
        boolean isDoubleClick = Math.abs(diffTime) < sDiff;
        if (isDoubleClick && lastViewId == viewId && lastClickTime > 0) {
            return true;
        }
        lastClickTime = currentTime;
        lastViewId = viewId;
        return false;
    }

}
