package MailDBweb.MailDBweb.Mail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RuleactionsRepository extends JpaRepository<Hm_rule_actions, Integer> {

    Optional<Hm_rule_actions> findByActionid(Integer actionid);

    List<Hm_rule_actions> findAll();


    @Query(value = "SELECT MAX(actionsortorder) FROM Hm_rule_actions ")
    public Integer maxsortorder();

}
