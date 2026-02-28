package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

import java.util.concurrent.CompletableFuture;

import org.jboss.logging.Logger;

@ApplicationScoped
public class StoreEventObserver {

  private static final Logger LOGGER = Logger.getLogger(StoreEventObserver.class.getName());

  @Inject
  LegacyStoreManagerGateway legacyStoreManagerGateway;

  public void onStoreCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreCreatedEvent event) {
    // This ONLY runs if the DB transaction committed successfully.
    // To keep the API response fast, you can hand this off to an Executor.
    CompletableFuture.runAsync(() -> {
      legacyStoreManagerGateway.createStoreOnLegacySystem(event.getStore());
    });
  }

  public void onStoreUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreUpdatedEvent event) {
    LOGGER.info("Store updated event received, syncing with legacy system: " + event.getStore().id);
    CompletableFuture.runAsync(() -> {
      legacyStoreManagerGateway.updateStoreOnLegacySystem(event.getStore());
    });
  }
}
