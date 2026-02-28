package com.example.util;

import com.example.models.Task;
import com.example.persistence.TaskDAO;

/**
 * Utility to initialize the SQLite database and insert sample tasks.
 * Run with: mvn compile exec:java -Dexec.mainClass=com.example.util.DbInitializer
 */
public class DbInitializer {
    public static void main(String[] args) {
        TaskDAO dao = new TaskDAO();
        try {
            System.out.println("Initializing database...");
            dao.initializeDatabase();

            System.out.println("Inserting sample tasks...");
            Task t1 = dao.createTask("Learn Java");
            Task t2 = dao.createTask("Build TODO app");

            System.out.println("Inserted tasks:");
            System.out.println("- " + t1.getId() + ": " + t1.getTitle());
            System.out.println("- " + t2.getId() + ": " + t2.getTitle());
            System.out.println("Database initialization complete. todo.db created in working directory.");
        } catch (Exception e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }
}
