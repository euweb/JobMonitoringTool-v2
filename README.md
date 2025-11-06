# JobMonitoringTool-v2

Eine moderne Full-Stack-Anwendung zur Überwachung von DWH-Jobs und Job-Ketten.

## 🏗️ Technologie-Stack

- **Backend:** Spring Boot 3.2.1, Java 21, Spring Security, JWT
- **Frontend:** React 18, TypeScript, Material-UI, Vite
- **Database:** SQLite (mit Hibernate/JPA)
- **Build:** Maven mit Frontend-Integration
- **Security:** JWT-basierte Authentifizierung mit BCrypt

## 📋 Voraussetzungen

### Entwicklung:
- Java 21+ (OpenJDK oder Oracle JDK)
- Maven 3.8+
- Git

### Production:
- **Nur Java 21+ erforderlich** (Maven Build erstellt Self-Contained JAR)

## 🚀 Build und Deployment

### 1. Repository klonen
```bash
git clone https://github.com/euweb/JobMonitoringTool-v2.git
cd JobMonitoringTool-v2
```

### 2. Anwendung bauen
```bash
# Vollständiger Build (Frontend + Backend)
mvn clean package -DskipTests

# Mit Tests
mvn clean package
```

**Was passiert beim Build:**
- ✅ Node.js und npm werden automatisch heruntergeladen und installiert
- ✅ Frontend-Dependencies werden installiert (`npm ci`)
- ✅ React/TypeScript Frontend wird gebaut (`npm run build`)
- ✅ Frontend-Build wird in Backend Static Resources kopiert
- ✅ Spring Boot JAR wird mit integriertem Frontend erstellt

**Ergebnis:** `backend/target/job-monitoring-backend-1.0.0-SNAPSHOT.jar` (~73 MB)

### 3. Lokale Entwicklung

#### Entwicklungsserver starten:
```bash
cd backend
mvn spring-boot:run
```

Die Anwendung ist verfügbar unter: **http://localhost:8080**

#### Standard-Benutzer:
| Benutzername | Passwort | Rolle |
|--------------|----------|-------|
| `admin` | `admin123` | Administrator |
| `user` | `user123` | Benutzer |
| `testuser` | `testpassword` | Benutzer |

### 4. Production Deployment

#### 4.1 JAR-Datei auf Produktionsserver kopieren:
```bash
scp backend/target/job-monitoring-backend-1.0.0-SNAPSHOT.jar user@prod-server:/opt/job-monitor/
```

#### 4.2 Verzeichnisstruktur erstellen:
```bash
sudo mkdir -p /opt/job-monitor/{data,logs}
sudo useradd -r -s /bin/false jobmonitor
sudo chown -R jobmonitor:jobmonitor /opt/job-monitor
```

#### 4.3 Production-Konfiguration erstellen:
Erstellen Sie `/opt/job-monitor/application-prod.yml`:

```yaml
spring:
  profiles:
    active: prod
    
  # Database Configuration
  datasource:
    url: jdbc:sqlite:/opt/job-monitor/data/jobmonitor.db
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      
  # Security
  security:
    jwt:
      secret: ${JWT_SECRET:your-very-secure-production-secret-minimum-512-bits-for-hs512}
      access-token-validity: 3600000  # 1 Stunde
      refresh-token-validity: 86400000  # 24 Stunden

# Server Configuration
server:
  port: 8080
  shutdown: graceful

# Logging
logging:
  level:
    root: INFO
    com.company.jobmonitor: DEBUG
  file:
    name: /opt/job-monitor/logs/application.log

# Management Endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

#### 4.4 Anwendung starten:

**Manueller Start:**
```bash
cd /opt/job-monitor
java -Xmx2g -Xms1g -Dspring.profiles.active=prod -jar job-monitoring-backend-1.0.0-SNAPSHOT.jar
```

**Als Systemd Service:**

1. Service-Datei erstellen (`/etc/systemd/system/job-monitor.service`):
```ini
[Unit]
Description=Job Monitoring Tool
After=network.target

[Service]
Type=simple
User=jobmonitor
WorkingDirectory=/opt/job-monitor
ExecStart=/usr/bin/java -Xmx2g -Xms1g -Dspring.profiles.active=prod -jar job-monitoring-backend-1.0.0-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

