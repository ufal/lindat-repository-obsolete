--
-- Name: license_definition 
--    add column
--

ALTER TABLE license_definition ADD COLUMN required_info varchar(64); 

-- Table: user_metadata

DROP TABLE user_metadata;

CREATE TABLE user_metadata
(
  user_metadata_id serial NOT NULL,
  eperson_id integer NOT NULL,
  metadata_key character varying(64) NOT NULL,
  metadata_value character varying(256) NOT NULL,
  transaction_id integer,
  CONSTRAINT user_metadata_pkey PRIMARY KEY (user_metadata_id),
  CONSTRAINT license_resource_user_allowance_user_metadata_fk FOREIGN KEY (transaction_id)
      REFERENCES license_resource_user_allowance (transaction_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT user_registration_user_metadata_fk FOREIGN KEY (eperson_id)
      REFERENCES user_registration (eperson_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE user_metadata
  OWNER TO dspace;