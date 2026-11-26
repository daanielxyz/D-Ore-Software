package com.dore.inventory;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            select p from Product p
            where (:includeInactive = true or p.active = true)
              and (lower(p.name) like :pattern escape '!'
                or lower(p.sku) like :pattern escape '!'
                or lower(p.category) like :pattern escape '!'
                or lower(p.supplier) like :pattern escape '!')
            order by p.name, p.id
            """)
    List<Product> search(@Param("pattern") String pattern,
            @Param("includeInactive") boolean includeInactive);

    /** Bloquea la fila para que dos cambios simultáneos no se pisen. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findForUpdate(@Param("id") long id);

    boolean existsBySkuIgnoreCase(String sku);
}
