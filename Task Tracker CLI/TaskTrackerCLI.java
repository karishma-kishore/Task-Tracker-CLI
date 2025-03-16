import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class TaskTrackerCLI {

    private static final String TASKS_FILE = "tasks.json";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: task-cli <command> [arguments]");
            return;
        }

        String command = args[0];

        try {
            switch (command) {
                case "add":
                    if (args.length < 2) {
                        System.out.println("Usage: task-cli add \"<task description>\"");
                        return;
                    }
                    addTask(args[1]);
                    break;
                case "update":
                    if (args.length < 3) {
                        System.out.println("Usage: task-cli update <task ID> \"<new description>\"");
                        return;
                    }
                    updateTask(Integer.parseInt(args[1]), args[2]);
                    break;
                case "delete":
                    if (args.length < 2) {
                        System.out.println("Usage: task-cli delete <task ID>");
                        return;
                    }
                    deleteTask(Integer.parseInt(args[1]));
                    break;
                case "mark-in-progress":
                    if (args.length < 2) {
                        System.out.println("Usage: task-cli mark-in-progress <task ID>");
                        return;
                    }
                    markTaskInProgress(Integer.parseInt(args[1]));
                    break;
                case "mark-done":
                    if (args.length < 2) {
                        System.out.println("Usage: task-cli mark-done <task ID>");
                        return;
                    }
                    markTaskDone(Integer.parseInt(args[1]));
                    break;
                case "list":
                    if (args.length == 1) {
                        listAllTasks();
                    } else {
                        listTasksByStatus(args[1]);
                    }
                    break;
                default:
                    System.out.println("Unknown command: " + command);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void addTask(String description) throws IOException {
        String tasks = readTasks();
        int newId = tasks.split("\\{").length; // Simple way to generate a new ID

        String newTask = String.format(
            "{\"id\":%d,\"description\":\"%s\",\"status\":\"todo\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}",
            newId, description, getCurrentDateTime(), getCurrentDateTime()
        );

        if (tasks.isEmpty()) {
            tasks = "[" + newTask + "]";
        } else {
            tasks = tasks.replace("]", "," + newTask + "]");
        }

        writeTasks(tasks);
        System.out.println("Task added successfully (ID: " + newId + ")");
    }

    private static void updateTask(int id, String newDescription) throws IOException {
        String tasks = readTasks();
        String[] taskArray = tasks.split("\\},\\{");

        for (int i = 0; i < taskArray.length; i++) {
            if (taskArray[i].contains("\"id\":" + id)) {
                String task = taskArray[i];
                task = task.replaceFirst("\"description\":\"[^\"]*\"", "\"description\":\"" + newDescription + "\"");
                task = task.replaceFirst("\"updatedAt\":\"[^\"]*\"", "\"updatedAt\":\"" + getCurrentDateTime() + "\"");
                taskArray[i] = task;
                tasks = "[" + String.join("},{", taskArray) + "]";
                writeTasks(tasks);
                System.out.println("Task updated successfully (ID: " + id + ")");
                return;
            }
        }
        System.out.println("Task not found (ID: " + id + ")");
    }

    private static void deleteTask(int id) throws IOException {
        String tasks = readTasks();
        String[] taskArray = tasks.split("\\},\\{");

        for (int i = 0; i < taskArray.length; i++) {
            if (taskArray[i].contains("\"id\":" + id)) {
                String updatedTasks = "";
                for (int j = 0; j < taskArray.length; j++) {
                    if (j != i) {
                        updatedTasks += (updatedTasks.isEmpty() ? "" : "},{") + taskArray[j];
                    }
                }
                updatedTasks = "[" + updatedTasks + "]";
                writeTasks(updatedTasks);
                System.out.println("Task deleted successfully (ID: " + id + ")");
                return;
            }
        }
        System.out.println("Task not found (ID: " + id + ")");
    }

    private static void markTaskInProgress(int id) throws IOException {
        updateTaskStatus(id, "in-progress");
    }

    private static void markTaskDone(int id) throws IOException {
        updateTaskStatus(id, "done");
    }

    private static void updateTaskStatus(int id, String status) throws IOException {
        String tasks = readTasks();
        String[] taskArray = tasks.split("\\},\\{");

        for (int i = 0; i < taskArray.length; i++) {
            if (taskArray[i].contains("\"id\":" + id)) {
                String task = taskArray[i];
                task = task.replaceFirst("\"status\":\"[^\"]*\"", "\"status\":\"" + status + "\"");
                task = task.replaceFirst("\"updatedAt\":\"[^\"]*\"", "\"updatedAt\":\"" + getCurrentDateTime() + "\"");
                taskArray[i] = task;
                tasks = "[" + String.join("},{", taskArray) + "]";
                writeTasks(tasks);
                System.out.println("Task marked as " + status + " (ID: " + id + ")");
                return;
            }
        }
        System.out.println("Task not found (ID: " + id + ")");
    }

    private static void listAllTasks() throws IOException {
        String tasks = readTasks();
        System.out.println(tasks);
    }

    private static void listTasksByStatus(String status) throws IOException {
        String tasks = readTasks();
        String[] taskArray = tasks.split("\\},\\{");

        for (String task : taskArray) {
            if (task.contains("\"status\":\"" + status + "\"")) {
                System.out.println("{" + task + "}");
            }
        }
    }

    private static String readTasks() throws IOException {
        File file = new File(TASKS_FILE);
        if (!file.exists()) {
            return "";
        }

        StringBuilder content = new StringBuilder();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine());
            }
        }
        return content.toString();
    }

    private static void writeTasks(String tasks) throws IOException {
        try (FileWriter writer = new FileWriter(TASKS_FILE)) {
            writer.write(tasks);
        }
    }

    private static String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}