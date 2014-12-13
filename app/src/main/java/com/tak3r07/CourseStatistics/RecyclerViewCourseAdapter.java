package com.tak3r07.CourseStatistics;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.tak3r07.unihelper.R;
import java.util.ArrayList;

/**
 * Created by tak3r07 on 12/8/14.
 * Adapter for RecyclerView in MainActivity
 */
public class RecyclerViewCourseAdapter extends RecyclerView.Adapter<RecyclerViewCourseAdapter.ViewHolder> {

    private ArrayList<Course> mCourseArrayList;
    private Context context;

    RecyclerViewCourseAdapter(ArrayList<Course> courses, Context context) {
        this.context = context;
        if (courses == null) {
            throw new IllegalArgumentException("courses ArrayList must not be null");
        }

        mCourseArrayList = courses;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        //Initialize views in Viewholder
        TextView mTextViewFirst;
        TextView mTextViewSecond;
        ImageView mImageView;
        TextView mEndPercentageTextView;


        //Holds views
        public ViewHolder(View itemView) {
            super(itemView);
            mTextViewFirst = (TextView) itemView.findViewById(R.id.course_firstLine);
            mTextViewSecond = (TextView) itemView.findViewById(R.id.course_secondLine);
            mImageView = (ImageView) itemView.findViewById(R.id.icon);
            mEndPercentageTextView = (TextView) itemView.findViewById(R.id.end_percentage_textview);
        }


    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate layout
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_course, parent, false);
        ViewHolder vh = new ViewHolder(itemView);

        return vh;
    }



    //Sets up the view
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Course course = mCourseArrayList.get(position);
        //Set Icon
        Character firstChar = course.getCourseName().toLowerCase().charAt(0);
        holder.mImageView.setImageResource(getLetterIconId(firstChar));
        //Set text
        holder.mTextViewFirst.setText(course.getCourseName());
        holder.mTextViewSecond.setText("\u00d8 " + course.getOverAllPercentage().toString() + " %");
        holder.mEndPercentageTextView.setText(course.getEndPercentage().toString() + " %");

        //Set Percentage Color
        holder.mEndPercentageTextView.setTextColor(getColorForPercentage(course.getEndPercentage()));



    }

    @Override
    public int getItemCount() {
        return mCourseArrayList.size();
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

    //Adds Course
    public void addCourse(Course course) {
        if (course != null) {
            mCourseArrayList.add(course);
            this.notifyDataSetChanged();
        }
    }
}
