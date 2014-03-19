package es.getbox.android.getboxapp.fragments;

import es.getbox.android.getboxapp.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentClose extends Fragment {
    public static final String ARG_OPTION_NUMBER = "option_number";

    public FragmentClose() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_close, container, false);
        
        return rootView;
    }
}
