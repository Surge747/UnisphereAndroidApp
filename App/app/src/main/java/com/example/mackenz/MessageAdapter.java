package com.example.mackenz;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> messages;
    private LayoutInflater inflater;
    private Context context;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context; // Initialize context
        this.messages = messages;
        this.inflater = LayoutInflater.from(context);
    }
    public String getCurrentUsername() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("username", "");
    }

    // Method to load messages from a file into the adapter
    public void loadMessagesFromFile(String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        List<Message> loadedMessages = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|\\|");
                if (parts.length == 3) {
                    int id = Integer.parseInt(parts[0]);
                    String content = parts[1];
                    String sender = parts[2];
                    loadedMessages.add(new Message(content, sender.equals(getCurrentUsername())));
                }
            }
            this.messages.clear();
            this.messages.addAll(loadedMessages);
            notifyDataSetChanged();
        } catch (IOException e) {
            Log.e("MessageAdapter", "Error loading messages", e);
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = viewType == 1 ? R.layout.right_message : R.layout.left_message; // Assuming viewType 1 is sent by user
        View view = inflater.inflate(layout, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageTextView.setText(message.getText());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Returning 1 if the message is sent by the user, otherwise 0
        return messages.get(position).isUser() ? 1 : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.chat_textview);
        }
    }
}
