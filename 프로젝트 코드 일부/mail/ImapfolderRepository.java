package MailDBweb.MailDBweb.Mail;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
/** IMAP Folder id
  * INBOX - 1
  * SENT - 2
  * WASTE - 3
  * TEMPORARY - 4
  * SPAM - 5
  **/
public interface ImapfolderRepository extends JpaRepository<Hm_imapfolders, Integer> {

    Optional<Hm_imapfolders> findById(Integer folderid);

    List<Hm_imapfolders> findAll();

}