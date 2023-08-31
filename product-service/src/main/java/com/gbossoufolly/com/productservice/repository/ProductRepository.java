package com.gbossoufolly.com.productservice.repository;

import com.gbossoufolly.com.productservice.models.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
