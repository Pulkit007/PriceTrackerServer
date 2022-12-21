package com.pulkit077.pricetrackerserver.scheduler;

import com.pulkit077.pricetrackerserver.model.Product;
import com.pulkit077.pricetrackerserver.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductScraperScheduler {

    @Autowired
    private ProductRepository productRepository;

    // Defined a cron job which will run at 00:00 everyday
    @Scheduled(cron = "0 0 * * * ?")
    public void scrapeProductsCronJob() {
        List<Product> products = productRepository.findAll();
        for(Product product : products) {
            ScraperThread thread = new ScraperThread(product, productRepository);
            thread.start();
        }
    }
}
