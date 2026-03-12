package baustro.fin.ec.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;

    // La BD se guarda en: C:\Users\<User>\AppData\Local\TechOpsManager\techops.db
    private static final String DB_DIR = System.getProperty("user.home")
            + File.separator + "AppData" + File.separator + "Local"
            + File.separator + "TechOpsManager";
    private static final String DB_PATH = DB_DIR + File.separator + "techops.db";

    private DatabaseManager() {
        initDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public static String getDbPath() {
        return DB_PATH;
    }

    private void initDatabase() {
        try {
            File dir = new File(DB_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            createTables();
        } catch (Exception e) {
            throw new RuntimeException("Error inicializando base de datos: " + e.getMessage(), e);
        }
    }

    private void createTables() throws SQLException {
        String[] ddl = {
            // === CORRECTIVOS / INCIDENCIAS ===
            """
            CREATE TABLE IF NOT EXISTS correctivos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero_tarea TEXT NOT NULL,
                titulo TEXT NOT NULL,
                descripcion TEXT,
                ambiente TEXT,
                servicio TEXT,
                error_presentado TEXT,
                solucion TEXT,
                estado TEXT DEFAULT 'Abierto',
                prioridad TEXT DEFAULT 'Media',
                fecha_reporte TEXT,
                fecha_solucion TEXT,
                responsable TEXT,
                observaciones TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            // === SERVIDORES & IPs ===
            """
            CREATE TABLE IF NOT EXISTS servidores (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                ip TEXT NOT NULL,
                tipo TEXT,
                ambiente TEXT,
                sistema_operativo TEXT,
                descripcion TEXT,
                usuario_acceso TEXT,
                puerto TEXT,
                estado TEXT DEFAULT 'Activo',
                notas TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            // === GESTOR DE CONTRASEÑAS ===
            """
            CREATE TABLE IF NOT EXISTS contrasenas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                titulo TEXT NOT NULL,
                usuario TEXT,
                contrasena_enc TEXT NOT NULL,
                url TEXT,
                categoria TEXT,
                notas TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            // === TAREAS TO-DO ===
            """
            CREATE TABLE IF NOT EXISTS tareas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                titulo TEXT NOT NULL,
                descripcion TEXT,
                prioridad TEXT DEFAULT 'Media',
                estado TEXT DEFAULT 'Pendiente',
                fecha_limite TEXT,
                categoria TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            // === NOTAS ===
            """
            CREATE TABLE IF NOT EXISTS notas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                titulo TEXT NOT NULL,
                contenido TEXT,
                etiquetas TEXT,
                color TEXT DEFAULT '#FFFFFF',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            // === COMANDOS ===
            """
            CREATE TABLE IF NOT EXISTS comandos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                titulo TEXT NOT NULL,
                comando TEXT NOT NULL,
                descripcion TEXT,
                categoria TEXT,
                sistema_operativo TEXT DEFAULT 'Linux',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            // === MANUALES / DOCUMENTOS ===
            """
            CREATE TABLE IF NOT EXISTS manuales (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                titulo TEXT NOT NULL,
                ruta_archivo TEXT,
                url TEXT,
                descripcion TEXT,
                categoria TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : ddl) {
                stmt.execute(sql);
            }
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reconnectando BD", e);
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
