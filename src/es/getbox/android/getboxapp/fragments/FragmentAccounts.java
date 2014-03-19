package es.getbox.android.getboxapp.fragments;

import es.getbox.android.getboxapp.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FragmentAccounts extends Fragment {
    public static final String ARG_ACCOUNTS_NUMBER = "accounts_number";
    public ListView DBaccounts;
    public ListView Baccounts;
    private View rootView;
    private TextView userName;
    
    public FragmentAccounts() {}
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_accounts, container, false);
        
        userName= (TextView) rootView.findViewById (R.id.textUserName);
        userName.setText(getArguments().getString("userName"));
 
        DBaccounts = (ListView) rootView.findViewById (R.id.DBAccounts);
        DBaccounts.setAdapter(new ArrayAdapter<String>(getActivity(), 
        		 android.R.layout.simple_list_item_1, 
        		 android.R.id.text1, 
        		 getArguments().getStringArrayList("arrayDB")));
        getActivity().registerForContextMenu(DBaccounts);
        
        Baccounts = (ListView) rootView.findViewById (R.id.BAccounts);
        Baccounts.setAdapter(new ArrayAdapter<String>(getActivity(), 
        		 android.R.layout.simple_list_item_1, 
        		 android.R.id.text1, 
        		 getArguments().getStringArrayList("arrayB")));
        getActivity().registerForContextMenu(Baccounts);
        int i = getArguments().getInt(ARG_ACCOUNTS_NUMBER);
        String option = getResources().getStringArray(R.array.options_array)[i];
        getActivity().setTitle(option);
        return rootView;
    }
}
