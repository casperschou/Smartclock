package io.smartplanapp.smartclock.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

        Shift shift = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_shift, parent, false);

            viewHolder.shiftBegin = (TextView) convertView.findViewById(R.id.txt_begin);
            viewHolder.shiftEnd = (TextView) convertView.findViewById(R.id.txt_end);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        long beginLong = Long.valueOf(shift.getBegin());
        long endLong = Long.valueOf(shift.getEnd());
        String beginDate = millisToBeginDate(beginLong);
        String beginTimestamp = millisToBeginTime(beginLong);
        String endTimestamp = millisToEndTime(endLong);
        viewHolder.shiftBegin.setText(beginDate + ": Fra kl. " + beginTimestamp);
        viewHolder.shiftEnd.setText(" til kl. " + endTimestamp);

//        DateFormat dateFormat = new SimpleDateFormat("dd/MM yyyy");
//        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
//        Date start = new Date(Long.parseLong(event.getStart())*1000);
//        Date end = new Date(Long.parseLong(event.getEnd())*1000);
//        String dateFormatted = dateFormat.format(start);
//        String startFormatted = timeFormat.format(start);
//        String endFormatted = timeFormat.format(end);

        return convertView;
    }

    private static class ViewHolder {
        TextView shiftBegin;
        TextView shiftEnd;
    }

    private String millisToBeginDate(Long millis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy");
        return simpleDateFormat.format(new Date(millis));
    }

    private String millisToBeginTime(Long millis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(new Date(millis));
    }

    private String millisToEndTime(Long millis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(new Date(millis));
    }

}
