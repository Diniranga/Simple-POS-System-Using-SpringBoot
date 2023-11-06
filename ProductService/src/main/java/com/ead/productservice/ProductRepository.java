package com.ead.productservice;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product,String> {
   Product findFirstByOrderByProductIdDesc();
   Product findProductByProductName(String productName);
   Product findProductByProductId(String productId);
   Product deleteProductByProductId(String productId);
}
