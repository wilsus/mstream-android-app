package io.mstream.mstream;

import android.graphics.Color;
import android.widget.BaseAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;


import java.util.LinkedList;


public class PlaylistBaseAdapter extends BaseAdapter{
    private final LinkedList<aListItem> mData;

    public PlaylistBaseAdapter(LinkedList<aListItem> playlist) {
        this.mData = playlist;
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

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        MyViewHolder mViewHolder;
//
//        if (convertView == null) {
//            convertView = inflater.inflate(R.layout.rowlayout, parent, false);
//            mViewHolder = new MyViewHolder(convertView);
//            convertView.setTag(mViewHolder);
//        } else {
//            mViewHolder = (MyViewHolder) convertView.getTag();
//        }
//
//        aListItem currentListData = getItem(position);
//
//        mViewHolder.tvTitle.setText(currentListData.getItemName());
//        mViewHolder.tvDesc.setText(currentListData.getItemName());
//        //mViewHolder.ivIcon.setImageResource(currentListData.getImgResId());
//
//        return convertView;
//    }
//
//    private class MyViewHolder {
//        TextView tvTitle, tvDesc;
//        //ImageView ivIcon;
//
//        public MyViewHolder(View item) {
//            tvTitle = (TextView) item.findViewById(android.R.id.text1);
//            tvDesc = (TextView) item.findViewById(android.R.id.text2);
//            //ivIcon = (ImageView) item.findViewById(R.id.ivIcon);
//        }
//
//    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlistrowlayout, parent, false);
        } else {
            result = convertView;
        }

        aListItem item = getItem(position);

        // TODO replace findViewById by ViewHolder
        // ((TextView) result.findViewById(android.R.id.text1)).setText(item.getItemName());
        ((TextView) result.findViewById(android.R.id.text2)).setText(item.getItemName());


        // Highlight the currently playing
        // TODO: This is so broken
        // FIXME: Fix it please
//        if(item.getCurrentlyPlayingStatus()){
//            result.setBackgroundColor(Color.YELLOW);
//        }


        return result;
    }





}
