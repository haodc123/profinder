package conghaodng.demo.profinder.utils;

import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.fragments.FrgLearn;
import conghaodng.demo.profinder.global.Functions;
import conghaodng.demo.profinder.view.MyGridView;

public class ListSbjAdapter extends BaseAdapter {
	private Context context;
    private List<ListSbjData> sbjItems;
 
    public ListSbjAdapter(Context context, List<ListSbjData> items) {
        this.context = context;
        this.sbjItems = items;
    }
 
    @Override
    public int getCount() {
        return sbjItems.size();
    }
 
    @Override
    public Object getItem(int position) {
        return sbjItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @SuppressLint({ "InflateParams", "ViewHolder" }) @Override
    public View getView(int position, View convertView, ViewGroup parent) {
 
        final ListSbjData m = sbjItems.get(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
 
        convertView = mInflater.inflate(R.layout.unit_sbj, null);
        convertView.setLayoutParams(new MyGridView.LayoutParams(MyGridView.AUTO_FIT, Functions.dpToPx(100)));
 
        TextView tv_unit_sbj = (TextView) convertView.findViewById(R.id.tv_unit_sbj);
        ImageView img_unit_sbj = (ImageView) convertView.findViewById(R.id.img_unit_sbj);
        img_unit_sbj.setVisibility(View.GONE);
        
        tv_unit_sbj.setText(m.getSbj_name());
        
        convertView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FrgLearn frgLearn = new FrgLearn();
				
				Bundle b = new Bundle();
				b.putInt("iField", m.getSbj_fID());
				b.putInt("iSbj", m.getSbj_id());
				b.putInt("iCat", m.getSbj_cID());
				frgLearn.setArguments(b);
				
			    ((Activity) context).getFragmentManager().beginTransaction()
			     .replace(R.id.fragment_container, frgLearn)
			     .addToBackStack(null)
			     .commit();
			}
		});
        
        return convertView;
    }
    
}
