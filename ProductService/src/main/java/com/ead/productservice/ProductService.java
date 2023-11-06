package com.ead.productservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public String generateProductId(){
        Product lastEnteredProduct = productRepository.findFirstByOrderByProductIdDesc();
        if(lastEnteredProduct != null){
            int lastProductId = Integer.parseInt(lastEnteredProduct.getProductId().substring(1));
            int newProductId = lastProductId + 1;
            return "P" + newProductId;
        }else {
            return "P1";
        }
    }

    public ResponseEntity<?> addProduct(Product product){
        if(productRepository.findProductByProductName(product.getProductName()) != null){
            return ResponseEntity.ok("Product " + product.getProductName() + " is already entered to the inventory");
        }
        product.setProductId(generateProductId());
        return ResponseEntity.ok(productRepository.save(product));
    }

    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }

    public Product getProductById(String id){
        if(productRepository.findProductByProductId(id) == null){
            return null;
        }
        return productRepository.findProductByProductId(id);
    }

    public ResponseEntity<String> updateProduct(String id, Product product){
        Product productToUpdate = productRepository.findProductByProductId(id);
        if(productToUpdate == null){
            throw new ProductNotFountException("Product not found with ID :: " + id);
        }
        productToUpdate.setProductName(product.getProductName());
        productToUpdate.setUnitPrice(product.getUnitPrice());
        productToUpdate.setQuantity(product.getQuantity());
        productRepository.save(productToUpdate);
        return ResponseEntity.ok("Product updated successfully");
    }

    public ResponseEntity<String> deleteProduct(String productId){
        Product productToDelete = productRepository.findProductByProductId(productId);
        if(productToDelete == null){
            throw new ProductNotFountException("Product not found with ID :: " + productId);
        }
        productRepository.deleteProductByProductId(productId);
        return ResponseEntity.ok("Product deleted successfully");
    }

    public ResponseEntity<?> checkAvailability(String productId, int quantity){
        Product product = getProductById(productId);
        if(product == null){
            return null;
        }
        if(product.getQuantity()>=quantity){
            return  ResponseEntity.ok(1);
        }
        return ResponseEntity.ok(0);
    }

    public ResponseEntity<?> updateQuantity(String productId, int quantity){
        Product product = getProductById(productId);
        if(product == null){
            return null;
        }
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
        return ResponseEntity.ok(product);
    }
}
