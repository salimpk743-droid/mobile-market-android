package pk.mobilemarket.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Used only if Chrome Custom Tabs cannot start, or the user retries from Offline.
 * Opens the live site in the default browser — never a WebView — so AdSense policy stays valid.
 */
class FallbackActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent?.data ?: Uri.parse(getString(R.string.host_url))
        val view = Intent(Intent.ACTION_VIEW, url).addCategory(Intent.CATEGORY_BROWSABLE)
        try {
            startActivity(view)
            finish()
        } catch (_: ActivityNotFoundException) {
            setContentView(R.layout.activity_offline)
            findViewById<TextView>(R.id.offline_title).text = getString(R.string.no_browser_title)
            findViewById<TextView>(R.id.offline_message).text = getString(R.string.no_browser_body)
            findViewById<Button>(R.id.retry_button).setOnClickListener {
                try {
                    startActivity(view)
                    finish()
                } catch (_: ActivityNotFoundException) {
                }
            }
            findViewById<Button>(R.id.browser_button).setOnClickListener { finish() }
        }
    }
}
