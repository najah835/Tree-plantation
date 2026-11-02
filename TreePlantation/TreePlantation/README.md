# Tree Plantation and Monitoring Management System

Java Swing + MySQL (JDBC) desktop app.

Screens: Dashboard (reports and statistics), Plantations, Condition monitoring,
Search and view, Tree species, Locations, People and organizations.

## 1. Set up the database (MySQL Workbench)
1. Start your MySQL server.
2. In MySQL Workbench: File > Open SQL Script > choose `database/schema.sql`.
3. Click the lightning-bolt icon (Execute) to run the whole script.
   This creates the `tree_plantation` database, the tables, and sample data.

## 2. Open the project in IntelliJ IDEA
1. File > Open > select this `TreePlantation` folder (the one containing `pom.xml`).
2. If IntelliJ asks, choose "Open as Project" and "Trust Project".
3. Wait for Maven to finish downloading dependencies (progress bar at the bottom).
4. File > Project Structure > Project: set the SDK to JDK 17 or newer
   (IntelliJ can download one: Add SDK > Download JDK).

## 3. Set your MySQL password
Edit `src/main/resources/db.properties`:

    url=jdbc:mysql://localhost:3306/tree_plantation?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    user=root
    password=YOUR_PASSWORD_HERE

## 4. Run
Open `src/main/java/treeplantation/Main.java` and click the green Run arrow next to `main`.

## Project layout
    database/schema.sql                      tables, keys, sample data
    src/main/resources/db.properties         connection settings
    src/main/java/treeplantation/Main.java   entry point
    .../db/Db.java                           JDBC connection + query helpers
    .../dao/*.java                           SQL for each table (insert, update, delete, search, reports)
    .../ui/*.java                            Swing screens and components

## Troubleshooting
- "Could not connect to the MySQL database": MySQL is not running, the schema script was not
  run, or the user/password in db.properties is wrong.
- "Access denied for user": fix `user` / `password` in db.properties.
- "Unknown database 'tree_plantation'": run database/schema.sql first.
- Red imports or "package does not exist": right-click `pom.xml` > Maven > Reload project.
- "invalid source release 17": set the project SDK to JDK 17 or newer (step 2.4).
