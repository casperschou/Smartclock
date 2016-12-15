package io.smartplanapp.smartclock.util;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.smartplanapp.smartclock.R;
import io.smartplanapp.smartclock.storage.Shift;

public class ShiftAdapter extends ArrayAdapter<Shift> {

    public ShiftAdapter(Context context, List<Shift> shifts) {
        super(context, 0, shifts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Shift shift = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_shift, parent, false);
            viewHolder.shiftContainer = (LinearLayout) convertView.findViewById(R.id.shift_container);
            viewHolder.shiftDate = (TextView) convertView.findViewById(R.id.txt_date);
            viewHolder.shiftBegin = (TextView) convertView.findViewById(R.id.txt_begin);
            viewHolder.shiftEnd = (TextView) convertView.findViewById(R.id.txt_end);
            viewHolder.shiftLocation = (TextView) convertView.findViewById(R.id.txt_location);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // "1475819640", "1475848860"

        long begin = Long.valueOf(shift.getBegin());
        long end = Long.valueOf(shift.getEnd());

        String beginDate = millisToBeginDate(begin);
        String beginTime = millisToHoursMinutes(begin);
        String endTime = millisToHoursMinutes(end);

        viewHolder.shiftDate.setText(beginDate);
        viewHolder.shiftBegin.setText(beginTime);
        viewHolder.shiftEnd.setText(endTime);
        viewHolder.shiftLocation.setText(shift.getLocation());

        viewHolder.shiftContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), shift.toString(), Toast.LENGTH_SHORT).show();
            }
        });

//        DateFormat dateFormat = new SimpleDateFormat("dd/MM yyyy");
//        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
//        Date start = new Date(Long.parseLong(event.getStart())*1000);
//        Date end = new Date(Long.parseLong(event.getEnd())*1000);
//        String dateFormatted = dateFormat.format(start);
//        String startFormatted = timeFormat.format(start);
//        String endFormatted = timeFormat.format(end);

        return convertView;
    }

    private String millisToBeginDate(Long millis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d. MMM yyyy");
        return simpleDateFormat.format(new Date(millis));
    }

    private String millisToHoursMinutes(Long millis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(new Date(millis));
    }

    private static class ViewHolder {
        LinearLayout shiftContainer;
        TextView shiftDate;
        TextView shiftBegin;
        TextView shiftEnd;
        TextView shiftLocation;
    }

}
