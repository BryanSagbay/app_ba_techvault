package baustro.fin.ec.ssh;

import com.jcraft.jsch.*;

/**
 * Gestor SSH para conectarse a servidores remotos.
 * Usa JSch (fork moderno: com.github.mwiede).
 */
public class SSHManager {

    private Session session;
    private final String host;
    private final String user;
    private final int port;

    public SSHManager(String host, String user, int port) {
        this.host = host;
        this.user = user;
        this.port = port;
    }

    public boolean connect(String password) {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(5000);
            System.out.println("[SSH] Conectado a " + host);
            return true;
        } catch (Exception e) {
            System.err.println("[SSH] Error al conectar a " + host + ": " + e.getMessage());
            return false;
        }
    }

    public String executeCommand(String command) {
        if (session == null || !session.isConnected()) return "[Error: No conectado]";
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            java.io.InputStream in = channel.getInputStream();
            channel.connect();

            StringBuilder output = new StringBuilder();
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                output.append(new String(buf, 0, len));
            }
            channel.disconnect();
            return output.toString();
        } catch (Exception e) {
            return "[Error: " + e.getMessage() + "]";
        }
    }

    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("[SSH] Desconectado de " + host);
        }
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }
}
