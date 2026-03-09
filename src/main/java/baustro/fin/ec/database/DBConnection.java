package baustro.fin.ec.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBConnection {

    private static final String URL = "jdbc:sqlite:data/ops.db";
    private static Connection instance;

    public static Connection connect() {
        try {
            if (instance == null || instance.isClosed()) {
                instance = DriverManager.getConnection(URL);
                instance.setAutoCommit(true);
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void initDatabase() {
        Connection conn = connect();
        if (conn == null) return;
        try {
            Statement stmt = conn.createStatement();

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS servers (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name TEXT NOT NULL," +
                "  ip TEXT NOT NULL," +
                "  environment TEXT DEFAULT 'PRODUCCION'," +
                "  os TEXT," +
                "  description TEXT," +
                "  ssh_user TEXT," +
                "  ssh_port INTEGER DEFAULT 22," +
                "  tags TEXT," +
                "  status TEXT DEFAULT 'ACTIVO'," +
                "  created_at TEXT DEFAULT (datetime('now','localtime'))," +
                "  updated_at TEXT DEFAULT (datetime('now','localtime'))" +
                ")"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS incidents (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  ticket_number TEXT," +
                "  title TEXT NOT NULL," +
                "  description TEXT," +
                "  solution TEXT," +
                "  severity TEXT DEFAULT 'MEDIA'," +
                "  status TEXT DEFAULT 'ABIERTO'," +
                "  server_id INTEGER," +
                "  affected_service TEXT," +
                "  root_cause TEXT," +
                "  start_date TEXT," +
                "  end_date TEXT," +
                "  created_at TEXT DEFAULT (datetime('now','localtime'))," +
                "  FOREIGN KEY (server_id) REFERENCES servers(id)" +
                ")"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS tasks (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  title TEXT NOT NULL," +
                "  description TEXT," +
                "  priority TEXT DEFAULT 'MEDIA'," +
                "  status TEXT DEFAULT 'PENDIENTE'," +
                "  due_date TEXT," +
                "  tags TEXT," +
                "  created_at TEXT DEFAULT (datetime('now','localtime'))," +
                "  updated_at TEXT DEFAULT (datetime('now','localtime'))" +
                ")"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS notes (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  title TEXT NOT NULL," +
                "  content TEXT," +
                "  category TEXT DEFAULT 'GENERAL'," +
                "  tags TEXT," +
                "  created_at TEXT DEFAULT (datetime('now','localtime'))," +
                "  updated_at TEXT DEFAULT (datetime('now','localtime'))" +
                ")"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS passwords (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  service_name TEXT NOT NULL," +
                "  username TEXT," +
                "  password_encrypted TEXT NOT NULL," +
                "  url TEXT," +
                "  notes TEXT," +
                "  category TEXT DEFAULT 'GENERAL'," +
                "  created_at TEXT DEFAULT (datetime('now','localtime'))," +
                "  updated_at TEXT DEFAULT (datetime('now','localtime'))" +
                ")"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS commands (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  title TEXT NOT NULL," +
                "  command TEXT NOT NULL," +
                "  description TEXT," +
                "  category TEXT DEFAULT 'GENERAL'," +
                "  os TEXT DEFAULT 'LINUX'," +
                "  tags TEXT," +
                "  created_at TEXT DEFAULT (datetime('now','localtime'))" +
                ")"
            );

            stmt.close();
            System.out.println("[DB] Base de datos inicializada correctamente.");

        } catch (Exception e) {
            System.err.println("[DB] Error al inicializar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
