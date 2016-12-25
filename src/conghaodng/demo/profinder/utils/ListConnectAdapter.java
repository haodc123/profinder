package conghaodng.demo.profinder.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;

public class ListConnectAdapter extends BaseAdapter {
	private Context context;
    private List<ListConnectData> connItems;
 
    public ListConnectAdapter(Context context, List<ListConnectData> items) {
        this.context = context;
        this.connItems = items;
    }
 
    @Override
    public int getCount() {
        return connItems.size();
    }
 
    @Override
    public Object getItem(int position) {
        return connItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @SuppressLint({ "InflateParams", "ViewHolder" }) @Override
    public View getView(int position, View convertView, ViewGroup parent) {
 
        /**
         * The following list not implemented reusable list items as list items
         * are showing incorrect data Add the solution if you have one
         * */
    	
        final ListConnectData m = connItems.get(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
 
        convertView = mInflater.inflate(R.layout.row_connect, null);
 
        TextView row_title = (TextView) convertView.findViewById(R.id.row_title);
        TextView row_field = (TextView) convertView.findViewById(R.id.row_field);
        TextView row_content = (TextView) convertView.findViewById(R.id.row_content);
        TextView row_date = (TextView) convertView.findViewById(R.id.row_date);
        
        row_title.setText(m.getUsername1() + " đã kết nối với " + m.getUsername2());
        row_field.setText("Lĩnh vực " + m.getField() + " > " + m.getSubject());
        row_content.setText(m.getContent());
        
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(Constants.DATETIME_FORMAT);
        String today = df.format(c.getTime());
        row_date.setText(Functions.getFriendlyJoinDate(today, m.getDate(), Constants.DATETIME_FORMAT));
        
        convertView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
        
        return convertView;
    }

}
