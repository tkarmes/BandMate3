-- ********************************************************************************
-- This script creates the database users and grants them the necessary permissions
-- ********************************************************************************

CREATE USER bandmate3_admin
WITH PASSWORD 'softwareforsofties';

GRANT SELECT, INSERT, UPDATE, DELETE
ON ALL TABLES IN SCHEMA public
TO bandmate3_admin;

GRANT USAGE, SELECT
ON ALL SEQUENCES IN SCHEMA public
TO bandmate3_admin;
