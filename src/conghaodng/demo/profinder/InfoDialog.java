package conghaodng.demo.profinder;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class InfoDialog extends Activity {
	TextView tvTitle, tvContent;
	Button btCancel, btGo;
    String msg; 
    int isBackgroud;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_info);
        
        Bundle b = getIntent().getExtras();
    	isBackgroud = b.getInt("isBackgroud");
    	msg = b.getString("msg");
    	
        tvTitle = (TextView)findViewById(R.id.tvTitle);
        tvContent = (TextView)findViewById(R.id.tvContent);
        btCancel = (Button)findViewById(R.id.btCancel);
        btGo = (Button)findViewById(R.id.btGo);
        
        btCancel.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
        btGo.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getBaseContext(), LoginActivity.class);
				i.putExtra("what_is_going_to", "");
				startActivity(i);
				finish();
			}
		});
        getInfo();
    }

    private void getInfo() {
		// TODO Auto-generated method stub
    	if (isBackgroud == 0) {
    		btGo.setClickable(false);
    		btGo.setTextColor(getResources().getColor(R.color.label_global_gray));
    	}
    	try {
			JSONObject jObj = new JSONObject(msg);
			tvTitle.setText(jObj.getString("title"));
			tvContent.setText(jObj.getString("content"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
