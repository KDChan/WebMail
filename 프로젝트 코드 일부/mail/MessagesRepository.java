package MailDBweb.MailDBweb.Mail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/** IMAP Folder id
 * INBOX - 1
 * SENT - 2
 * WASTE - 3
 * TEMPORARY - 4
 * SPAM - 5
 **/

public interface MessagesRepository extends JpaRepository<Hm_messages, Integer> {

    Optional<Hm_messages> findById(Integer messageid);

    List<Hm_messages> findAll();

    void deleteAllByMessagefolderid(Integer folderid);

    Optional<Hm_messages> findByMessageuid(Long uid);
    Optional<Hm_messages> findByMessageuidAndMessagefolderid(Long uid, Integer folderid);


    List<Hm_messages> findAllByMessageuid(Long uid);
}
