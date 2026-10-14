-- Carpeta por defecto para las fotos generales de la academia.
INSERT INTO gallery_folders (name, created_at)
SELECT 'Nuestra academia', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM gallery_folders WHERE name = 'Nuestra academia'
);

-- Migra cualquier foto subida antes de tener carpetas (folder_id nulo) a la
-- carpeta general, para que ninguna foto se quede sin carpeta asignada
UPDATE gallery_photos
SET folder_id = (SELECT id FROM gallery_folders WHERE name = 'Nuestra academia')
WHERE folder_id IS NULL;

-- Datos de carga inicial para desarrollo y pruebas.
-- Cada INSERT se protege con WHERE NOT EXISTS para no duplicar los talleres
-- en cada reinicio de la aplicacion (data.sql se ejecuta en cada arranque).

INSERT INTO workshops (name, description, date, time, recommended_age, active)
SELECT 'Manos a la obra',
       'Un taller sensorial pensado para los más pequeños, con materiales seguros y actividades para explorar texturas, colores y sonidos.',
       DATE '2026-10-24',
       TIME '11:00:00',
       '1-2 años',
       true
WHERE NOT EXISTS (
    SELECT 1 FROM workshops WHERE name = 'Manos a la obra' AND date = DATE '2026-10-24'
);

INSERT INTO workshops (name, description, date, time, recommended_age, active)
SELECT 'Breakout Halloween',
       'Un escape room temático de Halloween con retos sencillos y disfraces, pensado para los primeros pasos en el juego cooperativo.',
       DATE '2026-10-30',
       TIME '11:00:00',
       '3-6 años',
       true
WHERE NOT EXISTS (
    SELECT 1 FROM workshops WHERE name = 'Breakout Halloween' AND date = DATE '2026-10-30'
);

INSERT INTO workshops (name, description, date, time, recommended_age, active)
SELECT 'Breakout Halloween',
       'Un escape room de Halloween con acertijos y pruebas en equipo adaptados a esta franja de edad.',
       DATE '2026-10-31',
       TIME '11:00:00',
       '7-9 años',
       true
WHERE NOT EXISTS (
    SELECT 1 FROM workshops WHERE name = 'Breakout Halloween' AND date = DATE '2026-10-31' AND recommended_age = '7-9 años'
);

INSERT INTO workshops (name, description, date, time, recommended_age, active)
SELECT 'Breakout Halloween',
       'Un escape room de Halloween con retos más exigentes de lógica y trabajo en equipo para los mayores del grupo.',
       DATE '2026-10-31',
       TIME '11:00:00',
       '10-12 años',
       true
WHERE NOT EXISTS (
    SELECT 1 FROM workshops WHERE name = 'Breakout Halloween' AND date = DATE '2026-10-31' AND recommended_age = '10-12 años'
);
