create table if not exists order_status
(
  id   int          not null primary key,
  name varchar(100) not null
);

insert into order_status (id, name)
values (6, 'draft'),
       (0, 'new'),
       (1, 'approved'),
       (2, 'processing'),
       (3, 'ready'),
       (4, 'shipping'),
       (5, 'closed'),
       (7, 'canceled');

create table if not exists pizza
(
  id   serial not null primary key,
  name text
);

create table if not exists payment_type
(
  id   int          not null primary key,
  name varchar(100) not null
);

insert into payment_type (id, name)
values (0, 'cash'),
       (1, 'card');

create table if not exists public.order
(
  id          serial  not null primary key,
  status_id   int     not null references order_status,
  is_active   boolean not null,
  client_id   int     not null references client,
  manager_id  int default null references manager,
  operator_id int default null references operator,
  courier_id  int default null references courier,
  created_at  timestamp,
  updated_at  timestamp
);

create table if not exists order_pizza
(
  order_id int not null references public.order,
  pizza_id int not null references pizza
);



create table if not exists payment
(
  id          serial not null primary key,
  type_id     int    not null references payment_type,
  order_id    int    not null references public.order,
  amount      int,
  transaction varchar(255),
  created_at  timestamp,
  updated_at  timestamp
);
