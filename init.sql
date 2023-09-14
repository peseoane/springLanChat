DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;

CREATE TABLE users
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    avatar   VARCHAR(255)        NOT NULL,
    pin      varchar(4)          NOT NULL
);

INSERT INTO users (username, avatar, pin) VALUES ('admin', 'avatar/admin.jpg', '666');

CREATE TABLE messages
(
    id          SERIAL PRIMARY KEY,
    sender_id   INTEGER                 NOT NULL REFERENCES users (id),
    message     TEXT                    NOT NULL,
    timestamp   TIMESTAMP DEFAULT NOW() NOT NULL,
    is_response INT references messages (id)
);