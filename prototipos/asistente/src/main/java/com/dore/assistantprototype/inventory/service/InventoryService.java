package com.dore.assistantprototype.inventory.service;

import com.dore.assistantprototype.inventory.domain.MovementOrigin;
import com.dore.assistantprototype.inventory.dto.CreateProductRequest;
import com.dore.assistantprototype.inventory.dto.ProductResponse;
import com.dore.assistantprototype.inventory.dto.RecordMovementRequest;
import com.dore.assistantprototype.inventory.dto.StockMovementResponse;
import com.dore.assistantprototype.inventory.dto.UpdateProductRequest;
import java.util.List;

/**
 * Contrato de negocio del inventario de la floristería D'Ore (Fase 0).
 * Ningún cambio de stock ocurre fuera de este servicio.
 */
public interface InventoryService {

	List<ProductResponse> listProducts(String query, boolean includeInactive);

	ProductResponse getProduct(Long productId);

	ProductResponse createProduct(CreateProductRequest request, MovementOrigin origin);

	ProductResponse updateProduct(Long productId, UpdateProductRequest request);

	ProductResponse deactivateProduct(Long productId);

	StockMovementResponse recordMovement(Long productId, RecordMovementRequest request, MovementOrigin origin);

	List<StockMovementResponse> listProductMovements(Long productId);

	void resetToDefaultCatalog();
}
