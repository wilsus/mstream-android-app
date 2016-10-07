package io.mstream.mstream.filebrowser;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.mstream.mstream.BaseActivity;
import io.mstream.mstream.R;

// TODO: Replace Volley with OkHTTP
// TODO: Replace the map Object with an Array

public class FileBrowserFragment extends Fragment {
    public LinkedList<FileItem> serverFileList;
    private LinkedList<String> directoryMap = new LinkedList<>();
    private FileBrowserAdapter fileBrowserAdapter;
    private FileStore.OnFilesReturnedListener onFilesReturnedListener;

    public FileBrowserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
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
        onFilesReturnedListener = new FileStore.OnFilesReturnedListener() {
            @Override
            public void onFilesReturned(final List<FileItem> files) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fileBrowserAdapter.clear();
                        fileBrowserAdapter.add(files);
                        fileBrowserAdapter.notifyItemRangeChanged(0, fileBrowserAdapter.getItemCount());
                    }
                });
            }
        };

        // Call the server
        goToDirectory("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_file_browser, container, false);

        // Back Button click
        // TODO: should use device's back button or show a breadcrumb
        Button backButton = (Button) view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        // Add All Button Click
        Button addAllButton = (Button) view.findViewById(R.id.add_all);
        addAllButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                for (int i = 0; i < serverFileList.size(); i++) {
                    FileItem item = serverFileList.get(i);
                    // Don't add directories
                    if (!item.getItemType().equals(FileItem.DIRECTORY)) {
                        addTrackToPlaylist(item);
                    }
                }
            }
        });

        RecyclerView filesListView = (RecyclerView) view.findViewById(R.id.browse_recycler_view);
        filesListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        fileBrowserAdapter = new FileBrowserAdapter(new ArrayList<FileItem>(),
                new FileBrowserAdapter.OnClickFileItem() {
                    @Override
                    public void onDirectoryClick(String directory) {
                        goToDirectory(directory);
                    }

                    @Override
                    public void onFileClick(FileItem item) {
                        addTrackToPlaylist(item);
                    }
                });
        filesListView.setAdapter(fileBrowserAdapter);

        return view;
    }

    private void addTrackToPlaylist(FileItem selectedItem) {
        ((BaseActivity) getActivity()).addTrack(selectedItem);
    }

    private void goToDirectory(final String directory) {
        directoryMap.addLast(directory);
        new FileStore(getContext()).getFiles(directoryMap.getLast(), onFilesReturnedListener);

    }

    private void goBack() {
        if (!directoryMap.getLast().equals("")) {
            directoryMap.removeLast();
            new FileStore(getContext()).getFiles(directoryMap.getLast(), onFilesReturnedListener);
        }
    }
}
