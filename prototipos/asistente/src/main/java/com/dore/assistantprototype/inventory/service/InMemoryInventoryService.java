package com.dore.assistantprototype.inventory.service;

import com.dore.assistantprototype.inventory.domain.MovementOrigin;
import com.dore.assistantprototype.inventory.domain.MovementType;
import com.dore.assistantprototype.inventory.domain.Product;
import com.dore.assistantprototype.inventory.domain.StockMovement;
import com.dore.assistantprototype.inventory.dto.CreateProductRequest;
import com.dore.assistantprototype.inventory.dto.ProductResponse;
import com.dore.assistantprototype.inventory.dto.RecordMovementRequest;
import com.dore.assistantprototype.inventory.dto.StockMovementResponse;
import com.dore.assistantprototype.inventory.dto.UpdateProductRequest;
import com.dore.assistantprototype.inventory.exception.DuplicateSkuException;
import com.dore.assistantprototype.inventory.exception.InsufficientStockException;
import com.dore.assistantprototype.inventory.exception.InvalidInventoryOperationException;
import com.dore.assistantprototype.inventory.exception.ProductNotFoundException;
import com.dore.assistantprototype.inventory.repository.ProductRepository;
import com.dore.assistantprototype.inventory.repository.StockMovementRepository;
import jakarta.annotation.PostConstruct;
import java.text.Normalizer;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InMemoryInventoryService implements InventoryService {

	public static final ZoneId CALI_ZONE = ZoneId.of("America/Bogota");
	private static final String DEFAULT_ACTOR = "operador-taller";

	private final ProductRepository productRepository;
	private final StockMovementRepository movementRepository;
	private final Clock clock;

	@Autowired
	public InMemoryInventoryService(
			ProductRepository productRepository,
			StockMovementRepository movementRepository) {
		this(productRepository, movementRepository, Clock.system(CALI_ZONE));
	}

	public InMemoryInventoryService(
			ProductRepository productRepository,
			StockMovementRepository movementRepository,
			Clock clock) {
		this.productRepository = productRepository;
		this.movementRepository = movementRepository;
		this.clock = clock.withZone(CALI_ZONE);
	}

	@PostConstruct
	public void initializeSeedData() {
		if (productRepository.findAll().isEmpty()) {
			resetToDefaultCatalog();
		}
	}

	@Override
	public synchronized void resetToDefaultCatalog() {
		productRepository.clear();
		movementRepository.clear();
		seedItem("Rosa Freedom roja", "Flores", "tallo", "FL-001", 84, 30, 1800, 4200, "Cultivos La Montaña");
		seedItem("Rosa Vendela marfil", "Flores", "tallo", "FL-002", 48, 24, 2000, 4500, "Cultivos La Montaña");
		seedItem("Tulipán holandés", "Flores", "tallo", "FL-003", 42, 15, 6200, 12500, "Importadora Flora");
		seedItem("Hortensia azul", "Flores", "tallo", "FL-004", 7, 10, 9500, 18500, "Jardines del Cauca");
		seedItem("Girasol San Vicente", "Flores", "tallo", "FL-005", 30, 12, 2500, 5800, "Agroflores Dapa");
		seedItem("Eucalipto Baby Blue", "Follajes", "ramo", "FO-001", 18, 20, 2400, 5500, "Verde Vivo");
		seedItem("Papel coreano marfil", "Empaque", "pliego", "EM-001", 9, 12, 2100, 4800, "Empaques Nativa");
		seedItem("Cinta de algodón crudo", "Empaque", "rollo", "EM-002", 26, 10, 900, 2400, "Empaques Nativa");
		seedItem("Espuma floral Oasis", "Insumos", "bloque", "IN-001", 15, 8, 3200, 6500, "Insumos del Valle");
	}

	private void seedItem(
			String name,
			String category,
			String unit,
			String sku,
			int stock,
			int minStock,
			int cost,
			int price,
			String supplier) {
		Product seeded = productRepository.saveNew(name, category, unit, sku, stock, minStock, cost, price, supplier, true);
		movementRepository.append(
				seeded.getId(),
				seeded.getSku(),
				seeded.getName(),
				MovementType.ENTRY,
				stock,
				0,
				stock,
				"Carga inicial ficticia de demostración",
				MovementOrigin.MANUAL,
				"semilla-demo",
				ZonedDateTime.now(clock));
	}

	@Override
	public List<ProductResponse> listProducts(String query, boolean includeInactive) {
		String normalizedQuery = normalizeForSearch(query);
		return productRepository.findAll().stream()
				.filter(item -> includeInactive || item.isActive())
				.filter(item -> matchesSearch(item, normalizedQuery))
				.map(ProductResponse::fromDomain)
				.toList();
	}

	@Override
	public ProductResponse getProduct(Long productId) {
		return ProductResponse.fromDomain(requireExistingProduct(productId));
	}

	@Override
	public synchronized ProductResponse createProduct(CreateProductRequest request, MovementOrigin origin) {
		if (request == null) {
			throw new InvalidInventoryOperationException("Los datos del producto son obligatorios.");
		}
		String cleanName = requireNonBlank(request.name(), "El nombre del producto es obligatorio.");
		String cleanCategory = requireNonBlank(request.category(), "La categoría es obligatoria.");
		String cleanUnit = requireNonBlank(request.unit(), "La unidad de medida es obligatoria.");
		String cleanSku = requireNonBlank(request.sku(), "El SKU es obligatorio.").toUpperCase(Locale.ROOT);
		int initialStock = requireNonNegative(request.initialStock(), "El stock inicial");
		int minStock = requireNonNegative(request.minStock(), "El stock mínimo");
		int cost = requireNonNegative(request.cost(), "El costo");
		int price = requireNonNegative(request.price(), "El precio");
		String cleanSupplier = request.supplier() == null ? "" : request.supplier().trim();

		if (productRepository.findBySkuIgnoreCase(cleanSku).isPresent()) {
			throw new DuplicateSkuException(cleanSku);
		}

		Product created = productRepository.saveNew(
				cleanName,
				cleanCategory,
				cleanUnit,
				cleanSku,
				initialStock,
				minStock,
				cost,
				price,
				cleanSupplier,
				true);

		if (initialStock > 0) {
			movementRepository.append(
					created.getId(),
					created.getSku(),
					created.getName(),
					MovementType.ENTRY,
					initialStock,
					0,
					initialStock,
					"Inventario inicial al crear producto",
					Objects.requireNonNullElse(origin, MovementOrigin.MANUAL),
					resolveActor(request.performedBy()),
					ZonedDateTime.now(clock));
		}
		return ProductResponse.fromDomain(created);
	}

	@Override
	public synchronized ProductResponse updateProduct(Long productId, UpdateProductRequest request) {
		if (request == null) {
			throw new InvalidInventoryOperationException("Debes indicar al menos un cambio para el producto.");
		}
		Product current = requireActiveProduct(productId);
		boolean anyChange = false;

		if (request.name() != null && !request.name().isBlank()) {
			current.setName(request.name().trim());
			anyChange = true;
		}
		if (request.category() != null && !request.category().isBlank()) {
			current.setCategory(request.category().trim());
			anyChange = true;
		}
		if (request.unit() != null && !request.unit().isBlank()) {
			current.setUnit(request.unit().trim());
			anyChange = true;
		}
		if (request.sku() != null && !request.sku().isBlank()) {
			String candidateSku = request.sku().trim().toUpperCase(Locale.ROOT);
			productRepository.findBySkuIgnoreCase(candidateSku)
					.filter(existing -> !existing.getId().equals(productId))
					.ifPresent(existing -> {
						throw new DuplicateSkuException(candidateSku);
					});
			current.setSku(candidateSku);
			anyChange = true;
		}
		if (request.minStock() != null) {
			current.setMinStock(requireNonNegative(request.minStock(), "El stock mínimo"));
			anyChange = true;
		}
		if (request.cost() != null) {
			current.setCost(requireNonNegative(request.cost(), "El costo"));
			anyChange = true;
		}
		if (request.price() != null) {
			current.setPrice(requireNonNegative(request.price(), "El precio"));
			anyChange = true;
		}
		if (request.supplier() != null) {
			current.setSupplier(request.supplier().trim());
			anyChange = true;
		}
		if (!anyChange) {
			throw new InvalidInventoryOperationException("No se especificó ningún campo para modificar.");
		}

		return ProductResponse.fromDomain(productRepository.update(current));
	}

	@Override
	public synchronized ProductResponse deactivateProduct(Long productId) {
		Product current = requireActiveProduct(productId);
		current.setActive(false);
		return ProductResponse.fromDomain(productRepository.update(current));
	}

	@Override
	public synchronized StockMovementResponse recordMovement(
			Long productId,
			RecordMovementRequest request,
			MovementOrigin origin) {
		if (request == null || request.type() == null || request.quantity() == null) {
			throw new InvalidInventoryOperationException("El tipo de movimiento y la cantidad son obligatorios.");
		}
		Product current = requireActiveProduct(productId);
		int previousStock = current.getStock();
		int quantity = request.quantity();
		int nextStock = computeNextStock(current, request.type(), quantity);

		current.setStock(nextStock);
		productRepository.update(current);

		String reason = (request.reason() == null || request.reason().isBlank())
				? "Movimiento de " + request.type().spanishLabel()
				: request.reason().trim();

		StockMovement savedMovement = movementRepository.append(
				current.getId(),
				current.getSku(),
				current.getName(),
				request.type(),
				quantity,
				previousStock,
				nextStock,
				reason,
				Objects.requireNonNullElse(origin, MovementOrigin.MANUAL),
				resolveActor(request.performedBy()),
				ZonedDateTime.now(clock));

		return StockMovementResponse.fromDomain(savedMovement);
	}

	public int previewNextStock(ProductResponse product, MovementType type, int quantity) {
		Product snapshot = new Product(
				product.id(),
				product.name(),
				product.category(),
				product.unit(),
				product.sku(),
				product.stock(),
				product.minStock(),
				product.cost(),
				product.price(),
				product.supplier(),
				product.active());
		if (!snapshot.isActive()) {
			throw new InvalidInventoryOperationException("El producto '" + snapshot.getName() + "' está desactivado.");
		}
		return computeNextStock(snapshot, type, quantity);
	}

	private int computeNextStock(Product product, MovementType type, int quantity) {
		int currentStock = product.getStock();
		return switch (type) {
			case ENTRY -> {
				if (quantity <= 0) {
					throw new InvalidInventoryOperationException("La cantidad de entrada debe ser mayor a cero.");
				}
				yield currentStock + quantity;
			}
			case EXIT, WASTE -> {
				if (quantity <= 0) {
					throw new InvalidInventoryOperationException(
							"La cantidad de " + type.spanishLabel() + " debe ser mayor a cero.");
				}
				if (currentStock < quantity) {
					throw new InsufficientStockException(product.getName(), currentStock, quantity);
				}
				yield currentStock - quantity;
			}
			case ADJUSTMENT -> {
				if (quantity < 0) {
					throw new InvalidInventoryOperationException("El stock físico del ajuste no puede ser negativo.");
				}
				yield quantity;
			}
		};
	}

	@Override
	public List<StockMovementResponse> listProductMovements(Long productId) {
		requireExistingProduct(productId);
		return movementRepository.findByProductId(productId).stream()
				.map(StockMovementResponse::fromDomain)
				.toList();
	}

	private Product requireExistingProduct(Long productId) {
		if (productId == null) {
			throw new ProductNotFoundException("El identificador del producto es obligatorio.");
		}
		return productRepository.findById(productId)
				.orElseThrow(() -> new ProductNotFoundException("No existe un producto con ID " + productId + "."));
	}

	private Product requireActiveProduct(Long productId) {
		Product product = requireExistingProduct(productId);
		if (!product.isActive()) {
			throw new InvalidInventoryOperationException(
					"El producto '" + product.getName() + "' está desactivado y no admite cambios.");
		}
		return product;
	}

	private static String requireNonBlank(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new InvalidInventoryOperationException(message);
		}
		return value.trim();
	}

	private static int requireNonNegative(Integer value, String fieldLabel) {
		if (value == null || value < 0) {
			throw new InvalidInventoryOperationException(fieldLabel + " debe ser un entero mayor o igual a cero.");
		}
		return value;
	}

	private static String resolveActor(String actor) {
		return (actor == null || actor.isBlank()) ? DEFAULT_ACTOR : actor.trim();
	}

	private static boolean matchesSearch(Product item, String normalizedQuery) {
		if (normalizedQuery.isEmpty()) {
			return true;
		}
		String searchable = normalizeForSearch(
				item.getName() + " " + item.getSku() + " " + item.getCategory() + " " + item.getSupplier());
		for (String token : normalizedQuery.split("\\s+")) {
			if (!token.isEmpty() && !searchable.contains(token)) {
				// Permite coincidencia en singular/plural simple en español ("rosas" -> "rosa", "tulipanes" -> "tulipan")
				String singular = stripSpanishPlural(token);
				if (!searchable.contains(singular)) {
					return false;
				}
			}
		}
		return true;
	}

	private static String stripSpanishPlural(String token) {
		if (token.length() > 4 && token.endsWith("es")) {
			return token.substring(0, token.length() - 2);
		}
		if (token.length() > 3 && token.endsWith("s")) {
			return token.substring(0, token.length() - 1);
		}
		return token;
	}

	public static String normalizeForSearch(String input) {
		if (input == null || input.isBlank()) {
			return "";
		}
		String decomposed = Normalizer.normalize(input.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
		return decomposed.replaceAll("\\p{M}", "");
	}
}
