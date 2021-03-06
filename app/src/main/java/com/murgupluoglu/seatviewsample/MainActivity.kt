package com.murgupluoglu.seatviewsample

import android.os.Bundle
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.murgupluoglu.seatview.Seat
import com.murgupluoglu.seatview.SeatViewConfig
import com.murgupluoglu.seatview.SeatViewListener
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class MainActivity : AppCompatActivity() {


    object MY_TYPES {
        val DISABLED_PERSON = 10
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seatView.seatClickListener = object : SeatViewListener {

            override fun seatReleased(releasedSeat: Seat, selectedSeats: HashMap<String, Seat>) {
                Toast.makeText(this@MainActivity, "Released->" + releasedSeat.seatName, Toast.LENGTH_SHORT).show()
            }

            override fun seatSelected(selectedSeat: Seat, selectedSeats: HashMap<String, Seat>) {
                Toast.makeText(this@MainActivity, "Selected->" + selectedSeat.seatName, Toast.LENGTH_SHORT).show()
            }

            override fun canSelectSeat(clickedSeat: Seat, selectedSeats: HashMap<String, Seat>): Boolean {
                return clickedSeat.type != Seat.TYPE.UNSELECTABLE
            }
        }

        /* generate Sample
        val rowCount = 16
        val columnCount = 29
        val rowNames: HashMap<String, String> = HashMap()
        val seatArray = generateSample(rowCount, columnCount, rowNames)

        seatView!!.initSeatView(seatArray, rowCount, columnCount, rowNames)
        */

        //loadFromAssets Sample
        val rowNames: HashMap<String, String> = HashMap()

        val sample = JSONObject(loadJSONFromAsset())
        val rowCount = sample.getJSONObject("screen").getInt("totalRow")
        val columnCount = sample.getJSONObject("screen").getInt("totalColumn")
        val seatArray = Array(rowCount) { Array(columnCount) { Seat() } }
        val rowArray = sample.getJSONObject("screen").getJSONArray("rows")


        seatView.initSeatView(loadSample(seatArray, rowNames, rowArray, rowCount, columnCount), rowCount, columnCount, rowNames)

        button_test.setOnClickListener {

            val seatName = "A-3"
            val bean = seatView.findSeatWithName(seatName)
            if (bean != null) {
                if (bean.isSelected) {
                    seatView.releaseSeat(bean.rowIndex, bean.columnIndex)
                } else {
                    seatView.selectSeat(bean.rowIndex, bean.columnIndex)
                }
            }

        }

        setSwitches()
    }

    private fun setSwitches(){
        switch_seatnamesbar.isChecked = seatView.config.seatNamesBarActive
        switch_seatnamesbar.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            seatView.config.seatNamesBarActive = b
            seatView.invalidate()
        }

        switch_centerLine.isChecked = seatView.config.centerLineActive
        switch_centerLine.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            seatView.config.centerLineActive = b
            seatView.invalidate()
        }

        switch_cinemaScreen.isChecked = seatView.config.cinemaScreenViewActive
        switch_cinemaScreen.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            seatView.config.cinemaScreenViewActive = b
            seatView.invalidate()
        }

        switch_thumbSeatView.isChecked = seatView.config.thumbSeatViewActive
        switch_thumbSeatView.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            seatView.config.thumbSeatViewActive = b
            seatView.invalidate()
        }

        switch_zoomActive.isChecked = seatView.config.zoomActive
        switch_zoomActive.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            seatView.config.zoomActive = b
            seatView.invalidate()
        }

        switch_zoomAfterClick.isChecked = seatView.config.zoomAfterClickActive
        switch_zoomAfterClick.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            seatView.config.zoomAfterClickActive = b
            seatView.invalidate()
        }

        switch_cinameScreenSide.isChecked = (seatView.config.cinemaScreenViewSide == SeatViewConfig.SIDE_TOP)
        switch_cinameScreenSide.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            seatView.config.cinemaScreenViewSide = if(b) SeatViewConfig.SIDE_TOP else SeatViewConfig.SIDE_BOTTOM
            seatView.invalidate()
        }

        switch_seatViewBackgroundColor.isChecked = (seatView.config.seatViewBackgroundColor.equals("#F4F4F4"))
        switch_seatViewBackgroundColor.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            seatView.config.seatViewBackgroundColor = if(b) "#F4F4F4" else "#000000"
            seatView.invalidate()
        }

        switch_cinemaScreenText.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            seatView.config.cinemaScreenViewText = if(b) "Screen" else "Cinema Screen"
            seatView.invalidate()
        }
    }

    private fun loadSample(seatArray: Array<Array<Seat>>, rowNames: HashMap<String, String>, rowArray: JSONArray, rowCount: Int, columnCount: Int): Array<Array<Seat>> {

        val reverseSeats = true

        for (index in 0 until rowArray.length()) {

            val oneRow = rowArray.getJSONObject(index)

            val rowName = oneRow.getString("rowName")
            var rowIndex = oneRow.getInt("rowIndex")
            val seats = oneRow.getJSONArray("seats")

            if (reverseSeats) {
                rowIndex = (rowCount - 1) - rowIndex
            }
            rowNames[rowIndex.toString()] = rowName

            for (columnIndex in 0 until seats.length()) {
                val seatObject = seats.getJSONObject(columnIndex)

                var rowIndexObject = seatObject.getInt("rowIndex")
                val columnIndexObject = seatObject.getInt("columnIndex")
                val seatNameObject = seatObject.getString("name")
                val seatType = seatObject.getString("type")
                val seatIsSelected = seatObject.getBoolean("isSelected")


                if (reverseSeats) {
                    rowIndexObject = (rowCount - 1) - rowIndexObject
                }

                val seat = Seat()
                seat.id = seatNameObject
                seat.seatName = seatNameObject
                seat.rowIndex = rowIndexObject
                seat.columnIndex = columnIndexObject

                seat.rowName = rowName
                //seat.drawableColor = "#4fc3f7"
                //seat.selectedDrawableColor = "#c700ff"
                seat.isSelected = seatIsSelected


                if (seatObject.has("multiple")) { //check multiple seats exist
                    val multipleSeatsArray = seatObject.getJSONArray("multiple")
                    for (multipleSeatsIndex in 0 until multipleSeatsArray.length()) {
                        val oneSeatIdMultiple = multipleSeatsArray.getString(multipleSeatsIndex)

                        if(oneSeatIdMultiple == seat.seatName){
                            if (multipleSeatsIndex == 0) {
                                seat.multipleType = Seat.MULTIPLETYPE.LEFT
                                seat.drawableResourceName = if(seatType == "available") "seat_available_multiple_left" else "seat_notavailable_multiple_left"
                                seat.selectedDrawableResourceName = "seat_selected_multiple_left"
                            } else if (multipleSeatsIndex == (multipleSeatsArray.length() - 1)) {
                                seat.multipleType = Seat.MULTIPLETYPE.RIGHT
                                seat.drawableResourceName = if(seatType == "available") "seat_available_multiple_right" else "seat_notavailable_multiple_right"
                                seat.selectedDrawableResourceName = "seat_selected_multiple_right"
                            } else {
                                seat.multipleType = Seat.MULTIPLETYPE.CENTER
                                seat.drawableResourceName = if(seatType == "available") "seat_available_multiple_center" else "seat_notavailable_multiple_center"
                                seat.selectedDrawableResourceName = "seat_selected_multiple_center"
                            }
                            when(seatType){
                                "available" -> {
                                    seat.type = Seat.TYPE.SELECTABLE
                                }
                                "notavailable" -> {
                                    seat.type = Seat.TYPE.UNSELECTABLE
                                }
                            }
                        }
                        seat.multipleSeats.add(oneSeatIdMultiple)
                    }
                }

                if (seat.multipleType == Seat.MULTIPLETYPE.NOTMULTIPLE) {
                    seat.selectedDrawableResourceName = "seat_selected"
                    when (seatType) {
                        "available" -> {
                            seat.drawableResourceName = "seat_available"
                            seat.type = Seat.TYPE.SELECTABLE
                        }
                        "disabled" -> {
                            seat.drawableResourceName = "seat_disabledperson"
                            seat.type = MY_TYPES.DISABLED_PERSON
                            seat.selectedDrawableResourceName = "ic_android_24dp"
                        }
                        "notavailable" -> {
                            seat.drawableResourceName = "seat_notavailable"
                            seat.type = Seat.TYPE.UNSELECTABLE
                            seat.selectedDrawableResourceName = "ic_android_24dp"
                        }
                    }
                }

                seatArray[rowIndexObject][columnIndexObject] = seat
            }

        }


        return seatArray
    }

    private fun loadJSONFromAsset(): String {
        val fileName = "sample.json"
        val jsonString = assets.open(fileName).bufferedReader().use {
            it.readText()
        }
        return jsonString
    }

    fun generateSample(rowCount: Int, columnCount: Int, rowNames: HashMap<String, String>): Array<Array<Seat>> {
        val rowNamesArray = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "G", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")

        val seatArray = Array(rowCount) { Array(columnCount) { Seat() } }

        seatArray.forEachIndexed { rowIndex, arrayOfSeats ->
            rowNames[rowIndex.toString()] = rowNamesArray[rowIndex]

            arrayOfSeats.forEachIndexed { columnIndex, seat ->

                seat.id = (rowIndex.toString() + "_" + columnIndex.toString())
                seat.rowName = "Row: $rowIndex Column: $columnIndex"
                seat.columnIndex = columnIndex
                seat.rowIndex = rowIndex

                if (rowIndex == 3) {
                    seat.type = Seat.TYPE.UNSELECTABLE
                    seat.drawableColor = "#e57373"
                } else if (rowIndex == 0 && columnIndex == 7) {
                    seat.type = MY_TYPES.DISABLED_PERSON
                    seat.drawableColor = "#fff176"
                    seat.selectedDrawableColor = "#0d47a1"
                } else if (rowIndex == 5) {
                    seat.type = Seat.TYPE.SELECTABLE
                    seat.isSelected = true
                    seat.drawableColor = "#4fc3f7"
                    seat.selectedDrawableColor = "#c700ff"
                } else {
                    seat.type = Seat.TYPE.SELECTABLE
                    seat.drawableColor = "#4fc3f7"
                    seat.selectedDrawableColor = "#c700ff"
                }
            }
        }

        return seatArray
    }
}
