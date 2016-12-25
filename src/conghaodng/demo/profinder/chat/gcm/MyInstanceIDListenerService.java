package conghaodng.demo.profinder.chat.gcm;

import com.google.android.gms.iid.InstanceIDListenerService;

import android.content.Intent;
import android.util.Log;

public class MyInstanceIDListenerService extends InstanceIDListenerService {
	private static final String TAG = MyInstanceIDListenerService.class.getSimpleName();
	
	@Override
	public void onTokenRefresh() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onTokenRefresh");
		Intent intent = new Intent(this, GcmIntentService.class);
        startService(intent);
	}
}
