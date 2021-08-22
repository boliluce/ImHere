package kr.co.company.imhere;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
/*--------------------------경란------------------------------*/
/**
 * Created by boli on 2017-10-17.
 */

public class CourseListAdapter extends BaseAdapter {

    private Context context;
    protected List<Course> courseList;

    public CourseListAdapter(Context context, List<Course> courseList) {
        this.context = context;
        this.courseList = courseList;
    }

    @Override
    public int getCount() {
        return courseList.size();
    }

    @Override
    public Object getItem(int position) {
        return courseList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(context, R.layout.course, null);
        TextView courseTitle = (TextView)v.findViewById(R.id.course_title);
        TextView courseDay = (TextView)v.findViewById(R.id.course_day);
        TextView courseStart = (TextView)v.findViewById(R.id.course_start);
        TextView courseEnd = (TextView)v.findViewById(R.id.course_end);

        courseTitle.setText(courseList.get(position).getCourseTitle());
        courseDay.setText(courseList.get(position).getCourseDay());
        courseStart.setText(courseList.get(position).getCourseStart());
        courseEnd.setText(courseList.get(position).getCourseEnd());

        v.setTag(courseList.get(position).getCourseTitle());
        return v;
    }
}
