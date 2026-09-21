CREATE TABLE users (
    id UUID PRIMARY KEY,
    document_number VARCHAR(11) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    age INTEGER NOT NULL,
    monthly_income NUMERIC(14, 2) NOT NULL,
    city VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE scores (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    document_number VARCHAR(11) NOT NULL,
    name VARCHAR(150) NOT NULL,
    age INTEGER NOT NULL,
    monthly_income NUMERIC(14, 2) NOT NULL,
    city VARCHAR(50) NOT NULL,
    temperature_celsius NUMERIC(10, 2) NOT NULL,
    age_component NUMERIC(14, 4) NOT NULL,
    income_component NUMERIC(14, 4) NOT NULL,
    temperature_component NUMERIC(14, 4) NOT NULL,
    score INTEGER NOT NULL,
    approved BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX scores_user_created_id_idx ON scores (user_id, created_at DESC, id DESC);
