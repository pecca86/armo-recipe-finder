package org.recipefinder.recipefinder.recipe;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Page<Recipe> findAllByCustomerId(Long customerId, PageRequest pageRequest);
    List<Recipe> findAllById(Long id);

//
//    Page<Recipe> findAllByAccountIdAndIsVegan(Long accountId, Boolean isVegan, PageRequest pageRequest);
//
//    Page<Recipe> findAllByAccountIdAndNumServingsGreaterThanEqual(Long accountId, Integer numServings, PageRequest pageRequest);
//
//    Page<Recipe> findAllByAccountIdAndCookTimeLessThanEqual(Long accountId, Integer cookTime, PageRequest pageRequest);
//
//    Page<Recipe> findAllByAccountIdAndIsVeganAndNumServingsGreaterThanEqualAndCookTimeLessThanEqual(Long accountId, Boolean isVegan, Integer numServings, Integer cookTime, PageRequest pageRequest);


}
