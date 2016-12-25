package conghaodng.demo.profinder.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.global.Constants;
import conghaodng.demo.profinder.global.Functions;

public class FrgLearn extends Fragment {
	
	private Button bt_learn_go;
	private CheckBox cb_learn_1, cb_learn_0;
	private TextView tvTitle;
	FrgListener mCallback;
	
	private Bundle b;
	private int iField;
	private int iSbj;
	private int iCat;
	private int iLearn;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frg_learn, container, false);
		
		getBundle();
		setInitView(v);
		
		return v;
	}
	
	private void setInitView(View v) {
		// TODO Auto-generated method stub
		setCustomActionBar();
		
		bt_learn_go = (Button)v.findViewById(R.id.bt_learn_go);
		cb_learn_1 = (CheckBox)v.findViewById(R.id.cb_learn_1);
		cb_learn_0 = (CheckBox)v.findViewById(R.id.cb_learn_0);
        
		bt_learn_go.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onGo();
			}
		});
		
	}
	private void getBundle() {
		// TODO Auto-generated method stub
		b = this.getArguments();
		if (b != null) {
			iField = b.getInt("iField");
			iSbj = b.getInt("iSbj");
			iCat = b.getInt("iCat");
		}
	}
	
	protected void onGo() {
		// TODO Auto-generated method stub
		getCheckbox();
	}
	private void getCheckbox() {
		// TODO Auto-generated method stub
		if (!cb_learn_0.isChecked() && !cb_learn_1.isChecked())
			iLearn = -1;
		else if (cb_learn_0.isChecked() && !cb_learn_1.isChecked())
			iLearn = Constants.LEARN_TEACH;
		else if (!cb_learn_0.isChecked() && cb_learn_1.isChecked())
			iLearn = Constants.LEARN_LEARN;
		else if (cb_learn_0.isChecked() && cb_learn_1.isChecked())
			iLearn = Constants.LEARN_BOTH;
		
		if (iLearn == -1 || iLearn == Constants.LEARN_BOTH)
			Functions.toastString(getActivity().getResources().getString(R.string.toast_not_fill), getActivity());
		else
			goToChoosing();
	}

	private void goToChoosing() {
		// TODO Auto-generated method stub
		FrgChoosing frgChoosing = new FrgChoosing();
		
		Bundle b = new Bundle();
		b.putInt("iLearn", iLearn);
		b.putInt("iCat", iCat);
		b.putInt("iSbj", iSbj);
		b.putInt("iField", iField);
		frgChoosing.setArguments(b);
		
	    getActivity().getFragmentManager().beginTransaction()
	     .replace(R.id.fragment_container, frgChoosing)
	     .addToBackStack(null)
	     .commit();
	}

	private void setCustomActionBar() {
		// TODO Auto-generated method stub
		getActivity().getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
        getActivity().getActionBar().setCustomView(R.layout.actionbar_simple);
        tvTitle = (TextView)getActivity().findViewById(R.id.tvTitle);
        tvTitle.setText(getResources().getString(R.string.title_learn));
	}
	
	public interface FrgListener {
	    public void onFrgEvent(int value);
	}
	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
		        
	    try {
	        mCallback = (FrgListener) activity;
	    } catch (ClassCastException e) {
	        throw new ClassCastException(activity.toString()
	                + " must implement listener");
	    }
	}
}
