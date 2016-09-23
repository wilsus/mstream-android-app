package io.mstream.mstream;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ManageServersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ManageServersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManageServersFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public ManageServersFragment() {
        // Required empty public constructor
    }


    EditText _nameText;
    EditText _urlText;
    EditText _usernameText;
    EditText _passwordText;
    Button _addServerButton;
    CheckBox _makeDefault;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment ManageServersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ManageServersFragment newInstance() {
        ManageServersFragment fragment = new ManageServersFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_servers, container, false);
        _nameText = (EditText) view.findViewById(R.id.input_name);
        _usernameText = (EditText) view.findViewById(R.id.input_username);
        _passwordText = (EditText) view.findViewById(R.id.input_password);
        _urlText = (EditText) view.findViewById(R.id.input_url);
        _addServerButton = (Button) view.findViewById(R.id.button_addServer);
        _makeDefault = (CheckBox) view.findViewById(R.id.make_default);

        _addServerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addServer();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
//        else {
//            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public void addServer() {
        // Validate
        if (!validate()) {
            onSignupFailed();
            return;
        }

        // _addServerButton.setEnabled(false);

        String name = _nameText.getText().toString();
        String url = _urlText.getText().toString();
        String password = _passwordText.getText().toString();
        String username = _usernameText.getText().toString();
        Boolean isDefault = _makeDefault.isChecked();

        // Create new server Item
        ServerItem newServerItem = new ServerItem(name, url, username, password);
        newServerItem.setDefault(isDefault);


        // TODO: Test connection to server.  Return an error if it can't connect


        // Send serverItem to the main activity to be added the list of servers
        // TODO: Check if this function returns an error?
        Boolean status = ((BaseActivity) getActivity()).addItemToServerList(newServerItem);

        if (!status) {
            Toast.makeText(getActivity(), "Server Name Already Exists", Toast.LENGTH_LONG).show();
        }
    }

    public void onSignupFailed() {
        Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_LONG).show();

        // _addServerButton.setEnabled(true);
    }


    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String url = _urlText.getText().toString();


        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        // TODO: Url validation
//        if (url.isEmpty() || !Patterns.WEB_URL.matcher(url).matches()) {
//            _urlText.setError("Enter a valid URL");
//            valid = false;
//        } else {
//            _urlText.setError(null);
//        }


        return valid;
    }


}
