package app.eduroam.geteduroam.wifi.configurator.notification

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Its the class secondary responsable of init the app flow when the app is opened through a notification
 */
class NotificationActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        this.init(savedInstanceState, new ArrayList<Class<? extends Plugin>>() {{
//            // Additional plugins you've installed go here
//            // Ex: add(TotallyAwesomePlugin.class);
//            add(WifiEapConfigurator.class);
//            add(OAuth2ClientPlugin.class);
//        }});
    }

    companion object {
        var NOTIFICATION_ID = "1523"
    }
}