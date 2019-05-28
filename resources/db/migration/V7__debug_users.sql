insert into "user" (id, login, password, role)
values (default, 'client', '1f1d2e094cfe007dff322665917c3fcbbdfd47db', 0),
       (default, 'client1', '1f1d2e094cfe007dff322665917c3fcbbdfd47db', 0),
       (default, 'client2', '1f1d2e094cfe007dff322665917c3fcbbdfd47db', 0),
       (default, 'client3', '1f1d2e094cfe007dff322665917c3fcbbdfd47db', 0),
       (default, 'manager', '1f1d2e094cfe007dff322665917c3fcbbdfd47db', 1),
       --        (default, 'manager1', '1f1d2e094cfe007dff322665917c3fcbbdfd47db', 1),
       (default, 'operator', '1f1d2e094cfe007dff322665917c3fcbbdfd47db', 2),
       --        (default, 'operator1', '1f1d2e094cfe007dff322665917c3fcbbdfd47db', 2),
       (default, 'courier', '1f1d2e094cfe007dff322665917c3fcbbdfd47db', 3)
--        ,(default, 'courier1', '1f1d2e094cfe007dff322665917c3fcbbdfd47db', 4)
;

insert into client (id, address, phone)
values ((select id from "user" where login = 'client'), 'client 0 address', 'client 0 phone'),
       ((select id from "user" where login = 'client1'), 'client 1 address', 'client 1 phone'),
       ((select id from "user" where login = 'client2'), 'client 2 address', 'client 2 phone'),
       ((select id from "user" where login = 'client3'), 'client 3 address', 'client 3 phone');

insert into manager (id, restaurant)
values ((select id from "user" where login = 'manager'), 'restaurant 0')
--        ,((select if from "user" where login = 'manager1'), 'restaurant 1')
;

insert into operator (id, number)
values ((select id from "user" where login = 'operator'), 17);

insert into courier (id)
values ((select id from "user" where login = 'courier'));





