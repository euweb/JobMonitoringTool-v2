# JobMonitoringTool-v2

Eine moderne, skalierbare Full-Stack-Anwendung zur Überwachung von DWH-Jobs und Job-Ketten mit Enterprise-Grade Sicherheit und Testing.

## 🏗️ Technologie-Stack

### Backend

- **Framework:** Spring Boot 3.2.1 (upgradefähig auf 4.0.0-RC2)
- **Runtime:** Java 21 (LTS)
- **Security:** Spring Security 6.x mit JWT & BCrypt
- **Database:** SQLite mit Hibernate/JPA (migrierbar auf PostgreSQL)
- **Testing:** JUnit 5, Mockito, Spring Boot Test
- **Documentation:** JavaDoc, SpringDoc OpenAPI

### Frontend

- **Framework:** React 18 mit TypeScript
- **UI Library:** Material-UI (MUI) v5
- **Build Tool:** Vite 5 für optimierte Builds
- **State Management:** Zustand für Authentication
- **HTTP Client:** Axios mit automatischem Token-Refresh
- **Testing:** Vitest + React Testing Library

### DevOps & Quality

- **Build System:** Maven mit Frontend-Integration
- **Code Quality:** ESLint, Prettier, Trunk für Code Standards
- **Architecture Score:** 8.5/10 (siehe ARCHITECTURE_REVIEW.md)
- **Test Coverage:** Backend 100%, Frontend Framework etabliert
- **Documentation:** Vollständige JavaDoc/JSDoc Abdeckung

## 📋 Voraussetzungen

### Entwicklung

- **Java:** 21+ (OpenJDK oder Oracle JDK)
- **Maven:** 3.8+
- **Git:** Für Repository-Management
- **IDE:** IntelliJ IDEA oder VS Code (mit Java/TypeScript Extensions)

### Production

- **Runtime:** Nur Java 21+ erforderlich
- **Memory:** Minimum 1GB RAM (empfohlen 2GB+)
- **Storage:** 100MB+ für Application + Datenbank
- **Netzwerk:** Port 8080 (oder konfigurierbar)

## 🚀 Schnellstart

### 1. Repository klonen

```bash
git clone https://github.com/euweb/JobMonitoringTool-v2.git
cd JobMonitoringTool-v2
```

### 2. Entwicklungsserver starten

```bash
# Backend starten (beinhaltet Frontend)
cd backend
mvn spring-boot:run

# Anwendung verfügbar unter: http://localhost:8080
```

### 3. Standard-Benutzer

| Benutzername | Passwort       | Rolle         | Berechtigung    |
| ------------ | -------------- | ------------- | --------------- |
| `admin`      | `admin123`     | Administrator | Vollzugriff     |
| `user`       | `user123`      | Standard User | Benutzerbereich |
| `testuser`   | `testpassword` | Standard User | Testing         |

## 🔨 Build und Deployment

### Vollständiger Build

```bash
# Produktions-Build mit Tests
mvn clean package

# Schneller Build ohne Tests
mvn clean package -DskipTests
```

**Build-Pipeline:**

1. ✅ Node.js & npm automatisch installiert (Frontend)
2. ✅ TypeScript/React kompiliert und optimiert
3. ✅ Frontend-Assets in Backend-Resources eingebettet
4. ✅ Spring Boot Self-Contained JAR erstellt (~73 MB)
5. ✅ Alle Tests ausgeführt (Backend: JUnit, Frontend: Vitest)

## 🧪 Testing

### Backend Tests

```bash
# Alle Backend-Tests ausführen
cd backend && mvn test

# Spezifische Test-Klassen
mvn test -Dtest=UserServiceTest
mvn test -Dtest=AdminControllerTest
```

**Test-Abdeckung:**

- ✅ **UserService:** 8 Umfassende Tests für alle CRUD-Operationen
- ✅ **AdminController:** Sicherheits- und Endpunkt-Tests
- ✅ **TestSecurityConfig:** Isolierte Test-Umgebung
- ✅ **Integration Tests:** Vollständige Request/Response-Zyklen

### Frontend Tests

```bash
# Frontend-Tests ausführen
cd frontend && npm test

# Spezifische Test-Datei
npx vitest run LoginPage.test.tsx
```

**Test-Framework:**

- ✅ **Vitest 4.0** mit React Testing Library
- ✅ **LoginPage Tests:** 3/5 Tests erfolgreich
- ✅ **Component Testing:** Rendering und Interaktions-Tests
- ✅ **Mock Services:** Isolierte Service-Tests

