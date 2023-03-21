CREATE TABLE `tenant` (
    `id`                     BIGINT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    `name`                   VARCHAR(100)    NOT NULL,
    `password`     VARCHAR(32) NOT NULL,
    `home_dir_name`     VARCHAR(32) NOT NULL,

    CONSTRAINT tenant_unq UNIQUE (name)
);
