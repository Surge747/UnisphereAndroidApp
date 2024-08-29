package com.example.mackenz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class New_PostFragment extends Fragment {

    private Button uploadButton;
    private EditText titleEditText;
    private EditText contentEditText;
    private Spinner courseSpinner;
    private TextView textViewNewPost;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_post, container, false);

        uploadButton = view.findViewById(R.id.buttonSubmitPost);
        titleEditText = view.findViewById(R.id.Title_New_post);
        contentEditText = view.findViewById(R.id.New_post_content);
        courseSpinner = view.findViewById(R.id.New_Post_CourseSelector);
        textViewNewPost = view.findViewById(R.id.NewposttextView);

        // Set up course spinner with courses from Usercourses.txt in assets
        List<String> courses = readCoursesFromAssets();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, courses);
        courseSpinner.setAdapter(adapter);

        setupButtonListeners();

        return view;
    }

    /**
     * Feature: Data-Formats
     * Description: Reads the list of courses from a local text file located in the assets folder.
     */
    private List<String> readCoursesFromAssets() {
        List<String> courses = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getContext().getAssets().open("Usercourses.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                courses.add(line);
            }
        } catch (IOException e) {
            Log.e("New_PostFragment", "Error reading Usercourses.txt", e);
        }
        return courses;
    }

    private void setupButtonListeners() {
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleEditText.getText().toString();
                String content = contentEditText.getText().toString();
                String courseCode = courseSpinner.getSelectedItem().toString();

                // Get username from SharedPreferences
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                String username = sharedPreferences.getString("username", "defaultUser");

                createNewPost(courseCode, title, content, username, new CreatePostCallback() {
                    @Override
                    public void onSuccess() {
                        // Show a toast message
                        Toast.makeText(getContext(), "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                        // Clear the EditText fields
                        titleEditText.setText("");
                        contentEditText.setText("");
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        // Show a toast message
                        Toast.makeText(getContext(), "Failed to upload post", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void createNewPost(String courseCode, String title, String content, String author, CreatePostCallback callback) {
        DatabaseReference forumRef = FirebaseDatabase.getInstance().getReference().child("Forum").child(courseCode);

        forumRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long newPostId = snapshot.getChildrenCount() + 1;
                String postId = String.valueOf(newPostId);

                Map<String, Object> postValues = new HashMap<>();
                postValues.put("Title", title);
                postValues.put("Content", content);
                postValues.put("By", author);

                // Initialize empty replies list
                Map<String, Object> emptyReplies = new HashMap<>();
                postValues.put("Replies", emptyReplies);

                forumRef.child(postId).setValue(postValues, (databaseError, databaseReference) -> {
                    if (databaseError != null) {
                        callback.onFailure(databaseError.toException());
                    } else {
                        callback.onSuccess();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }
}
