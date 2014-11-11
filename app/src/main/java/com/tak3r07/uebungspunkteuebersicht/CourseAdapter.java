package com.tak3r07.uebungspunkteuebersicht;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

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
        if (courses.size() <=0) return 1;
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

    public static class ViewHolder{
        public TextView first;
        public TextView second;
        public ImageView image;
    }

    public View getView(int position, View convertView, ViewGroup parent) {


        //Get all Views and inflate item-layout
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView mTextViewFirst = (TextView) rowView.findViewById(R.id.firstLine);
        TextView mTextViewSecond = (TextView) rowView.findViewById(R.id.secondLine);
        ImageView mImageView = (ImageView) rowView.findViewById(R.id.icon);

        //Set text
        mTextViewFirst.setText(courses.get(position).getCourseName());
        mTextViewSecond.setText(courses.get(position).getOverAllPercentage().toString());

       return rowView;
    }


    @Override
    public void onClick(View v) {

    }
}
