package io.mstream.mstream;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedList;


public class FileBrowserBaseAdapter extends BaseAdapter {

    private final LinkedList<aListItem> mData;

    public FileBrowserBaseAdapter(LinkedList<aListItem> fileList) {
        this.mData = fileList;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        // TODO implement you own logic with ID
        return 0;
    }

    @Override
    public aListItem getItem(int position) {
        return mData.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
        } else {
            result = convertView;
        }

        aListItem item = getItem(position);


        final String tempName = item.getItemName();

        String displayThis;

        if (item.getItemType().equals("directory")) {
            displayThis = "\uD83D\uDCC1 " + tempName;
        } else {
            displayThis = "\uD83C\uDFB5 " + tempName;
        }

        ((TextView) result.findViewById(android.R.id.text1)).setText(displayThis);

        return result;
    }

}
