--
-- Name: eperson
--    last_login for CoC after X years we should delete...
--

ALTER TABLE eperson ADD COLUMN last_login varchar(30);
