-- =====================================================
-- TechOps Manager - Schema SQLite
-- =====================================================

-- Configuración general de la app
CREATE TABLE IF NOT EXISTS config (
    key   TEXT PRIMARY KEY,
    value TEXT NOT NULL
);

-- =====================================================
-- MÓDULO: INCIDENCIAS / CORRECTIVOS
-- =====================================================
CREATE TABLE IF NOT EXISTS incidencias (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    numero      TEXT NOT NULL,
    titulo      TEXT NOT NULL,
    descripcion TEXT,
    servicio    TEXT,
    solucion    TEXT,
    estado      TEXT NOT NULL DEFAULT 'ABIERTO', -- ABIERTO, EN_PROCESO, CERRADO
    prioridad   TEXT NOT NULL DEFAULT 'MEDIA',   -- BAJA, MEDIA, ALTA, CRITICA
    fecha_inicio TEXT NOT NULL,
    fecha_cierre TEXT,
    tags        TEXT,
    creado_en   TEXT NOT NULL DEFAULT (datetime('now')),
    actualizado_en TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_incidencias_numero ON incidencias(numero);
CREATE INDEX IF NOT EXISTS idx_incidencias_estado ON incidencias(estado);

-- =====================================================
-- MÓDULO: SERVIDORES / IPs
-- =====================================================
CREATE TABLE IF NOT EXISTS servidores (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre      TEXT NOT NULL,
    ip          TEXT NOT NULL,
    hostname    TEXT,
    so          TEXT,
    rol         TEXT,  -- DB, APP, WEB, PROXY, etc.
    ambiente    TEXT,  -- PROD, QA, DEV, HOM
    descripcion TEXT,
    puerto_ssh  TEXT DEFAULT '22',
    usuario     TEXT,
    notas       TEXT,
    activo      INTEGER NOT NULL DEFAULT 1,
    creado_en   TEXT NOT NULL DEFAULT (datetime('now')),
    actualizado_en TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_servidores_ip ON servidores(ip);
CREATE INDEX IF NOT EXISTS idx_servidores_ambiente ON servidores(ambiente);

-- =====================================================
-- MÓDULO: GESTOR DE CONTRASEÑAS
-- =====================================================
CREATE TABLE IF NOT EXISTS contrasenas (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo      TEXT NOT NULL,
    usuario     TEXT,
    password    TEXT NOT NULL,  -- Cifrada con AES-256-GCM
    url         TEXT,
    categoria   TEXT,           -- DB, SSH, APP, WEB, SISTEMA
    servidor_id INTEGER REFERENCES servidores(id) ON DELETE SET NULL,
    notas       TEXT,
    creado_en   TEXT NOT NULL DEFAULT (datetime('now')),
    actualizado_en TEXT NOT NULL DEFAULT (datetime('now'))
);

-- =====================================================
-- MÓDULO: NOTAS
-- =====================================================
CREATE TABLE IF NOT EXISTS notas (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo      TEXT NOT NULL,
    contenido   TEXT,
    categoria   TEXT,
    tags        TEXT,
    creado_en   TEXT NOT NULL DEFAULT (datetime('now')),
    actualizado_en TEXT NOT NULL DEFAULT (datetime('now'))
);

-- =====================================================
-- MÓDULO: COMANDOS / SNIPPETS
-- =====================================================
CREATE TABLE IF NOT EXISTS comandos (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo      TEXT NOT NULL,
    comando     TEXT NOT NULL,
    descripcion TEXT,
    categoria   TEXT,  -- LINUX, SQL, GIT, DOCKER, WINDOWS, ORACLE, etc.
    so          TEXT,  -- Linux, Windows, Ambos
    tags        TEXT,
    favorito    INTEGER NOT NULL DEFAULT 0,
    creado_en   TEXT NOT NULL DEFAULT (datetime('now')),
    actualizado_en TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_comandos_categoria ON comandos(categoria);
CREATE INDEX IF NOT EXISTS idx_comandos_favorito ON comandos(favorito);

-- =====================================================
-- MÓDULO: TAREAS
-- =====================================================
CREATE TABLE IF NOT EXISTS tareas (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo      TEXT NOT NULL,
    descripcion TEXT,
    estado      TEXT NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, EN_CURSO, COMPLETADA
    prioridad   TEXT NOT NULL DEFAULT 'MEDIA',
    fecha_limite TEXT,
    incidencia_id INTEGER REFERENCES incidencias(id) ON DELETE SET NULL,
    creado_en   TEXT NOT NULL DEFAULT (datetime('now')),
    actualizado_en TEXT NOT NULL DEFAULT (datetime('now'))
);

-- Datos iniciales de configuración
INSERT OR IGNORE INTO config(key, value) VALUES ('app_version', '1.0.0');
INSERT OR IGNORE INTO config(key, value) VALUES ('master_password_hash', '');
INSERT OR IGNORE INTO config(key, value) VALUES ('master_password_salt', '');
INSERT OR IGNORE INTO config(key, value) VALUES ('setup_done', 'false');
