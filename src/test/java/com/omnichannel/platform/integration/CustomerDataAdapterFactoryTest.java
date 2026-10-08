package com.omnichannel.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;

class CustomerDataAdapterFactoryTest {

    private CustomerDataAdapter adapter(String type) {
        CustomerDataAdapter adapter = mock(CustomerDataAdapter.class);
        when(adapter.type()).thenReturn(type);
        return adapter;
    }

    private CrmProperties config(String adapter) {
        return new CrmProperties(adapter, new CrmProperties.Rest("http://x", "json"), new CrmProperties.FileSource("x.csv"));
    }

    @Test
    void picksTheAdapterNamedInConfig() {
        CustomerDataAdapter file = adapter("file");
        CustomerDataAdapterFactory factory = new CustomerDataAdapterFactory(
                List.of(adapter("core-banking"), file, adapter("rest-crm")), config("file"));

        assertThat(factory.current()).isSameAs(file);
        assertThat(factory.availableTypes()).containsExactly("core-banking", "file", "rest-crm");
    }

    @Test
    void failsFastWithTheAvailableOptionsWhenConfigIsWrong() {
        assertThatThrownBy(() -> new CustomerDataAdapterFactory(List.of(adapter("file")), config("salesforce")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("salesforce")
                .hasMessageContaining("file");
    }
}
