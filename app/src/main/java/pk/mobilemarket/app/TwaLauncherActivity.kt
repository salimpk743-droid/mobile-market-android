package pk.mobilemarket.app

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.androidbrowserhelper.trusted.LauncherActivity

/**
 * Trusted Web Activity entry. Never loads the site in a WebView.
 * Deep links on our host stay in-app; Chrome Custom Tabs provides back/history.
 */
class TwaLauncherActivity : LauncherActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        if (!hasNetwork(this)) {
            startActivity(
                Intent(this, OfflineActivity::class.java).apply {
                    data = intent?.data
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                }
            )
            finish()
            return
        }
        super.onCreate(savedInstanceState)
    }

    override fun getLaunchingUrl(): Uri {
        val incoming = intent?.data
        if (incoming != null && HOST.equals(incoming.host, ignoreCase = true)) {
            return incoming
        }
        return Uri.parse(getString(R.string.host_url))
    }

    companion object {
        const val HOST = "market-place-six-chi.vercel.app"

        fun hasNetwork(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }
}
