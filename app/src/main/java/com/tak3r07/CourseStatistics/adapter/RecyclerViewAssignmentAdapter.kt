package com.tak3r07.CourseStatistics.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.tak3r07.CourseStatistics.activities.AssignmentsActivity
import com.tak3r07.CourseStatistics.database.DataHelper
import com.tak3r07.unihelper.R

import java.text.DecimalFormat
import java.util.ArrayList

import objects.Assignment
import objects.Course
import objects.FixedPointsCourse

/**
 * Created by tak3r07 on 12/8/14.
 * Adapter for RecyclerView in MainActivity
 */
class RecyclerViewAssignmentAdapter(private val context: Context,
                                    private var currentCourse: Course?,
                                    private val assignmentsActivity: AssignmentsActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var assignments: ArrayList<Assignment>? = null

    init {
        this.assignments = currentCourse.getAssignments()
        if (assignments == null) {
            throw IllegalArgumentException("assignments ArrayList must not be null")
        }

        hasFixedPoints = currentCourse is FixedPointsCourse

        dataHelper = DataHelper(assignmentsActivity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //Inflate layout
        val itemView: View
        when (viewType) {
            TYPE_ITEM -> {
                itemView = LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.list_item_assignment, parent, false)

                return VHItem(itemView, context, this, assignmentsActivity)
            }

            TYPE_HEADER -> {
                //Get correct header for dynamic/fixed points
                val layout: Int
                if (currentCourse!!.hasFixedPoints()) {
                    layout = R.layout.assignments_header_fixed_points
                } else {
                    layout = R.layout.assignments_header_dynamic_points
                }
                itemView = LayoutInflater
                        .from(parent.context)
                        .inflate(layout, parent, false)

                return VHHeader(itemView, currentCourse)
            }
        }
        throw RuntimeException(
                "there is no type that matches the type "
                        + viewType
                        + " + make sure your using types correctly")
    }

    //Sets up the view
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is VHItem) {
            val itemHolder = holder
            //Store current assignment
            val currentAssignment = assignments!![position - 1] // -1 since first position is header


            //Set text
            itemHolder.mTextViewTitle.text = context.getString(R.string.assignment_number) + currentAssignment.index
            itemHolder.mTextViewPoints.text = currentAssignment.achievedPoints.toString() + " / " + currentAssignment.maxPoints

            //Test if Assignment is Extraassignment:
            if (currentAssignment.isExtraAssignment) {
                itemHolder.mTextViewPercentage.text = "+"
            } else {
                //Else set usual text
                itemHolder.mTextViewPercentage.text = currentAssignment.percentage!!.toString() + " %"

            }
        } else {
            if (holder is VHHeader) {
                val headerHolder = holder

                //SetupHeader
                if (currentCourse!!.hasFixedPoints()) {
                    setupFixedPointsHeaderHolder(headerHolder)
                } else {
                    setupDynamicPointsHeaderHolder(headerHolder)
                }

            }
        }


    }

    override fun getItemCount(): Int {
        return assignments!!.size + 1
    }

    fun addAssignment(assignment: Assignment) {
        assignments!!.add(assignment)
        notifyItemInserted(assignments!!.size)
    }

    fun removeAssignment(position: Int) {
        //Log for possible errors
        Log.d("Tak3r07", "Removing Assignment at pos: " + position
                + ", arraysize: " + assignments!!.size)

        assignments!!.removeAt(position)
        notifyItemRemoved(position)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        if (isPositionHeader(position))
            return TYPE_HEADER

        return TYPE_ITEM
    }

    private fun isPositionHeader(position: Int): Boolean {
        return position == 0
    }

    fun setupFixedPointsHeaderHolder(headerHolder: VHHeader) {

        val fpc = currentCourse as FixedPointsCourse?

        /*
        headerHolder.mTextViewOverall.setText(
                fpc.getProgress().toString()
                        + " % - "
                        + fpc.getTotalPoints()
                        + "/"
                        + fpc.getNumberOfAssignments() * fpc.getMaxPoints()
        );

        //Average
        headerHolder.mTextViewAverage.setText(
                fpc.getAverage(true).toString()
                        + " % - "
                        + fpc.getAveragePointsPerAssignment(true)
                        + "/" + fpc.getMaxPoints()
        );*/

        //Nedded Points per assignment until 50% is reached
        headerHolder.mTextViewNecPoiPerAss.text = fpc!!.necessaryPointsPerAssignmentUntilFin!!.toString()

        //Number of assignments until 50% is reached
        headerHolder.mTextViewAssUntilFin.text = fpc.numberOfAssUntilFin.toString()

        setupPiChartAverage(headerHolder, fpc)
        setupPiChartOverall(headerHolder, fpc)
        setupBarGraph(headerHolder, fpc)


    }

    private fun setupPiChartAverage(headerHolder: VHHeader, fpc: FixedPointsCourse) {
        val pc = headerHolder.pieChartAverage

        val entries = ArrayList<Entry>()
        val avg = fpc.getAverage(true)!!
        entries.add(Entry(avg.toFloat(), 0))
        entries.add(Entry(100 - avg.toFloat(), 1))
        val dataset = PieDataSet(entries, "")
        val colors = ArrayList<Int>()
        colors.add(context.resources.getColor(R.color.light_green_400))
        colors.add(context.resources.getColor(R.color.red_300))
        dataset.colors = colors
        val dataSet = PieData(arrayOf("", ""), dataset)
        pc.setDescription("Average")
        pc.data = dataSet
        pc.invalidate()
    }

    private fun setupPiChartOverall(headerHolder: VHHeader, fpc: FixedPointsCourse) {
        val pc = headerHolder.pieChartOverall

        val entries = ArrayList<Entry>()
        val progress = fpc.progress!!
        entries.add(Entry(progress.toFloat(), 0))
        val nec = fpc.necPercentToPass * 100
        val missing = (nec - progress.toFloat()).toFloat()
        entries.add(Entry(missing, 1))
        val optional = (if (missing > 0) 100 - nec else 100 - progress).toFloat()
        entries.add(Entry(optional, 2))
        val dataset = PieDataSet(entries, "")
        val colors = ArrayList<Int>()
        colors.add(context.resources.getColor(R.color.light_green_400))
        colors.add(context.resources.getColor(R.color.orange_300))
        colors.add(context.resources.getColor(R.color.grey_300))
        dataset.colors = colors
        val dataSet = PieData(arrayOf("Achieved", "Missing", "Optional"), dataset)
        pc.setDescription("Overall")
        pc.data = dataSet
        pc.invalidate()
    }

    fun setupDynamicPointsHeaderHolder(headerHolder: VHHeader) {
        headerHolder.mTextViewAverage!!.text = currentCourse!!.getAverage(true)!!.toString() + " %"
        headerHolder.mTextViewProgress.text = currentCourse!!.progress!!.toString() + " %"

        setupBarGraph(headerHolder, currentCourse)
    }

    /**
     * Setup the bargraph

     * @param headerHolder
     * *
     * @param course
     */
    private fun setupBarGraph(headerHolder: VHHeader, course: Course) {

        //Get chart reference
        val chart = headerHolder.graph

        //Disable legend
        chart.legend.isEnabled = false

        //Disable description
        chart.setDescription("")

        //Disable touch
        chart.setTouchEnabled(false)

        //Set background color Transparent
        chart.setGridBackgroundColor(Color.TRANSPARENT)

        //Set no chart data message
        chart.setNoDataText(context.getString(R.string.no_assignments_added_yet_chart))

        //Create entry and Label arraylist
        val entries = ArrayList<BarEntry>()
        val xVals = ArrayList<String>()
        val colors = ArrayList<Int>()

        //For each assignment create a new entry and its label
        for (a in assignments!!) {
            val e: BarEntry

            //Check if course is FPC: use absolute points
            if (course.hasFixedPoints()) {
                e = BarEntry(a.achievedPoints.toFloat(), a.index - 1)

                //If course is DPC: use relative points
            } else {
                e = BarEntry(a.percentage!!.toFloat(), a.index - 1)
            }

            //Add color for this entry
            if (a.percentage >= course.necPercentToPass * 100f || a.isExtraAssignment) {
                colors.add(context.resources.getColor(R.color.light_green_400))
            } else {
                colors.add(context.resources.getColor(R.color.red_300))
            }
            //Add new entry
            entries.add(e)
            xVals.add(a.index.toString())
        }

        //Create line
        val bds = BarDataSet(entries, "Data")

        //Setup line settings
        bds.axisDependency = YAxis.AxisDependency.LEFT

        //Catch exception which is thrown when no assignments are added yet
        if (!assignments!!.isEmpty()) {
            bds.colors = colors
        }

        //Grab Right YAxis reference and disable it
        val ryAxis = chart.axisRight
        ryAxis.setDrawLabels(false)


        //Grab Left YAxis reference
        val lyAxis = chart.axisLeft
        lyAxis.setStartAtZero(true)
        lyAxis.setDrawGridLines(false)

        val labelsPerYAxis = 5
        val forceLabelCount = true
        lyAxis.setLabelCount(labelsPerYAxis, forceLabelCount)
        if (course.hasFixedPoints()) {
            lyAxis.axisMaxValue = (course as FixedPointsCourse).maxPoints!!.toFloat()
        } else {
            val yAxisMaxValue = 100f
            lyAxis.axisMaxValue = yAxisMaxValue
        }


        //Create limit line at necessary points to pass
        val ll: LimitLine
        val limit: Float
        //If course is FPC show absolute average in chart
        if (course.hasFixedPoints()) {
            limit = (course.necPercentToPass * course.toFPC().maxPoints!!).toFloat()
            //Else show relative average in chart
        } else {
            limit = (course.necPercentToPass * 100f).toFloat()
        }

        val label = DecimalFormat("####0.00").format(limit.toDouble())
        ll = LimitLine(limit, "")

        ll.lineColor = context.resources.getColor(R.color.grey_500)
        ll.lineWidth = 0.75f
        ll.labelPosition = LimitLine.LimitLabelPosition.POS_RIGHT
        ll.textColor = context.resources.getColor(R.color.grey_500)
        ll.textSize = 8f

        //clear old limit lines
        lyAxis.removeAllLimitLines()
        //Add new limit line
        lyAxis.addLimitLine(ll)

        lyAxis.setDrawGridLines(false)
        ryAxis.setDrawGridLines(false)
        ryAxis.setDrawAxisLine(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)


        val datasets = ArrayList<BarDataSet>()
        datasets.add(bds)

        val data = BarData(xVals, datasets)
        chart.data = data
        chart.notifyDataSetChanged()
        chart.invalidate()

        //Animate
        //chart.animateY( 1000, Easing.EasingOption.Linear);

    }

    fun hideGraphIfTooLessAssignments(headerHolder: VHHeader): Boolean {
        if (assignments!!.size < 2) {
            //hide
            headerHolder.graph.visibility = View.INVISIBLE
            return true
        } else {
            headerHolder.graph.visibility = View.VISIBLE
        }
        return false
    }

    //Count extra-assignments
    val countExtraAssignments: Int
        get() {
            var countExtraAssignments = 0
            for (assignment in assignments!!) {
                if (assignment.isExtraAssignment) countExtraAssignments++
            }
            return countExtraAssignments
        }

    fun setCurrentCourse(currentCourse: Course) {
        this.currentCourse = currentCourse
    }

    class VHHeader(view: View, internal var currentCourse: Course) : RecyclerView.ViewHolder(view) {

        //Refer to TextView objects
        internal var mTextViewAverage: TextView? = null
        internal var mTextViewNecPoiPerAss: TextView
        internal var mTextViewAssUntilFin: TextView
        internal var mTextViewOverall: TextView? = null
        internal var mTextViewProgress: TextView
        internal var graph: BarChart
        internal var pieChartAverage: PieChart
        internal var pieChartOverall: PieChart

        init {
            //Refer to TextView objects
            //mTextViewAverage = (TextView) view.findViewById(R.id.course_overview_average);
            mTextViewNecPoiPerAss = view.findViewById<View>(R.id.course_overview_nec_pointspass) as TextView
            mTextViewAssUntilFin = view.findViewById<View>(R.id.course_overview_assignments_until_finished) as TextView
            //mTextViewOverall = (TextView) view.findViewById(R.id.course_overview_overall_percentage_text);
            pieChartAverage = view.findViewById<View>(R.id.chart_pi_average) as PieChart
            pieChartOverall = view.findViewById<View>(R.id.chart_pi_overall) as PieChart
            graph = view.findViewById<View>(R.id.chart) as BarChart
            if (!this.currentCourse.hasFixedPoints()) {
                mTextViewProgress = view.findViewById<View>(R.id.textView_progress) as TextView
            }
        }
    }

    class VHItem//Holds views
    (itemView: View, //Context to refer to app context (for intent, dialog etc)
     internal var context: Context, //Adapter to notifiy data set changed
     internal var mAssignmentsAdapter: RecyclerViewAssignmentAdapter, //Activity to notify Overview for data set change
     internal var assignmentsActivity: AssignmentsActivity) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

        //Initialize views in Viewholder
        internal var mTextViewTitle: TextView
        internal var mTextViewPoints: TextView
        internal var mTextViewPercentage: TextView


        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            mTextViewTitle = itemView.findViewById<View>(R.id.assignment_title) as TextView
            mTextViewPoints = itemView.findViewById<View>(R.id.points_textview) as TextView
            mTextViewPercentage = itemView.findViewById<View>(R.id.percentage_textview) as TextView
        }


        /*
        OnClick: Assignment at the specific position shall be opened
         */
        override fun onClick(v: View) {
            //@TODO: Implement onlcik

        }


        override fun onLongClick(v: View): Boolean {
            //Open Alert dialog to delete item

            val alert = AlertDialog.Builder(v.context)

            //Set title and message
            alert.setTitle(context.getString(R.string.delete))
            alert.setMessage(context.getString(R.string.do_you_want_to_delete) + mTextViewTitle.text.toString() + "?")

            //Set positive button behaviour
            alert.setPositiveButton(context.getString(R.string.delete)) { dialog, whichButton ->
                //Get current assignment
                val currentAssignment = mAssignmentsAdapter.assignments.get(getItemPosition(position))

                //Delete assignment and notify
                Toast.makeText(context, mTextViewTitle.text.toString() + context.getString(R.string.deleted), Toast.LENGTH_LONG).show()

                //Save changes in Database
                val result = dataHelper.deleteAssignment(currentAssignment)

                //Remove Assignment
                mAssignmentsAdapter.removeAssignment(getItemPosition(position))
                mAssignmentsAdapter.notifyDataSetChanged()
            }

            alert.setNeutralButton(context.getString(R.string.cancel)) { dialog, whichButton ->
                // Canceled.
            }

            alert.setNegativeButton(context.getString(R.string.edit_assignment), DialogInterface.OnClickListener { dialog, which ->
                val alert = AlertDialog.Builder(v.context)

                alert.setTitle(context.getString(R.string.dialog_edit_assignment_title))
                alert.setMessage(context.getString(R.string.enter_assignment_points))

                // Set an custom dialog view to get user input
                val view = View.inflate(v.context, R.layout.dialog_add_assignment, null)
                alert.setView(view)

                //Get assignment reference
                val currentAssignment = mAssignmentsAdapter.currentCourse!!.getAssignment(getItemPosition(position))

                val mEditTextMaxPoints = view.findViewById<View>(R.id.editText_maxPoints) as EditText
                val textView = view.findViewById<View>(R.id.textView_maxPoints) as TextView

                //If this course has fixed Points, dont show the possibilities of setting maxPoints
                if (hasFixedPoints) {
                    val viewManager = mEditTextMaxPoints.parent as ViewManager
                    viewManager.removeView(mEditTextMaxPoints)
                    viewManager.removeView(textView)
                }

                //Checkbox reference
                val mCheckBox = view.findViewById<View>(R.id.checkBox_extra_assignment) as CheckBox
                mCheckBox.isChecked = currentAssignment.isExtraAssignment

                //Get EditText views
                val mEditTextAchievedPoints = view.findViewById<View>(R.id.editText_achievedPoints) as EditText

                //Set hints
                mEditTextAchievedPoints.hint = currentAssignment.achievedPoints.toString() + ""
                mEditTextMaxPoints.hint = currentAssignment.maxPoints.toString() + ""


                alert.setPositiveButton(context.getString(R.string.dialog_edit_assignment_save), DialogInterface.OnClickListener { dialog, whichButton ->
                    //Get data from edittext
                    var achievedPointsString = mEditTextAchievedPoints.text.toString().replace(',', '.')
                    var maxPointsString = mEditTextMaxPoints.text.toString().replace(',', '.')

                    //If no new maxPoints is entered: get the old one
                    if (maxPointsString.isEmpty()) {
                        maxPointsString = currentAssignment.maxPoints.toString()
                    }
                    //If no new achievedPoints is entered: get the old one
                    if (achievedPointsString.isEmpty()) {
                        achievedPointsString = currentAssignment.achievedPoints.toString()
                    }

                    //Check if the entered Values are numeric (doubles)
                    if (AssignmentsActivity.isNumeric(achievedPointsString) && AssignmentsActivity.isNumeric(maxPointsString)) {


                        val maxPoints: Double?
                        if (hasFixedPoints) {
                            maxPoints = mAssignmentsAdapter.currentCourse!!.toFPC().maxPoints
                        } else {
                            maxPoints = java.lang.Double.parseDouble(maxPointsString)
                        }

                        //Set extra assignment if checked
                        if (mCheckBox.isChecked) {
                            currentAssignment.isExtraAssignment(true)
                        } else {
                            //Only allow to uncheck if there are enough assignments left
                            // (else one could have 3 of 3 assignments and another extra
                            // assignment, then uncheck it and he will have 4 out of 3
                            // assignments which is not intended
                            if (mAssignmentsAdapter.currentCourse!!.numberAssignmentsLeft() > 0) {
                                currentAssignment.isExtraAssignment(false)
                                currentAssignment.maxPoints = maxPoints!!
                            } else {
                                Toast.makeText(context, R.string.course_has_max_number_of_assignments, Toast.LENGTH_LONG).show()
                                return@OnClickListener
                            }
                        }

                        //Set new achieved points value
                        val achievedPoints = java.lang.Double.parseDouble(achievedPointsString)
                        currentAssignment.achievedPoints = achievedPoints

                        //Update
                        mAssignmentsAdapter.notifyDataSetChanged()

                        //Update Listitem
                        //Set text
                        mTextViewTitle.text = context.getString(R.string.assignment_number) + currentAssignment.index
                        mTextViewPoints.text = currentAssignment.achievedPoints.toString() + " / " + currentAssignment.maxPoints

                        //Test if Assignment is Extraassignment:
                        if (currentAssignment.isExtraAssignment) {
                            mTextViewPercentage.text = "+"
                        } else {
                            //Else set usual text
                            mTextViewPercentage.text = currentAssignment.percentage!!.toString() + " %"

                        }

                        dataHelper.updateAssignment(currentAssignment)
                        Log.d("DATABASE", "Assignment " + currentAssignment.index + " updated")


                    } else {
                        //If data was not numeric
                        Toast.makeText(context, context.getString(R.string.invalid_values), Toast.LENGTH_LONG).show()

                    }
                })

                alert.setNegativeButton(context.getString(R.string.cancel)) { dialog, whichButton ->
                    // Canceled.
                }

                alert.show()
            })

            alert.show()


            return true
        }


    }

    companion object {


        private val TYPE_HEADER = 0
        private val TYPE_ITEM = 1
        private var dataHelper: DataHelper<AssignmentsActivity>
        private var hasFixedPoints: Boolean

        /**
         * Returns correct item position since first item is header

         * @return item positon
         */
        fun getItemPosition(position: Int): Int {
            return position - 1
        }
    }
}
