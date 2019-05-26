create table if not exists client
(
    id      int not null primary key
        constraint client_user_id_fk
            references public.user
            on update cascade on delete cascade,
    address text,
    phone varchar (100)
);

create table if not exists manager
(
    id         int not null primary key
        constraint client_user_id_fk
            references public.user
            on update cascade on delete cascade,
    restaurant text
);


create table if not exists operator
(
    id     int not null primary key
        constraint client_user_id_fk
            references public.user
            on update cascade on delete cascade,
    number int
);


create table if not exists courier
(
    id int not null primary key
        constraint client_user_id_fk
            references public.user
            on update cascade on delete cascade
);
