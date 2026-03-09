package baustro.fin.ec.model;

public class Server {
    private int id;
    private String name;
    private String ip;
    private String environment;
    private String os;
    private String description;
    private String sshUser;
    private int sshPort;
    private String tags;
    private String status;
    private String createdAt;

    public Server() { this.sshPort = 22; this.status = "ACTIVO"; }

    public Server(int id, String name, String ip, String environment, String os,
                  String description, String sshUser, int sshPort, String tags, String status, String createdAt) {
        this.id = id; this.name = name; this.ip = ip; this.environment = environment;
        this.os = os; this.description = description; this.sshUser = sshUser;
        this.sshPort = sshPort; this.tags = tags; this.status = status; this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSshUser() { return sshUser; }
    public void setSshUser(String sshUser) { this.sshUser = sshUser; }
    public int getSshPort() { return sshPort; }
    public void setSshPort(int sshPort) { this.sshPort = sshPort; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() { return name + " (" + ip + ")"; }
}
