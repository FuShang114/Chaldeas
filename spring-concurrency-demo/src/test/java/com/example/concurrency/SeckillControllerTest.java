package com.example.concurrency;

import com.example.concurrency.controller.SeckillController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 秒杀控制器测试类
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SeckillControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/v1/seckill/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
    
    @Test
    public void testGetSystemStatus() throws Exception {
        mockMvc.perform(get("/api/v1/seckill/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
    
    @Test
    public void testPing() throws Exception {
        mockMvc.perform(get("/api/v1/seckill/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }
    
    @Test
    public void testPingSlow() throws Exception {
        mockMvc.perform(get("/api/v1/seckill/ping/slow")
                .param("delay", "50"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong with 50ms delay"));
    }
    
    @Test
    public void testGetProducts() throws Exception {
        mockMvc.perform(get("/api/v1/seckill/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}