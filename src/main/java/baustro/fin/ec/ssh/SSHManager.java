package baustro.fin.ec.ssh;

import com.jcraft.jsch.*;

public class SSHManager {

    public static void connect(String host, String user, String password) {

        try {

            JSch jsch = new JSch();

            Session session = jsch.getSession(user, host, 22);
            session.setPassword(password);

            session.setConfig("StrictHostKeyChecking", "no");

            session.connect();

            System.out.println("Conectado a " + host);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}