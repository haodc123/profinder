package conghaodng.demo.profinder.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.utils.SQLiteHandler;

public class FrgCat extends Fragment {
	
	private Button bt_cat_gs, bt_cat_ntg;
	private TextView tvTitle;
	private ProgressDialog pDialog;
	
	private Bundle b;
	private int iLearn;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frg_cat, container, false);
		
		setInitView(v);
		getBundle();
		
		return v;
	}
	
	private void setInitView(View v) {
		// TODO Auto-generated method stub
		setCustomActionBar();
		
		bt_cat_gs = (Button)v.findViewById(R.id.bt_cat_gs);
		bt_cat_ntg = (Button)v.findViewById(R.id.bt_cat_ntg);
        
		bt_cat_gs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onGS();
			}
		});
		bt_cat_ntg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onNTG();
			}
		});
		// Progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
	}
	private void getBundle() {
		// TODO Auto-generated method stub
		b = this.getArguments();
		if (b != null) {
			iLearn = b.getInt("iLearn");
		}
	}
	
	protected void onNTG() {
		// TODO Auto-generated method stub
		FrgField frgFiled = new FrgField();
		
		Bundle b = new Bundle();
		b.putInt("iCat", 1);
		b.putInt("iLearn", iLearn);
		frgFiled.setArguments(b);
		
	    getActivity().getFragmentManager().beginTransaction()
	     .replace(R.id.fragment_container, frgFiled)
	     .addToBackStack(null)
	     .commit();
	}
	protected void onGS() {
		// TODO Auto-generated method stub
		FrgField frgFiled = new FrgField();
		
		Bundle b = new Bundle();
		b.putInt("iCat", 0);
		b.putInt("iLearn", iLearn);
		frgFiled.setArguments(b);
		
	    getActivity().getFragmentManager().beginTransaction()
	     .replace(R.id.fragment_container, frgFiled)
	     .addToBackStack(null)
	     .commit();
	}
		
	private void setCustomActionBar() {
		// TODO Auto-generated method stub
		getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
        getActivity().getActionBar().setCustomView(R.layout.actionbar_simple);
        tvTitle = (TextView)getActivity().findViewById(R.id.tvTitle);
        tvTitle.setText(getResources().getString(R.string.title_chsg));
	}
}
