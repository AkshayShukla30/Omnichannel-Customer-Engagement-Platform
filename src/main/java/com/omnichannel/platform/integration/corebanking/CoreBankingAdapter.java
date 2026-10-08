package com.omnichannel.platform.integration.corebanking;

import com.omnichannel.platform.integration.CustomerDataAdapter;
import com.omnichannel.platform.integration.CustomerProfile;
import com.omnichannel.platform.integration.PhoneNumbers;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class CoreBankingAdapter implements CustomerDataAdapter {

    public static final String TYPE = "core-banking";

    private static final String SELECT = "SELECT customer_id, full_name, email, phone, segment, account_number, "
            + "account_balance, active_loans, card_type FROM core_banking_customers ";

    private static final RowMapper<CustomerProfile> MAPPER = (rs, rowNum) -> new CustomerProfile(
            rs.getString("customer_id"), rs.getString("full_name"), rs.getString("email"), rs.getString("phone"),
            rs.getString("segment"), rs.getString("account_number"), rs.getBigDecimal("account_balance"),
            rs.getInt("active_loans"), rs.getString("card_type"), TYPE);

    private final JdbcTemplate jdbc;

    public CoreBankingAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Optional<CustomerProfile> findById(String customerId) {
        return jdbc.query(SELECT + "WHERE customer_id = ?", MAPPER, customerId).stream().findFirst();
    }

    @Override
    public Optional<CustomerProfile> findByPhone(String phone) {
        String sql = SELECT + "WHERE RIGHT(REGEXP_REPLACE(phone, '[^0-9]', ''), 10) = ?";
        return jdbc.query(sql, MAPPER, PhoneNumbers.normalize(phone)).stream().findFirst();
    }
}
