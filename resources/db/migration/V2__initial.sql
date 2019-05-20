create table if not exists public.user
(
    id       serial       not null primary key,
    login    varchar(255) not null,
    password varchar(64)  not null
);

create unique index if not exists user_login_uindex
    on public.user (login);

