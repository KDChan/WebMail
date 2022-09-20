package MailDBweb.MailDBweb.Mail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RulesRepository extends JpaRepository<Hm_rules, Integer> {

    Optional<Hm_rules> findByRuleid(Integer folderid);

    Optional<Hm_rules> findByRuleaccountid(Integer accountid);

    List<Hm_rules> findAllByRuleaccountid(Integer accountid);

    Optional<Hm_rules> findByRulename(String rulename);

    List<Hm_rules> findAll();

    @Query(value = "SELECT MAX(rulesortorder) FROM Hm_rules ")
    public Integer maxsortorder();

}
