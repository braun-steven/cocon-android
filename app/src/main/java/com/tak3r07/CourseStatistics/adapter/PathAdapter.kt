package com.tak3r07.CourseStatistics.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import com.tak3r07.unihelper.R

import java.util.ArrayList

/**
 * Created by tak3r07 on 3/22/15.
 */
class PathAdapter<String>(context: Context, private val backupList: ArrayList<String>) : ArrayAdapter<String>(context, 0, backupList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        // Get the data item for this position
        val backupPath = backupList[position]


        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_backupdialog, parent, false)
        }
        // Lookup view for data population
        val pathTextView = convertView!!.findViewById<View>(R.id.backuppath_textView) as TextView

        // Populate the data into the template view using the data object
        pathTextView.text = backupPath as CharSequence
        // Return the completed view to render on screen
        return convertView

    }
}
