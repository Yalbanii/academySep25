package com.javatechie.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatechie.entity.Stock;
import com.javatechie.service.StockService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequestMapping("/stocks")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("/list")
    public List<Stock> getAllStocks() {
        return stockService.getAllStocks();
    }

    @GetMapping("/stream")
    public StreamingResponseBody streamStocks(HttpServletResponse response) {
        response.setContentType("text/event-stream");
        return outputStream -> {
            stockService.getAllStocks()
                    .forEach(stock -> {
                        try {
                            String json = new ObjectMapper()
                                    .writeValueAsString(stock) + "\n";
                            outputStream.write(json.getBytes());
                            outputStream.flush();

                        } catch (Exception ex) {
                            throw new RuntimeException(ex.getMessage());
                        }
                    });
        };
    }

 


}
