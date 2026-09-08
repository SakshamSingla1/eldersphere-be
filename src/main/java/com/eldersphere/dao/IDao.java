package com.eldersphere.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.Optional;

public interface IDao<T, ID extends Serializable> {

    default T findById(ID id, boolean noException) {
        Optional<T> result = getRepository().findById(id);
        if (noException) {
            return result.orElse(null);
        } else {
            return result.orElseThrow(() -> new RuntimeException("Resource not found with id: " + id));
        }
    }

    default T findById(ID id) {
        return findById(id, false);
    }

    JpaRepository<T, ID> getRepository();
}
