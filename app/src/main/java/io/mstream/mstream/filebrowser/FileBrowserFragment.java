package io.mstream.mstream.filebrowser;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.mstream.mstream.R;
import io.mstream.mstream.serverlist.NewDefaultServerEvent;

/**
 * A fragment that displays the File Browser.
 */
public class FileBrowserFragment extends Fragment {
    private static final String ROOT_DIRECTORY = "";
    private LinkedList<String> directoryMap = new LinkedList<>();
    private FileBrowserAdapter fileBrowserAdapter;
    private FileStore.OnFilesReturnedListener onFilesReturnedListener;

    /**
     * Use this factory method to create a new instance of this fragment
     * @return A new instance of fragment FileBrowserFragment.
     */
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
                    }
                });
            }
        };

        // Call the server
        goToDirectory(ROOT_DIRECTORY);
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
                for (FileItem item : fileBrowserAdapter.getItems()) {
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

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void refreshServer(NewDefaultServerEvent e) {
        directoryMap.clear();
        goToDirectory(ROOT_DIRECTORY);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void addTrackToPlaylist(FileItem selectedItem) {
        // TODO: hook up to the mediacontroller
    }

    private void goToDirectory(final String directory) {
        directoryMap.addLast(directory);
        new FileStore(getContext()).getFiles(directoryMap.getLast(), onFilesReturnedListener);
    }

    private void goBack() {
        if (!directoryMap.getLast().equals(ROOT_DIRECTORY)) {
            directoryMap.removeLast();
            new FileStore(getContext()).getFiles(directoryMap.getLast(), onFilesReturnedListener);
        }
    }
}
