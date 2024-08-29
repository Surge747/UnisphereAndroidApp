package com.example.mackenz;

import java.util.ArrayList;
import java.util.List;

public class AVLTree {
    class Node {
        ForumPost post;
        int height;
        Node left, right;

        Node(ForumPost post) {
            this.post = post;
            this.left = null;
            this.right = null;
            height = 1;
        }
    }

    private Node root;

    public void insert(ForumPost post) {
        if (post.getTitle() != null) {
            root = insertNode(root, post);
        } else {
            // Handle the case where the title is null
            System.err.println("Error: Trying to insert a post with a null title.");
        }
    }

    private Node insertNode(Node node, ForumPost post) {
        if (node == null) {
            return new Node(post);
        }

        int cmp = post.getTitle().compareTo(node.post.getTitle());
        if (cmp < 0) {
            node.left = insertNode(node.left, post);
        } else if (cmp > 0) {
            node.right = insertNode(node.right, post);
        } else {
            return node;  // ignore duplicate titles
        }

        node.height = 1 + Math.max(height(node.left), height(node.right));
        return balance(node);
    }

    private int height(Node node) {
        return (node == null) ? 0 : node.height;
    }

    private Node balance(Node node) {
        int balance = getBalance(node);
        if (balance > 1 && getBalance(node.left) >= 0) {
            return rightRotate(node);
        }
        if (balance < -1 && getBalance(node.right) <= 0) {
            return leftRotate(node);
        }
        if (balance > 1 && getBalance(node.left) < 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }
        if (balance < -1 && getBalance(node.right) > 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }
        return node;
    }

    private int getBalance(Node node) {
        return (node == null) ? 0 : height(node.left) - height(node.right);
    }

    private Node rightRotate(Node y) {
        Node x = y.left;
        Node T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;

        return x;
    }

    private Node leftRotate(Node x) {
        Node y = x.right;
        Node T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;

        return y;
    }

    public List<String> searchPartial(String query) {
        List<String> results = new ArrayList<>();
        searchPartialHelper(root, query, results);
        return results;
    }

    private void searchPartialHelper(Node node, String query, List<String> results) {
        if (node == null) {
            return;
        }
        if (node.post.getTitle().toLowerCase().contains(query.toLowerCase())) {
            results.add(node.post.getTitle().toLowerCase());
        }
        searchPartialHelper(node.left, query, results);
        searchPartialHelper(node.right, query, results);
    }

    public List<ForumPost> searchPosts(String query) {
        List<ForumPost> results = new ArrayList<>();
        searchPostsHelper(root, query, results);
        return results;
    }

    private void searchPostsHelper(Node node, String query, List<ForumPost> results) {
        if (node == null) {
            return;
        }
        if (node.post.getTitle().toLowerCase().contains(query.toLowerCase())) {
            results.add(node.post);
        }
        searchPostsHelper(node.left, query, results);
        searchPostsHelper(node.right, query, results);
    }
}
