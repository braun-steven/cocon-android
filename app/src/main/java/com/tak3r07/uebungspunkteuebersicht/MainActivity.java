package com.tak3r07.uebungspunkteuebersicht;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.system.Os;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.ListView;

import java.util.ArrayList;


public class MainActivity extends Activity {

    private ListView mListView;

    //********TEST
    private ArrayList<Course> testArrayList = new ArrayList<Course>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ListView setup
        mListView = (ListView) findViewById(R.id.listView);



        //*******TEST
        Course c1 = new Course("First Course");
        c1.addAssignment(new Assignment(1,20,15));
        testArrayList.add(c1);




        final CourseAdapter mCourseAdapter = new CourseAdapter(this, testArrayList);
        mListView.setAdapter(mCourseAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
