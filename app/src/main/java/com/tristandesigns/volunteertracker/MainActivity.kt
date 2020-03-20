package com.tristandesigns.volunteertracker

import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.icu.util.Calendar
import android.os.Bundle
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Reference and initialize the content layout + padding
        val linearLayout = findViewById<LinearLayout>(R.id.linearlayout)

        val topPadding = Space(this)
        topPadding.minimumHeight = 20
        linearLayout.addView(topPadding)

        // Entry class: items inside
        class Entry(layout: LinearLayout, dateStr: String, hoursInt:Int) {
            val extHoursInt = hoursInt
            val innerLayout = LinearLayout(this@MainActivity)
            val date = TextView(this@MainActivity)
            val hours = TextView(this@MainActivity)
            val textPadding = LinearLayout(this@MainActivity)

            init{
                Toast.makeText(applicationContext, dateStr,Toast.LENGTH_SHORT).show()

                layout.addView(innerLayout)
                innerLayout.orientation = LinearLayout.HORIZONTAL
                innerLayout.setPadding(30,0,30,0)

                innerLayout.addView(date)
                date.text = dateStr
                date.textSize = 20F
                date.setTypeface(date.typeface, Typeface.BOLD)
                date.setTextColor(Color.parseColor("#000000"))

                textPadding.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0,1F)
                innerLayout.addView(textPadding)
                innerLayout.addView(hours)
                hours.textSize = 20F
                hours.setTextColor(Color.parseColor("#000000"))
                hours.setTypeface(date.typeface, Typeface.BOLD)
                hours.text = "$extHoursInt Hours"
            }
        }


        // Category class: containers for Entry class above
        class Category(description: String, allEnt: MutableMap<String,Entry>) {
            var allEntries = allEnt
            var hourCount: Int = 0
            var text = SpannableString("${hourCount}\nhours")
            val cardView = CardView(this@MainActivity)
            val outerLayout = LinearLayout(this@MainActivity)
            val superOuterLayout = LinearLayout(this@MainActivity)
            val innerLayout = LinearLayout(this@MainActivity)
            val date = TextView(this@MainActivity)
            val desc = TextView(this@MainActivity)
            val hours = TextView(this@MainActivity)
            val textPadding = View(this@MainActivity)
            val padding = Space(this@MainActivity)

            var isExpanded = false
            val height = 200
            val color = "#000000"

            var listOfEntries = setOf<String>()

            var buffer = 5
            val entryLayout = LinearLayout(this@MainActivity)

            init{
                linearLayout.addView(cardView)
                cardView.radius = 50.0F
                cardView.layoutParams = LinearLayout.LayoutParams(1020, height)
                cardView.addView(superOuterLayout)
                innerLayout.orientation = LinearLayout.VERTICAL
                innerLayout.setPadding(30,15,30,15)
                outerLayout.orientation = LinearLayout.HORIZONTAL
                outerLayout.addView(innerLayout)
                outerLayout.setPadding(0,0,30,20)
                superOuterLayout.orientation = LinearLayout.VERTICAL
                superOuterLayout.addView(outerLayout)
                outerLayout.setBackgroundResource(R.drawable.bg_gradient)
                superOuterLayout.setBackgroundResource(R.drawable.bg2_gradient)

                desc.text = description
                desc.textSize = 26F
                desc.setTypeface(date.typeface, Typeface.BOLD)
                desc.setTextColor(Color.parseColor(color))
                innerLayout.addView(desc)

                text.setSpan(RelativeSizeSpan(2f), 0,2, 0)
                hours.text = text
                hours.gravity = Gravity.END
                hours.textSize = 20F
                hours.setLineSpacing(0F,0.85F)
                hours.setTypeface(date.typeface, Typeface.BOLD)
                hours.setTextColor(Color.parseColor(color))

                textPadding.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0,1F)
                outerLayout.addView(textPadding)
                outerLayout.addView(hours)

                padding.minimumHeight = 20
                linearLayout.addView(padding)

                // Animation
                cardView.layoutTransition = LayoutTransition()
                fun animated(startHeight: Int, endHeight: Int) {
                    val anim = ValueAnimator()
                    anim.setIntValues(startHeight, endHeight)
                    anim.duration = 500
                    anim.addUpdateListener {
                        val value: Int = anim.animatedValue.toString().toInt()
                        cardView.layoutParams = LinearLayout.LayoutParams(cardView.layoutParams.width, value)
                    }
                    val animSet = AnimatorSet()
                    animSet.play(anim)
                    animSet.interpolator = AccelerateDecelerateInterpolator()
                    animSet.start()
                }

                // Creating a layout for the entries, under the category
                entryLayout.orientation = LinearLayout.VERTICAL
                superOuterLayout.addView(entryLayout)

                // Click card expands/contracts
                cardView.setOnClickListener {
                    if (isExpanded) {
                        animated(cardView.layoutParams.height,height)
                    } else {
//                        if (!listOfEntries.isEmpty()) animated(cardview.layoutParams.height,cardview.layoutParams.height + (78 * listOfEntries.size))
                        if (listOfEntries.isNotEmpty()) animated(cardView.layoutParams.height,cardView.layoutParams.height + buffer)
                    }
                    if (listOfEntries.isNotEmpty()) isExpanded = !isExpanded
                }
            }

            fun updateCount(){
                for (i in listOfEntries) {
                    hourCount += allEntries.getValue(i).extHoursInt
                    text = SpannableString("${hourCount}\nhours")
                    text.setSpan(RelativeSizeSpan(2f), 0,2, 0)
                    hours.text = text
                }
            }
        }

        val allCats = mutableMapOf<String, Category>()
        val allEnt = mutableMapOf<String, Entry>()

        // Reference buttons
        val addCat = findViewById<FloatingActionButton>(R.id.addCat)
        val addEnt = findViewById<FloatingActionButton>(R.id.addEnt)

        // Button to create Categories
        addCat.setOnClickListener {

            // Set category description
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Create: Category")
            val input = EditText(this@MainActivity)
            builder.setView(input)
            builder.setPositiveButton("Select") { _, _ ->
                val addText = input.text.toString()
                allCats[addText] = Category(addText,allEnt)
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }

        // Button to create Entries
        addEnt.setOnClickListener {

            // Date Picker
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            var date: String
            val datePick = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, theYear, monthOfYear, dayOfMonth ->
                val monthsRef = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
                date = "${monthsRef[monthOfYear]} ${dayOfMonth}, $theYear"

                // Hour Picker
                val picker = NumberPicker(this)
                picker.minValue = 1
                picker.maxValue = 24
                val pickerLayout = LinearLayout(this)
                pickerLayout.orientation = LinearLayout.VERTICAL
                pickerLayout.addView(picker)
                val pickerBuild = AlertDialog.Builder(this)
                pickerBuild.setTitle("Create Entry: Select Hour Count")
                pickerBuild.setView(pickerLayout)
                pickerBuild.setPositiveButton("Ok") { dialog1, _ ->
                    val hours = picker.value

                    // Category Picker
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Create Entry: Select Category")
                    val listOfCats = allCats.keys.toTypedArray()

                    builder.setSingleChoiceItems(listOfCats, -1) { dialog2, which ->
                        // Create Entries
                        allEnt[listOfCats[which]] = Entry(allCats.getValue(listOfCats[which]).entryLayout,date,hours)
                        allCats.getValue(listOfCats[which]).listOfEntries += listOfCats[which]
                        allCats.getValue(listOfCats[which]).buffer += 70
                        allCats.getValue(listOfCats[which]).updateCount()
                        dialog2.dismiss()
                    }
                    builder.setNegativeButton("Cancel") { dialog2, _ ->
                        dialog2.cancel()
                    }
                    builder.show()

                    dialog1.dismiss()
                }
                pickerBuild.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                pickerBuild.show()
            }, year, month, day)
            datePick.show()


        }


    }
}
