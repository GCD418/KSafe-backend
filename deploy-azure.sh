#!/usr/bin/env bash
set -euo pipefail

SESSION_NAME="ksafe-backend"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "=========================================================="
echo "  Despliegue Automatizado - KSafeBackend (Ubuntu Azure)"
echo "=========================================================="

# 1. Instalar Java 21 y tmux si no están instalados
NEED_INSTALL=false
if ! command -v tmux >/dev/null 2>&1 || ! command -v curl >/dev/null 2>&1; then
    NEED_INSTALL=true
fi

if ! command -v java >/dev/null 2>&1; then
    NEED_INSTALL=true
else
    JAVA_VER=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | cut -d'.' -f1)
    if [ "${JAVA_VER:-0}" -lt 21 ]; then
        NEED_INSTALL=true
    fi
fi

if [ "$NEED_INSTALL" = true ]; then
    echo "[1/5] Instalando OpenJDK 21, tmux y curl..."
    sudo apt-get update -y
    sudo apt-get install -y openjdk-21-jdk tmux curl
else
    echo "[1/5] OpenJDK 21 y tmux ya están instalados."
fi

# 2. Verificar y cargar archivo .env
if [ ! -f "$PROJECT_DIR/.env" ]; then
    echo "ERROR: No se encontró el archivo .env en $PROJECT_DIR/.env"
    echo "Crea o copia tu archivo .env con DB_URL, DB_USERNAME y DB_PASSWORD antes de ejecutar este script."
    exit 1
fi

echo "[2/5] Cargando variables desde .env..."
set -a
# shellcheck disable=SC1091
source "$PROJECT_DIR/.env"
set +a

PORT="${SERVER_PORT:-8080}"

# 3. Abrir puerto en UFW (si el firewall local de Ubuntu está activo)
if command -v ufw >/dev/null 2>&1; then
    if sudo ufw status | grep -q "Status: active"; then
        echo "[3/5] Abriendo puerto ${PORT}/tcp en UFW..."
        sudo ufw allow "${PORT}/tcp" >/dev/null
    else
        echo "[3/5] UFW está inactivo, no requiere regla local."
    fi
else
    echo "[3/5] UFW no instalado, omitiendo configuración de firewall local."
fi

# 4. Compilar el ejecutable JAR con Gradle Wrapper
echo "[4/5] Compilando artefacto ejecutable (bootJar)..."
chmod +x ./gradlew
./gradlew bootJar --no-daemon

JAR_FILE=$(find "$PROJECT_DIR/build/libs" -maxdepth 1 -name "*.jar" ! -name "*-plain.jar" | head -n 1)
if [ -z "$JAR_FILE" ]; then
    echo "ERROR: No se encontró el archivo JAR compilado en build/libs/"
    exit 1
fi

# 5. Iniciar (o reiniciar) el servicio dentro de tmux
echo "[5/5] Iniciando servicio en sesión tmux '$SESSION_NAME'..."
if tmux has-session -t "$SESSION_NAME" 2>/dev/null; then
    echo "      Deteniendo sesión anterior '$SESSION_NAME'..."
    tmux kill-session -t "$SESSION_NAME"
fi

tmux new-session -d -s "$SESSION_NAME" \
    "cd '$PROJECT_DIR' && set -a && source '$PROJECT_DIR/.env' && set +a && exec java -jar '$JAR_FILE'"

sleep 3
PUBLIC_IP=$(curl -s --max-time 5 ifconfig.me || echo "<TU_IP_PUBLICA_AZURE>")

echo ""
echo "=========================================================="
echo "  Servicio desplegado y corriendo en tmux ($SESSION_NAME)"
echo "=========================================================="
echo "URL base para tu app Android:"
echo "  http://${PUBLIC_IP}:${PORT}/api/v1/auth/"
echo ""
echo "Endpoints disponibles:"
echo "  POST http://${PUBLIC_IP}:${PORT}/api/v1/auth/login"
echo "  POST http://${PUBLIC_IP}:${PORT}/api/v1/auth/biometric/initial-token"
echo "  POST http://${PUBLIC_IP}:${PORT}/api/v1/auth/biometric/refresh"
echo ""
echo "Comandos útiles de tmux:"
echo "  Ver logs en vivo:   tmux attach -t $SESSION_NAME"
echo "  Salir sin apagar:   Ctrl+B y luego presiona D"
echo "  Detener servicio:   tmux kill-session -t $SESSION_NAME"
echo "=========================================================="
