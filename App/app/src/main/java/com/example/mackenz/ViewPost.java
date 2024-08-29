package com.example.mackenz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewPost extends AppCompatActivity {

    private TextView textViewTitle;
    private TextView textViewContent;
    private TextView textViewPostBy;
    private LinearLayout linearLayoutReplies;
    private EditText editTextAddReply;
    private Button buttonSubmitReply;
    private Button buttonDeletePost;

    private FirebaseConnector firebaseConnector;
    private String courseId;
    private String postId;
    private ForumPost forumPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpost);

        firebaseConnector = new FirebaseConnector(this);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewContent = findViewById(R.id.textViewContent);
        textViewPostBy = findViewById(R.id.textViewPostBy);
        linearLayoutReplies = findViewById(R.id.linearLayoutReplies);
        editTextAddReply = findViewById(R.id.editTextAddReply);
        buttonSubmitReply = findViewById(R.id.buttonSubmitReply);
        buttonDeletePost = findViewById(R.id.Delete_post_button);

        courseId = getIntent().getStringExtra("courseId");
        postId = getIntent().getStringExtra("postId");

        loadPostDetails();

        buttonSubmitReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReply();
            }
        });

        /**
         * Feature: Privacy-Visibility
         * Description: Sets the click listener for the delete button and checks if the current user is an admin.
         */
        buttonDeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePost();
            }
        });

        // Check if the user is an admin and enable the buttons if they are
        checkIfAdmin();
    }

    /**
     * Feature: LoadShowData
     * Description: Loads the details of a post including title, content, and replies from the Firebase database.
     */
    private void loadPostDetails() {
        firebaseConnector.fetchPostDetailsWithRealTimeUpdates(courseId, postId, new PostDetailsFetchListener() {
            @Override
            public void onPostDetailsFetched(ForumPost post) {
                forumPost = post;
                textViewTitle.setText(post.getTitle());
                textViewContent.setText(post.getContent());
                textViewPostBy.setText("By: " + post.getBy());

                linearLayoutReplies.removeAllViews();
                List<Reply> replies = post.getReplies();
                for (Reply reply : replies) {
                    View replyView = getLayoutInflater().inflate(R.layout.reply_item, null);
                    TextView textViewReplyContent = replyView.findViewById(R.id.textViewReplyContent);
                    TextView textViewReplyBy = replyView.findViewById(R.id.textViewReplyBy);
                    textViewReplyContent.setText(reply.getContent());
                    textViewReplyBy.setText("By: " + reply.getBy());
                    linearLayoutReplies.addView(replyView);
                }
            }
        });
    }

    private void submitReply() {
        String replyContent = editTextAddReply.getText().toString().trim();
        if (replyContent.isEmpty()) {
            Toast.makeText(this, "Please enter a reply", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = getCurrentUsername();
        Reply newReply = new Reply(null, replyContent, username);

        firebaseConnector.addReplyToPost(courseId, postId, newReply, new ReplyCallback() {
            @Override
            public void onReplyAdded() {
                Toast.makeText(ViewPost.this, "Reply added successfully", Toast.LENGTH_SHORT).show();
                editTextAddReply.setText("");
                loadPostDetails();
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(ViewPost.this, "Failed to add reply", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePost() {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Forum").child(courseId).child(postId);
        postRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ViewPost.this, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                finish(); // Go back to the previous activity (ForumFragment)
            } else {
                Toast.makeText(ViewPost.this, "Failed to delete post", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Feature: Data-Formats
     * Description: Retrieves the current username from SharedPreferences.
     */
    private String getCurrentUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("username", "Guest");
    }

    /**
     * Feature: Privacy-Visibility
     * Description: Checks if the current user is an admin and adjusts the visibility of the delete button accordingly.
     */
    private void checkIfAdmin() {
        Set<String> adminCourses = readAdminCourses();
        String currentUsername = getCurrentUsername();
        // If the current user is an admin, make the delete button visible
        if (adminCourses.contains(currentUsername)) {
            buttonDeletePost.setVisibility(View.VISIBLE);
        } else {
            buttonDeletePost.setVisibility(View.GONE);
        }
    }

    /**
     * Feature: Privacy-Visibility,Data-Formats
     * Description: Reads the list of admin courses from the "Admin.txt" file in the assets folder.
     * @return A set of admin course names.
     */
    private Set<String> readAdminCourses() {
        Set<String> adminCourses = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("Admin.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                adminCourses.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return adminCourses;
    }
}
