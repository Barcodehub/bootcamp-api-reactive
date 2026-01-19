CREATE TABLE IF NOT EXISTS bootcamp (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(90) NOT NULL,
    launch_date DATE NOT NULL,
    duration INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS bootcamp_capacity (
    id BIGSERIAL PRIMARY KEY,
    bootcamp_id BIGINT NOT NULL,
    capacity_id BIGINT NOT NULL,
    CONSTRAINT fk_bootcamp FOREIGN KEY (bootcamp_id) REFERENCES bootcamp(id) ON DELETE CASCADE,
    CONSTRAINT uk_bootcamp_capacity UNIQUE (bootcamp_id, capacity_id)
);



