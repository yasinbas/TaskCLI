import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TaskCli {

    private static final String FILE_NAME = "tasks.json";
    private static final Path PATH = Paths.get(FILE_NAME);

    // Görev Modeli
    static class Task {
        int id;
        String description;
        String status; // todo, in-progress, done
        String createdAt;
        String updatedAt;

        Task(int id, String description, String status, String createdAt, String updatedAt) {
            this.id = id;
            this.description = description;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        // Nesneyi manuel JSON String'e çevirme
        public String toJson() {
            return String.format(
                    "{\"id\":%d,\"description\":\"%s\",\"status\":\"%s\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}",
                    id, escapeJson(description), status, createdAt, updatedAt
            );
        }

        // Basit JSON parse edici
        public static Task fromJson(String json) {
            json = json.trim().replaceAll("^\\{|\\}$", "");
            String[] parts = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            int id = 0;
            String description = "";
            String status = "todo";
            String createdAt = "";
            String updatedAt = "";

            for (String part : parts) {
                String[] pair = part.split(":", 2);
                if (pair.length < 2) continue;

                String key = pair[0].trim().replace("\"", "");
                String value = pair[1].trim().replaceAll("^\"|\"$", "");

                switch (key) {
                    case "id": id = Integer.parseInt(value); break;
                    case "description": description = unescapeJson(value); break;
                    case "status": status = value; break;
                    case "createdAt": createdAt = value; break;
                    case "updatedAt": updatedAt = value; break;
                }
            }
            return new Task(id, description, status, createdAt, updatedAt);
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        List<Task> tasks = loadTasks();
        String command = args[0];

        try {
            switch (command) {
                case "add":
                    if (args.length < 2) {
                        System.out.println("Hata: Görev açıklaması girilmedi.");
                        return;
                    }
                    addTask(tasks, args[1]);
                    break;

                case "update":
                    if (args.length < 3) {
                        System.out.println("Kullanım: task-cli update <id> <yeni_açıklama>");
                        return;
                    }
                    updateTask(tasks, Integer.parseInt(args[1]), args[2]);
                    break;

                case "delete":
                    if (args.length < 2) {
                        System.out.println("Kullanım: task-cli delete <id>");
                        return;
                    }
                    deleteTask(tasks, Integer.parseInt(args[1]));
                    break;

                case "mark-in-progress":
                    if (args.length < 2) {
                        System.out.println("Kullanım: task-cli mark-in-progress <id>");
                        return;
                    }
                    updateStatus(tasks, Integer.parseInt(args[1]), "in-progress");
                    break;

                case "mark-done":
                    if (args.length < 2) {
                        System.out.println("Kullanım: task-cli mark-done <id>");
                        return;
                    }
                    updateStatus(tasks, Integer.parseInt(args[1]), "done");
                    break;

                case "list":
                    String filter = args.length > 1 ? args[1] : "all";
                    listTasks(tasks, filter);
                    break;

                default:
                    System.out.println("Bilinmeyen komut: " + command);
                    printUsage();
            }
        } catch (NumberFormatException e) {
            System.out.println("Hata: ID bir sayı olmalıdır.");
        } catch (Exception e) {
            System.out.println("Bir hata oluştu: " + e.getMessage());
        }
    }

    // --- FONKSİYONLAR ---

    private static void addTask(List<Task> tasks, String description) {
        int nextId = tasks.stream().mapToInt(t -> t.id).max().orElse(0) + 1;
        String now = getCurrentTimestamp();
        Task newTask = new Task(nextId, description, "todo", now, now);
        tasks.add(newTask);
        saveTasks(tasks);
        System.out.println("Task added successfully (ID: " + nextId + ")");
    }

    private static void updateTask(List<Task> tasks, int id, String newDescription) {
        Task task = findTask(tasks, id);
        if (task != null) {
            task.description = newDescription;
            task.updatedAt = getCurrentTimestamp();
            saveTasks(tasks);
            System.out.println("Task updated successfully (ID: " + id + ")");
        } else {
            System.out.println("Hata: ID " + id + " bulunamadı.");
        }
    }

    private static void updateStatus(List<Task> tasks, int id, String status) {
        Task task = findTask(tasks, id);
        if (task != null) {
            task.status = status;
            task.updatedAt = getCurrentTimestamp();
            saveTasks(tasks);
            System.out.println("Task marked as " + status + " (ID: " + id + ")");
        } else {
            System.out.println("Hata: ID " + id + " bulunamadı.");
        }
    }

    private static void deleteTask(List<Task> tasks, int id) {
        Task task = findTask(tasks, id);
        if (task != null) {
            tasks.remove(task);
            saveTasks(tasks);
            System.out.println("Task deleted successfully (ID: " + id + ")");
        } else {
            System.out.println("Hata: ID " + id + " bulunamadı.");
        }
    }

    private static void listTasks(List<Task> tasks, String filter) {
        if (tasks.isEmpty()) {
            System.out.println("Görev bulunamadı.");
            return;
        }

        boolean found = false;
        for (Task task : tasks) {
            if (filter.equals("all") || task.status.equalsIgnoreCase(filter)) {
                System.out.printf("[%d] %s - Durum: %s (Oluşturulma: %s)\n",
                        task.id, task.description, task.status, task.createdAt);
                found = true;
            }
        }

        if (!found) {
            System.out.println("Bu kritere uygun görev bulunamadı: " + filter);
        }
    }

    private static Task findTask(List<Task> tasks, int id) {
        return tasks.stream().filter(t -> t.id == id).findFirst().orElse(null);
    }

    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // --- DOSYA İŞLEMLERİ (Native JSON parser/serializer) ---

    private static List<Task> loadTasks() {
        List<Task> tasks = new ArrayList<>();
        if (!Files.exists(PATH)) {
            return tasks;
        }

        try {
            String content = Files.readString(PATH).trim();
            if (content.isEmpty() || content.equals("[]")) {
                return tasks;
            }

            // JSON array içeriğini ayırma
            content = content.substring(1, content.length() - 1);
            String[] items = content.split("(?<=\\}),\\s*(?=\\{)");
            for (String item : items) {
                if (!item.isBlank()) {
                    tasks.add(Task.fromJson(item));
                }
            }
        } catch (IOException e) {
            System.out.println("Dosya okuma hatası: " + e.getMessage());
        }
        return tasks;
    }

    private static void saveTasks(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < tasks.size(); i++) {
            sb.append("  ").append(tasks.get(i).toJson());
            if (i < tasks.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]");

        try {
            Files.writeString(PATH, sb.toString());
        } catch (IOException e) {
            System.out.println("Dosya yazma hatası: " + e.getMessage());
        }
    }

    private static String escapeJson(String input) {
        return input.replace("\"", "\\\"");
    }

    private static String unescapeJson(String input) {
        return input.replace("\\\"", "\"");
    }

    private static void printUsage() {
        System.out.println("Kullanım:");
        System.out.println("  java TaskCli add \"Görev açıklaması\"");
        System.out.println("  java TaskCli update <id> \"Yeni açıklama\"");
        System.out.println("  java TaskCli delete <id>");
        System.out.println("  java TaskCli mark-in-progress <id>");
        System.out.println("  java TaskCli mark-done <id>");
        System.out.println("  java TaskCli list [done|todo|in-progress]");
    }
}