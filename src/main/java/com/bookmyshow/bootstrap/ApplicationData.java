package com.bookmyshow.bootstrap;

import com.bookmyshow.repository.CatalogRepository;
import com.bookmyshow.repository.ShowSeatInventoryRepository;

public record ApplicationData(CatalogRepository catalogRepository,
                              ShowSeatInventoryRepository inventoryRepository) {
}
