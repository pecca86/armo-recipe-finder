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

create table if not exists recipe
(
    id          bigserial not null,
    description TEXT not null,
    is_vegan   BOOLEAN not null,
    num_servings INTEGER not null,
    ingredients TEXT not null,
    customer_id BIGINT not null,
    primary key (id),
    FOREIGN KEY (customer_id) REFERENCES customer (id)
);
