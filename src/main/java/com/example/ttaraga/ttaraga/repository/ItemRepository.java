package com.example.ttaraga.ttaraga.repository;

import com.example.ttaraga.ttaraga.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
}
