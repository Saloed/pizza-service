create table if not exists public.user
(
    id serial not null
        constraint user_pk
            primary key,
    login varchar(255) not null
);

create unique index if not exists user_login_uindex
    on public.user (login);

