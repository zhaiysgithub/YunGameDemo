package kptech.game.kit.redfinger.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import kptech.game.kit.redfinger.widget.PlayerEventHandler;

/* compiled from: MyGLSurfaceView */
public class g0 extends GLSurfaceView {
    private static volatile boolean f;
    static int g;
    static int h;
    a a;
    private int b;
    private int c;
    ExecutorService d = Executors.newSingleThreadExecutor();
    private PlayerEventHandler e;

    /* access modifiers changed from: package-private */
    /* compiled from: MyGLSurfaceView */
    public class a implements GLSurfaceView.Renderer {
        private int a;
        SurfaceTexture b;
        private String c;
        private String d;
        private int e;
        private int f;
        private int g;
        private Bitmap h;
        ByteArrayOutputStream i;
        ByteBuffer j;
        private u k;
        FloatBuffer l;
        FloatBuffer m;
        private ShortBuffer n;
        private final float[] o = {-1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f};
        private final float[] p = {0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f};
        private final short[] q = {0, 1, 2, 0, 2, 3};
        private Handler r;
        private int s;
        private int t;
        int u = -1;
        ArrayBlockingQueue<ByteBuffer> arrayQueue = new ArrayBlockingQueue<>(1);
        private RunnableC0000a w = new RunnableC0000a();

        /* renamed from: yunapp.gamebox.g0$a$a  reason: collision with other inner class name */
        /* compiled from: MyGLSurfaceView */
        public class RunnableC0000a implements Runnable {
            ByteArrayOutputStream a = new ByteArrayOutputStream();
            BufferedOutputStream b = new BufferedOutputStream(this.a);

            public RunnableC0000a() {
            }

            public void run() {
                int size = a.this.arrayQueue.size();
                if (size > 0) {
                    Log.d("MyRenderer", "HandlerRgbBuffer 1");
                    if (size > 25) {
                        for (int i = 5; i > 0; i--) {
                            a.this.arrayQueue.remove();
                        }
                    }
                    this.a.reset();
                    ByteBuffer remove = a.this.arrayQueue.remove();
                    try {
                        this.b.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Bitmap createBitmap = Bitmap.createBitmap(a.this.s, a.this.t, Bitmap.Config.ARGB_8888);
                    createBitmap.copyPixelsFromBuffer(remove);
                    createBitmap.compress(Bitmap.CompressFormat.JPEG, 25, this.b);
                    createBitmap.recycle();
                    byte[] byteArray = this.a.toByteArray();
                    if (a.this.k != null) {
                        Log.d("MyRenderer", "HandlerRgbBuffer 2");
                        a.this.k.onScreenCapture(byteArray);
                    }
                    Log.d("MyRenderer", "HandlerRgbBuffer 3");
                }
            }
        }

        a() {
        }

        private void e() {
            if (g0.f && this.s > 0 && this.t > 0) {
                Log.d("MyRenderer", "handlerScreenCapture1 start");
                boolean unused = g0.f = false;
                int i2 = this.s;
                int i3 = g0.this.c;
                ByteBuffer allocate = ByteBuffer.allocate(this.s * this.t * 4);
                allocate.order(ByteOrder.BIG_ENDIAN);
                allocate.clear();
                allocate.limit(i2 * i3 * 4);
                allocate.position(0);
                GLES20.glReadPixels(0, 0, i2, i3, 6408, 5121, allocate);
                GLES20.glFlush();
                allocate.rewind();
                if (!this.arrayQueue.offer(allocate)) {
                    this.arrayQueue.remove();
                    this.arrayQueue.offer(allocate);
                }
                ExecutorService executorService = g0.this.d;
                if (executorService != null) {
                    executorService.execute(this.w);
                }
                Log.d("MyRenderer", "handlerScreenCapture1 end");
            }
        }

