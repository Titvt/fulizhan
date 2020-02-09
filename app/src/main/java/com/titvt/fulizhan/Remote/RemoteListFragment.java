package com.titvt.fulizhan.Remote;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.titvt.fulizhan.ProgressButton;
import com.titvt.fulizhan.R;

public class RemoteListFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remote_list, container, false);
        RecyclerView rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        ProgressButton pb = view.findViewById(R.id.pb);
        RemoteListAdapter remoteListAdapter = new RemoteListAdapter(getContext(), pb);
        rv.setAdapter(remoteListAdapter);
        view.findViewById(R.id.pb).setOnClickListener(v -> {
            if (pb.getProgress() >= 65025)
                remoteListAdapter.refresh();
        });
        remoteListAdapter.getHosts();
        return view;
    }
}
