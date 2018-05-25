package cat.dcat.pixivnoadv2

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.telephony.TelephonyManager
import android.util.Base64
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import org.jetbrains.anko.*
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Future
import java.io.*
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import kotlin.experimental.xor

/**
 * Created by DCat on 2018/2/3.
 */
val tagPrefix = "PixivNoAd"

fun Any.getTAG() = tagPrefix + "-${this.javaClass.simpleName}"

fun String.v(msg: Any = "") = {}

fun String.v(msg: Any = "", e: Throwable) = Log.v(this, "" + msg, e)
fun String.d(msg: Any = "") = Log.d(this, "" + msg)

fun String.d(msg: Any = "", e: Throwable) = Log.d(this, "" + msg, e)

fun String.w(msg: Any = "") = Log.w(this, "" + msg)
fun String.w(msg: Any = "", e: Throwable) = Log.w(this, "" + msg, e)

fun String.e(msg: Any = "") = Log.e(this, "" + msg)
fun String.e(msg: Any = "", e: Throwable) = Log.e(this, "" + msg, e)

fun <T> T.catSync(
        name: String = "[unknownTask]",
        f: () -> Unit
) {
    try {
        f()
    } catch (e: Throwable) {
        "catSync".e("task ${name} failed", e)
    }
}

fun <T> T.trulyCatSync(
        name: String = "[unknownTask]",
        f: () -> Unit
) {
    val latch = CountDownLatch(1)
    doAsync {
        try {
            f()
        } catch (e: Throwable) {
            "catSync".e("task ${name} failed", e)
        }
        latch.countDown()
        onComplete {

        }
    }
    while (!latch.await(5, TimeUnit.SECONDS)) {
        "catSync".w("waiting for ${name}...")
    }

}

class XC_MethodHookK : XC_MethodHook {
    constructor() {

    }

    constructor(f: XC_MethodHookK.() -> Unit) {
        f()
    }

    var beforeFun: XC_MethodHookK.(param: XC_MethodHook.MethodHookParam) -> Unit = {}
    var afterFun: XC_MethodHookK.(param: XC_MethodHook.MethodHookParam) -> Unit = {}

    override fun beforeHookedMethod(param: MethodHookParam?) {
        if (param == null) return
        beforeFun(param)
    }

    override fun afterHookedMethod(param: MethodHookParam?) {
        if (param == null) return
        afterFun(param)
    }


}

fun XC_MethodHookK.before(f: XC_MethodHookK.(param: XC_MethodHook.MethodHookParam) -> Unit): XC_MethodHookK {
    beforeFun = f
    return this
}

fun XC_MethodHookK.after(f: XC_MethodHookK.(param: XC_MethodHook.MethodHookParam) -> Unit): XC_MethodHookK {
    afterFun = f
    return this
}

fun Context.getPreferencesAndKeepItReadable(prefName: String): SharedPreferences {
    val prefs = getSharedPreferences(prefName, Context.MODE_PRIVATE)
    val prefsFile = File("${ctx.getFilesDir()}/../shared_prefs/${prefName}.xml")
    prefsFile.setReadable(true, false)
    val prefDir=prefsFile.parentFile
    prefDir.setReadable(true,false)
    return prefs
}