-- Ketemu schema (PostgreSQL 16)

CREATE TABLE IF NOT EXISTS users (
    id            SERIAL PRIMARY KEY,
    nrp           VARCHAR(12)  UNIQUE NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(120) UNIQUE NOT NULL,
    password_hash VARCHAR(72)  NOT NULL,
    phone         VARCHAR(20),
    role          VARCHAR(10)  NOT NULL DEFAULT 'student' CHECK (role IN ('student','admin')),
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS categories (
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(40) UNIQUE NOT NULL,
    icon       VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS locations (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(80) UNIQUE NOT NULL,
    zone        VARCHAR(40),
    is_skk_post BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS items (
    id              SERIAL PRIMARY KEY,
    code            VARCHAR(10) UNIQUE,
    reporter_id     INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id     INT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    location_id     INT NOT NULL REFERENCES locations(id) ON DELETE RESTRICT,
    kind            VARCHAR(5)  NOT NULL CHECK (kind IN ('lost','found')),
    title           VARCHAR(80) NOT NULL,
    description     TEXT,
    location_detail VARCHAR(120),
    event_date      DATE NOT NULL,
    held_at_id      INT REFERENCES locations(id) ON DELETE SET NULL,
    status          VARCHAR(10) NOT NULL DEFAULT 'open' CHECK (status IN ('open','claimed','returned','closed')),
    created_at      TIMESTAMP DEFAULT now(),
    updated_at      TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS item_photos (
    id         SERIAL PRIMARY KEY,
    item_id    INT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    content    BYTEA NOT NULL,
    mime_type  VARCHAR(30) NOT NULL,
    sort_order SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS claims (
    id               SERIAL PRIMARY KEY,
    item_id          INT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    claimant_id      INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    proof            TEXT NOT NULL,
    meet_location_id INT REFERENCES locations(id) ON DELETE SET NULL,
    status           VARCHAR(10) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','approved','rejected','cancelled')),
    created_at       TIMESTAMP DEFAULT now(),
    decided_at       TIMESTAMP
);

-- One active (pending or approved) claim per person per item.
CREATE UNIQUE INDEX IF NOT EXISTS ux_claims_active
    ON claims (item_id, claimant_id) WHERE status IN ('pending','approved');

CREATE TABLE IF NOT EXISTS comments (
    id         SERIAL PRIMARY KEY,
    item_id    INT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    author_id  INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    body       TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now()
);

CREATE TABLE IF NOT EXISTS watchlist (
    user_id    INT REFERENCES users(id) ON DELETE CASCADE,
    item_id    INT REFERENCES items(id) ON DELETE CASCADE,
    note       VARCHAR(140),
    created_at TIMESTAMP DEFAULT now(),
    PRIMARY KEY (user_id, item_id)
);

CREATE TABLE IF NOT EXISTS notifications (
    id         SERIAL PRIMARY KEY,
    user_id    INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_id    INT REFERENCES items(id) ON DELETE CASCADE,
    message    VARCHAR(200) NOT NULL,
    is_read    BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT now()
);

CREATE INDEX IF NOT EXISTS ix_items_kind_status   ON items (kind, status);
CREATE INDEX IF NOT EXISTS ix_items_category      ON items (category_id);
CREATE INDEX IF NOT EXISTS ix_items_location      ON items (location_id);
CREATE INDEX IF NOT EXISTS ix_items_created       ON items (created_at DESC);
CREATE INDEX IF NOT EXISTS ix_item_photos_item    ON item_photos (item_id);
CREATE INDEX IF NOT EXISTS ix_claims_item         ON claims (item_id);
CREATE INDEX IF NOT EXISTS ix_comments_item       ON comments (item_id);
CREATE INDEX IF NOT EXISTS ix_notifications_user  ON notifications (user_id, is_read);
