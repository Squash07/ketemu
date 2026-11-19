# Ketemu

**Live site:** https://ketemu-app-c0e20.containers.snapdeploy.app/

**Source:** https://github.com/Squash07/ketemu

Ketemu (Indonesian for "found it", and also "we meet") is a lost-property office for ITS on the web. Students post what they lost or found on campus, file claims with proof of ownership, and hand items over at an SKK (campus security) post. The finder approves a claim, both sides agree on a pickup post, and the item gets stamped returned.

Ketemu is a student project for Web Programming at ITS. It is not an official ITS service.

![Home page](docs/screenshots/01-home-board.png)

## Features

Eight CRUD features, each with create, read, update and delete:

| # | Feature | Create | Read | Update | Delete |
|---|---|---|---|---|---|
| 1 | Accounts and profile | Register | Profile page, mini-profile on items | Edit details, change password | Delete account (asks for the password) |
| 2 | Item reports | Post a lost or found item | Board with filters, search and paging; item page | Edit report, close, reopen, mark recovered | Delete report |
| 3 | Item photos | Upload up to 3 photos with preview | Gallery and thumbnails, `/photo/{id}` | Replace a photo, change the cover | Remove a photo |
| 4 | Claims | File a claim with proof and a pickup post | My claims, claim ticket, finder's incoming claims | Edit proof, approve, reject, mark returned | Cancel a claim |
| 5 | Comments and sightings | Comment on an item | Thread on the item page | Edit your comment | Delete your comment (admins can delete any) |
| 6 | Watchlist | Save an item | Watchlist page | Private note per item | Remove from watchlist |
| 7 | Categories (admin) | Add a category with an icon | Category table | Rename, change the icon | Delete (blocked while items use it) |
| 8 | Campus locations (admin) | Add a location | Location table | Edit name, zone, SKK post flag | Delete (blocked while items use it) |

Extras: notifications with an unread count (mark read, mark all read, delete, clear all), user management for admins (search, change role, remove), and a desk ledger that shows items by status and the newest reports.

## Tech stack

- Java 17, Jakarta EE 10 (Servlet 6.0, JSP 3.1, JSTL 3.0)
- Apache Tomcat 10.1
- PostgreSQL 16 through JDBC, HikariCP connection pool
- jBCrypt for password hashing
- Plain CSS with custom properties and a small vanilla JavaScript file
- Maven, Docker; hosted on SnapDeploy (Docker container) and Neon (PostgreSQL)

The app follows MVC: servlets are controllers, DAOs talk to the database with `PreparedStatement` only, and JSPs are views written with JSTL and EL (scripting is disabled in `web.xml`).

## Run it locally

### With Docker Compose (easiest)

```bash
cp .env.example .env        # adjust values if you like
docker compose up --build
```

Open http://localhost:8080. On the first start the app creates the tables and loads the sample data.

### Without Docker

You need JDK 17+, Maven 3.9+, PostgreSQL 16 and Tomcat 10.1.

```bash
createdb ketemu                          # or create the database in pgAdmin
export DB_URL=jdbc:postgresql://localhost:5432/ketemu
export DB_USER=postgres
export DB_PASSWORD=your-local-password
mvn package                              # builds target/ROOT.war
cp target/ROOT.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/catalina.sh run
```

## Environment variables

| Name | Required | Description |
|---|---|---|
| `DB_URL` | yes | JDBC URL, for example `jdbc:postgresql://host/db?sslmode=require`. A `postgresql://user:pass@host/db` URL also works. |
| `DB_USER` | yes | Database user |
| `DB_PASSWORD` | yes | Database password |
| `DEMO_PASSWORD` | no | Password given to the seeded demo accounts on first start (default `ketemu123`) |
| `PORT` | no | HTTP port. Most hosting platforms set it automatically; the default is 8080. |
| `DB_POOL_SIZE` | no | Maximum connections in the pool (default 5) |

## Demo accounts

All demo accounts use the password from `DEMO_PASSWORD` (default `ketemu123`).

| Role | Email | Good for trying |
|---|---|---|
| Admin (SKK desk) | `admin@ketemu.test` | Desk ledger, categories, locations, users |
| Student | `rani@ketemu.test` | Finder of the grey umbrella and the earbuds case |
| Student | `ayu@ketemu.test` | Has a pending claim on the Casio calculator |
| Student | `bima@ketemu.test` | Approved claim on the earbuds case, finder of the calculator |

## Deployment

The live site runs as a Docker container on SnapDeploy's free tier, with the database on Neon's free PostgreSQL. The app is built from the `Dockerfile` in this repository, and the database connection is set with the environment variables above. On the first start, the app creates the tables and loads the sample data by itself.

Two things to know about the free tier:

- Free SnapDeploy containers sleep after about 15 idle minutes, and the first visit afterwards takes about a minute to wake up. **Open the site 2 minutes before a demo.**
- The container's filesystem is not permanent, so photos are stored in PostgreSQL as `BYTEA` (max 1 MB each, 3 per item) and served with `ETag` and `Cache-Control` headers.

## Project structure

```
ketemu/
├─ pom.xml, Dockerfile, docker-entrypoint.sh, docker-compose.yml, .env.example
├─ docs/                     screenshots
└─ src/main/
   ├─ java/id/ketemu/
   │  ├─ config/   Db (HikariCP from env vars), AppInit (creates schema and seed data)
   │  ├─ model/    User, Category, Location, Item, ItemPhoto, Claim, Comment, WatchlistEntry, Notification
   │  ├─ dao/      one DAO per table, PreparedStatement only
   │  ├─ service/  ItemService, ClaimService, NotificationService (ownership and status rules)
   │  ├─ web/      servlets per feature, PhotoServlet
   │  ├─ filter/   EncodingFilter, CsrfFilter, AuthFilter, AdminFilter
   │  └─ util/     Flash, Validator, Paging, Csrf, Uploads, Fmt, Icons
   ├─ resources/db/  schema.sql, seed.sql
   └─ webapp/
      ├─ WEB-INF/  web.xml, tags/ (layout, tag, stamp, penfield, ticket, icon, ...), views/
      └─ assets/   css/, js/app.js, img/favicon.svg
```

## Screenshots

More screenshots of every page are in [docs/screenshots](docs/screenshots).

| Item page | Claim ticket |
|---|---|
| ![Item page](docs/screenshots/02-item-detail.png) | ![Claim ticket](docs/screenshots/09-claim-ticket.png) |
