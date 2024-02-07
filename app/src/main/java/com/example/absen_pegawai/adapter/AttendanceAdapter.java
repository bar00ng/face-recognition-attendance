package com.example.absen_pegawai.adapter;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.absen_pegawai.R;
import com.example.absen_pegawai.model.Attendance;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.time.Month;

public class AttendanceAdapter extends FirestoreRecyclerAdapter<Attendance, AttendanceAdapter.AttendanceHolder> {
    private onAttendanceItemClickListener mListener;
    public AttendanceAdapter(@NonNull FirestoreRecyclerOptions<Attendance> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull AttendanceAdapter.AttendanceHolder holder, int position, @NonNull Attendance model) {
        int year = model.getTahun_masuk();
        int month = model.getBulan_masuk();
        int dayOfMonth = model.getTgl_masuk();

        String monthInString = Month.of(month).name();

        String absenId = dayOfMonth + " " + monthInString + " " + year;

        holder.tanggalTextView.setText(absenId);
        holder.jamMasukTextView.setText(model.getJam_masuk());
    }

    @NonNull
    @Override
    public AttendanceAdapter.AttendanceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_item,
                parent, false);
        return new AttendanceAdapter.AttendanceHolder(view);
    }

    public void deleteAttendanceItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class AttendanceHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener{
        TextView tanggalTextView, jamMasukTextView;
        public AttendanceHolder(@NonNull View itemView) {
            super(itemView);
            tanggalTextView = itemView.findViewById(R.id.tanggalAbsen);
            jamMasukTextView = itemView.findViewById(R.id.jam_masuk);

            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Actions");
            MenuItem delete = contextMenu.add(Menu.NONE, 1, 1, "Delete");

            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    switch (menuItem.getItemId()) {
                        case 1:
                            mListener.onDeleteClick(getSnapshots().getSnapshot(position), position);
                            return true;
                    }
                }
            }
            return false;
        }
    }

    public interface onAttendanceItemClickListener {
        void onDeleteClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnAttendanceItemClickListener(onAttendanceItemClickListener listener) {
        mListener = listener;
    }
}
