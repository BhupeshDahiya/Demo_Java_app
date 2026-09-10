CREATE TABLE persons (
    id     BIGSERIAL PRIMARY KEY,
    name   VARCHAR(100) NOT NULL,
    dob    DATE         NOT NULL,
    email  VARCHAR(150) NOT NULL UNIQUE
);
