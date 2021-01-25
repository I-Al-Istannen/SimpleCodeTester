CREATE TABLE check_category
(
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255)
);

CREATE TABLE code_check
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    approved      boolean NOT NULL,
    check_type    VARCHAR(50),
    creation_time TIMESTAMP,
    name          VARCHAR(255),
    text          TEXT,
    update_time   TIMESTAMP,
    category_id   INTEGER,
    creator_id    VARCHAR(255)
);

CREATE TABLE user
(
    id            VARCHAR(255) PRIMARY KEY NOT NULL,
    enabled       BOOLEAN,
    name          VARCHAR(255),
    password_hash VARCHAR(255)
);

CREATE TABLE user_authorities
(
    user_id     VARCHAR(255) NOT NULL,
    authorities VARCHAR(255)
);
