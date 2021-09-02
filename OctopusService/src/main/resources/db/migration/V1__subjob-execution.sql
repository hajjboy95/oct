-- SUBJOB-HOST

CREATE TABLE IF NOT EXISTS subjob_execution
(
    id          SERIAL  NOT NULL,
    subjob_id   INTEGER NOT NULL,
    host_id     INTEGER NOT NULL,
    sent_at     TIMESTAMP DEFAULT NOW(),
    started_at  TIMESTAMP,
    finished_at TIMESTAMP,
    created_at  TIMESTAMP DEFAULT now(),
    updated_at  TIMESTAMP DEFAULT now()
);

ALTER TABLE subjob_execution
    ADD CONSTRAINT subjob_execution_pk PRIMARY KEY (id);
ALTER TABLE subjob_execution
    ADD CONSTRAINT fk_subjob_execution_subjob FOREIGN KEY (subjob_id) REFERENCES subjob (id);
ALTER TABLE subjob_execution
    ADD CONSTRAINT fk_subjob_execution_host FOREIGN KEY (host_id) REFERENCES host (id);

CREATE INDEX subjob_execution_subjob_id_index ON subjob_execution (subjob_id);
CREATE INDEX subjob_execution_host_id_index ON subjob_execution (host_id);


--CREATE UNIQUE INDEX subjob_uuid_index ON subjob (uuid);