        public void onDrawFrame(GL10 gl10) {
            a(this.s, this.t);
            GLES20.glClear(16640);
            SurfaceTexture surfaceTexture = this.b;
            if (surfaceTexture != null) {
                try {
                    surfaceTexture.updateTexImage();
                    a();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            e();
        }

        public void onSurfaceChanged(GL10 gl10, int i2, int i3) {
            GLES20.glViewport(0, 0, i2, i3);
            this.u = -1;
            this.s = i2;
            this.t = i3;
            Bitmap bitmap = this.h;
            if (bitmap != null) {
                bitmap.recycle();
                this.h = null;
            }
            ByteBuffer byteBuffer = this.j;
            if (byteBuffer != null) {
                byteBuffer.clear();
                this.h = null;
            }
            v.d("onSurfaceChanged width = " + i2 + ", height = " + i3);
            Handler handler = this.r;
            if (handler != null) {
                handler.sendEmptyMessage(1);
            }
        }

        public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
            GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
            this.s = -1;
            this.t = -1;
        }

        private String b(int i2, int i3) {
            if (g0.h > g0.g) {
                if (j0.b() == 1) {
                    if (j0.c() == 1) {
                        this.c = "attribute vec4 position;\nattribute vec2 inputTextureCoordinate;\nvarying vec2 vTexCoord;\nvoid main(){\n    mat4 RotationMatrix = mat4( 0.0, 1.0, 0.0, 0.0,\n                               -1.0, 0.0, 0.0, 0.0,\n                                0.0, 0.0, 1.0, 0.0,\n                                0.0, 0.0, 0.0, 1.0 );\n    gl_Position = RotationMatrix * position;\n    vTexCoord = inputTextureCoordinate;\n}";
                    } else {
                        this.c = "attribute vec4 position;\nattribute vec2 inputTextureCoordinate;\nvarying vec2 vTexCoord;\nvoid main(){\n    mat4 RotationMatrix = mat4( -1.0,  0.0, 0.0, 0.0,\n                                 0.0, -1.0, 0.0, 0.0,\n                                 0.0,  0.0, 1.0, 0.0,\n                                 0.0,  0.0, 0.0, 1.0 );\n    gl_Position = RotationMatrix * position;\n    vTexCoord = inputTextureCoordinate;\n}";
                    }
                } else if (i2 < i3) {
                    this.c = "attribute vec4 position;\nattribute vec2 inputTextureCoordinate;\nvarying vec2 vTexCoord;\nvoid main(){\n    mat4 RotationMatrix = mat4( 0.0, 1.0, 0.0, 0.0,\n                               -1.0, 0.0, 0.0, 0.0,\n                                0.0, 0.0, 1.0, 0.0,\n                                0.0, 0.0, 0.0, 1.0 );\n    gl_Position = RotationMatrix * position;\n    vTexCoord = inputTextureCoordinate;\n}";
                } else {
                    this.c = "attribute vec4 position;\nattribute vec2 inputTextureCoordinate;\nvarying vec2 vTexCoord;\nvoid main(){\n    mat4 RotationMatrix = mat4( -1.0,  0.0, 0.0, 0.0,\n                                 0.0, -1.0, 0.0, 0.0,\n                                 0.0,  0.0, 1.0, 0.0,\n                                 0.0,  0.0, 0.0, 1.0 );\n    gl_Position = RotationMatrix * position;\n    vTexCoord = inputTextureCoordinate;\n}";
                }
            } else if (j0.b() == 1) {
                if (j0.c() == 1) {
                    Log.d("MyRenderer", "initTexture: IVC_VERT_SHADER_L 1");
                    this.c = "attribute vec4 position;\nattribute vec2 inputTextureCoordinate;\nvarying vec2 vTexCoord;\nvoid main(){\n    mat4 RotationMatrix = mat4( -1.0,  0.0, 0.0, 0.0,\n                                 0.0, -1.0, 0.0, 0.0,\n                                 0.0,  0.0, 1.0, 0.0,\n                                 0.0,  0.0, 0.0, 1.0 );\n    gl_Position = RotationMatrix * position;\n    vTexCoord = inputTextureCoordinate;\n}";
                } else {
                    Log.d("MyRenderer", "initTexture: IVC_VERT_SHADER_P 1");
                    this.c = "attribute vec4 position;\nattribute vec2 inputTextureCoordinate;\nvarying vec2 vTexCoord;\nvoid main(){\n    mat4 RotationMatrix = mat4( 0.0, 1.0, 0.0, 0.0,\n                               -1.0, 0.0, 0.0, 0.0,\n                                0.0, 0.0, 1.0, 0.0,\n                                0.0, 0.0, 0.0, 1.0 );\n    gl_Position = RotationMatrix * position;\n    vTexCoord = inputTextureCoordinate;\n}";
                }
            } else if (i2 < i3) {
                Log.d("MyRenderer", "initTexture: IVC_VERT_SHADER_L0 ");
                this.c = "attribute vec4 position;\nattribute vec2 inputTextureCoordinate;\nvarying vec2 vTexCoord;\nvoid main(){\n    mat4 RotationMatrix = mat4( 1.0,  0.0, 0.0, 0.0,\n                                 0.0, 1.0, 0.0, 0.0,\n                                 0.0,  0.0, 1.0, 0.0,\n                                 0.0,  0.0, 0.0, 1.0 );\n    gl_Position = RotationMatrix * position;\n    vTexCoord = inputTextureCoordinate;\n}";
            } else {
                Log.d("MyRenderer", "initTexture: IVC_VERT_SHADER_P 2");
                this.c = "attribute vec4 position;\nattribute vec2 inputTextureCoordinate;\nvarying vec2 vTexCoord;\nvoid main(){\n    mat4 RotationMatrix = mat4( 0.0, 1.0, 0.0, 0.0,\n                               -1.0, 0.0, 0.0, 0.0,\n                                0.0, 0.0, 1.0, 0.0,\n                                0.0, 0.0, 0.0, 1.0 );\n    gl_Position = RotationMatrix * position;\n    vTexCoord = inputTextureCoordinate;\n}";
            }
            return this.c;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private SurfaceTexture c() {
            this.a = d();
            SurfaceTexture surfaceTexture = new SurfaceTexture(this.a);
            this.b = surfaceTexture;
            return surfaceTexture;
        }

        private int d() {
            int[] iArr = new int[1];
            GLES20.glGenTextures(1, iArr, 0);
            GLES20.glBindTexture(36197, iArr[0]);
            GLES20.glTexParameterf(36197, 10241, 9729.0f);
            GLES20.glTexParameterf(36197, 10240, 9729.0f);
            GLES20.glTexParameteri(36197, 10242, 33071);
            GLES20.glTexParameteri(36197, 10243, 33071);
            return iArr[0];
        }

        public void a(Handler handler) {
            this.r = handler;
        }

        public void a(u uVar) {
            this.k = uVar;
        }

        /* access modifiers changed from: package-private */
        public void a(int i2, int i3) {
            String str;
            if (j0.c() != this.u || (str = this.c) == null || !str.equals(b(i2, i3))) {
                this.u = j0.c();
                this.l = f0.a(this.o);
                this.m = f0.a(this.p);
                this.n = f0.a(this.q);
                b(i2, i3);
                this.d = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTexCoord;\nuniform samplerExternalOES sampler2d;\n\nvoid main() {\n    gl_FragColor  = texture2D(sampler2d, vTexCoord);\n}";
                int a2 = f0.a(this.c, "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTexCoord;\nuniform samplerExternalOES sampler2d;\n\nvoid main() {\n    gl_FragColor  = texture2D(sampler2d, vTexCoord);\n}");
                this.e = a2;
                this.f = GLES20.glGetAttribLocation(a2, "position");
                this.g = GLES20.glGetAttribLocation(this.e, "inputTextureCoordinate");
                g0.this.b = i2;
                g0.this.c = i3;
            }
        }

        /* access modifiers changed from: package-private */
        public void a() {
            GLES20.glUseProgram(this.e);
            GLES20.glEnableVertexAttribArray(this.f);
            GLES20.glVertexAttribPointer(this.f, 2, 5126, false, 8, (Buffer) this.l);
            GLES20.glEnableVertexAttribArray(this.g);
            GLES20.glVertexAttribPointer(this.g, 2, 5126, false, 8, (Buffer) this.m);
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(36197, this.a);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(this.e, "sampler2d"), 0);
            f0.a(0, 0, g0.this.b, g0.this.c, false, 0, 0);
            GLES20.glDrawElements(4, this.q.length, 5123, this.n);
            f0.a("glDrawElements");
            GLES20.glDisableVertexAttribArray(this.f);
            GLES20.glDisableVertexAttribArray(this.g);
        }

        /* access modifiers changed from: package-private */
        public void b() {
            v.a("MyRenderer deleting program " + this.e);
            this.e = -1;
            v.a("MyRenderer releasing SurfaceTexture");
            SurfaceTexture surfaceTexture = this.b;
            if (surfaceTexture != null) {
                surfaceTexture.release();
                this.b = null;
            }
            Bitmap bitmap = this.h;
            if (bitmap != null) {
                bitmap.recycle();
                this.h = null;
            }
            ByteArrayOutputStream byteArrayOutputStream = this.i;
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                this.i = null;
            }
            ByteBuffer byteBuffer = this.j;
            if (byteBuffer != null) {
                byteBuffer.clear();
                this.j = null;
            }
            this.k = null;
        }
    }

