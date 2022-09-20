package MailDBweb.MailDBweb.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Integer> {

    boolean existsByUsername(String username);

    Account findByUsername(String username);

    Account findByNickname(String nickname);

    Optional<Account> findById(Integer accountid);

    List<Account> findAll();

    void deleteById(Integer accountid);

}