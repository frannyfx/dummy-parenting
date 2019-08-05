package com.example.dummyparenting;

import android.view.View;

public interface ListItemListener {
    public void onClick(View view, int position);
    public void onLongClick(View view, int position);
}