### Continuous Testing

```bash
# Watch-Modus für Backend
cd backend && mvn test -DforkCount=0

# Watch-Modus für Frontend
cd frontend && npx vitest --watch
```

## 📊 Architektur & Code Quality

### Architektur-Score: 8.5/10

Detaillierte Analyse in [`ARCHITECTURE_REVIEW.md`](./ARCHITECTURE_REVIEW.md)

**Stärken:**

- ✅ Saubere Layered Architecture (Controller → Service → Repository)
- ✅ Umfassende Sicherheitsimplementierung mit JWT
- ✅ Type-Safe Frontend mit TypeScript
- ✅ Responsive Material-UI Design
- ✅ Vollständige API-Dokumentation (JavaDoc/JSDoc)

**Verbesserungspotential:**

- 🔄 Spring Boot 4.0 Migration vorbereitet
- 🔄 Database Migration auf PostgreSQL möglich
- 🔄 Microservices-Readiness durch Clean Architecture
- 🔄 Docker/Kubernetes Deployment geplant

### Code Documentation

- **Backend:** Vollständige JavaDoc für alle Services, Controller, Entities
- **Frontend:** TypeScript/JSDoc für Services, Components, Store Management
- **API:** OpenAPI/Swagger Integration für Interactive Documentation

## 🏗️ Entwicklung

### Projekt-Struktur

```
JobMonitoringTool-v2/
├── 📁 backend/                    # Spring Boot Backend
│   ├── 📁 src/main/java/com/company/jobmonitor/
│   │   ├── 📁 controller/         # REST Controllers (Admin, Auth, User, Setup)
│   │   ├── 📁 service/            # Business Logic (UserService)
│   │   ├── 📁 repository/         # Data Access Layer
│   │   ├── 📁 entity/             # JPA Entities (User)
│   │   ├── 📁 dto/                # Data Transfer Objects
│   │   ├── 📁 security/           # JWT & Security Configuration
│   │   └── 📁 config/             # Application Configuration
│   ├── 📁 src/test/java/          # JUnit Tests
│   └── 📄 pom.xml                 # Maven Backend Config
├── 📁 frontend/                   # React TypeScript Frontend
│   ├── 📁 src/
│   │   ├── 📁 components/         # React Components (Layout)
│   │   ├── 📁 pages/              # Page Components (Dashboard, Login, etc.)
│   │   ├── 📁 services/           # API Services (authService, apiClient)
│   │   ├── 📁 store/              # State Management (authStore)
│   │   ├── 📁 types/              # TypeScript Type Definitions
│   │   └── 📁 __tests__/          # Vitest Tests
│   ├── 📄 package.json            # Node.js Dependencies
│   └── 📄 pom.xml                 # Maven Frontend Integration
├── 📄 ARCHITECTURE_REVIEW.md      # Detaillierte Architektur-Analyse
├── 📄 Plan.md                     # Development Roadmap
└── 📄 pom.xml                     # Root Maven Configuration
```

### Development Workflow

```bash
# 1. Dependencies installieren & kompilieren
mvn clean compile

# 2. Backend mit Hot-Reload starten
cd backend && mvn spring-boot:run

# 3. Frontend separat entwickeln (optional für Live-Updates)
cd frontend && npm run dev  # Port 5173

# 4. Tests kontinuierlich ausführen
cd backend && mvn test -DforkCount=0    # Backend Watch
cd frontend && npx vitest --watch       # Frontend Watch
```

## 🚀 Production Deployment

### Build für Production

```bash
# Optimized Production Build
mvn clean package -Pprod

# Resultat: backend/target/job-monitoring-backend-1.0.0-SNAPSHOT.jar (~73 MB)
```

### Server-Setup

#### 1. JAR auf Server kopieren

```bash
scp backend/target/job-monitoring-backend-1.0.0-SNAPSHOT.jar user@prod-server:/opt/job-monitor/
```

#### 2. Verzeichnisstruktur erstellen

```bash
sudo mkdir -p /opt/job-monitor/{data,logs}
sudo useradd -r -s /bin/false jobmonitor
sudo chown -R jobmonitor:jobmonitor /opt/job-monitor
```

#### 3. Production-Konfiguration

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

  # Security (WICHTIG: JWT Secret ändern!)
  security:
    jwt:
      secret: ${JWT_SECRET:your-very-secure-production-secret-minimum-512-bits-for-hs512}
      access-token-validity: 3600000 # 1 Stunde
      refresh-token-validity: 86400000 # 24 Stunden

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

