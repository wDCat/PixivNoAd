package cat.dcat.pixivnoadv2

import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Created by DCat on 2018/5/25.
 */
class MainHook : IXposedHookLoadPackage {
    val TAG = getTAG()
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam == null || !"jp.pxv.android".equals(lpparam.packageName)) return
        TAG.d("handling pkg:${lpparam.packageName}")
        XposedBridge.hookAllMethods(XposedHelpers.findClass("android.accounts.AccountManager", lpparam.classLoader), "getUserData", XC_MethodHookK {
            after {
                if (it.args.size >= 2 && it.args[1].toString().equals("isPremium", ignoreCase = true)) {
                    Log.d(TAG, "getUserData() called.")
                    it.result = "true"
                }
            }
        })
        val accountManagerClass = XposedHelpers.findClass("jp.pxv.android.account.b", lpparam.classLoader)
        if (accountManagerClass != null) {
            accountManagerClass.methods.forEach {
                if (it.name.length == 1) {
                    XposedBridge.hookMethod(it, XC_MethodHookK {
                        fun setPremium(param: XC_MethodHook.MethodHookParam) {
                            if (param.thisObject == null) return
                            Log.d(TAG, param.method.toString() + "() called<<!")
                            val isPremium = param.thisObject.javaClass.getField("f")
                            isPremium.set(param.thisObject, true)
                        }
                        before {
                            setPremium(it)
                        }
                        after {
                            setPremium(it)
                        }
                    })
                }
            }
        }

        TAG.d("hook done.")

    }
}