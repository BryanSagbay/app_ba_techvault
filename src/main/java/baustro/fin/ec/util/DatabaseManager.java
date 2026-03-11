package baustro.fin.ec.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_FOLDER = ".techops";
    private static final String DB_FILE   = "techops.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    private void connect() throws SQLException {
        try {
            // Crear carpeta si no existe
            Path dbFolder = Paths.get(System.getProperty("user.home"), DB_FOLDER);
            if (!Files.exists(dbFolder)) {
                Files.createDirectories(dbFolder);
            }

            Path dbPath = dbFolder.resolve(DB_FILE);
            String url = "jdbc:sqlite:" + dbPath.toAbsolutePath();

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(url);
            connection.setAutoCommit(true);

            // Activar foreign keys
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA journal_mode = WAL");
            }

            // Inicializar schema si es primera vez
            initSchema();

        } catch (ClassNotFoundException | IOException e) {
            throw new SQLException("Error al inicializar base de datos: " + e.getMessage(), e);
        }
    }

    private void initSchema() throws SQLException, IOException {
        try (InputStream is = getClass().getResourceAsStream("/db/schema.sql")) {
            if (is == null) throw new IOException("No se encontró schema.sql en el classpath");

            // Leer línea a línea, ignorar comentarios, acumular statements
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder current = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                // Ignorar líneas vacías y comentarios SQL (--)
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) continue;

                // Remover comentarios inline al final de la línea
                int commentIdx = trimmedLine.indexOf("--");
                if (commentIdx > 0) trimmedLine = trimmedLine.substring(0, commentIdx).trim();

                current.append(trimmedLine).append(" ");

                // Cuando encontramos ; es el fin del statement
                if (trimmedLine.endsWith(";")) {
                    String stmt = current.toString().trim();
                    // Quitar el ; final para execute()
                    if (stmt.endsWith(";")) stmt = stmt.substring(0, stmt.length() - 1).trim();
                    if (!stmt.isEmpty()) {
                        try (Statement s = connection.createStatement()) {
                            s.execute(stmt);
                        } catch (SQLException e) {
                            // Ignorar errores de "ya existe" (IF NOT EXISTS debería evitarlos,
                            // pero por si acaso con INSERT OR IGNORE)
                            System.err.println("Schema warning (ignorado): " + e.getMessage());
                        }
                    }
                    current.setLength(0);
                }
            }
        }
    }

    public Path getDbPath() {
        return Paths.get(System.getProperty("user.home"), DB_FOLDER, DB_FILE);
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
