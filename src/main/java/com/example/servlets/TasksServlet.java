package com.example.servlets;

import com.example.models.Task;
import com.example.persistence.TaskDAO;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/tasks/*")
public class TasksServlet extends HttpServlet {

    /**
     * Serialization identifier required by {@link java.io.Serializable}. Not used.
     */
    private static final long serialVersionUID = 1L;

    /** Data access object for task persistence. */
    private TaskDAO taskDAO;

    /** JSON serializer/deserializer. */
    private final Gson gson = new Gson();

    /**
     * Initializes the servlet and ensures the backing database is ready.
     *
     * @throws ServletException if the database cannot be initialized
     */
    @Override
    public void init() throws ServletException {
        taskDAO = new TaskDAO();
        try {
            taskDAO.initializeDatabase();
        } catch (SQLException e) {
            throw new ServletException("Failed to initialize database", e);
        }
    }

    /**
     * Handles HTTP GET requests.
     *
     * <p>If no specific task id is provided, all tasks are returned. If a
     * single numerical id is specified in the path, that task is returned.</p>
     *
     * @param request  the servlet request
     * @param response the servlet response
     * @throws ServletException on servlet problem
     * @throws IOException      on I/O problem
     */
    @Override
    protected void doGet(final HttpServletRequest request,
                         final HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Task> tasks = taskDAO.getAllTasks();
                response.getWriter().write(gson.toJson(tasks));
                return;
            }

            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isEmpty()) {
                try {
                    int id = Integer.parseInt(parts[1]);
                    Task task = taskDAO.getTaskById(id);
                    if (task != null) {
                        response.getWriter().write(gson.toJson(task));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write(gson.toJson(new SimpleError("Task not found")));
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(gson.toJson(new SimpleError("Invalid task ID")));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(new SimpleError("Invalid path")));
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(new SimpleError("Database error: "
                    + e.getMessage())));
        }
    }

    /**
     * Handles HTTP POST requests for creating new tasks.
     *
     * @param request  servlet request containing JSON payload
     * @param response servlet response to which the created task is written
     * @throws ServletException on servlet problem
     * @throws IOException      on I/O problem
     */
    @Override
    protected void doPost(final HttpServletRequest request,
                          final HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");

        try {
            Task incoming = gson.fromJson(request.getReader(), Task.class);
            if (incoming == null || incoming.getTitle() == null
                    || incoming.getTitle().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(new SimpleError("Title is required")));
                return;
            }

            Task newTask = taskDAO.createTask(incoming.getTitle().trim());
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(newTask));
        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(new SimpleError("Invalid JSON")));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(new SimpleError("Database error: "
                    + e.getMessage())));
        }
    }

    /**
     * Handles HTTP PUT requests for updating an existing task.
     *
     * @param request  servlet request containing JSON payload and task id in path
     * @param response servlet response returning the updated task or error
     * @throws ServletException on servlet problem
     * @throws IOException      on I/O problem
     */
    @Override
    protected void doPut(final HttpServletRequest request,
                         final HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(new SimpleError("Task ID is required")));
                return;
            }

            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isEmpty()) {
                int id;
                try {
                    id = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(gson.toJson(new SimpleError("Invalid task ID")));
                    return;
                }

                Task incoming;
                try {
                    incoming = gson.fromJson(request.getReader(), Task.class);
                } catch (JsonSyntaxException jse) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(gson.toJson(new SimpleError("Invalid JSON")));
                    return;
                }

                if (incoming == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(gson.toJson(new SimpleError("Invalid request body")));
                    return;
                }

                incoming.setId(id);
                boolean updated = taskDAO.updateTask(incoming);
                if (updated) {
                    Task updatedTask = taskDAO.getTaskById(id);
                    response.getWriter().write(gson.toJson(updatedTask));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write(gson.toJson(new SimpleError("Task not found")));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(new SimpleError("Invalid path")));
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(new SimpleError("Database error: "
                    + e.getMessage())));
        }
    }

    /**
     * Handles HTTP DELETE requests to remove a task by ID.
     *
     * @param request  servlet request with task id in the path
     * @param response servlet response containing success or error message
     * @throws ServletException on servlet problem
     * @throws IOException      on I/O problem
     */
    @Override
    protected void doDelete(final HttpServletRequest request,
                            final HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(new SimpleError("Task ID is required")));
                return;
            }

            String[] parts = pathInfo.split("/");
            if (parts.length == 2 && !parts[1].isEmpty()) {
                try {
                    int id = Integer.parseInt(parts[1]);
                    boolean deleted = taskDAO.deleteTask(id);
                    if (deleted) {
                        response.getWriter().write(gson.toJson(
                                new SimpleMessage("Task deleted successfully")));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write(gson.toJson(
                                new SimpleError("Task not found")));
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(gson.toJson(
                            new SimpleError("Invalid task ID")));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(new SimpleError("Invalid path")));
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(new SimpleError("Database error: "
                    + e.getMessage())));
        }
    }

    /**
     * Minimal container for error responses.
     */
    @SuppressWarnings("unused")
    private static class SimpleError {

        private final String error;

        /**
         * Construct an error wrapper.
         *
         * @param error the error message to return in JSON
         */
        SimpleError(final String error) {
            this.error = error;
        }

        /**
         * @return the contained error message
         */
        public String getError() {
            return error;
        }
    }

    /**
     * Minimal container for informational responses.
     */
    @SuppressWarnings("unused")
    private static class SimpleMessage {

        private final String message;

        /**
         * Construct a message wrapper.
         *
         * @param message the message text to return in JSON
         */
        SimpleMessage(final String message) {
            this.message = message;
        }

        /**
         * @return the contained message text
         */
        public String getMessage() {
            return message;
        }
    }
}

