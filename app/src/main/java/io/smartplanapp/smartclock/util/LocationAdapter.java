package io.smartplanapp.smartclock.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import java.util.List;

import io.smartplanapp.smartclock.ClockActivity;
import io.smartplanapp.smartclock.R;

import static io.smartplanapp.smartclock.LocationActivity.EXTRA_LOCATION;

public class LocationAdapter extends ArrayAdapter<String> {

    public LocationAdapter(Context context, List<String> strings) {
        super(context, 0, strings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final String location = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_location, parent, false);
            viewHolder.btnLocation = (Button) convertView.findViewById(R.id.btn_location);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.btnLocation.setText(location);
        viewHolder.btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                Intent intent = new Intent(getContext(), ClockActivity.class);
                                intent.putExtra(EXTRA_LOCATION, location);
                                getContext().startActivity(intent);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Vil du stemple ind her?\n" + location)
                        .setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.cancel, dialogClickListener)
                        .show();

            }
        });

        return convertView;
    }

    private static class ViewHolder {
        Button btnLocation;
    }

}
