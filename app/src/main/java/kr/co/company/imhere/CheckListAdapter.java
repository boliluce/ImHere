package kr.co.company.imhere;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/******************현영**************************/

//강의자쪽

public class CheckListAdapter extends BaseAdapter {

    private Context context;
    protected List<AttendanceCheck> AttendcheckList;


    public CheckListAdapter(Context context, List<AttendanceCheck> attendcheckList) {
        this.context = context;
        this.AttendcheckList = attendcheckList;
    }

    @Override
    public int getCount() {
        return AttendcheckList.size();
    }

    @Override
    public Object getItem(int position) {
        return AttendcheckList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(context, R.layout.student_check, null);
        TextView name = (TextView)v.findViewById(R.id.name);
        TextView check = (TextView)v.findViewById(R.id.check);

        name.setText(AttendcheckList.get(position).getName());
        String a="";
        if(AttendcheckList.get(position).getOX().equals("1")) {


            check.setText(a+"출석");
            check.setTextColor(Color.GREEN);
        }
        else {
            check.setTextColor(Color.WHITE);
            check.setText(a + "미출석");
        }

        //check.setText(AttendcheckList.get(position).getOX());
        v.setTag(AttendcheckList.get(position).getName());
        return v;
    }
}
