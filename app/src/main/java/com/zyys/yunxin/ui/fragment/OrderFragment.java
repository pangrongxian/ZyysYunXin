package com.zyys.yunxin.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zyys.yunxin.R;


/**
 * 需要两个参数:appointment_id,voipAccount
 */

public class OrderFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.order_fragment, container, false);
        return view;
    }
}