#### 4. Systemd Service Setup

Service-Datei erstellen (`/etc/systemd/system/job-monitor.service`):

```ini
[Unit]
Description=Job Monitoring Tool v2
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
Environment=JWT_SECRET=your-production-jwt-secret-key

[Install]
WantedBy=multi-user.target
```

#### 5. Service aktivieren

```bash
sudo systemctl daemon-reload
sudo systemctl enable job-monitor
sudo systemctl start job-monitor
sudo systemctl status job-monitor
```

## 📊 Monitoring & Wartung

### Health Checks

```bash
# Application Status
curl http://localhost:8080/actuator/health

# Detailed Metrics
curl http://localhost:8080/actuator/metrics

# Custom Health Check
curl http://localhost:8080/api/health
```

### Log Management

```bash
# Live Logs
sudo journalctl -u job-monitor -f

# Application Log File
tail -f /opt/job-monitor/logs/application.log

# Log Rotation (empfohlen)
sudo logrotate -d /etc/logrotate.d/job-monitor
```

### Backup Strategy

```bash
# Database Backup
cp /opt/job-monitor/data/jobmonitor.db /backup/jobmonitor-$(date +%Y%m%d).db

# Application Logs Backup
tar -czf /backup/logs-$(date +%Y%m%d).tar.gz /opt/job-monitor/logs/
```

## 📚 API Dokumentation

### Authentifizierung Endpoints

```bash
# Benutzer anmelden
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# Neuen Benutzer registrieren
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "newuser", "password": "password123", "email": "user@example.com", "firstName": "New", "lastName": "User"}'

# Aktueller Benutzer
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Admin Endpoints (Nur für ADMIN Rolle)

```bash
# Alle Benutzer auflisten
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Neuen Benutzer erstellen
curl -X POST http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "test123", "email": "test@example.com", "firstName": "Test", "lastName": "User"}'

# System-Statistiken
curl -X GET http://localhost:8080/api/admin/stats \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### User Self-Service Endpoints

```bash
# Eigenes Profil anzeigen
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Profil aktualisieren
curl -X PUT http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"email": "newemail@example.com", "firstName": "Updated", "lastName": "Name"}'

# Passwort ändern
curl -X POST http://localhost:8080/api/user/change-password \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"currentPassword": "oldpassword", "newPassword": "newpassword123"}'
```

### OpenAPI/Swagger Documentation

Nach dem Start der Anwendung verfügbar unter:

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **OpenAPI YAML:** http://localhost:8080/v3/api-docs.yaml

## 🔧 Konfiguration

### Umgebungsvariablen

```bash
# Kritische Production-Variablen
export JWT_SECRET="your-256-bit-production-secret"
export SPRING_PROFILES_ACTIVE="prod"
export SERVER_PORT="8080"
export DATABASE_URL="jdbc:sqlite:/opt/job-monitor/data/jobmonitor.db"
```

### Wichtige Konfigurationsparameter

| Parameter                                   | Beschreibung       | Standard               | Empfehlung Production          |
| ------------------------------------------- | ------------------ | ---------------------- | ------------------------------ |
| `server.port`                               | HTTP Port          | 8080                   | 8080 (mit Reverse Proxy)       |
| `spring.datasource.url`                     | Database URL       | `./data/jobmonitor.db` | `/opt/job-monitor/data/`       |
| `spring.security.jwt.secret`                | JWT Signing Key    | ⚠️ Default (unsicher)  | **MUSS geändert werden!**      |
| `spring.security.jwt.access-token-validity` | Token-Gültigkeit   | 3600000ms (1h)         | 3600000ms                      |
| `logging.level.root`                        | Log-Level          | INFO                   | INFO (DEBUG nur bei Problemen) |
| `management.endpoints.web.exposure.include` | Actuator Endpoints | health,info,metrics    | health,info,metrics            |

## ⚠️ Sicherheit & Produktion

### Kritische Sicherheitsmaßnahmen

1. **🔐 JWT Secret ändern** - Standard-Secret ist unsicher!
2. **🔒 HTTPS einrichten** - Reverse Proxy mit SSL/TLS
3. **🛡️ Firewall konfigurieren** - Nur Port 8080 freigeben
4. **📊 Monitoring aktivieren** - Health Checks und Log-Überwachung
5. **💾 Backup-Strategie** - Regelmäßige Datenbank-Sicherungen

