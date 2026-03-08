package com.seowon.coding.service;

import com.seowon.coding.domain.model.Product;
import com.seowon.coding.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    public Product updateProduct(Long id, Product product) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        product.setId(id);
        return productRepository.save(product);
    }
    
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> findProductsByCategory(String category) {
        // TODO #1: 구현 항목

        /** 풀이 : 각 메소드는 ResponseDTO(VO) 가 아닌 Entity를 반환하고 있음.
                 따라서 해당 풀이에서도 DTO(VO)와 Entity를 구분하지 않음.
        **/

        // 1. 인자로 받은 카테고리로 품목 리스트를 조회
        List<Product> products = productRepository.findByCategory(category);
        System.out.println("products 잘 넘어왔나 확인:{}" + products );
        // 2. Opiontal로 받지 않으므로 추가적 Null 처리
        if (products.isEmpty()) {
            throw new RuntimeException(" 해당 카테고리에서 발견된 상품이 없습니다.: " + category);
        }

        // 3. 반환
        return products;
    }

    /**
     * TODO #6 (리펙토링): 대량 가격 변경 로직을 도메인 객체 안으로 리팩토링하세요.
     */
    public void applyBulkPriceChange(List<Long> productIds, double percentage, boolean includeTax) {
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("empty productIds");
        }
        // 잘못된 구현 예시: double 사용, 루프 내 개별 조회/저장, 하드코딩 세금/반올림 규칙
        for (Long id : productIds) {
            Product p = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

            double base = p.getPrice() == null ? 0.0 : p.getPrice().doubleValue();
            double changed = base + (base * (percentage / 100.0)); // 부동소수점 오류 가능
            if (includeTax) {
                changed = changed * 1.1; // 하드코딩 VAT 10%, 지역/카테고리별 규칙 미반영
            }
            // 임의 반올림: 일관되지 않은 스케일/반올림 모드
            BigDecimal newPrice = BigDecimal.valueOf(changed).setScale(2, RoundingMode.HALF_UP);
            p.setPrice(newPrice);
            productRepository.save(p); // 루프마다 저장 (비효율적)
        }
    }
}
