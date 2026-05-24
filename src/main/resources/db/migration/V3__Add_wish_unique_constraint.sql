ALTER TABLE wish
    ADD CONSTRAINT uq_wish_member_product UNIQUE (member_id, product_id);
