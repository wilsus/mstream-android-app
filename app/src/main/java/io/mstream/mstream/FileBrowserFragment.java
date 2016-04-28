package io.mstream.mstream;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FileBrowserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FileBrowserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

// TODO: Replace Volley with OkHTTP
// TODO: Replace the map Object with an Array

public class FileBrowserFragment extends Fragment {


    private OnFragmentInteractionListener mListener;

    public LinkedList<aListItem> serverFileList;
    private LinkedList<String> directoryMap = new LinkedList<>();
    private LinkedList<Parcelable>  scrollPosition = new LinkedList<>();

    // TODO: Pull this in from elsewhere
    public String currentServerAddress =  "http://209.6.75.121:3030/";

    public FileBrowserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FileBrowserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FileBrowserFragment newInstance() {
        FileBrowserFragment fragment = new FileBrowserFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // Call the server
        goToDirectory("");
    }


    // TODO: Make multiple server select
    public String getCurrentServerString(){
        return this.currentServerAddress;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_file_browser, container, false);

        // Back Button click
        Button backButton = (Button)view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                    goBack();
            }
        });


        // Add All Button Click
        Button addAllButton = (Button)view.findViewById(R.id.addAllButton);
        addAllButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                for (int i = 0; i < serverFileList.size(); i++) {
                    aListItem item = serverFileList.get(i);

                    // Don't add directories
                    if (!item.getItemType().equals("directory")) {
                        addTrackToPlaylist(item);
                    }

                }
            }
        });

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//             throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");  // TODO: This was causing errors
        }
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





    public void addTrackToPlaylist(aListItem selectedItem){

        ((BaseActivity)getActivity()).addTrack(selectedItem);

    }




    public void writeToList(String response, Boolean goBack){

        final ListView listView  = (ListView) getView().findViewById(R.id.listViewX);
        listView.setAdapter(null);




        // Parse JSON and build objects from there
        try{
            JSONObject decodedServerObject = new JSONObject(response);
            JSONArray jObj = decodedServerObject.getJSONArray("contents");
            serverFileList = new LinkedList<>();
            String currentPath = decodedServerObject.getString("path");


            for (int i = 0; i < jObj.length(); i++) {
                JSONObject newObj = jObj.getJSONObject(i);

                String serverAddress = getCurrentServerString();


                String type = newObj.getString("type");


                String link ;
                if(type.equals("directory")){
                    // For directories use the relative directory path
                    link = currentPath + newObj.getString("name") +  "/";
                }else{
                    // For music we provide the whole URL
                    // This way the playlist can handle files from multiple servers
                    String urlStr = serverAddress + currentPath + newObj.getString("name");

                    try{
                        // We need to encode the URL to handle files with special characters
                        // Thank You stack overflow
                        URL url = new URL(urlStr);
                        URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                        link = uri.toASCIIString();

                    }catch(MalformedURLException e){
                        link = ""; // TODO: Better exception handling
                    }catch(URISyntaxException e){
                        link = "";
                    }



                }


                String name = newObj.getString("name");

                serverFileList.addLast(new aListItem(name, type, link));
            }

            // Set the Adapter
            FileBrowserBaseAdapter adapter = new FileBrowserBaseAdapter(serverFileList);
            listView.setAdapter(adapter);


            if(!scrollPosition.isEmpty() && goBack.equals(true)) {

                Parcelable thisPos = scrollPosition.removeLast();
                listView.onRestoreInstanceState(thisPos);
            }


            // On Click
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // final Map.Entry<String, aListItem> itemX = (Map.Entry<String, aListItem>) parent.getItemAtPosition(position);

                    final aListItem thisItem = (aListItem)  parent.getItemAtPosition(position);

                    // final aListItem thisItem = itemX.getValue();
                    final String link = thisItem.getItemLink();
                    String type = thisItem.getItemType();




                    // TODO: Create a new Activity
                    //Intent intent = new Intent(this, BaseActivity.class);
                    // intent.putExtra("path", filename);
                    //startActivity(intent);
                    if(type.equals("directory")){

                        final ListView listView  = (ListView) getView().findViewById(R.id.listViewX);
                        Parcelable state = listView.onSaveInstanceState();
                        scrollPosition.addLast(state);

                        goToDirectory(link);
                    }
                    else{

                        addTrackToPlaylist(thisItem);
                    }


                }
            });

        }catch (JSONException e){
            JSONObject decodedServerObject = new JSONObject();
            // TODO: Do Something
        }
    }


    public void goToDirectory(final String directory){
        directoryMap.addLast(directory);

        callServer(directory, false);
    }

    public void goBack(){

        if(!directoryMap.getLast().equals("")){
            directoryMap.removeLast();
            callServer(directoryMap.getLast(), true);

        }

    }

    public void callServer(final String directory, final Boolean goBack){
        // Server URL
         String url = getCurrentServerString() + "dirparser";

        // Send POST request to server
        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            // JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(String  response) {

                writeToList(response, goBack);
            }
        },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }
        ) {
            @Override
            protected Map<String, String> getParams() {  // Hash Map of POST params
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("dir", directory );
                params.put("filetypes", "[\"mp3\",\"flac\",\"wav\",\"ogg\"]" );

                return params;
            }
        };

        // Send request
        Volley.newRequestQueue(getActivity().getApplicationContext()).add(postRequest);
    }


}
