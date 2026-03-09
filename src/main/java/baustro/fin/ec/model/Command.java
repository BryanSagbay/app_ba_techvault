package baustro.fin.ec.model;

public class Command {
    private int id;
    private String title;
    private String command;
    private String description;
    private String category;
    private String os;
    private String tags;
    private String createdAt;

    public Command() { this.category = "GENERAL"; this.os = "LINUX"; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
