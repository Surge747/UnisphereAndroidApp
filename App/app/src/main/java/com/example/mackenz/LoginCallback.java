package com.example.mackenz;

/**
 * This interface defines the callback methods for login operations.
 * It is used to communicate the results of the login attempt back to the calling class.
 */
public interface LoginCallback {
    /**
     * Called when login is successful.
     * This method should be implemented to handle any actions that need to be taken after a successful login.
     */
    void onSuccess();

    /**
     * Called when login fails due to invalid credentials, server issues, etc.
     * This method should be implemented to handle any actions that need to be taken after a login failure.
     */
    void onFailure();

    /**
     * Called when an error occurs during the login process.
     * This includes network errors, database errors, etc.
     * @param exception The exception thrown during the login attempt, providing details about the error.
     */
    void onError(Exception exception);
}

