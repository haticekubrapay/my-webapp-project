package com.example.models;

/**
 * Task model representing a TODO item.
 * Encapsulates id, title, and completed status.
 */
public class Task {
    private int id;
    private String title;
    private boolean completed;

    /**
     * Default constructor for Task.
     */
    public Task() {
    }

    /**
     * Constructor with id, title, and completed status.
     *
     * @param id        the unique identifier for the task
     * @param title     the task title
     * @param completed whether the task is completed
     */
    public Task(int id, String title, boolean completed) {
        this.id = id;
        this.title = title;
        this.completed = completed;
    }

    /**
     * Constructor with title only (for new tasks).
     *
     * @param title the task title
     */
    public Task(String title) {
        this.title = title;
        this.completed = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
