package com.example.alarmapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyGrildAdapter extends ArrayAdapter {

    List<Date> dates;
    Calendar calendar;
    List<Events> events;
    LayoutInflater inflater;
    public MyGrildAdapter(@NonNull Context context, List<Date> dates,Calendar calendar ,List<Events> events) {
        super(context, R.layout.single_cell_layout);
        this.dates = dates;
        this.calendar = calendar;
        this.events = events;
        inflater = LayoutInflater.from(context);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Date monthDate = dates.get(position);
        Calendar dateCalender = Calendar.getInstance();
        dateCalender.setTime(monthDate);
        int dayNo = dateCalender.get(Calendar.DAY_OF_MONTH);
        int displayMonth = dateCalender.get(Calendar.MONTH)+1;
        int displayYear = dateCalender.get(Calendar.YEAR);
        int currenMonth = calendar.get(Calendar.MONTH)+1;
        int currenYear = calendar.get(Calendar.YEAR);

        View view = convertView;
        if (view == null){
            view = inflater.inflate(R.layout.single_cell_layout,parent,false);
        }

        if (displayMonth == currenMonth && displayYear == currenYear){
            view.setBackgroundColor(getContext().getResources().getColor(R.color.green));
        }else{
            view.setBackgroundColor(getContext().getResources().getColor(android.R.color.darker_gray));
        }

        TextView day_nunber = view.findViewById(R.id.calenderDayId);
        TextView event_number = view.findViewById(R.id.events_Id);
        day_nunber.setText(String.valueOf(dayNo));
        Calendar eventCalender = Calendar.getInstance();

        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0 ; i < events.size() ; i++){
            eventCalender.setTime(ConventStringToDate(events.get(i).getDATE()));
            if (dayNo == eventCalender.get(Calendar.DAY_OF_MONTH)
                    && displayMonth == eventCalender.get(Calendar.MONTH)+1
                    && displayYear == eventCalender.get(Calendar.YEAR)) {
                arrayList.add(events.get(i).getEVENT());
                event_number.setText(arrayList.size()+"Events");
            }
        }

        return view;
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

    @Override
    public int getCount() {
        return dates.size();
    }

    @Override
    public int getPosition(@Nullable Object item) {
        return dates.indexOf(item);
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return dates.get(position);
    }
}
