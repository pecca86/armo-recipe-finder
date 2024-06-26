package org.recipefinder.recipefinder.recipe;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RecipePagingRepository extends PagingAndSortingRepository<Recipe, Long>, JpaSpecificationExecutor<Recipe> {
}
