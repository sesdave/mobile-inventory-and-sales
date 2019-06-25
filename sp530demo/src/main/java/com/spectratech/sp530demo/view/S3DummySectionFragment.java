package com.spectratech.sp530demo.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.spectratech.sp530demo.R;

/**
 * A dummy fragment representing a section of the app, but that simply displays dummy text.
 */
public class S3DummySectionFragment extends S3CommonFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_dummy, container, false);
        Bundle args = getArguments();
        return rootView;
    }
}
