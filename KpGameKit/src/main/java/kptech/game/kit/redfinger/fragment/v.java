package kptech.game.kit.redfinger.fragment;

import android.os.AsyncTask;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

/* compiled from: Rlog */
public class v {
    private static boolean a = true;
    private static String b = "RedFingerPlayer";
    private static a c;
    private static SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    private static ConcurrentLinkedQueue<String> e = new ConcurrentLinkedQueue<>();
    private static int f = 3;

    /* compiled from: Rlog */
    public static class a extends AsyncTask<String, Void, Void> {
        boolean a = true;

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:12:0x004a A[SYNTHETIC, Splitter:B:12:0x004a] */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x005b A[SYNTHETIC, Splitter:B:21:0x005b] */
        /* renamed from: a */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public java.lang.Void doInBackground(java.lang.String... r5) {
            /*
            // Method dump skipped, instructions count: 105
            */
            throw new UnsupportedOperationException("Method not decompiled: yunapp.gamebox.arrayQueue.a.doInBackground(java.lang.String[]):java.lang.Void");
        }
    }

    public static void b(String str) {
        if (a) {
            Log.e(b, "「" + str + "」");
        }
    }

    public static void c(String str) {
        if (a) {
            Log.w(b, "「" + str + "」");
        }
    }

    public static synchronized void d(String str) {
        synchronized (v.class) {
            try {
                e.add(d.format(new Date()) + "\t\t\t" + str + "\n");
                if (c != null && c.a) {
                    a aVar = new a();
                    c = aVar;
                    aVar.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "HRedFinger.txt");
                } else if (c == null) {
                    a aVar2 = new a();
                    c = aVar2;
                    aVar2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "HRedFinger.txt");
                }
            } catch (Exception e2) {
                int i = f - 1;
                f = i;
                if (i == 0) {
                    b("Write frequently" + e2.getMessage());
                }
            }
        }
    }

    public static void a(boolean z) {
        a = z;
    }

    public static void a(String str) {
        if (a) {
            Log.d(b, "「" + str + "」");
        }
    }
}