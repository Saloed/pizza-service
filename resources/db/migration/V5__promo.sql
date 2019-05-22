create table if not exists promo_status
(
    id   int not null primary key,
    name varchar(255)
);

insert into promo_status (id, name)
values (0, 'draft'),
       (1, 'active'),
       (2, 'closed');

create table if not exists promo
(
    id         int not null primary key,
    manager_id int not null references manager,
    status_id  int not null references promo_status,
    result     text,
    created_at timestamp,
    updated_at timestamp
);

create table if not exists promo_client_status
(
    id   int not null primary key,
    name varchar(255)
);

insert into promo_client_status (id, name)
values (0, 'notInformed'),
       (1, 'processing'),
       (2, 'informed');

create table if not exists promo_client
(
    promo_id   int not null references promo,
    client_id  int not null references client,
    status_id  int not null references promo_client_status,
    created_at timestamp,
    updated_at timestamp
);

create table if not exists order_promo
(
    order_id int not null primary key references public.order,
    promo_id int not null references promo
)
