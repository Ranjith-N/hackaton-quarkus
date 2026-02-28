package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseEndpointITTest {

  @Inject
  EntityManager em;

  @BeforeEach
  @Transactional
  public void setup() {
    em.createQuery("DELETE FROM DbWarehouse").executeUpdate();
    em.createNativeQuery(
        "INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, version) VALUES (1, 'MWH.001', 'ZWOLLE-001', 100, 10, 0)")
        .executeUpdate();
    em.createNativeQuery(
        "INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, version) VALUES (2, 'MWH.012', 'AMSTERDAM-001', 50, 5, 0)")
        .executeUpdate();
    em.createNativeQuery(
        "INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, version) VALUES (3, 'MWH.023', 'TILBURG-001', 30, 27, 0)")
        .executeUpdate();
  }

  @Test
  public void testSimpleListWarehouses() {

    final String path = "warehouse";

    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  public void testSimpleCheckingArchivingWarehouses() {

    // Uncomment the following lines to test the WarehouseResourceImpl
    // implementation

    final String path = "warehouse";

    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(
            containsString("MWH.001"),
            containsString("MWH.012"),
            containsString("MWH.023"),
            containsString("ZWOLLE-001"),
            containsString("AMSTERDAM-001"),
            containsString("TILBURG-001"));

    // // Archive the ZWOLLE-001:
    given().when().delete(path + "/MWH.001").then().statusCode(204);

    // List all, ZWOLLE-001 should be missing now:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(
            not(containsString("ZWOLLE-001")),
            containsString("AMSTERDAM-001"),
            containsString("TILBURG-001"));
  }
}
