package es.getbox.android.getboxapp.fragments;

import es.getbox.android.getboxapp.GetBoxActivity;
import es.getbox.android.getboxapp.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FragmentNewAccount extends Fragment {
    public static final String ARG_NEWACCOUNT_NUMBER = "newaccount_number";
    public Button dropbox;
    public Button box;
    
    public FragmentNewAccount() {
    	
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_newaccount, container, false);
        dropbox=(Button) rootView.findViewById(R.id.buttonNewDropbox);
    	dropbox.setOnClickListener(((GetBoxActivity)getActivity()));
    	box=(Button) rootView.findViewById(R.id.buttonNewBox);
    	box.setOnClickListener(((GetBoxActivity)getActivity()));
        int i = getArguments().getInt(ARG_NEWACCOUNT_NUMBER);
        String option = getResources().getStringArray(R.array.options_array)[i];
        getActivity().setTitle(option);
        return rootView;
    }
}
