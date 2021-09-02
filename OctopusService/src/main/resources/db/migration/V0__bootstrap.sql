--  ACCOUNT

CREATE TABLE IF NOT EXISTS account
(
    id       SERIAL              NOT NULL,
    username VARCHAR(50) UNIQUE  NOT NULL,
    email    VARCHAR(100) UNIQUE NOT NULL,
    password TEXT                NOT NULL
);

ALTER TABLE account
    ADD CONSTRAINT accounts_pk primary key (id);
ALTER TABLE account
    OWNER TO "pg-user";

CREATE UNIQUE INDEX accounts_username_index ON account (username);
CREATE UNIQUE INDEX accounts_email_index ON account (email);

-- HOST

CREATE TABLE IF NOT EXISTS host
(
    id          SERIAL             NOT NULL,
    mac_id      VARCHAR(64) UNIQUE NOT NULL,
    account_id  INTEGER            NOT NULL
);

ALTER TABLE host
    ADD CONSTRAINT hosts_pk PRIMARY KEY (id),
    ADD CONSTRAINT fk_host_account FOREIGN KEY (account_id) REFERENCES account (id);

ALTER TABLE host
    OWNER TO "pg-user";

CREATE UNIQUE INDEX hosts_mac_id_uindex
    ON host (mac_id);

-- JOBTYPE

CREATE TYPE JOBTYPE AS ENUM ('NONE', 'WEB_SCRAPING', 'VIDEO_TRANSCODE');

-- JOB

CREATE TABLE IF NOT EXISTS job
(
    id          SERIAL             NOT NULL,
    job_type    JOBTYPE            NOT NULL,
    created_at  TIMESTAMP DEFAULT now(),
    updated_at  TIMESTAMP DEFAULT now(),
    started_at  TIMESTAMP,
    finished_at TIMESTAMP,
    uuid        VARCHAR(36) UNIQUE NOT NULL,
    account_id  INTEGER            NOT NULL
);

ALTER TABLE job
    ADD CONSTRAINT job_pk PRIMARY KEY (id);
ALTER TABLE job
    ADD CONSTRAINT fk_account_job FOREIGN KEY (account_id) REFERENCES account (id);

ALTER TABLE job
    OWNER TO "pg-user";

-- SUBJOB

CREATE TYPE PAYMENT_STATUS AS ENUM ('NOT_PAID', 'PAID', 'PROCESSING', 'ABORTED');

CREATE TABLE IF NOT EXISTS subjob
(
    id              SERIAL             NOT NULL,
    job_id          INTEGER            NOT NULL,
    uuid            VARCHAR(36) UNIQUE NOT NULL,
    sequence_number INTEGER            NOT NULL,
    created_at      TIMESTAMP      DEFAULT now(),
    updated_at      TIMESTAMP      DEFAULT now(),
    started_at      TIMESTAMP,
    finished_at     TIMESTAMP,
    retries         INTEGER        DEFAULT 0,
    payment_status  PAYMENT_STATUS DEFAULT 'NOT_PAID'
);

ALTER TABLE subjob
    ADD CONSTRAINT subjob_pk PRIMARY KEY (id);
ALTER TABLE subjob
    ADD CONSTRAINT fk_subjob_job FOREIGN KEY (job_id) REFERENCES job (id);
ALTER TABLE subjob
    OWNER TO "pg-user";
