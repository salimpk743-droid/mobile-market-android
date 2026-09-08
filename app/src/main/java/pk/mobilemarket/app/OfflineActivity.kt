package pk.mobilemarket.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OfflineActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline)
        findViewById<TextView>(R.id.offline_message).text = getString(R.string.offline_body)
        findViewById<Button>(R.id.retry_button).setOnClickListener {
            if (TwaLauncherActivity.hasNetwork(this)) {
                startActivity(
                    Intent(this, TwaLauncherActivity::class.java).apply {
                        data = intent?.data
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                )
                finish()
            }
        }
        findViewById<Button>(R.id.browser_button).setOnClickListener {
            startActivity(
                Intent(this, FallbackActivity::class.java).apply { data = intent?.data }
            )
        }
    }
}
