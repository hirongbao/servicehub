package com.shirongbao.admin;

import com.shirongbao.admin.mapper.HttpRequestLogMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
public class LogMapperTest {

    @Autowired
    private HttpRequestLogMapper logMapper;

    @Test
    public void testQueries() {
        System.out.println("Starting tests...");
        try {
            logMapper.getSummary(24);
            System.out.println("getSummary SUCCESS");
        } catch (Exception e) {
            System.out.println("getSummary ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            logMapper.getHourlyTrend(24);
            System.out.println("getHourlyTrend SUCCESS");
        } catch (Exception e) {
            System.out.println("getHourlyTrend ERROR: " + e.getMessage());
        }
        
        try {
            logMapper.getStatusDistribution(24);
            System.out.println("getStatusDistribution SUCCESS");
        } catch (Exception e) {
            System.out.println("getStatusDistribution ERROR: " + e.getMessage());
        }
        
        try {
            logMapper.getTopPaths(24);
            System.out.println("getTopPaths SUCCESS");
        } catch (Exception e) {
            System.out.println("getTopPaths ERROR: " + e.getMessage());
        }
        
        try {
            logMapper.getTopIps(24);
            System.out.println("getTopIps SUCCESS");
        } catch (Exception e) {
            System.out.println("getTopIps ERROR: " + e.getMessage());
        }
        
        try {
            logMapper.getLatencyDistribution(24);
            System.out.println("getLatencyDistribution SUCCESS");
        } catch (Exception e) {
            System.out.println("getLatencyDistribution ERROR: " + e.getMessage());
        }
    }
}