Environment=JAVA_HOME=/usr/lib/jvm/java-21-openjdk
Environment=SPRING_PROFILES_ACTIVE=prod

[Install]
WantedBy=multi-user.target
```

2. Service aktivieren und starten:
```bash
sudo systemctl daemon-reload
sudo systemctl enable job-monitor
sudo systemctl start job-monitor
sudo systemctl status job-monitor
```

## 📊 Überwachung und Logs

### Application Health Check:
```bash
curl http://localhost:8080/actuator/health
```

### Logs anzeigen:
```bash
# Systemd Journal
sudo journalctl -u job-monitor -f

# Log-Datei
tail -f /opt/job-monitor/logs/application.log
```

### Metriken abrufen:
```bash
curl http://localhost:8080/actuator/metrics
```

## 🔧 Konfiguration

### Umgebungsvariablen:
```bash
export JWT_SECRET="your-production-jwt-secret-key"
export SPRING_PROFILES_ACTIVE="prod"
export SERVER_PORT="8080"
```

### Wichtige Konfigurationsparameter:

| Parameter | Beschreibung | Standard |
|-----------|--------------|----------|
| `server.port` | HTTP-Port | 8080 |
| `spring.datasource.url` | Datenbankpfad | `./data/jobmonitor.db` |
| `spring.security.jwt.secret` | JWT Secret Key | ⚠️ In Prod ändern! |
| `logging.level.root` | Log-Level | INFO |

## 🏗️ Entwicklung

### Projekt-Struktur:
```
JobMonitoringTool-v2/
├── backend/                 # Spring Boot Backend
│   ├── src/main/java/      # Java Source Code
│   ├── src/main/resources/ # Konfiguration & Static Files
│   └── pom.xml            # Maven Backend Configuration
├── frontend/               # React Frontend
│   ├── src/               # TypeScript/React Source
│   ├── dist/              # Build Output (automatisch)
│   ├── package.json       # Node.js Dependencies
│   └── pom.xml           # Maven Frontend Integration
└── pom.xml               # Root Maven Configuration
```

### Entwicklungs-Workflow:
```bash
# 1. Dependencies installieren
mvn clean compile

# 2. Backend starten (mit Auto-Reload)
cd backend && mvn spring-boot:run

# 3. Frontend separat entwickeln (optional)
cd frontend && npm run dev
```

### Build ohne Tests:
```bash
mvn clean package -DskipTests
```

### Nur Frontend bauen:
```bash
cd frontend && npm run build
```

## 📚 API Dokumentation

### Authentifizierung:
- **POST** `/api/auth/login` - Benutzer anmelden
- **POST** `/api/auth/register` - Neuen Benutzer registrieren

### Management:
- **GET** `/api/health` - Health Check
- **GET** `/actuator/health` - Detailed Health Information
- **GET** `/actuator/metrics` - Application Metrics

### Frontend:
- **GET** `/` - React Single Page Application
- **GET** `/assets/*` - Frontend Static Assets

## ⚠️ Produktions-Hinweise

1. **JWT Secret ändern:** Setzen Sie einen sicheren, langen JWT-Secret in der Produktion
2. **Datenbank-Backups:** Erstellen Sie regelmäßige Backups der SQLite-Datenbank
3. **Logs rotieren:** Konfigurieren Sie Log-Rotation für `/opt/job-monitor/logs/`
4. **Firewall:** Öffnen Sie nur Port 8080 für HTTP-Traffic
5. **SSL/TLS:** Verwenden Sie einen Reverse-Proxy (nginx/Apache) für HTTPS
6. **Monitoring:** Überwachen Sie die Anwendung über `/actuator/health`

## 🐛 Troubleshooting

### Häufige Probleme:

**Port bereits belegt:**
```bash
netstat -tulpn | grep :8080
kill <PID>
```

**Datenbank-Berechtigungen:**
```bash
sudo chown jobmonitor:jobmonitor /opt/job-monitor/data/jobmonitor.db
```

**Logs prüfen:**
```bash
sudo journalctl -u job-monitor --since "1 hour ago"
```

**Memory Issues:**
```bash
# JVM Memory erhöhen
java -Xmx4g -Xms2g -jar job-monitoring-backend-1.0.0-SNAPSHOT.jar
```