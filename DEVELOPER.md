# Developer Documentation - JobMonitoringTool-v2

Eine umfassende Anleitung für Entwickler zur lokalen Entwicklung und zum Debugging der JobMonitoringTool-v2 Anwendung.

## 🛠️ Entwicklungsumgebung Setup

### Voraussetzungen

- **Java 21+** (OpenJDK oder Oracle JDK)
- **Maven 3.8+**
- **Node.js 18+** (wird automatisch heruntergeladen, aber für IDE-Unterstützung empfohlen)
- **Git**
- **IDE**: IntelliJ IDEA, VS Code oder Eclipse

### Repository Setup

```bash
git clone https://github.com/euweb/JobMonitoringTool-v2.git
cd JobMonitoringTool-v2
```

## 🔧 Entwicklungsmodus

### Backend Development Server

Das Backend (Spring Boot) in Entwicklungsmodus starten:

```bash
cd backend
mvn spring-boot:run
```

oder mit dev-Profil

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Backend läuft auf:** http://localhost:8080

**Features im Dev-Modus:**

- ✅ Hot Reload für Java-Klassen (mit Spring Boot DevTools)
- ✅ Automatische Datenbankinitialisierung
- ✅ Debug-Logging aktiviert
- ✅ H2 Console verfügbar (falls konfiguriert)

### Frontend Development Server

Das Frontend (React/Vite) in Entwicklungsmodus starten:

```bash
cd frontend
npm install  # Nur beim ersten Mal oder bei neuen Dependencies
npm run dev
```

falls der Build fehl schlägt:

```bash
npm install patch-package --save-dev
npm run build
```

**Frontend läuft auf:** http://localhost:5173

**Features im Dev-Modus:**

- ✅ **Hot Module Replacement (HMR)** - Änderungen werden sofort im Browser sichtbar
- ✅ **TypeScript Checking** in Echtzeit
- ✅ **ESLint Integration** für Code-Qualität
- ✅ **Proxy zu Backend** für API-Calls (automatisch konfiguriert)

### Mail-Versand testen

```bash
docker run -d -p 2525:1025 -p 8025:8025 mailhog/mailhog
```

