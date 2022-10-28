package com.example.alarmapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CustomCalender extends LinearLayout {

    ImageButton nextBtn,previousBtn;
    TextView currentDate;
    GridView gridView;
    private static final int MAX = 42;
    Calendar calendar = Calendar.getInstance(Locale.JAPAN);
    Context context;

    List<Date> dates = new ArrayList<>();
    List<Events> eventsList = new ArrayList<>();
    int alarmYear,alarmMonth,alarmDay,alarmHour,alrmMinuit;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy  MMMM",Locale.JAPAN);
    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM",Locale.JAPAN);
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy",Locale.JAPAN);
    SimpleDateFormat eventDateFormate = new SimpleDateFormat("yyyy-MM-dd",Locale.JAPAN);


    AlertDialog alertDialog;
    DBOpenHelper dbOpem;
    MyGrildAdapter myGrildAdapter;
    public CustomCalender(Context context) {
        super(context);
    }

    public CustomCalender(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        IntializeLayout();

        SetUpCalender();

        previousBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH,-1);
                SetUpCalender();
            }
        });
        nextBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH,1);
                SetUpCalender();
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                String date = eventDateFormate.format(dates.get(position));

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                View showView  = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_events_layout,null);
                RecyclerView recyclerView = showView.findViewById(R.id.Events_RV_id);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(showView.getContext());
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setHasFixedSize(true);
                EventRecylerAdapter eventRecylerAdapter = new EventRecylerAdapter(showView.getContext(),
                        CollectEventByDate(date));
                recyclerView.setAdapter(eventRecylerAdapter);
                eventRecylerAdapter.notifyDataSetChanged();

                builder.setView(showView);
                alertDialog = builder.create();
                alertDialog.show();
                alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        SetUpCalender();
                    }
                });
                return true;
            }
        });


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                View addView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_newevent_layout,null);
                EditText eventName = addView.findViewById(R.id.textView);
                TextView eventTime = addView.findViewById(R.id.seteven_time_id);
                ImageButton setTime =addView.findViewById(R.id.time_icon_id);

                CheckBox checkBoxAlarmMe = addView.findViewById(R.id.checkBoxAlarmMe);
                Calendar dateCalendar = Calendar.getInstance();
                dateCalendar.setTime(dates.get(position));
                alarmYear = dateCalendar.get(Calendar.YEAR);
                alarmMonth = dateCalendar.get(Calendar.MONTH);
                alarmDay = dateCalendar.get(Calendar.DAY_OF_MONTH);


                Button addEvent =addView.findViewById(R.id.add_events_id);

                setTime.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Calendar calendar = Calendar.getInstance();
                        int hours = calendar.get(Calendar.HOUR_OF_DAY);
                        int minuts = calendar.get(Calendar.MINUTE);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(addView.getContext(), androidx.appcompat.R.style.Base_ThemeOverlay_AppCompat_Dialog_Alert,new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Calendar c = Calendar.getInstance();
                                c.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                c.set(Calendar.MINUTE,minute);
                                c.setTimeZone(TimeZone.getDefault());
                                SimpleDateFormat h = new SimpleDateFormat("HH:mm", Locale.JAPAN);
//                                String dasd = null;
//                                dasd = c.getTime()+"";
                                String event_time = h.format(c.getTime());
                                eventTime.setText(event_time);
                                alarmHour = c.get(Calendar.HOUR_OF_DAY);
                                alrmMinuit = c.get(Calendar.MINUTE);
                            }
                        },hours,minuts,true);

                        timePickerDialog.show();
                    }
                });

                final String date = eventDateFormate.format(dates.get(position));
                final String month = monthFormat.format(dates.get(position));
                final String year = yearFormat.format(dates.get(position));

                addEvent.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!eventTime.getText().toString().trim().equals("00時00分")){
                            if (checkBoxAlarmMe.isChecked()){
                                SaveEvent(eventName.getText().toString().trim(),eventTime.getText().toString(),date,month,year,"on");
                                SetUpCalender();
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(alarmYear,alarmMonth,alarmDay,alarmHour,alrmMinuit);
                                setAlarm(calendar,eventName.getText().toString(),eventTime.getText().toString(),getRequsetCode(date,eventName.getText().toString(),eventTime.getText().toString()));
                                alertDialog.dismiss();
                            }else {

                                SaveEvent(eventName.getText().toString().trim(),eventTime.getText().toString(),date,month,year,"off");
                                SetUpCalender();
                                alertDialog.dismiss();
                            }
                        }else{
                            Toast.makeText(context, "時間を選んでください", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                builder.setView(addView);
                alertDialog = builder.create();
                alertDialog.show();
            }
        });



    }

    @SuppressLint("Range")
    private int getRequsetCode(String date, String event, String time){

        int code = 0;
        dbOpem = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpem.getReadableDatabase();
        Cursor cursor  = dbOpem.ReadIdEvents(date,event,time,database);
        while (cursor.moveToNext()){
            code= cursor.getInt(cursor.getColumnIndex(DBStructure.ID));

        }
        cursor.close();
        dbOpem.close();
        return code;
    }

    private void setAlarm(Calendar calendar,String event ,String time ,int RequestCode){

        Intent intent = new Intent(context.getApplicationContext(),AlarmReceiver.class);
        intent.putExtra("event",event);
        intent.putExtra("time",time);
        intent.putExtra("id",RequestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,RequestCode,intent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

    }

    private ArrayList<Events> CollectEventByDate (String date ) {
        ArrayList<Events> events = new ArrayList<>();
        dbOpem = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpem.getReadableDatabase();
        Cursor cursor  = dbOpem.ReadEvents(date,database);
        while (cursor.moveToNext()){
            @SuppressLint("Range") String event = cursor.getString(cursor.getColumnIndex(DBStructure.EVENT));
            @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex(DBStructure.TIME));
            @SuppressLint("Range") String Date = cursor.getString(cursor.getColumnIndex(DBStructure.DATE));
            @SuppressLint("Range") String month = cursor.getString(cursor.getColumnIndex(DBStructure.MONTH));
            @SuppressLint("Range") String Year = cursor.getString(cursor.getColumnIndex(DBStructure.YEAR));
            Events events1 = new Events(event,time,Date,month,Year);
            events.add(events1);
        }
        cursor.close();
        dbOpem.close();
        return events;
    }

    public CustomCalender(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private  void SaveEvent (String event,String time, String date, String month, String year,String notify){

        dbOpem = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpem.getWritableDatabase();
        dbOpem.SaveEvent(event,time,date,month,year,notify,database);
        dbOpem.close();
        Toast.makeText(context, "Event save", Toast.LENGTH_SHORT).show();

    }

    private void IntializeLayout(){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.calender_layout,this);
        nextBtn = view.findViewById(R.id.nextBtn);
        previousBtn = view.findViewById(R.id.previousBtn);
        currentDate = view.findViewById(R.id.current_Date_Id);
        gridView = view.findViewById(R.id.gridViewId);
    }

    private void SetUpCalender(){
        String CurrentDate = yearFormat.format(calendar.getTime())+"年" + monthFormat.format(calendar.getTime());
        currentDate.setText(CurrentDate);
        dates.clear();
        Calendar MonthCalender = (Calendar) calendar.clone();
        MonthCalender.set(Calendar.DAY_OF_MONTH,1);
        int FirstDayOfMonth = MonthCalender.get(Calendar.DAY_OF_WEEK)-1;
        MonthCalender.add(Calendar.DAY_OF_MONTH,-FirstDayOfMonth);
        CollectEvent(monthFormat.format(calendar.getTime()),yearFormat.format(calendar.getTime()));

        while (dates.size()<MAX){
             dates.add(MonthCalender.getTime());
             MonthCalender.add(Calendar.DAY_OF_MONTH,1);

        }

        myGrildAdapter = new MyGrildAdapter(context,dates,calendar,eventsList);
        gridView.setAdapter(myGrildAdapter);
    }

    private void CollectEvent(String Month , String year){

        eventsList.clear();
        dbOpem = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpem.getReadableDatabase();
        Cursor cursor = dbOpem.ReadEventssperMonth(Month,year,database);
        while (cursor.moveToNext()){

            @SuppressLint("Range") String event = cursor.getString(cursor.getColumnIndex(DBStructure.EVENT));
            @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex(DBStructure.TIME));
            @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(DBStructure.DATE));
            @SuppressLint("Range") String month = cursor.getString(cursor.getColumnIndex(DBStructure.MONTH));
            @SuppressLint("Range") String Year = cursor.getString(cursor.getColumnIndex(DBStructure.YEAR));

            Events events = new Events(event , time , date , month , Year);
            eventsList.add(events);

        }
        cursor.close();
        dbOpem.close();
    }

}
