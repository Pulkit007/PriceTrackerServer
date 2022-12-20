package com.pulkit077.pricetrackerserver.repository;

import com.pulkit077.pricetrackerserver.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Product findProductByUrl(String url);
}
