package com.omnichannel.platform.integration;

import com.omnichannel.platform.common.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integration")
@Tag(name = "CRM Integration", description = "Customer lookup through whichever adapter is configured")
public class CustomerController {

    private final CustomerDataAdapterFactory adapters;

    public CustomerController(CustomerDataAdapterFactory adapters) {
        this.adapters = adapters;
    }

    @GetMapping("/adapter")
    @Operation(summary = "Which adapter is active and which ones exist")
    public AdapterInfo adapter() {
        return new AdapterInfo(adapters.current().type(), adapters.availableTypes());
    }

    @GetMapping("/customers/{customerId}")
    @Operation(summary = "Look up a customer through the active adapter")
    public CustomerProfile customer(@PathVariable String customerId) {
        return adapters.current().findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer " + customerId + " not found"));
    }

    public record AdapterInfo(String active, List<String> available) {
    }
}
