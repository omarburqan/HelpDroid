package com.example.abuil.helpdroid.Adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.abuil.helpdroid.R;

import java.util.List;

public class messageAdapter extends ArrayAdapter<String> {
    List<String> objects;
    List<String> names; // messages sender
    Context context;
    int resource;
    // a constructor for messages adapter
    public messageAdapter(@NonNull Context context, int resource, @NonNull List<String> objects , List<String> names) {
        super(context, resource, objects);
        this.context=context;
        this.objects=objects;
        this.names=names;
        this.resource=resource;
    }


    @NonNull
    @Override
    public View getView(int position,  View convertView,  ViewGroup parent) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View view =inflater.inflate(resource,null);
        TextView name= view.findViewById(R.id.messageName);
        TextView link= view.findViewById(R.id.messageLink);
        // set the message sender and the message content
        name.setText(names.get(position));
        link.setText(objects.get(position));
        return view;
    }
}
