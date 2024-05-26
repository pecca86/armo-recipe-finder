create table if not exists customer
(
    id         bigserial not null,
    email      VARCHAR(50) not null ,
    first_name VARCHAR(50) not null ,
    last_name  VARCHAR(50) not null,
    password   VARCHAR(50) not null,
    primary key (id),
    UNIQUE (email)
);
