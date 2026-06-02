ALTER TABLE options ADD CONSTRAINT chk_options_quantity CHECK (quantity >= 0);
ALTER TABLE member ADD CONSTRAINT chk_member_point CHECK (point >= 0);
