package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  // list only the warehouses that are not archived
  @Override
  public List<Warehouse> getAll() {
    return this.find("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
    dbWarehouse.version = warehouse.version;

    this.persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse dbWarehouse = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (dbWarehouse == null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode + "' does not exist");
    }

    // Set the version from the domain model to enable optimistic locking check
    dbWarehouse.version = warehouse.version;

    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.archivedAt = warehouse.archivedAt;

    // Hibernate will automatically perform version check and increment on
    // flush/commit
    getEntityManager().flush();
    getEntityManager().clear();
  }

  @Override
  public void remove(Warehouse warehouse) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'remove'");
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }

  @Override
  public List<Warehouse> search(
      String location,
      Integer minCapacity,
      Integer maxCapacity,
      String sortBy,
      String sortOrder,
      Integer page,
      Integer pageSize) {

    StringBuilder query = new StringBuilder("archivedAt is null");
    java.util.Map<String, Object> params = new java.util.HashMap<>();

    if (location != null && !location.isBlank()) {
      query.append(" and location = :location");
      params.put("location", location);
    }

    if (minCapacity != null) {
      query.append(" and capacity >= :minCapacity");
      params.put("minCapacity", minCapacity);
    }

    if (maxCapacity != null) {
      query.append(" and capacity <= :maxCapacity");
      params.put("maxCapacity", maxCapacity);
    }

    Sort sort = Sort.by(sortBy != null ? sortBy : "createdAt");
    if ("desc".equalsIgnoreCase(sortOrder)) {
      sort = sort.direction(Sort.Direction.Descending);
    } else {
      sort = sort.direction(Sort.Direction.Ascending);
    }

    return this.find(query.toString(), sort, params)
        .page(Page.of(page != null ? page : 0, pageSize != null ? pageSize : 10))
        .list()
        .stream()
        .map(DbWarehouse::toWarehouse)
        .toList();
  }
}
