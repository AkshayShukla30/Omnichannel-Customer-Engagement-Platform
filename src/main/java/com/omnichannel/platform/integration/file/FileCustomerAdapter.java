package com.omnichannel.platform.integration.file;

import com.omnichannel.platform.integration.CrmProperties;
import com.omnichannel.platform.integration.CustomerDataAdapter;
import com.omnichannel.platform.integration.CustomerProfile;
import com.omnichannel.platform.integration.PhoneNumbers;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class FileCustomerAdapter implements CustomerDataAdapter {

    public static final String TYPE = "file";

    private final ResourceLoader resourceLoader;
    private final String path;
    private final ObjectMapper mapper;
    private volatile Map<String, CustomerProfile> customers;

    public FileCustomerAdapter(ResourceLoader resourceLoader, CrmProperties properties, ObjectMapper mapper) {
        this.resourceLoader = resourceLoader;
        this.path = properties.file().path();
        this.mapper = mapper;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Optional<CustomerProfile> findById(String customerId) {
        return Optional.ofNullable(customers().get(customerId));
    }

    @Override
    public Optional<CustomerProfile> findByPhone(String phone) {
        String wanted = PhoneNumbers.normalize(phone);
        return customers().values().stream()
                .filter(c -> PhoneNumbers.normalize(c.phone()).equals(wanted))
                .findFirst();
    }

    // loaded on first use so an unused adapter never fails startup because of a missing file
    private Map<String, CustomerProfile> customers() {
        Map<String, CustomerProfile> loaded = customers;
        if (loaded == null) {
            synchronized (this) {
                if (customers == null) {
                    customers = load();
                }
                loaded = customers;
            }
        }
        return loaded;
    }

    private Map<String, CustomerProfile> load() {
        Resource resource = resourceLoader.getResource(path);
        if (!resource.exists()) {
            throw new IllegalStateException("CRM file not found: " + path);
        }
        String name = String.valueOf(resource.getFilename()).toLowerCase();
        try (InputStream in = resource.getInputStream()) {
            List<CustomerProfile> rows;
            if (name.endsWith(".json")) {
                rows = List.of(mapper.readValue(in, CustomerProfile[].class));
            } else if (name.endsWith(".csv")) {
                rows = readCsv(in);
            } else {
                throw new IllegalStateException("Unsupported CRM file type (use .csv or .json): " + path);
            }
            Map<String, CustomerProfile> byId = new HashMap<>();
            rows.forEach(c -> byId.put(c.customerId(), c.withSource(TYPE)));
            return byId;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read CRM file " + path, e);
        }
    }

    private List<CustomerProfile> readCsv(InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return List.of();
            }
            Map<String, Integer> col = new HashMap<>();
            List<String> header = CsvLineParser.split(headerLine);
            for (int i = 0; i < header.size(); i++) {
                col.put(header.get(i).trim(), i);
            }
            return reader.lines().filter(l -> !l.isBlank()).map(line -> toProfile(CsvLineParser.split(line), col)).toList();
        }
    }

    private CustomerProfile toProfile(List<String> f, Map<String, Integer> col) {
        return new CustomerProfile(value(f, col, "customerId"), value(f, col, "fullName"), value(f, col, "email"),
                value(f, col, "phone"), value(f, col, "segment"), value(f, col, "accountNumber"),
                new BigDecimal(value(f, col, "accountBalance")), Integer.parseInt(value(f, col, "activeLoans")),
                value(f, col, "cardType"), TYPE);
    }

    private String value(List<String> fields, Map<String, Integer> col, String name) {
        Integer index = col.get(name);
        if (index == null || index >= fields.size()) {
            throw new IllegalArgumentException("CSV is missing column " + name);
        }
        return fields.get(index).trim();
    }
}
