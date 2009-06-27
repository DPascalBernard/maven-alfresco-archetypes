create database ${db.name};
grant all on ${db.name}.* to 'alfresco'@'localhost' identified by 'alfresco' with grant option;
grant all on ${db.name}.* to 'alfresco'@'localhost.localdomain' identified by 'alfresco' with grant option;