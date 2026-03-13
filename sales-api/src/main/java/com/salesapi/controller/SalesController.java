package com.salesapi.controller;

import com.salesapi.model.CitySale;
import com.salesapi.model.SellerSale;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    private final JdbcTemplate jdbcTemplate;

    public SalesController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @GetMapping("/cities/top")
    public List<CitySale> getTopCities() {
        String sql = "SELECT city_name, total_amount FROM top_sales_city ORDER BY total_amount DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new CitySale(
                rs.getString("city_name"),
                rs.getDouble("total_amount")
        ));
    }

    @GetMapping("/sellers/top")
    public List<SellerSale> getTopSellers() {
        String sql = "SELECT seller_id, total_amount, avg_performance FROM top_sales_seller ORDER BY total_amount DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new SellerSale(
                rs.getString("seller_id"),
                rs.getDouble("total_amount"),
                rs.getDouble("avg_performance")
        ));
    }
}