package conghaodng.demo.profinder.chat.utils;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import conghaodng.demo.profinder.R;
import conghaodng.demo.profinder.chat.MyChat;
import conghaodng.demo.profinder.global.Functions;

public class ChatRoomAdapter extends BaseAdapter {
	 
    private List<ChatRoomData> listChatRomData;
    private static String today;
    private Context context;
 
    public ChatRoomAdapter(Context ct, List<ChatRoomData> data) {
    	this.context = ct;
    	this.listChatRomData = data;
        Calendar calendar = Calendar.getInstance();
        today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }
 
    @Override
    public int getCount() {
        return listChatRomData.size();
    }
 
    @Override
    public Object getItem(int position) {
        return position;
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ChatRoomData data = listChatRomData.get(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
 
        convertView = mInflater.inflate(R.layout.row_chat_rooms_list, null);
 
        TextView name, message, timestamp, count;
        
        name = (TextView) convertView.findViewById(R.id.name);
        message = (TextView) convertView.findViewById(R.id.message);
        timestamp = (TextView) convertView.findViewById(R.id.timestamp);
        count = (TextView) convertView.findViewById(R.id.count);
        
        name.setText(data.getName());
        message.setText(data.getLastMessage());
        timestamp.setText(Functions.getTimeStamp(today, data.getTimestamp(), "yyyy-MM-dd HH:mm:ss"));
        if (data.getUnreadCount() > 0) {
        	count.setText(String.valueOf(data.getUnreadCount()));
        	count.setVisibility(View.VISIBLE);
        } else {
        	count.setVisibility(View.INVISIBLE);
        }
        
        convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(context, MyChat.class);
				i.putExtra("chat_room_id", data.getId());
				i.putExtra("chat_room_name", data.getName());
				context.startActivity(i);
			}
		});
        return convertView;
    }
 
}