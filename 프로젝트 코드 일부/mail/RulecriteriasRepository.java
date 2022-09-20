package MailDBweb.MailDBweb.Mail;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RulecriteriasRepository extends JpaRepository<Hm_rule_criterias, Integer> {

    Optional<Hm_rule_criterias> findByCriteriaruleid(Integer ruleid);

    List<Hm_rule_criterias> findAll();

    List<Hm_rule_criterias> findAllByCriteriaruleid(Integer ruleid);

    boolean existsByCriteriamatchvalue(String address);

    void deleteByCriteriaruleidAndCriteriamatchvalue(Integer ruleid, String address);
}
