# PigTracker

PigTracker is a JavaFX desktop prototype for importing, exploring, and visualizing Pig Performance Testing (PPT) data from Agrisys-style CSV exports.

## Features

- Login with username and password
- Import PPT data from CSV
- Export stored visit data to CSV
- Dashboard with KPIs and trend graphs
- Detailed views for animals and visits
- Filtering, searching, and sorting across herd data
- Report history per import
- Manual animal management, including stop/reactivate

## Tech Stack

- Java
- JavaFX
- Gradle
- MSSQL Server
- JUnit

## Project Structure

- `app/src/main/java/pigtracker/controller`  
  JavaFX controllers and UI flow
- `app/src/main/java/pigtracker/service`  
  Business logic, import pipeline, KPI/report generation
- `app/src/main/java/pigtracker/dao`  
  Database access layer
- `app/src/main/java/pigtracker/model`  
  Domain and view models
- `app/src/main/resources`  
  FXML views, components, styles, assets
- `database.sql`  
  Database schema setup
- `mockdata.sql`  
  Seed user data

## Setup

### Prerequisites

- JDK 25
- MSSQL Server
- Gradle wrapper included in repo

### Environment

Create `app/.env` based on `app/.env.example`:

```env
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=PigTracker;encrypt=true;trustServerCertificate=true
DB_USER=sa
DB_PASSWORD=your_password
```

### Database

Run:

- `database.sql` to create the database and tables
- `mockdata.sql` to insert a default user

## Run

```bash
./gradlew run
```

On Windows:

```powershell
.\gradlew.bat run
```

## Test

```bash
./gradlew test
```

## Notes

- The app is built around CSV import as the main source of truth.
- Imports create reports, visits, groups, and automatically sync animal data.
- This project is a prototype focused on making PPT data easier to understand than spreadsheet-based workflows.
