INSERT INTO roles (id, name) VALUES (gen_random_uuid(), 'CUSTOMER') ON CONFLICT DO NOTHING;
INSERT INTO roles (id, name) VALUES (gen_random_uuid(), 'ADMIN') ON CONFLICT DO NOTHING;
