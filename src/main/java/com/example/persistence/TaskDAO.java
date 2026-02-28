package com.example.persistence;

import com.example.models.Task;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Task persistence using SQLite.
 * Handles all database operations with the todo.db SQLite database.
 */
public class TaskDAO {
    private static final String DB_URL = "jdbc:sqlite:todo.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ensure the tasks table exists when a TaskDAO instance is created.
     * This helps when Tomcat runs with a different working directory.
     */
    public TaskDAO() {
        try {
            initializeDatabase();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize tasks table", e);
        }
    }

    /**
     * Initialize the database and create the tasks table if it doesn't exist.
     *
     * @throws SQLException if database connection fails
     */
    public void initializeDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            // exact statement required by Tomcat's working directory behavior
            String sql = "CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, completed BOOLEAN);";
            stmt.execute(sql);
        }
    }

    /**
     * Retrieve all tasks from the database.
     *
     * @return a list of all tasks
     * @throws SQLException if database operation fails
     */
    public List<Task> getAllTasks() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT id, title, completed FROM tasks ORDER BY id ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getBoolean("completed")
                );
                tasks.add(task);
            }
        }
        return tasks;
    }

    /**
     * Retrieve a specific task by ID.
     *
     * @param id the task ID
     * @return the Task object, or null if not found
     * @throws SQLException if database operation fails
     */
    public Task getTaskById(int id) throws SQLException {
        String sql = "SELECT id, title, completed FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Task(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getBoolean("completed")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Create a new task in the database.
     *
     * @param title the task title
     * @return the newly created Task object with generated ID
     * @throws SQLException if database operation fails
     */
    public Task createTask(String title) throws SQLException {
        String sql = "INSERT INTO tasks (title, completed) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, title);
            pstmt.setBoolean(2, false);
            pstmt.executeUpdate();

            // Try to get generated keys; if not returned by the driver, fallback to last_insert_rowid()
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys != null && generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return new Task(id, title, false);
                }
            }

            // Fallback for drivers (like some SQLite drivers) that don't return generated keys
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new Task(id, title, false);
                }
            }
        }
        throw new SQLException("Failed to create task, no ID generated");
    }

    /**
     * Update an existing task.
     *
     * @param task the Task object to update
     * @return true if update was successful, false if task not found
     * @throws SQLException if database operation fails
     */
    public boolean updateTask(Task task) throws SQLException {
        String sql = "UPDATE tasks SET title = ?, completed = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setBoolean(2, task.isCompleted());
            pstmt.setInt(3, task.getId());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Delete a task by ID.
     *
     * @param id the task ID to delete
     * @return true if delete was successful, false if task not found
     * @throws SQLException if database operation fails
     */
    public boolean deleteTask(int id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}
