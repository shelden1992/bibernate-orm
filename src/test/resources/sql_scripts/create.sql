DROP SCHEMA IF EXISTS public CASCADE;

CREATE SCHEMA IF NOT EXISTS public;

CREATE TABLE IF NOT EXISTS public.person
(
    id         SERIAL       NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    email      VARCHAR(255),
    address    VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE (email)
);

-- CREATE TABLE public.note
-- (
--     id        SERIAL NOT NULL,
--     body      TEXT,
--     person_id BIGINT,
--     PRIMARY KEY (id),
--     FOREIGN KEY (person_id) REFERENCES public.person (id)
-- );