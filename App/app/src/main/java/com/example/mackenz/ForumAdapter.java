package com.example.mackenz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.ForumViewHolder> {
    private Context context;
    private List<ForumPost> forumPosts;
    private OnItemClickListener onItemClickListener;

    public ForumAdapter(Context context, List<ForumPost> forumPosts) {
        this.context = context;
        this.forumPosts = forumPosts;
    }

    @Override
    public ForumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.forum_item, parent, false);
        return new ForumViewHolder(view);
    }

    // Feature: LoadShowData
    // Purpose: Binds the forum post data to the view holder to display in the RecyclerView.
    @Override
    public void onBindViewHolder(ForumViewHolder holder, int position) {
        ForumPost post = forumPosts.get(position);
        holder.titleTextView.setText(post.getTitle());
        holder.bylineTextView.setText("By: " + post.getBy());
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(post); // Handle item click event
            }
        });
    }

    @Override
    public int getItemCount() {
        return forumPosts.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(ForumPost post);
    }

    public static class ForumViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView bylineTextView;

        public ForumViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.Forum_item_Title);
            bylineTextView = itemView.findViewById(R.id.Byline);
        }
    }
}
