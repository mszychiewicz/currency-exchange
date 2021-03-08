package io.github.mszychiewicz.currencyexchange.domain;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    UUID save(Account account);

    Optional<Account> findById(UUID id);
}
