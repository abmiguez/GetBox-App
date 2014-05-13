package es.getbox.android.getboxapp.fragments;

import es.getbox.android.getboxapp.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentAbout extends Fragment {
    public static final String ARG_ABOUT_NUMBER = "about_number";
    private View rootView;
    
    public FragmentAbout() {}
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_about, container, false);
        
        int i = getArguments().getInt(ARG_ABOUT_NUMBER);
        getActivity().setTitle("Acerca de");
        return rootView;
    }
}
