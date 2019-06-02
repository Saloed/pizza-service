create table if not exists promo
(
    id          serial       not null primary key,
    manager_id  int          not null references manager,
    status      varchar(255) not null,
    effect      varchar(255) not null,
    description text         not null,
    result      text,
    created_at  timestamp,
    updated_at  timestamp
);

create table if not exists promo_client
(
    id serial not null primary key ,
    promo_id    int          not null references promo,
    client_id   int          not null references client,
    operator_id int default null references operator,
    status      varchar(255) not null,
    created_at  timestamp,
    updated_at  timestamp
);

create table if not exists order_promo
(
    order_id int not null primary key references client_order,
    promo_id int not null references promo
)
