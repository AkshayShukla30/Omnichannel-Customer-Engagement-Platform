package com.omnichannel.platform.integration.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.omnichannel.platform.integration.CrmProperties;
import com.omnichannel.platform.integration.CustomerProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.DefaultResourceLoader;

class FileCustomerAdapterTest {

    @TempDir
    Path dir;

    private FileCustomerAdapter adapterFor(String location) {
        CrmProperties props = new CrmProperties("file", new CrmProperties.Rest("http://x", "json"),
                new CrmProperties.FileSource(location));
        return new FileCustomerAdapter(new DefaultResourceLoader(), props, new ObjectMapper());
    }

    @Test
    void readsCsvIncludingQuotedFields() throws IOException {
        Path csv = dir.resolve("customers.csv");
        Files.writeString(csv, """
                customerId,fullName,email,phone,segment,accountNumber,accountBalance,activeLoans,cardType
                CUST-1,"Sharma, Aarav",a@example.com,+919800010001,PREMIER,111,2500.75,2,PLATINUM
                """);

        Optional<CustomerProfile> profile = adapterFor(csv.toUri().toString()).findById("CUST-1");

        assertThat(profile).isPresent();
        assertThat(profile.get().fullName()).isEqualTo("Sharma, Aarav");
        assertThat(profile.get().accountBalance()).isEqualByComparingTo(new BigDecimal("2500.75"));
        assertThat(profile.get().source()).isEqualTo("file");
    }

    @Test
    void readsJsonAndFindsByPhoneIgnoringFormatting() throws IOException {
        Path json = dir.resolve("customers.json");
        Files.writeString(json, """
                [{"customerId":"CUST-2","fullName":"Diya Verma","phone":"+919800010002","segment":"STANDARD",
                  "accountNumber":"222","accountBalance":10,"activeLoans":0,"cardType":"CLASSIC"}]""");

        Optional<CustomerProfile> profile = adapterFor(json.toUri().toString()).findByPhone("98000 10002");

        assertThat(profile).isPresent();
        assertThat(profile.get().customerId()).isEqualTo("CUST-2");
    }

    @Test
    void shippedSampleCsvLoads() {
        assertThat(adapterFor("classpath:data/customers.csv").findById("CUST-1001")).isPresent();
    }

    @Test
    void shippedSampleJsonLoads() {
        assertThat(adapterFor("classpath:data/customers.json").findById("CUST-1010")).isPresent();
    }

    @Test
    void missingFileFailsOnFirstUseNotAtStartup() {
        FileCustomerAdapter adapter = adapterFor(dir.resolve("nope.csv").toUri().toString());

        assertThatThrownBy(() -> adapter.findById("x")).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void csvSplitterHandlesEscapedQuotes() {
        assertThat(CsvLineParser.split("a,\"he said \"\"hi\"\"\",c")).isEqualTo(List.of("a", "he said \"hi\"", "c"));
    }
}
