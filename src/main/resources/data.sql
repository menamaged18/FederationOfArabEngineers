-- 1. Insert Default Roles
INSERT INTO roles (name, slug, description, created_at)
VALUES 
    ('Super Admin', 'super_admin', 'Full system access and administration', NOW()),
    ('Member', 'member', 'Standard user access', NOW())
ON DUPLICATE KEY UPDATE slug = VALUES(slug);

-- 2. Insert Default Users 
-- password is: password 
INSERT INTO users (role_id, name, email, phone, password, is_active, created_at, updated_at)
VALUES 
    (
        (SELECT id FROM roles WHERE slug = 'super_admin'),
        'Super Admin',
        'superadmin@fae.com',
        '+10000000000',
        '$2a$12$xlLDxezg7i3mMiKRJhsF6eh1bewXoN0U90uEwL5lXaufM/wq1xK32', 
        TRUE,
        NOW(),
        NOW()
    ),
    (
        (SELECT id FROM roles WHERE slug = 'member'),
        'Admin User',
        'admin@fae.com',
        '+10000000001',
        '$2a$12$xlLDxezg7i3mMiKRJhsF6eh1bewXoN0U90uEwL5lXaufM/wq1xK32', 
        TRUE,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE email = VALUES(email);