package kr.co.company.imhere;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


public class WeekListAdapter extends BaseAdapter {

    private Context context;
    protected List<String> weeknum;

    public WeekListAdapter(Context context, List<String> weeknum) {
        this.context = context;
        this.weeknum = weeknum;
    }

    @Override
    public int getCount() {
        return weeknum.size();
    }

    @Override
    public Object getItem(int position) {
        return weeknum.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(context, R.layout.week, null);
        TextView week = (TextView)v.findViewById(R.id.Week);
        //TextView distant = (TextView)v.findViewById(R.id.distant);

        week.setText(weeknum.get(position).toString()+"주차");

        v.setTag(weeknum.get(position).toString());
        return v;
    }
}
