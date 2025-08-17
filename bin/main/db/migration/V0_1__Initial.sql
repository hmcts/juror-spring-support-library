CREATE SCHEMA IF NOT EXISTS support_library;

CREATE SEQUENCE support_library.users_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 10
    CACHE 1
    NO CYCLE;



CREATE TABLE support_library."permission"
(
    "name" varchar(255) NOT NULL,
    CONSTRAINT permission_pkey PRIMARY KEY (name)
);

CREATE TABLE support_library."role"
(
    "name" varchar(255) NOT NULL,
    CONSTRAINT role_pkey PRIMARY KEY (name)
);


-- support_library.users definition

-- Drop table

-- DROP TABLE support_library.users;

CREATE TABLE support_library.users
(
    account_non_expired     bool      NOT NULL,
    account_non_locked      bool      NOT NULL,
    credentials_non_expired bool      NOT NULL,
    enabled                 bool      NOT NULL,
    id                      bigserial NOT NULL,
    email                   varchar(255) NULL,
    firstname               varchar(255) NULL,
    lastname                varchar(255) NULL,
    "password"              varchar(255) NULL,
    CONSTRAINT users_email_key UNIQUE (email),
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

-- support_library.role_inherited_roles definition

-- Drop table

-- DROP TABLE support_library.role_inherited_roles;

CREATE TABLE support_library.role_inherited_roles
(
    inherited_roles_name varchar(255) NOT NULL,
    role_name            varchar(255) NOT NULL,
    CONSTRAINT role_inherited_roles_pkey PRIMARY KEY (inherited_roles_name, role_name),
    CONSTRAINT fkfufff8hqmybtq3kcl6226n9ak FOREIGN KEY (inherited_roles_name) REFERENCES support_library."role" ("name"),
    CONSTRAINT fkkt6cb6mtv6tmq750n1lxequ38 FOREIGN KEY (role_name) REFERENCES support_library."role" ("name")
);


-- support_library.role_permissions definition

-- Drop table

-- DROP TABLE support_library.role_permissions;

CREATE TABLE support_library.role_permissions
(
    permissions_name varchar(255) NOT NULL,
    role_name        varchar(255) NOT NULL,
    CONSTRAINT role_permissions_pkey PRIMARY KEY (permissions_name, role_name),
    CONSTRAINT fkcppvu8fk24eqqn6q4hws7ajux FOREIGN KEY (role_name) REFERENCES support_library."role" ("name"),
    CONSTRAINT fkf5aljih4mxtdgalvr7xvngfn1 FOREIGN KEY (permissions_name) REFERENCES support_library."permission" ("name")
);


-- support_library.users_permissions definition

-- Drop table

-- DROP TABLE support_library.users_permissions;

CREATE TABLE support_library.users_permissions
(
    user_id          int8         NOT NULL,
    permissions_name varchar(255) NOT NULL,
    CONSTRAINT users_permissions_pkey PRIMARY KEY (user_id, permissions_name),
    CONSTRAINT fk69cfatgplsb0u6rxkfn14fv5b FOREIGN KEY (user_id) REFERENCES support_library.users (id),
    CONSTRAINT fkbnmkkji6sjtiykk1gf28aacft FOREIGN KEY (permissions_name) REFERENCES support_library."permission" ("name")
);


-- support_library.users_roles definition

-- Drop table

-- DROP TABLE support_library.users_roles;

CREATE TABLE support_library.users_roles
(
    user_id    int8         NOT NULL,
    roles_name varchar(255) NOT NULL,
    CONSTRAINT users_roles_pkey PRIMARY KEY (user_id, roles_name),
    CONSTRAINT fk2o0jvgh89lemvvo17cbqvdxaa FOREIGN KEY (user_id) REFERENCES support_library.users (id),
    CONSTRAINT fk7tacasmhqivyolfjjxseeha5c FOREIGN KEY (roles_name) REFERENCES support_library."role" ("name")
);


INSERT INTO support_library."permission" ("name")
VALUES ('user::create'),
       ('user::view::self'),
       ('user::view::all'),
       ('user::permissions::assign'),
       ('user::delete'),
       ('user::password::reset::self'),
       ('user::password::reset::all');

INSERT INTO support_library."role" ("name")
VALUES ('USER'),
       ('ADMIN'),
       ('EXTERNAL_API');

INSERT INTO support_library.role_inherited_roles(role_name, inherited_roles_name)
VALUES ('ADMIN', 'USER');

INSERT INTO support_library.role_permissions (permissions_name, role_name)
VALUES ('user::password::reset::self', 'USER'),
       ('user::view::self', 'USER'),
       ('user::password::reset::all', 'ADMIN'),
       ('user::permissions::assign', 'ADMIN'),
       ('user::view::all', 'ADMIN'),
       ('user::create', 'ADMIN'),
       ('user::delete', 'ADMIN');

INSERT INTO support_library.users
(account_non_expired, account_non_locked, credentials_non_expired, enabled, email, firstname, lastname, "password")
VALUES (true, true, true, true, 'admin@scheduler.cgi.com', 'Admin',
        'Admin',
        '$2a$10$H5xgNpQ8ZWrlkrTrzofTJep21hwn4EHw.bPEOcn.T0WkQpHsDC3mm');

INSERT INTO support_library.users_roles
    (user_id, roles_name)
VALUES ((SELECT ID from support_library.users WHERE email = 'admin@scheduler.cgi.com'), 'ADMIN');