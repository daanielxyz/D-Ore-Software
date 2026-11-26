package com.dore.inventory;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Cambios hechos desde la interfaz: siempre con origen MANUAL. */
@RestController
@RequestMapping("/api/products")
class ProductController {

    private final InventoryService inventory;

    ProductController(InventoryService inventory) {
        this.inventory = inventory;
    }

    @GetMapping
    List<ProductView> search(@RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return inventory.search(q, includeInactive);
    }

    @GetMapping("/{id}")
    ProductView get(@PathVariable long id) {
        return inventory.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProductView create(@Valid @RequestBody NewProduct product) {
        return inventory.create(product, MovementSource.MANUAL);
    }

    @PutMapping("/{id}")
    ProductView update(@PathVariable long id, @Valid @RequestBody ProductChanges changes) {
        return inventory.update(id, changes);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deactivate(@PathVariable long id) {
        inventory.deactivate(id);
    }

    @GetMapping("/{id}/movements")
    List<MovementView> movements(@PathVariable long id) {
        return inventory.movements(id);
    }

    @PostMapping("/{id}/movements")
    @ResponseStatus(HttpStatus.CREATED)
    MovementView record(@PathVariable long id, @Valid @RequestBody NewMovement movement) {
        return inventory.recordMovement(id, movement, MovementSource.MANUAL);
    }
}
