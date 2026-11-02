-- Tree Plantation and Monitoring Management System
-- Run this whole script once in MySQL Workbench (File > Open SQL Script > Run).
-- Re-running it resets the tables to the sample data below.

CREATE DATABASE IF NOT EXISTS tree_plantation
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tree_plantation;

DROP TABLE IF EXISTS plantation;
DROP TABLE IF EXISTS species;
DROP TABLE IF EXISTS location;
DROP TABLE IF EXISTS planter;

CREATE TABLE species (
    species_id      INT AUTO_INCREMENT PRIMARY KEY,
    common_name     VARCHAR(80)  NOT NULL,
    scientific_name VARCHAR(120),
    species_type    VARCHAR(30)  NOT NULL DEFAULT 'Fruit'
);

CREATE TABLE location (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    area        VARCHAR(80)  NOT NULL,
    place_type  VARCHAR(30)  NOT NULL DEFAULT 'Other'
);

CREATE TABLE planter (
    planter_id INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(120) NOT NULL,
    kind       ENUM('Person','Organization') NOT NULL DEFAULT 'Person',
    contact    VARCHAR(120)
);

CREATE TABLE plantation (
    plantation_id   INT AUTO_INCREMENT PRIMARY KEY,
    species_id      INT  NOT NULL,
    location_id     INT  NOT NULL,
    planter_id      INT  NOT NULL,
    plantation_date DATE NOT NULL,
    tree_count      INT  NOT NULL CHECK (tree_count > 0),
    tree_condition  ENUM('Healthy','Weak','Damaged','Dead') NOT NULL DEFAULT 'Healthy',
    last_checked    DATE NULL,
    note            VARCHAR(255) NULL,
    CONSTRAINT fk_plantation_species  FOREIGN KEY (species_id)  REFERENCES species(species_id)   ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_plantation_location FOREIGN KEY (location_id) REFERENCES location(location_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_plantation_planter  FOREIGN KEY (planter_id)  REFERENCES planter(planter_id)   ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_plantation_date (plantation_date),
    INDEX idx_plantation_condition (tree_condition)
);

-- Sample data -------------------------------------------------------------
INSERT INTO species (common_name, scientific_name, species_type) VALUES
 ('Mango',        'Mangifera indica',         'Fruit'),
 ('Neem',         'Azadirachta indica',       'Medicinal'),
 ('Mahogany',     'Swietenia macrophylla',    'Timber'),
 ('Krishnachura', 'Delonix regia',            'Ornamental'),
 ('Jackfruit',    'Artocarpus heterophyllus', 'Fruit'),
 ('Arjun',        'Terminalia arjuna',        'Medicinal');

INSERT INTO location (name, area, place_type) VALUES
 ('University Main Campus',     'Dhaka',       'Campus'),
 ('Hatirjheel Lakeside',        'Dhaka',       'Public park'),
 ('Green Model School Ground',  'Savar',       'School'),
 ('Riverbank Embankment',       'Narayanganj', 'Roadside'),
 ('Community Garden, Ward 12',  'Mirpur',      'Community land');

INSERT INTO planter (name, kind, contact) VALUES
 ('University Green Club',    'Organization', 'greenclub@uni.edu'),
 ('Rahim Uddin',              'Person',       '01711 000 111'),
 ('Bangladesh Tree Society',  'Organization', 'info@treesociety.org'),
 ('Nusrat Jahan',             'Person',       '01822 000 222'),
 ('Green Model School',       'Organization', 'office@gms.edu');

INSERT INTO plantation (species_id, location_id, planter_id, plantation_date, tree_count, tree_condition, last_checked, note) VALUES
 (1, 1, 1, '2026-03-12',  40, 'Healthy', '2026-09-28', 'New leaves on most trees.'),
 (2, 2, 3, '2026-04-02',  60, 'Healthy', '2026-09-30', NULL),
 (3, 4, 3, '2026-05-18', 120, 'Weak',    '2026-10-02', 'Yellowing after flooding.'),
 (4, 3, 5, '2026-06-05',  25, 'Healthy', '2026-09-20', NULL),
 (5, 5, 4, '2026-06-21',  18, 'Damaged', '2026-10-05', 'Goat damage on young stems.'),
 (6, 1, 2, '2026-07-09',  30, 'Dead',    '2026-10-01', 'Replant needed.'),
 (1, 3, 5, '2026-08-14',  35, 'Healthy', '2026-10-06', NULL);
