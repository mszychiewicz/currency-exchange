package io.github.mszychiewicz.currencyexchange.infrastructure;

import io.github.mszychiewicz.currencyexchange.domain.Account;
import io.github.mszychiewicz.currencyexchange.domain.AccountRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InMemoryAccountRepository implements AccountRepository {
    private final Map<UUID, Account> accounts;

    @Override
    public UUID save(Account account) {
        UUID id = account.getId();
        accounts.put(id, account);
        return id;
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return Optional.ofNullable(accounts.get(id));
    }
}
