package com.spectratech.sp530demo.component;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import java.util.List;
import java.util.Map;

/**
 * An adapter for ListView which able to assign a sub-button in each row data.
 *
 * @author Refresh Team
 *
 */
public class ButtonAdapter1 extends SimpleAdapter {

    private List<? extends Map<String, ?>> data;
    private int buttonId;
    private String tag;

    /**
     * Construct a new ButtonAdapter
     * @param context
     * @param data
     * @param resource
     * @param from
     * @param to
     * @param buttonId
     * @param tag
     */
    public ButtonAdapter1(Context context, List<? extends Map<String, ?>> data,
                         int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.data = data;
        this.buttonId = buttonId;
        this.tag = tag;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        //((View) view.findViewById(buttonId)).setTag(data.get(position).get(tag));
        return view;
    }

}
