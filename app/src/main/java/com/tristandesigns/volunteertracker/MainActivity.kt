package com.tristandesigns.volunteertracker

import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Set Night Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        var nightMode = true
        fun toggleNightMode() {
            if (nightMode) { nightMode = false
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else { nightMode = true
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
        // Reference and initialize the content layout + padding
        val linearLayout = findViewById<LinearLayout>(R.id.linearlayout)

        val topPadding = Space(this)
        topPadding.minimumHeight = 20
        linearLayout.addView(topPadding)

        // Entry class: items inside
        class Entry(layoutLL: LinearLayout, dateStr: String, hoursInt: Int, catFromStr: String) {
            val layout = layoutLL
            val catFrom = catFromStr
            var extHoursInt = hoursInt
            val innerLayout = LinearLayout(this@MainActivity)
            val date = TextView(this@MainActivity)
            val hours = TextView(this@MainActivity)
            val textPadding = LinearLayout(this@MainActivity)

            init{
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
                val text = "$extHoursInt Hours"
                hours.text = text
            }

            override fun toString(): String{
                return "Entry($layout, ${date.text}, $extHoursInt)"
            }

            fun remove() {
                layout.removeView(innerLayout)
            }
        }
        val allEnt = mutableMapOf<String, Entry>()

        // Category class: containers for Entry class above
        class Category(descriptionStr: String) {
            val description = descriptionStr
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

                // Creating a layout for the entries, under the category
                entryLayout.orientation = LinearLayout.VERTICAL
                superOuterLayout.addView(entryLayout)

                // Click card expands/contracts
                cardView.setOnClickListener {
                    if (isExpanded) {
                        contract()
                    } else {
                        expand()
                    }
                }
            }

            fun remove() {
                linearLayout.removeView(cardView)
                linearLayout.removeView(padding)
            }

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

            fun contract() {
                animated(cardView.layoutParams.height,height)
                isExpanded = false
            }

            fun expand() {
                if (listOfEntries.isNotEmpty()){
                    animated(cardView.layoutParams.height,height + buffer)
                }
                isExpanded = true
            }

            fun updateCount(){
                hourCount = 0
                for (i in listOfEntries) {
                    hourCount += allEnt.getValue(i).extHoursInt
                    text = SpannableString("${hourCount}\nhours")
                    text.setSpan(RelativeSizeSpan(2f), 0,2, 0)
                    hours.text = text
                }
            }
        }
        val allCat = mutableMapOf<String, Category>()

        // Saving Mechanism
        fun save() {
            // Categories
            val catSharedPrefs = getSharedPreferences("Categories", Context.MODE_PRIVATE)
            val catPrefsEditor: SharedPreferences.Editor = catSharedPrefs.edit()
            Toast.makeText(applicationContext,"Saving",Toast.LENGTH_SHORT).show()
            // Categories
            val categories = mutableSetOf<String>()
            for (categoryName in allCat.keys) {
                categories.add(categoryName)
            }
            catPrefsEditor.putStringSet("categoriesSet", categories)
            catPrefsEditor.apply()

            // Entries
            val entSharedPrefs = getSharedPreferences("Entries", Context.MODE_PRIVATE)
            val entPrefsEditor: SharedPreferences.Editor = entSharedPrefs.edit()
            // entryDate is the identifier
            // Entry(layoutLL: LinearLayout, dateStr: String, hoursInt: Int, catFromStr: String)
            for (entryDate in allEnt.keys) {
                val insert = mutableSetOf<String>()
                insert.add("d$entryDate") // dateStr
                insert.add("h${allEnt[entryDate]?.extHoursInt.toString()}") // hoursInt
                insert.add("c${allEnt[entryDate]?.catFrom}")
                entPrefsEditor.putStringSet("${allEnt[entryDate]?.catFrom }: $entryDate", insert)
            }
            entPrefsEditor.apply()
        }

        fun load() {
            // Categories
            val catSharedPrefs = getSharedPreferences("Categories",Context.MODE_PRIVATE)
            var categories = catSharedPrefs.getStringSet("categoriesSet",mutableSetOf<String>())
            if (categories != null) {
                for (category in allCat.keys) {
                    allCat[category]?.remove()
                }; allCat.clear()
                categories = categories.toSortedSet()
                for (categoryName in categories) {
                    allCat[categoryName] = Category(categoryName)
                }
            }

            // Entries
            val entSharedPrefs = getSharedPreferences("Entries",Context.MODE_PRIVATE)
            for (entry in allEnt.keys) {
                allEnt[entry]?.remove()
            }; allEnt.clear()
            val allEntries = entSharedPrefs.all.keys
            for (entryShared in allEntries) {
                val entryAttr = entSharedPrefs.getStringSet(entryShared, mutableSetOf<String>())
                if (entryAttr != null && entryAttr.size != 0) {
                    var date = ""
                    var hours = 0
                    var catFrom = ""
                    for (attr in entryAttr) {
                        if (attr.toString().startsWith("d")) { date = attr.substring(1) }
                        if (attr.toString().startsWith("h")) { hours = attr.substring(1).toInt() }
                        if (attr.toString().startsWith("c")) { catFrom = attr.substring(1) }
                    }
                    allEnt[date] = Entry(allCat.getValue(catFrom).entryLayout,date,hours,allCat.getValue(catFrom).description)
                    allCat.getValue(catFrom).listOfEntries += date
                    allCat.getValue(catFrom).buffer += 70
                    allCat.getValue(catFrom).updateCount()
                }

            }
        }
        load()

        // Reference buttons
        val addCat = findViewById<FloatingActionButton>(R.id.addCat)
        val addEnt = findViewById<FloatingActionButton>(R.id.addEnt)

        // Button to create Categories
        addCat.setOnClickListener {

            // Set category description
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Create: Category")
            val input = EditText(this@MainActivity)
            val textLayout = LinearLayout(this@MainActivity)
            textLayout.setPadding(50,0,50,0)
            textLayout.addView(input)
            builder.setView(textLayout)
            builder.setPositiveButton("Select") { _, _ ->
                val addText = input.text.toString()
                allCat[addText] = Category(addText)
                save()
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }

        // Button to create Entries
        addEnt.setOnClickListener {

            // Date Picker: Calendar
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            var date: String
            val datePick = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, theYear, monthOfYear, dayOfMonth ->
                val monthsRef = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
                date = "${monthsRef[monthOfYear]} ${dayOfMonth}, $theYear"

                // Hour Picker: Slider
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

                    // Category Picker: Selector
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Create Entry: Select Category")
                    val listOfCats = allCat.keys.toTypedArray()

                    builder.setSingleChoiceItems(listOfCats, -1) { dialog2, which ->
                        // Create Entries
                        allEnt[date] = Entry(allCat.getValue(listOfCats[which]).entryLayout,date,hours,allCat.getValue(listOfCats[which]).description)
                        allCat.getValue(listOfCats[which]).listOfEntries += date
                        allCat.getValue(listOfCats[which]).buffer += 70
                        allCat.getValue(listOfCats[which]).updateCount()
                        dialog2.dismiss()
                        save()
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
