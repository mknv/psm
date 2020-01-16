--TABLES

CREATE TABLE roles (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    CONSTRAINT roles_pk PRIMARY KEY (id)
);

CREATE UNIQUE INDEX roles_unique_idx ON roles (lower(name));

CREATE TABLE users (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    CONSTRAINT users_pk PRIMARY KEY (id)
);

CREATE UNIQUE INDEX users_unique_idx ON users (lower(name));

CREATE TABLE users_roles (
    role_id integer NOT NULL,
    user_id integer NOT NULL,
    CONSTRAINT users_roles_pk PRIMARY KEY (role_id, user_id)
);

CREATE TABLE groups (
    id integer NOT NULL,
    user_id integer NOT NULL,
    name character varying(255) NOT NULL,
    CONSTRAINT groups_pk PRIMARY KEY (id)
);

CREATE UNIQUE INDEX groups_unique_idx ON groups (user_id, lower(name));

CREATE TABLE entries (
    id integer NOT NULL,
    group_id integer,
    user_id integer NOT NULL,
    name character varying(255) NOT NULL,
    login character varying(255),
    password character varying(1000),
    email character varying(255),
    phone character varying(50),
    description character varying(1000),
    expired_date date,
    CONSTRAINT entries_pk PRIMARY KEY (id)
);

--FOREIGN KEYS

ALTER TABLE users_roles
    ADD CONSTRAINT users_roles_roles_fk FOREIGN KEY (role_id) REFERENCES roles(id);

ALTER TABLE users_roles
    ADD CONSTRAINT users_roles_users_fk FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE groups
    ADD CONSTRAINT groups_users_fk FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE entries
    ADD CONSTRAINT entries_groups_fk FOREIGN KEY (group_id) REFERENCES groups(id);

ALTER TABLE entries
    ADD CONSTRAINT entries_users_fk FOREIGN KEY (user_id) REFERENCES users(id);

--SEQUENCES

CREATE SEQUENCE users_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE groups_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE entries_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
