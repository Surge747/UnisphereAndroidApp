package com.example.mackenz;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageView sendButton;
    private MessageAdapter messageAdapter;
    private List<Message> messagesList;
    private FirebaseConnector firebaseConnector;
    private String currentUser;
    private String otherUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_chat);

        firebaseConnector = new FirebaseConnector(this);
        otherUser = getIntent().getStringExtra("username"); // Get other user's username
        currentUser = getCurrentUsername(); // Get current user's username from SharedPreferences

        setupUI(otherUser);
    }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseConnector.loadMessages(currentUser, otherUser, this::updateMessageList);

        sendButton.setOnClickListener(view -> {
            String text = messageInput.getText().toString();
            if (!text.isEmpty()) {
                Message newMessage = new Message(text, true);
                // Get the next message ID before sending and saving the message
                String filename = firebaseConnector.getSortedFileName(currentUser, otherUser);
                File messageFile = firebaseConnector.getMessageFile(filename);
                int nextId = firebaseConnector.getLastMessageId(messageFile) + 1; // Calculate the next ID

                // Send the message and save it locally
                firebaseConnector.saveMessageLocally(filename, String.valueOf(nextId), text, currentUser);
                firebaseConnector.writeMessage(currentUser, otherUser, text);

                messagesList.add(newMessage);
                messageAdapter.notifyItemInserted(messagesList.size() - 1);
                chatRecyclerView.scrollToPosition(messagesList.size() - 1);
                messageInput.setText("");
            }
        });
    }

    private void setupUI(String username) {
        TextView bannerTextView = findViewById(R.id.chatBanner);
        bannerTextView.setText(username);

        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);

        messagesList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messagesList);
        chatRecyclerView.setAdapter(messageAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateMessageList(List<Message> messages) {
        messagesList.clear();
        messagesList.addAll(messages);
        messageAdapter.notifyDataSetChanged();
        if (!messagesList.isEmpty()) {
            chatRecyclerView.scrollToPosition(messagesList.size() - 1);
        }
    }

    private String getCurrentUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("username", "");
    }
}
