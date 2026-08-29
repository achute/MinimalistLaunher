import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.telecom.TelecomManager
import android.provider.Telephony

fun test(context: Context) {
    val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
    val dialerPackage = telecomManager?.defaultDialerPackage
    
    val smsPackage = Telephony.Sms.getDefaultSmsPackage(context)
}
