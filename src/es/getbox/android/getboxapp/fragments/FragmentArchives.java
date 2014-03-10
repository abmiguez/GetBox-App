package es.getbox.android.getboxapp.fragments;

import es.getbox.android.getboxapp.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentArchives extends Fragment {
    public static final String ARG_ARCHIVE_NUMBER = "option_number";

    public FragmentArchives() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_archives, container, false);
        int i = getArguments().getInt(ARG_ARCHIVE_NUMBER);
        String option = getResources().getStringArray(R.array.options_array)[i];
        getActivity().setTitle(option);
        return rootView;
    }
}