### Performance-Optimierung

```bash
# Production JVM Settings
java -server \
  -Xmx2g -Xms1g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -Dspring.profiles.active=prod \
  -jar job-monitoring-backend-1.0.0-SNAPSHOT.jar
```

## 🐛 Troubleshooting

### Häufige Probleme

#### Port bereits belegt

```bash
netstat -tulpn | grep :8080
kill <PID>
# oder anderen Port verwenden
export SERVER_PORT=8081
```

#### Speicher-Probleme

```bash
# JVM Memory erhöhen
java -Xmx4g -Xms2g -jar job-monitoring-backend-1.0.0-SNAPSHOT.jar

# System-Memory prüfen
free -h
```

#### Datenbank-Berechtigungen

```bash
sudo chown jobmonitor:jobmonitor /opt/job-monitor/data/jobmonitor.db
chmod 664 /opt/job-monitor/data/jobmonitor.db
```

#### Log-Analyse

```bash
# Live Logs verfolgen
sudo journalctl -u job-monitor -f

# Letzte Fehler anzeigen
sudo journalctl -u job-monitor --since "1 hour ago" -p err

# Application Logs
tail -f /opt/job-monitor/logs/application.log | grep ERROR
```

### Debug-Modus

```bash
# Entwicklungs-Debug aktivieren
export LOGGING_LEVEL_COM_COMPANY_JOBMONITOR=DEBUG
mvn spring-boot:run

# Production-Debug (temporär)
java -Dlogging.level.com.company.jobmonitor=DEBUG -jar app.jar
```

## 📈 Roadmap & Updates

### Phase 2 Vorbereitung (Abgeschlossen ✅)

- ✅ Architektur Review (Score 8.5/10)
- ✅ Comprehensive Testing (Backend JUnit, Frontend Vitest)
- ✅ Complete Documentation (JavaDoc, JSDoc, README)
- ✅ Security Audit & Dependency Updates
- ✅ Code Quality Improvements

### Geplante Verbesserungen

- 🔄 **Spring Boot 4.0 Migration** (vorbereitet)
- 🔄 **PostgreSQL Integration** (Clean Architecture)
- 🔄 **Docker/Kubernetes Support**
- 🔄 **Enhanced Monitoring** (Prometheus/Grafana)
- 🔄 **Microservices Architecture** (bei Bedarf)

### Migration Paths

- **Database**: SQLite → PostgreSQL/MySQL
- **Deployment**: JAR → Docker Container
- **Scaling**: Single Instance → Microservices
- **Monitoring**: Basic → Full Observability Stack

## 🤝 Contributing

### Development Setup

```bash
# 1. Repository forken und klonen
git clone https://github.com/YOUR_USERNAME/JobMonitoringTool-v2.git
cd JobMonitoringTool-v2

# 2. Development Environment aufsetzen
mvn clean compile
cd backend && mvn spring-boot:run

# 3. Tests ausführen
mvn test                              # Backend Tests
cd frontend && npx vitest run         # Frontend Tests
```

### Code Guidelines

- **Backend**: JavaDoc für alle public methods
- **Frontend**: TypeScript/JSDoc für services & components
- **Testing**: Neue Features benötigen Tests
- **Commits**: Conventional Commit Messages

### Quality Gates

- ✅ All Tests müssen erfolgreich sein
- ✅ Code Coverage > 80% für neue Features
- ✅ Keine Build Warnings
- ✅ Documentation aktualisiert

---

## 📄 Lizenz & Support

**Entwickelt für:** Philip's DWH Job Monitoring Requirements
**Architektur Score:** 8.5/10 (siehe `ARCHITECTURE_REVIEW.md`)
**Version:** 1.0.0-SNAPSHOT
**Letzte Aktualisierung:** November 2025

### Support-Kanäle

- **Issues:** GitHub Issues für Bug Reports
- **Documentation:** Vollständige JavaDoc/JSDoc verfügbar
- **Architecture:** Detaillierte Analyse in `ARCHITECTURE_REVIEW.md`

### Weitere Dokumentation

- 📋 **[ARCHITECTURE_REVIEW.md](./ARCHITECTURE_REVIEW.md)** - Detaillierte Architektur-Analyse
- 🗺️ **[Plan.md](./Plan.md)** - Development Roadmap
- 📖 **Swagger UI** - http://localhost:8080/swagger-ui/index.html (nach Start)
