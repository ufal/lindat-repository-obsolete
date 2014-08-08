--
-- Name: handle
--    url for external resources
--

ALTER TABLE handle ADD COLUMN url varchar(2048);

--
-- Name: eperson
--   can_edit_submission_metadata 
--

ALTER TABLE eperson ADD COLUMN can_edit_submission_metadata boolean;
