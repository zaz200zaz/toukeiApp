package com.example.alarmapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventRecylerAdapter extends RecyclerView.Adapter<EventRecylerAdapter.MyViewHolder> {

    Context context;
    ArrayList<Events> arrayList;
    DBOpenHelper dbOpenHelper;

    public EventRecylerAdapter(Context context, ArrayList<Events> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_events_rowlayout,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Events events = arrayList.get(position);
        holder.Event.setText(events.getEVENT());
        holder.DateTxt.setText(events.getDATE());
        holder.Time.setText(events.getTIME());
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCalenderEvent(events.getEVENT(),events.getDATE(),events.getTIME());
                arrayList.remove(position);
                notifyDataSetChanged();
            }
        });
        if (isAarmed(events.getDATE(),events.getEVENT(),events.getTIME())){
            holder.imageButtonAlarm.setImageResource(R.drawable.ic_baseline_notifications_active_24);
//            notifyDataSetChanged();
        }else {
            holder.imageButtonAlarm.setImageResource(R.drawable.ic_baseline_notifications_off_24);
//            notifyDataSetChanged();
        }
        Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(ConventStringToDate(events.getDATE()));
            final int alarmYear = dateCalendar.get(Calendar.YEAR);
            final int alarmMonth= dateCalendar.get(Calendar.MONTH);
            final int alarmDay= dateCalendar.get(Calendar.DAY_OF_MONTH);

        Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(ConventStringToTome(events.getTIME()));
            final int alarmHour = timeCalendar.get(Calendar.HOUR);
            final int alarmMinute= timeCalendar.get(Calendar.MINUTE);



        holder.imageButtonAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAarmed(events.getDATE(),events.getEVENT(),events.getTIME())){
                    holder.imageButtonAlarm.setImageResource(R.drawable.ic_baseline_notifications_off_24);
                    cancelAlarm(getRequsetCode(events.getDATE(), events.getEVENT(), events.getTIME()));
                    updateEvent(events.getDATE(), events.getEVENT(), events.getTIME(),"off");

                }else {
                    holder.imageButtonAlarm.setImageResource(R.drawable.ic_baseline_notifications_active_24);
                    Calendar alarmCalendar = Calendar.getInstance();
                    alarmCalendar.set(alarmYear,alarmMonth,alarmDay,alarmHour,alarmMinute);
                    setAlarm(alarmCalendar,events.getEVENT(),events.getTIME(),getRequsetCode(events.getDATE(),events.EVENT,events.TIME));
                    updateEvent(events.getDATE(), events.getEVENT(), events.getTIME(),"on");

                }
                notifyDataSetChanged();
            }
        });
    }
    private void setAlarm(Calendar calendar, String event , String time , int RequestCode){

        Intent intent = new Intent(context.getApplicationContext(),AlarmReceiver.class);
        intent.putExtra("event",event);
        intent.putExtra("time",time);
        intent.putExtra("id",RequestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,RequestCode,intent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);

    }

    private void cancelAlarm(int RequestCode){

        Intent intent = new Intent(context.getApplicationContext(),AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,RequestCode,intent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView DateTxt,Event,Time;
        Button btnDelete;
        ImageButton imageButtonAlarm;
//        CheckBox checkBoxAlarm;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            DateTxt =itemView.findViewById(R.id.event_day_id);
            Event =itemView.findViewById(R.id.event_name_id);
            Time =itemView.findViewById(R.id.event_time_id);
            btnDelete =itemView.findViewById(R.id.btn_delete_id);
            imageButtonAlarm =itemView.findViewById(R.id.imageButton_alarm);
//            checkBoxAlarm =itemView.findViewById(R.id.checkBoxAlarmMe);
        }

    }

    private Date ConventStringToTome(String eventDate){
        SimpleDateFormat format = new SimpleDateFormat("KK:mm", Locale.JAPAN);
        Date date = null;
        try {
            date =format.parse(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    private Date ConventStringToDate(String eventDate){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN);
        Date date = null;
        try {
            date =format.parse(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public void deleteCalenderEvent(String event, String date, String time){
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.deleteEvent(event,date,time,database);
        dbOpenHelper.close();

    }

    private boolean isAarmed(String date, String event, String time){
        boolean alarm =false;
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
        Cursor cursor  = dbOpenHelper.ReadIdEvents(date,event,time,database);
        while (cursor.moveToNext()){
            @SuppressLint("Range") String notify= cursor.getString(cursor.getColumnIndex(DBStructure.NOTIFY));
            if (notify.equals("on")){
                alarm = true;
            }else {
                alarm= false;
            }
        }
        cursor.close();
        dbOpenHelper.close();
        return alarm;
    }

    @SuppressLint("Range")
    private int getRequsetCode(String date, String event, String time){

        int code = 0;
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
        Cursor cursor  = dbOpenHelper.ReadIdEvents(date,event,time,database);
        while (cursor.moveToNext()){
            code= cursor.getInt(cursor.getColumnIndex(DBStructure.ID));

        }
        cursor.close();
        dbOpenHelper.close();
        return code;
    }

    private void updateEvent(String event, String date, String time,String notify){
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.upDateEvent(event,date,time,notify,database);
        dbOpenHelper.close();
    }
}
