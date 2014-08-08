DROP INDEX license_resource_user_allowance_idx;

DROP INDEX license_resource_mapping_idx;
ALTER TABLE license_resource_mapping ADD COLUMN active boolean DEFAULT true NOT NULL;