    public g0(Context context) {
        super(context);
        a(context);
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.a.c();
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        PlayerEventHandler playerEventHandler = this.e;
        if (playerEventHandler == null) {
            return true;
        }
        playerEventHandler.setOnKeyDown(keyEvent.getScanCode(), keyEvent);
        return true;
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        PlayerEventHandler playerEventHandler = this.e;
        if (playerEventHandler == null) {
            return true;
        }
        playerEventHandler.setOnKeyUp(keyEvent.getScanCode(), keyEvent);
        return true;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        PlayerEventHandler playerEventHandler = this.e;
        if (playerEventHandler == null) {
            return true;
        }
        playerEventHandler.setOnTouchEvent(motionEvent);
        return true;
    }

    public void setHandler(Handler handler) {
        a aVar = this.a;
        if (aVar != null) {
            aVar.a(handler);
        }
    }

//    public void setPlatCallback(u uVar) {
//        a aVar = this.a;
//        if (aVar != null) {
//            aVar.a(uVar);
//        }
//    }

    public void setPlayerEventHandler(PlayerEventHandler playerEventHandler) {
        this.e = playerEventHandler;
    }

    public static void a(int i, int i2) {
        g = i;
        h = i2;
    }

    public static void b(boolean z) {
        f = z;
    }

    private void a(Context context) {
        Activity activity = (Activity) context;
        setEGLContextClientVersion(2);
        a aVar = new a();
        this.a = aVar;
        setRenderer(aVar);
        setRenderMode(0);
    }

    public void a() {
        a aVar = this.a;
        if (aVar != null) {
            aVar.b();
        }
        ExecutorService executorService = this.d;
        if (executorService != null) {
            executorService.shutdownNow();
            this.d = null;
        }
        this.a = null;
    }
}