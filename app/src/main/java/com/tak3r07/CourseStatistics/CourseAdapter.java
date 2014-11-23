package com.tak3r07.CourseStatistics;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tak3r07.unihelper.R;

import java.util.ArrayList;

/**
 * Created by tak3r07 on 11/10/14.
 */
public class CourseAdapter extends BaseAdapter implements View.OnClickListener {
    private final Context context;
    private final ArrayList<Course> courses;

    private LayoutInflater inflater;


    public CourseAdapter(Context context, ArrayList<Course> courses) {

        this.context = context;
        this.courses = courses;
        //Get layoutinflater
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (courses.size() <= 0) return 0;
        return courses.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        if (getCount()<=0) return null;

        //Get all Views and inflate item-layout
        View rowView = inflater.inflate(R.layout.list_item_course, parent, false);
        TextView mTextViewFirst = (TextView) rowView.findViewById(R.id.course_firstLine);
        TextView mTextViewSecond = (TextView) rowView.findViewById(R.id.course_secondLine);
        ImageView mImageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView mEndPercentageTextView = (TextView) rowView.findViewById(R.id.end_percentage_textview);


        //Set Icon
        Character firstChar = courses.get(position).getCourseName().toLowerCase().charAt(0);
        mImageView.setImageResource(getLetterIconId(firstChar));
        //Set text
        mTextViewFirst.setText(courses.get(position).getCourseName());
        mTextViewSecond.setText("\u00d8 " + courses.get(position).getOverAllPercentage().toString() + " %");
        mEndPercentageTextView.setText(courses.get(position).getEndPercentage().toString() + " %");

        return rowView;
    }


    @Override
    public void onClick(View v) {

    }

    public int getLetterIconId(Character character) {
        switch (character) {
            case 'a':
                return R.drawable.letters_a;
            case 'b':
                return R.drawable.letters_b;
            case 'c':
                return R.drawable.letters_c;
            case 'd':
                return R.drawable.letters_d;
            case 'e':
                return R.drawable.letters_e;
            case 'f':
                return R.drawable.letters_f;
            case 'g':
                return R.drawable.letters_g;
            case 'h':
                return R.drawable.letters_h;
            case 'i':
                return R.drawable.letters_i;
            case 'j':
                return R.drawable.letters_j;
            case 'k':
                return R.drawable.letters_k;
            case 'l':
                return R.drawable.letters_l;
            case 'm':
                return R.drawable.letters_m;
            case 'n':
                return R.drawable.letters_n;
            case 'o':
                return R.drawable.letters_o;
            case 'p':
                return R.drawable.letters_p;
            case 'q':
                return R.drawable.letters_q;
            case 'r':
                return R.drawable.letters_r;
            case 's':
                return R.drawable.letters_s;
            case 't':
                return R.drawable.letters_t;
            case 'u':
                return R.drawable.letters_u;
            case 'v':
                return R.drawable.letters_v;
            case 'w':
                return R.drawable.letters_w;
            case 'x':
                return R.drawable.letters_x;
            case 'y':
                return R.drawable.letters_y;
            case 'z':
                return R.drawable.letters_z;
            default: return 0;
        }
    }

    public void addCourse(Course course){
        if (course!=null){
            courses.add(course);
            this.notifyDataSetChanged();
        }
    }
}
