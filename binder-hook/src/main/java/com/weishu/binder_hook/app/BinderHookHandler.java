package com.weishu.binder_hook.app;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author weishu
 * @date 16/2/16
 */
public class BinderHookHandler implements InvocationHandler {

    private static final String TAG = "BinderHookHandler";

    // 原始的Service对象 (IInterface)
    Object base;

    public BinderHookHandler(IBinder base, Class<?> stubClass) {
        try {
            //替换存根的传参对象
            Method asInterfaceMethod = stubClass.getDeclaredMethod("asInterface", IBinder.class);
            // IClipboard.Stub.asInterface(base);
            Log.e("TAG", "BinderHookHandler BinderHookHandler base :"+base);
            // Expected recediver of type android.content.IClipboard, but got android.os.BinderProxy
            // this.base = base;
            //android.content.IClipboard$Stub$Proxy@c2cc6b3 主要需要获得存根的代理，这里用了个技巧
            this.base = asInterfaceMethod.invoke(null, base);
            Log.d("TAG", "BinderHookHandler BinderHookHandler this.base :"+this.base);

        } catch (Exception e) {
            throw new RuntimeException("hooked failed!");
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Log.d(TAG,Log.getStackTraceString(new Throwable()));
        Log.e("TAG", "BinderHookHandler invoke---------- method:"+method);
        // 把剪切版的内容替换为 "you are hooked"
        if ("getPrimaryClip".equals(method.getName())) {
            Log.d(TAG, "hook getPrimaryClip");
            return ClipData.newPlainText(null, "you are hooked");
        }

        // 欺骗系统,使之认为剪切版上一直有内容
        if ("hasPrimaryClip".equals(method.getName())) {
            return true;
        }

        return method.invoke(base, args);
    }
}
