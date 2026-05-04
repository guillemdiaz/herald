-- Fake tenants
-- {noop} tells Spring Security to accept plaintext passwords for local dev.
INSERT INTO tenants (company_name, email, password, is_active, created_at)
VALUES ('Stark Industries', 'stark@herald.dev', '{noop}password', true, CURRENT_TIMESTAMP)
    ON CONFLICT (email) DO NOTHING;

INSERT INTO tenants (company_name, email, password, is_active, created_at)
VALUES ('Wayne Enterprises', 'wayne@herald.dev', '{noop}password', true, CURRENT_TIMESTAMP)
    ON CONFLICT (email) DO NOTHING;

-- Fake messages
INSERT INTO message_logs (tenant_id, recipient_number, content, status, sent_at)
VALUES (
           (SELECT id FROM tenants WHERE email = 'stark@herald.dev'),
           '+34600000001',
           'Stark Auth: Your verification code is 481516. Do not share ' ||
           'this code.',
           'SENT',
           CURRENT_TIMESTAMP
       );

INSERT INTO message_logs (tenant_id, recipient_number, content, status, sent_at)
VALUES (
           (SELECT id FROM tenants WHERE email = 'stark@herald.dev'),
           '+34600000002',
           'Shipping Alert: Package tracking #SI-3000 is out for delivery ' ||
           'today.',
           'PENDING',
           CURRENT_TIMESTAMP
       );

INSERT INTO message_logs (tenant_id, recipient_number, content, status, sent_at)
VALUES (
           (SELECT id FROM tenants WHERE email = 'wayne@herald.dev'),
           '+34600000003',
           'WayneBank Security: A login was detected from a new device in ' ||
           'Gotham',
           'SENT',
           CURRENT_TIMESTAMP
       );

INSERT INTO message_logs (tenant_id, recipient_number, content, status, sent_at)
VALUES (
           (SELECT id FROM tenants WHERE email = 'wayne@herald.dev'),
           '+34600000004',
           'Reminder: Quarterly earnings call starts in 30 minutes.',
           'SENT',
           CURRENT_TIMESTAMP
       );