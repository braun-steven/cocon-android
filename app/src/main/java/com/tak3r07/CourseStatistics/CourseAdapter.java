package com.tak3r07.CourseStatistics;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tak3r07.unihelper.R;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by tak3r07 on 11/10/14.
 */
public class CourseAdapter extends BaseAdapter implements View.OnClickListener {
    private final Context context;
    private final ArrayList<Course> courses;

    private LayoutInflater inflater;
    private ViewHolder holder;


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

    static class ViewHolder {
        TextView mTextViewFirst;
        TextView mTextViewSecond;
        ImageView mImageView;
        TextView mEndPercentageTextView;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        if (getCount() <= 0) return null;

        //If converView is null -> inflate layout and store in viewholder object
        if(convertView==null){

            // inflate the layout
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_course, parent, false);

            holder = new ViewHolder();

            //Get all Views and inflate item-layout
            holder.mTextViewFirst = (TextView) convertView.findViewById(R.id.course_firstLine);
            holder.mTextViewSecond = (TextView) convertView.findViewById(R.id.course_secondLine);
            holder.mImageView = (ImageView) convertView.findViewById(R.id.icon);
            holder.mEndPercentageTextView = (TextView) convertView.findViewById(R.id.end_percentage_textview);
            convertView.setTag(holder);
        }else {
            //if convertview was not null -> use viewholder from tag
            holder = (ViewHolder) convertView.getTag();
        }




        //Set Icon
        Character firstChar = courses.get(position).getCourseName().toLowerCase().charAt(0);
        holder.mImageView.setImageResource(getLetterIconId(firstChar));
        //Set text
        holder.mTextViewFirst.setText(courses.get(position).getCourseName());
        holder.mTextViewSecond.setText("\u00d8 " + courses.get(position).getOverAllPercentage().toString() + " %");
        holder.mEndPercentageTextView.setText(courses.get(position).getEndPercentage().toString() + " %");

        //Set Percentage Color
        holder.mEndPercentageTextView.setTextColor(getColorForPercentage(courses.get(position).getEndPercentage()));



        return convertView;
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
            default:
                return 0;
        }
    }

    public void addCourse(Course course) {
        if (course != null) {
            courses.add(course);
            this.notifyDataSetChanged();
        }
    }

    /**
     * This method receives a percentage and retrieves a color
     */
    public int getColorForPercentage(Double percentage) {

        if (percentage < 10) return context.getResources().getColor(R.color.red_500);
        if (percentage < 20) return context.getResources().getColor(R.color.deep_orange_500);
        if (percentage < 30) return context.getResources().getColor(R.color.orange_500);
        if (percentage < 40) return context.getResources().getColor(R.color.yellow_500);
        if (percentage < 50) return context.getResources().getColor(R.color.light_green_500);
        if (percentage > 50) return context.getResources().getColor(R.color.green_500);
        return Color.BLACK;
    }
}
