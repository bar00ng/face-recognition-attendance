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
import com.example.absen_pegawai.model.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class UserAdapter extends FirestoreRecyclerAdapter<User, UserAdapter.UserHolder>{
    private OnItemClickListener mListener;
    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull UserHolder holder, int position, @NonNull User model) {
        holder.nikTextView.setText(model.getNik());
        holder.namaTextView.setText(model.getNama());
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item,
                parent, false);
        return new UserHolder(view);
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        TextView nikTextView, namaTextView;

        public UserHolder(@NonNull View itemView) {
            super(itemView);

            nikTextView = itemView.findViewById(R.id.nikText);
            namaTextView = itemView.findViewById(R.id.namaText);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Actions");
            MenuItem delete = contextMenu.add(Menu.NONE, 1, 1, "Delete");
            MenuItem updatePassword = contextMenu.add(Menu.NONE, 2, 2, "Change Password");

            delete.setOnMenuItemClickListener(this);
            updatePassword.setOnMenuItemClickListener(this);
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
                        case 2:
                            mListener.onUpdatePasswordClick(getSnapshots().getSnapshot(position), position);
                            return true;
                    }
                }
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
        void onDeleteClick(DocumentSnapshot documentSnapshot, int position);
        void onUpdatePasswordClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}
