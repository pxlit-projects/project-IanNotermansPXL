package be.pxl.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 *  GatewayServiceApplication
 *
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