Im Browser [Mailhog](http://localhost:8025/#) aufrufen

### 🔥 Live Development Workflow

1. **Terminal 1 - Backend:**

   ```bash
   cd backend && mvn spring-boot:run
   ```

2. **Terminal 2 - Frontend:**

   ```bash
   cd frontend && npm run dev
   ```

3. **Browser öffnen:** http://localhost:5173

4. **Entwickeln:**
   - Bearbeiten Sie Frontend-Dateien in `frontend/src/`
   - Änderungen erscheinen **sofort** im Browser (ohne Reload)
   - Bearbeiten Sie Backend-Dateien in `backend/src/main/java/`
   - Spring Boot DevTools lädt Klassen automatisch neu

## 📁 Projektstruktur

```
JobMonitoringTool-v2/
├── backend/                    # Spring Boot Backend
│   ├── src/main/java/         # Java Source Code
│   │   └── com/company/jobmonitor/
│   │       ├── controller/    # REST Controllers
│   │       ├── service/       # Business Logic
│   │       ├── repository/    # JPA Repositories
│   │       ├── entity/        # JPA Entities
│   │       ├── dto/           # Data Transfer Objects
│   │       ├── config/        # Spring Configuration
│   │       └── security/      # Security Configuration
│   ├── src/main/resources/    # Resources
│   │   ├── application.yml    # Spring Configuration
│   │   ├── db/migration/      # Flyway Database Migrations
│   │   └── static/            # Static Web Assets (nach Build)
│   └── src/test/             # Backend Tests
├── frontend/                  # React/TypeScript Frontend
│   ├── src/
│   │   ├── components/       # React Components
│   │   ├── pages/           # Page Components
│   │   ├── services/        # API Service Layer
│   │   ├── store/           # State Management
│   │   ├── types/           # TypeScript Definitions
│   │   └── theme.ts         # Material-UI Theme
│   ├── package.json
│   └── vite.config.ts       # Vite Configuration
└── pom.xml                  # Root Maven Configuration
```

## 🔄 API Integration

### Frontend zu Backend Kommunikation

Das Frontend kommuniziert über REST API mit dem Backend:

**Entwicklung:**

- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Proxy in `vite.config.ts` leitet API-Calls an Backend weiter

**Produktion:**

- Beide unter http://localhost:8080 (Frontend als Static Assets)

### API Endpoints

```typescript
// Beispiel API Service (frontend/src/services/apiClient.ts)
const API_BASE_URL = "/api";

// Authentication
POST / api / auth / login;
POST / api / auth / register;
GET / api / auth / me;

// Jobs
GET / api / jobs;
POST / api / jobs;
PUT / api / jobs / { id };
DELETE / api / jobs / { id };
```

## 🧪 Testing

### Backend Tests

```bash
cd backend
mvn test                    # Alle Tests
mvn test -Dtest=ClassName   # Spezifische Test-Klasse
```

### Frontend Tests

```bash
cd frontend
npm test                    # Jest Tests
npm run test:watch          # Watch Mode
npm run test:coverage       # Mit Coverage Report
```

### Integration Tests

```bash
# Vollständiger Integration Test
mvn clean verify
```

## 🐛 Debugging

### Backend Debugging

**IntelliJ IDEA:**

1. Debug Configuration erstellen (Spring Boot Application)
2. Main Class: `com.company.jobmonitor.JobMonitorApplication`
3. Debug starten
4. Breakpoints in Java-Code setzen

**VS Code:**

1. Launch Configuration in `.vscode/launch.json`:

```json
{
  "type": "java",
  "name": "Debug JobMonitor",
  "request": "launch",
  "mainClass": "com.company.jobmonitor.JobMonitorApplication",
  "projectName": "job-monitoring-backend"
}
```

### Frontend Debugging

**Browser DevTools:**

- Source Maps sind aktiviert
- TypeScript Source Code ist verfügbar
- React DevTools Browser Extension empfohlen

**VS Code:**

- TypeScript Fehler werden inline angezeigt
- ESLint Integration für Code-Qualität

## 📊 Database Development

### SQLite Database

**Location:** `backend/data/jobmonitor.db`

**Database Schema:**

- Flyway Migrations in `backend/src/main/resources/db/migration/`
- V1\_\_Initial_Schema.sql - Grundschema
- V2\_\_Sample_Data.sql - Testdaten

**Database Access:**

```bash
# SQLite CLI (falls installiert)
sqlite3 backend/data/jobmonitor.db

# Oder verwenden Sie ein GUI-Tool wie DB Browser for SQLite
```

### Schema Changes

1. Neue Migration erstellen: `V{version}__{description}.sql`
2. Backend neu starten - Flyway führt Migration automatisch aus
3. Entity-Klassen entsprechend anpassen

## 🔧 Konfiguration

### Backend Configuration

**Development:** `backend/src/main/resources/application.yml`
**Production:** `application-prod.yml` (Root-Verzeichnis)

```yaml
# Entwicklungsumgebung
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:sqlite:data/jobmonitor.db
  jpa:
    hibernate:
      ddl-auto: validate # Flyway übernimmt Schema-Management
    show-sql: true # SQL Logging für Development
```

### Frontend Configuration

**Vite Config:** `frontend/vite.config.ts`

```typescript
export default defineConfig({
  server: {
    proxy: {
      "/api": "http://localhost:8080", // Proxy für API-Calls
    },
  },
});
```

## 🚀 Build & Deployment

### Development Build

```bash
# Nur Backend
cd backend && mvn spring-boot:run

# Nur Frontend
cd frontend && npm run dev

# Beide gleichzeitig (separate Terminals)
```

### Production Build

```bash
# Vollständiger Build (Frontend wird automatisch in Backend integriert)
mvn clean package -DskipTests

# Ausführen
java -jar backend/target/job-monitoring-backend-1.0.0.jar
```

## 📝 Code Style & Best Practices

### Backend (Java)

- **Naming:** PascalCase für Klassen, camelCase für Methoden/Variablen
- **Packages:** Klare Trennung nach Layern (controller, service, repository)
- **DTOs:** Verwenden Sie DTOs für API-Requests/Responses
- **Validation:** Bean Validation (@Valid, @NotNull, etc.)

### Frontend (TypeScript/React)

- **Components:** Funktionale Components mit Hooks
- **Naming:** PascalCase für Components, camelCase für Functions
- **Types:** Starke Typisierung, Interface Definitions in `types/`
- **State:** Zustand Management mit Zustand (oder Redux bei Bedarf)

### Git Workflow

```bash
# Feature Branch erstellen
git checkout -b feature/your-feature-name

# Entwickeln und committen
git add .
git commit -m "feat: add new feature"

# Push und Pull Request
git push origin feature/your-feature-name

# Tags erstellen
git tag v1.1.1
git push origin v1.1.1
```

## 🔍 Troubleshooting

### Häufige Probleme

**Frontend startet nicht:**

```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

**Backend startet nicht:**

- Java Version prüfen: `java -version`
- Maven Version prüfen: `mvn -version`
- Port 8080 bereits belegt: `lsof -i :8080`

**Database Probleme:**

- SQLite Datei löschen: `rm backend/data/jobmonitor.db`
- Flyway Migrations prüfen: `mvn flyway:info`

**CORS Probleme:**

- CORS Konfiguration in `SecurityConfig.java` prüfen
- Proxy Konfiguration in `vite.config.ts` prüfen

### Performance Monitoring

**Backend:**

- Spring Boot Actuator Endpoints: http://localhost:8080/actuator
- Health Check: http://localhost:8080/actuator/health

**Frontend:**

- Vite Bundle Analyzer: `npm run build -- --analyze`
- React DevTools Performance Tab

## 📚 Zusätzliche Ressourcen

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev)
- [Vite Documentation](https://vitejs.dev)
- [Material-UI Documentation](https://mui.com)
- [TypeScript Handbook](https://www.typescriptlang.org/docs)

---

**Happy Coding! 🎉**

Bei Fragen oder Problemen erstellen Sie bitte ein Issue im Repository oder kontaktieren Sie das Entwicklungsteam.
