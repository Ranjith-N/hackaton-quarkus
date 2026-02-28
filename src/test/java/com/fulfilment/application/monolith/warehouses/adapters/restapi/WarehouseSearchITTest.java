package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseSearchITTest {

        @Inject
        EntityManager em;

        @BeforeEach
        @Transactional
        public void setup() {
                // Reset database to a known state for search tests
                em.createQuery("DELETE FROM DbWarehouse").executeUpdate();

                // MWH.001: Zwolle, Capacity 100
                em.createNativeQuery(
                                "INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, version, createdAt) "
                                                +
                                                "VALUES (1, 'MWH.001', 'ZWOLLE-001', 100, 10, 0, '2024-01-01 10:00:00')")
                                .executeUpdate();

                // MWH.012: Amsterdam, Capacity 50
                em.createNativeQuery(
                                "INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, version, createdAt) "
                                                +
                                                "VALUES (2, 'MWH.012', 'AMSTERDAM-001', 50, 5, 0, '2024-01-02 10:00:00')")
                                .executeUpdate();

                // MWH.023: Tilburg, Capacity 30
                em.createNativeQuery(
                                "INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, version, createdAt) "
                                                +
                                                "VALUES (3, 'MWH.023', 'TILBURG-001', 30, 27, 0, '2024-01-03 10:00:00')")
                                .executeUpdate();

                // Archived warehouse: Should NOT appear in search
                em.createNativeQuery(
                                "INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, version, archivedAt) "
                                                +
                                                "VALUES (4, 'MWH.999', 'ARCHIVED-001', 50, 0, 0, '2024-01-01 12:00:00')")
                                .executeUpdate();
        }

        @Test
        public void testSearchByLocation() {
                given()
                                .queryParam("location", "ZWOLLE-001")
                                .when().get("/warehouse/search")
                                .then()
                                .statusCode(200)
                                .body("$", hasSize(1))
                                .body("[0].businessUnitCode", is("MWH.001"))
                                .body("[0].location", is("ZWOLLE-001"));
        }

        @Test
        public void testSearchByCapacityRange() {
                // Search for capacity between 40 and 60 (Should only find Amsterdam MWH.012)
                given()
                                .queryParam("minCapacity", 40)
                                .queryParam("maxCapacity", 60)
                                .when().get("/warehouse/search")
                                .then()
                                .statusCode(200)
                                .body("$", hasSize(1))
                                .body("[0].businessUnitCode", is("MWH.012"));
        }

        @Test
        public void testSearchSortingByCapacityDesc() {
                given()
                                .queryParam("sortBy", "capacity")
                                .queryParam("sortOrder", "desc")
                                .when().get("/warehouse/search")
                                .then()
                                .statusCode(200)
                                .body("$", hasSize(3))
                                .body("[0].businessUnitCode", is("MWH.001")) // 100
                                .body("[1].businessUnitCode", is("MWH.012")) // 50
                                .body("[2].businessUnitCode", is("MWH.023")); // 30
        }

        @Test
        public void testSearchPagination() {
                // Page 0, Size 2
                given()
                                .queryParam("page", 0)
                                .queryParam("pageSize", 2)
                                .queryParam("sortBy", "capacity")
                                .queryParam("sortOrder", "asc")
                                .when().get("/warehouse/search")
                                .then()
                                .statusCode(200)
                                .body("$", hasSize(2))
                                .body("[0].businessUnitCode", is("MWH.023")) // 30
                                .body("[1].businessUnitCode", is("MWH.012")); // 50

                // Page 1, Size 2
                given()
                                .queryParam("page", 1)
                                .queryParam("pageSize", 2)
                                .queryParam("sortBy", "capacity")
                                .queryParam("sortOrder", "asc")
                                .when().get("/warehouse/search")
                                .then()
                                .statusCode(200)
                                .body("$", hasSize(1))
                                .body("[0].businessUnitCode", is("MWH.001")); // 100
        }

        @Test
        public void testArchivedWarehousesAreExcluded() {
                // Search for the location of the archived warehouse
                given()
                                .queryParam("location", "ARCHIVED-001")
                                .when().get("/warehouse/search")
                                .then()
                                .statusCode(200)
                                .body("$", hasSize(0));
        }
}
