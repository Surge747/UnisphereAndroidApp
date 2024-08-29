package com.example.mackenz;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ForumFragment extends Fragment {
    private RecyclerView chatRecyclerView;
    private Spinner spinnerCourses;
    private EditText searchInput;
    private Button searchButton;
    private TextView suggestionText;
    private ForumAdapter forumAdapter;
    private List<ForumPost> forumPostsList;
    private List<ForumPost> allForumPosts;
    private FirebaseConnector firebaseConnector;
    private AVLTree avlTree;

    public ForumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseConnector = new FirebaseConnector(getContext());
        avlTree = new AVLTree();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forum, container, false);
        TextView greeting = view.findViewById(R.id.textViewGreeting);
        String username = getCurrentUsername();
        greeting.setText("Hello, " + username + "!");

        spinnerCourses = view.findViewById(R.id.spinnerCourses);
        List<String> courses = readCoursesFromAssets(getContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, courses);
        spinnerCourses.setAdapter(adapter);
        // Feature: Course Selection Filter
        // Purpose: This code sets up an event listener for the course selection spinner.
        // When a user selects a course from the spinner, the selected course is retrieved,
        // the search input field is cleared, and the forum posts related to the selected
        // course are loaded and displayed. If no course is selected, no action is taken.

        spinnerCourses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = (String) parent.getItemAtPosition(position);
                searchInput.setText(""); // Clear the search bar
                loadForumPosts(selectedCourse);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // When no item is selected, no action is performed
            }
        });

        setupRecyclerView(view);
        setupSearchFunctionality(view);

        return view;
    }

    // Feature: LoadShowData
    // Purpose: Initializes and sets up the RecyclerView with the forum posts.
    private void setupRecyclerView(View view) {
        chatRecyclerView = view.findViewById(R.id.recyclerViewPosts);
        forumPostsList = new ArrayList<>();
        allForumPosts = new ArrayList<>();
        forumAdapter = new ForumAdapter(getActivity(), forumPostsList);
        chatRecyclerView.setAdapter(forumAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // Set the item click listener on the adapter
        forumAdapter.setOnItemClickListener(new ForumAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ForumPost post) {
                Intent intent = new Intent(getActivity(), ViewPost.class);
                intent.putExtra("courseId", spinnerCourses.getSelectedItem().toString());
                intent.putExtra("postId", post.getId());
                startActivity(intent);// Start activity to view the post
            }
        });
    }

    // Feature: Data-Formats
    // Purpose: Gets the current username from shared preferences.
    private String getCurrentUsername() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("username", "Guest");
    }

    // Feature: Data-Formats
    // Purpose: Reads courses from a local text file in the assets folder.
    private List<String> readCoursesFromAssets(Context context) {
        List<String> courseList = new ArrayList<>();
        try (InputStream is = context.getAssets().open("Usercourses.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                courseList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return courseList;
    }

    // Feature: LoadShowData, Data-Formats
    // Purpose: Fetches forum posts for a specific course from Firebase and displays them in a RecyclerView.
    private void loadForumPosts(String course) {
        firebaseConnector.fetchForumPostsForCourse(course, new ForumPostsFetchListener() {
            @Override
            public void onPostsFetched(List<ForumPost> forumPosts) {
                forumPostsList.clear();
                allForumPosts.clear();
                forumPostsList.addAll(forumPosts);
                allForumPosts.addAll(forumPosts);

                avlTree = new AVLTree();
                for (ForumPost post : forumPosts) {
                    avlTree.insert(post); // Insert each post into the AVL tree for searching
                }

                forumAdapter.notifyDataSetChanged();// Notify adapter to update the UI
            }
        });
    }

    private void setupSearchFunctionality(View view) {
        searchInput = view.findViewById(R.id.editTextSearch);
        searchButton = view.findViewById(R.id.Search_button);
        suggestionText = view.findViewById(R.id.suggestion_text);

        searchButton.setOnClickListener(v -> performSearch(searchInput.getText().toString().trim()));
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            forumPostsList.clear();
            forumPostsList.addAll(allForumPosts);
            forumAdapter.notifyDataSetChanged();
            suggestionText.setVisibility(View.GONE);
            return;
        }

        // Tokenize and parse the query
        SearchTokenizer tokenizer = new SearchTokenizer(query);
        SearchParser parser = new SearchParser(tokenizer);
        List<String> parsedTokens = parser.getParsedQuery();

        // Direct match search
        List<String> searchResults = new ArrayList<>();
        for (String token : parsedTokens) {
            searchResults.addAll(avlTree.searchPartial(token));
        }

        // Filter posts based on search results
        List<ForumPost> filteredPosts = filterResults(searchResults);
        if (filteredPosts.isEmpty()) {
            // If no results, perform fuzzy search
            String suggestion = findClosestMatch(query);
            if (suggestion != null) {
                suggestionText.setVisibility(View.VISIBLE);
                suggestionText.setText("Did you mean: " + suggestion + "?");
                SearchTokenizer suggestionTokenizer = new SearchTokenizer(suggestion);
                SearchParser suggestionParser = new SearchParser(suggestionTokenizer);
                List<String> suggestionParsedTokens = suggestionParser.getParsedQuery();
                searchResults.clear();
                for (String token : suggestionParsedTokens) {
                    searchResults.addAll(avlTree.searchPartial(token));
                }
                filteredPosts = filterResults(searchResults);
            } else {
                suggestionText.setVisibility(View.GONE);
            }
        } else {
            suggestionText.setVisibility(View.GONE);
        }
        forumPostsList.clear();
        forumPostsList.addAll(filteredPosts);
        forumAdapter.notifyDataSetChanged();
    }

    private List<ForumPost> filterResults(List<String> titles) {
        Set<String> uniqueTitles = new HashSet<>(titles);
        List<ForumPost> filteredPosts = new ArrayList<>();
        for (ForumPost post : allForumPosts) {
            if (uniqueTitles.contains(post.getTitle().toLowerCase(Locale.ROOT))) {
                filteredPosts.add(post);
            }
        }
        return filteredPosts;
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }
        return dp[a.length()][b.length()];
    }

    private boolean isSimilar(String word, String query) {
        int distance = levenshteinDistance(word, query);
        return distance <= 2; // Allow up to two edit distance
    }

    private String findClosestMatch(String query) {
        List<ForumPost> allPosts = avlTree.searchPosts("");
        for (ForumPost post : allPosts) {
            String[] words = post.getTitle().split("\\s+");
            for (String word : words) {
                if (isSimilar(word.toLowerCase(), query)) {
                    return word;
                }
            }
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadForumPosts(spinnerCourses.getSelectedItem().toString());
        }
    }
}
