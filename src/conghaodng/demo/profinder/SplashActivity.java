package conghaodng.demo.profinder;

import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.utils.SQLiteHandler;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SplashActivity extends Activity {
	
	private RelativeLayout rl_splash_container;
	/** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 4000;
	private Handler h;
	private Runnable r;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		
		rl_splash_container = (RelativeLayout)findViewById(R.id.rl_splash_container);
		rl_splash_container.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent mainIntent = new Intent(SplashActivity.this,LoginActivity.class);
				mainIntent.putExtra("what_is_going_to", "");
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
			}
		});
		setGlobalVars();
		
		setAutoLoadLoginScreen();
	}

	private void setAutoLoadLoginScreen() {
		// TODO Auto-generated method stub
		r = new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashActivity.this,LoginActivity.class);
                mainIntent.putExtra("what_is_going_to", "");
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        };
        /* New Handler to start the LoginActivity 
         * and close this SplashActivity after some seconds.*/
        h = new Handler();
        h.postDelayed(r, SPLASH_DISPLAY_LENGTH);
	}

	private void setGlobalVars() {
		// TODO Auto-generated method stub
		Functions.initFont(this); // initial fonts
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		h.removeCallbacks(r);
		super.onBackPressed();
	}
}