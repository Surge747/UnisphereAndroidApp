package com.example.mackenz;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseConnector {

    private DatabaseReference databaseReference;
    private Context context;

    public FirebaseConnector(Context context) {
        this.context = context;
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void loginUser(String username, String password, LoginCallback callback) {
        databaseReference.child("Users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String storedPassword = dataSnapshot.child("password").getValue(String.class);
                    if (password.equals(storedPassword)) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure();
                    }
                } else {
                    callback.onFailure();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    public void createNewUser(String username, String password, String profilePicUrl, CreateUserCallback callback) {
        if (profilePicUrl == null || profilePicUrl.isEmpty()) {
            profilePicUrl = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png";
        }

        Map<String, Object> userValues = new HashMap<>();
        userValues.put("password", password);
        userValues.put("profilePicUrl", profilePicUrl);

        databaseReference.child("Users").child(username).setValue(userValues, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                callback.onFailure(databaseError.toException());
            } else {
                callback.onSuccess();
            }
        });
    }

    public void updateCourses(String username, String[] courses, UpdateCoursesCallback callback) {
        Map<String, Object> coursesValues = new HashMap<>();
        coursesValues.put("coursesTaken", Arrays.asList(courses));

        databaseReference.child("Users").child(username).updateChildren(coursesValues, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                callback.onFailure(databaseError.toException());
            } else {
                callback.onSuccess();
            }
        });
    }

    public void updateUserPassword(String username, String newPassword, FirebaseCallback callback) {
        DatabaseReference userRef = databaseReference.child("Users").child(username).child("password");
        userRef.setValue(newPassword)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure())
                .addOnCanceledListener(() -> callback.onError(new Exception("Request canceled")));
    }

    public void writeMessage(String sender, String receiver, String content) {
        String filename = getSortedFileName(sender, receiver);
        DatabaseReference msgRef = databaseReference.child("Messages").child(filename);
        int nextId = getLastMessageId(getMessageFile(filename)); // Ensuring it starts at 0 if the file is new

        Map<String, String> messageData = new HashMap<>();
        messageData.put("content", content);
        messageData.put("sender", sender);

        // Correctly call with String parameters
        msgRef.child(String.valueOf(nextId)).setValue(messageData)
                .addOnSuccessListener(aVoid -> saveMessageLocally(filename, String.valueOf(nextId), content, sender))
                .addOnFailureListener(e -> Log.e("FirebaseConnector", "Failed to send message: " + e.getMessage()));
    }


    public String getSortedFileName(String user1, String user2) {
        String[] users = {user1, user2};
        Arrays.sort(users);
        return users[0] + "_" + users[1];
    }


    public File getMessageFile(String filename) {
        File directory = new ContextWrapper(context).getDir("Messages", Context.MODE_PRIVATE);
        return new File(directory, filename + ".txt");
    }

    public int getLastMessageId(File file) {
        int lastId = 0;
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|\\|");
                    if (parts.length == 3) {
                        try {
                            int currentId = Integer.parseInt(parts[0]);
                            lastId = Math.max(lastId, currentId);
                        } catch (NumberFormatException e) {
                            Log.e("FirebaseConnector", "Skipping invalid line: " + line);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e("FirebaseConnector", "Error reading message file", e);
            }
        }
        return lastId;
    }

    public void fetchAllMessagesForCurrentUser(String currentUser, MessagesFetchListener listener) {
        DatabaseReference usersRef = databaseReference.child("Messages");
        List<String> relatedUsers = new ArrayList<>();

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                relatedUsers.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String chatKey = childSnapshot.getKey();
                    if (chatKey.contains(currentUser)) {
                        updateLocalMessageFile(currentUser, chatKey, childSnapshot);
                        String[] participants = chatKey.split("_");
                        String otherUser = participants[0].equals(currentUser) ? participants[1] : participants[0];
                        if (!relatedUsers.contains(otherUser)) {
                            relatedUsers.add(otherUser);
                        }
                    }
                }
                listener.onMessagesFetched(relatedUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseConnector", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void updateLocalMessageFile(String currentUser, String chatKey, DataSnapshot chatSnapshot) {
        File file = getMessageFile(chatKey);
        try (FileWriter writer = new FileWriter(file, false)) {  // Overwrite file for full sync
            for (DataSnapshot messageSnapshot : chatSnapshot.getChildren()) {
                String id = messageSnapshot.getKey();
                String content = messageSnapshot.child("content").getValue(String.class);
                String sender = messageSnapshot.child("sender").getValue(String.class);
                writer.write(id + "||" + content + "||" + sender + "\n");
            }
        } catch (IOException e) {
            Log.e("FirebaseConnector", "Error writing messages to file", e);
        }
    }

    public void loadMessages(String currentUser, String otherUser, MessageLoadListener listener) {
        String filename = getSortedFileName(currentUser, otherUser);
        File messageFile = getMessageFile(filename);
        DatabaseReference msgRef = databaseReference.child("Messages").child(filename);

        msgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Message> loadedMessages = new ArrayList<>();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    String content = messageSnapshot.child("content").getValue(String.class);
                    String sender = messageSnapshot.child("sender").getValue(String.class);
                    boolean isCurrentUser = sender.equals(currentUser);
                    loadedMessages.add(new Message(content, isCurrentUser));
                }
                // Update the file with the latest messages from Firebase
                updateLocalMessageFile(currentUser, filename, dataSnapshot);
                listener.onMessagesLoaded(loadedMessages);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseConnector", "Database error: " + databaseError.getMessage());
            }
        });
    }


    public void saveMessageLocally(String filename, String id, String content, String sender) {
        try (FileWriter writer = new FileWriter(getMessageFile(filename), true)) {
            writer.append(id + "||" + content + "||" + sender + "\n");
        } catch (IOException e) {
            Log.e("FirebaseConnector", "Error writing to local message file", e);
        }
    }

    public void addReplyToPost(String courseId, String postId, Reply reply, ReplyCallback callback) {
        DatabaseReference repliesRef = databaseReference.child("Forum").child(courseId).child(postId).child("Replies");

        repliesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long maxId = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    long id = Long.parseLong(snapshot.getKey());
                    if (id > maxId) {
                        maxId = id;
                    }
                }
                String newId = String.valueOf(maxId + 1);
                reply.setId(newId);
                repliesRef.child(newId).setValue(reply).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onReplyAdded();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }
    public void fetchPostDetailsWithRealTimeUpdates(String courseId, String postId, PostDetailsFetchListener listener) {
        DatabaseReference postRef = databaseReference.child("Forum").child(courseId).child(postId);

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ForumPost post = dataSnapshot.getValue(ForumPost.class);
                if (post != null) {
                    post.setId(postId);
                    List<Reply> replies = new ArrayList<>();
                    DataSnapshot repliesSnapshot = dataSnapshot.child("Replies");
                    for (DataSnapshot replySnapshot : repliesSnapshot.getChildren()) {
                        Reply reply = replySnapshot.getValue(Reply.class);
                        if (reply != null) {
                            reply.setId(replySnapshot.getKey()); // Set the ID of the reply
                            replies.add(reply);
                        }
                    }
                    post.setReplies(replies);
                    listener.onPostDetailsFetched(post);
                } else {
                    System.err.println("Error: Post not found in Firebase.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    public void fetchForumPostsForCourse(String course, ForumPostsFetchListener listener) {
        databaseReference.child("Forum").child(course).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<ForumPost> forumPosts = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ForumPost post = snapshot.getValue(ForumPost.class);
                    if (post != null && post.getTitle() != null) {
                        post.setId(snapshot.getKey()); // Set the temp ID for local reference
                        forumPosts.add(post);
                    } else {
                        System.err.println("Error: Post with null title fetched from Firebase.");
                    }
                }
                listener.onPostsFetched(forumPosts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}
