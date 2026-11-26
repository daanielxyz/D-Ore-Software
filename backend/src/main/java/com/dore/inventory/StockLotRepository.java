package com.dore.inventory;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface StockLotRepository extends JpaRepository<StockLot, Long> {

    /** Lotes con existencias, primero el que vence antes; sin fecha al final. */
    @Query("""
            select l from StockLot l
            where l.productId in :productIds and l.quantity > 0
            order by l.expiresOn asc nulls last, l.id
            """)
    List<StockLot> findAvailable(@Param("productIds") Collection<Long> productIds);

    @Query("""
            select l from StockLot l, Product p
            where p.id = l.productId and p.active = true
              and l.quantity > 0 and l.expiresOn <= :limit
            order by l.expiresOn, l.id
            """)
    List<StockLot> findExpiringUpTo(@Param("limit") LocalDate limit);
}
