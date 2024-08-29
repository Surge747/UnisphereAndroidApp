package com.example.mackenz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<String> users;
    private OnUserClickListener listener;

    public UserAdapter(Context context, List<String> users, OnUserClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.users_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        String username = users.get(position);
        holder.usernameTextView.setText(username);
        holder.itemView.setOnClickListener(v -> listener.onUserClick(username));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateUsers(List<String> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;

        public UserViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.Forum_item_Title);
        }
    }

    public interface OnUserClickListener {
        void onUserClick(String username);
    }
}
