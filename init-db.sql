--run in database postgres:
create database scraping;
create role scraping with password 'scraping';
grant all privileges on database scraping to scraping;

--run in database scraping:
grant all on all tables in schema "scraping", "public" to scraping;
grant all on all sequences in schema "scraping", "public" to scraping;