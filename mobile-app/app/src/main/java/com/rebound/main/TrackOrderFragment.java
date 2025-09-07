package com.rebound.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.rebound.R;
import com.rebound.adapters.TimelineAdapter;
import com.rebound.models.Orders.TimelineStep;
import com.rebound.repository.OrderRepository;
import com.rebound.utils.TimelineDecoration;

import java.util.List;

public class TrackOrderFragment extends Fragment {

    public TrackOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_track_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        OrderRepository repo = new OrderRepository();

        String orderId = "1234"; // truyền vào từ Fragment arguments chẳng hạn

        // Load timeline
        RecyclerView recycler = view.findViewById(R.id.recyclerTimeline);
        List<TimelineStep>steps = repo.getTimelineSteps(orderId);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(new TimelineAdapter(steps));
        recycler.addItemDecoration(new TimelineDecoration(requireContext()));


        // Load info
        String estimatedDate = repo.getEstimatedDate(orderId);
        String text = getString(R.string.estimated_date_label, estimatedDate);
        ((TextView) view.findViewById(R.id.txtEstimatedDate)).setText(text);

    }

}

