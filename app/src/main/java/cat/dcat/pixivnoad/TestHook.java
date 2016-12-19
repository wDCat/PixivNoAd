package cat.dcat.pixivnoad;

import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by DCat on 2016/12/18.
 */
public class TestHook implements IXposedHookLoadPackage {
    private final static String TAG = TestHook.class.getSimpleName();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.contains("jp.pxv.android")) return;
        Log.d(TAG, "hook:" + lpparam.packageName);
        XposedBridge.hookAllMethods(XposedHelpers.findClass("android.accounts.AccountManager", lpparam.classLoader), "getUserData", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args.length >= 2 && param.args[1].toString().equalsIgnoreCase("isPremium")) {
                    Log.d(TAG, "called.");
                    param.setResult("true");
                }
            }
        });
        Log.d(TAG, "hooked:" + lpparam.packageName);
    }
}
