INSERT INTO customers (first_name, last_name, email, mobile_number, address, dob) VALUES ('Tony', 'Stark', 'tony@example.com', '9000000002', 'Malibu', '1994-05-29');
INSERT INTO customers (first_name, last_name, email, mobile_number, address, dob) VALUES ('Bruce', 'Wayne', 'bruce@example.com', '9000000003', 'Gotham', '1995-02-19');

INSERT INTO accounts (account_type, balance, currency, customer_id, account_opening_date, account_active, bank_name, mobile_number, deactivated_message, deactivated_at) VALUES ('SAVING_INDIVIDUAL', 300000.00, 'INR', 1, '2021-01-18 10:32:00', TRUE, 'HDFC','9000000002', NULL, NULL);
INSERT INTO accounts (account_type, balance, currency, customer_id, account_opening_date, account_active, bank_name, mobile_number, deactivated_message, deactivated_at) VALUES ('SAVING_INDIVIDUAL', 200000.00, 'INR', 1, '2026-02-28 11:26:00', TRUE, 'SBI', '9000000002', NULL, NULL);
INSERT INTO accounts (account_type, balance, currency, customer_id, account_opening_date, account_active, bank_name, mobile_number, deactivated_message, deactivated_at) VALUES ('SAVING_INDIVIDUAL', 100000.00, 'INR', 1, '2016-02-11 11:14:00', TRUE, 'ICICI','9000000002', NULL, NULL);

INSERT INTO accounts (account_type, balance, currency, customer_id, account_opening_date, account_active, bank_name, mobile_number, deactivated_message, deactivated_at) VALUES ('CURRENT', 2000.00, 'INR', 2, '2022-12-16 02:45:00', TRUE, 'HDFC','9000000003', NULL, NULL);
INSERT INTO accounts (account_type, balance, currency, customer_id, account_opening_date, account_active, bank_name, mobile_number, deactivated_message, deactivated_at) VALUES ('CURRENT', 2000.00, 'INR', 2, '2025-11-20 02:26:00', TRUE, 'SBI','9000000003', NULL, NULL);
