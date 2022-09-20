package MailDBweb.MailDBweb.Mail;

import MailDBweb.MailDBweb.account.Account;
import MailDBweb.MailDBweb.account.AccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
@AllArgsConstructor
public class MailService {
    private AccountRepository accountRepository;
    private ImapfolderRepository imapfolderRepository;
    private ReadIMAP readIMAP;
    private RuleactionsRepository ruleactionsRepository;
    private RulecriteriasRepository rulecriteriasRepository;
    private RulesRepository rulesRepository;
    private MessagesRepository messagesRepository;

    //private final static String FROM_ADDRESS = "user1@stsoft.kr";

    public void reply(SendMailDto mailDto, String pass, GetImapMail getImapMail, Long uid) throws Exception {
        Account account = accountRepository.findByUsername(mailDto.getFrom());

        Message ori = getImapMail.getMessageByUID(uid, false);

        Message msg = readIMAP.sendreply(ori,account.getAccountaddress(), account.getNickname(), pass, mailDto);

        readIMAP.saveSendMail(getImapMail, msg);

    }
    //메일 보내기
    public void setMailSender(SendMailDto mailDto, String pass, GetImapMail getImapMail, String[] path) throws Exception {
        Account account = accountRepository.findByUsername(mailDto.getFrom());

        if (mailDto.getSubject().isEmpty()) {
            mailDto.setSubject("(제목 없음)");
        }
        try {
            mailDto.getTo();
            mailDto.getFrom();
        } catch (Exception exception) {
            exception.getStackTrace();
        }

        Message msg = readIMAP.sendMail(account.getAccountaddress(), pass, account.getNickname(), mailDto.getTo(), mailDto.getSubject(),
                mailDto.getText(), true, mailDto.getCC(), mailDto.getBCC(), path);

        readIMAP.saveSendMail(getImapMail, msg);
    }

    //메일 임시보관함 저장
    public void saveTemp(SendMailDto mailDto, String pass, GetImapMail getImapMail, String[] path) throws Exception {
        Account account = accountRepository.findByUsername(mailDto.getFrom());

        if (mailDto.getSubject().isEmpty()) {
            mailDto.setSubject("(제목 없음)");
        }
        if (mailDto.getTo().isEmpty()) {
            mailDto.setTo("NULL@To");
        }
        if (mailDto.getFrom().isEmpty()) {
            mailDto.setFrom("null@From");
        }

        Message msg = readIMAP.sendMail(account.getAccountaddress(), pass, account.getNickname(), mailDto.getTo(), mailDto.getSubject(),
                mailDto.getText(), false, mailDto.getCC(), mailDto.getBCC(), path);
        readIMAP.saveTempSendMail(getImapMail, msg);
    }

    //메일 폴더 설정
    public void SetMailFolder(Integer id, String foldername) {
        Hm_imapfolders hm_imapfolders = Hm_imapfolders.builder()
                .folderaccountid(id)
                .folderparentid(-1)
                .foldername(foldername)
                .folderissubscribed(1)
                .foldercreationtime(Instant.now())
                .foldercurrentuid(2)
                .build();
        imapfolderRepository.save(hm_imapfolders);
    }

    //메일 리스트 불러오기
    public List<ReadVo> getMailList(GetImapMail getImapMail, String file, boolean onlyNotRead) throws Exception {

        return readIMAP.LoadMailList(getImapMail, file, onlyNotRead).stream().
                sorted(Comparator.comparing(ReadVo::getSentDate).reversed())
                .collect(Collectors.toList());
    }

    //메일 상세보기
    public ReadVo getMail(GetImapMail getImapMail, Long uid) throws Exception {
        Integer folderid = messagesRepository.findByMessageuid(uid).get().getMessagefolderid();
        String foldername = imapfolderRepository.findById(folderid).get().getFoldername();

        Message msg = getImapMail.getMessageByUID(uid, true);

        return readIMAP.msgToVo(foldername, msg);
    }

    //읽음 표시
    public void setRead(GetImapMail getImapMail, List<Long> uidList) throws MessagingException {
        for (Long uid : uidList) {
            getImapMail.getMessageByUID(uid, true);
        }
    }

    //중요메일 설정, 해제
    public void setImportant(GetImapMail getImapMail, Long uid) throws Exception {

        Message msg = getImapMail.getMessageByUID(uid, false);

        if (uid == ((UIDFolder) msg.getFolder()).getUID(msg)) {
            if (msg.isSet(Flags.Flag.FLAGGED)) {
                msg.setFlag(Flags.Flag.FLAGGED, false);
            } else {
                msg.setFlag(Flags.Flag.FLAGGED, true);
            }
        }
    }

    //메일 삭제
    public void deleteMail(List<Long> uid, GetImapMail getImapMail, String username) throws Exception {

        for (Long id : uid) {
            Optional<Hm_messages> msg = messagesRepository.findByMessageuid(uid.get(0));
            Integer folderid = msg.get().getMessagefolderid();
            String folderName = imapfolderRepository.findById(folderid).get().getFoldername().toUpperCase();
            readIMAP.delete(id, folderName, getImapMail);
        }


        File mailDir = new File("C:\\Users\\kd706\\Desktop\\hMailServer\\Data\\stsoft.kr\\" + username);

        if (mailDir.isDirectory()) {
            for (File dirPath : Objects.requireNonNull(mailDir.listFiles())) {
                if (Objects.requireNonNull(dirPath.listFiles()).length == 0) {
                    dirPath.delete();
                }
            }
        }
        getImapMail.logoutIMAP(true);

    }

    //휴지통 비우기
    public void cleanUp(GetImapMail getImapMail, String username) throws MessagingException {

        readIMAP.cleanUpWaste(getImapMail);

        File mailDir = new File("C:\\Users\\kd706\\Desktop\\hMailServer\\Data\\stsoft.kr\\" + username);

        if (mailDir.isDirectory()) {
            for (File dirPath : Objects.requireNonNull(mailDir.listFiles())) {
                if (Objects.requireNonNull(dirPath.listFiles()).length == 0) {
                    dirPath.delete();
                }
            }
        }
    }

    //spam rule 등록
    public void SpamRule(Integer accountid, String address) {
        Hm_rules hm_rules = Hm_rules.builder()
                .ruleaccountid(accountid)
                .rulename(address + "_SPAM")
                .ruleactive(1)
                .ruleuseand(0)
                .rulesortorder(1)
                .build();
        rulesRepository.save(hm_rules);

        Hm_rule_actions hm_rule_actions = Hm_rule_actions.builder()
                .actionruleid(1)
                .actiontype(4)
                .actionimapfolder("SPAM")
                .actionrouteid(0)
                .actionbody("")
                .actionsubject("")
                .actionfromname("")
                .actionfromaddress("")
                .actionto("")
                .actionfilename("")
                .actionscriptfunction("")
                .actionvalue("")
                .actionheader("")
                .actionsortorder(1)
                .build();
        ruleactionsRepository.save(hm_rule_actions);
    }

    //spam 목록 등록
    public String SpamRegister(String address, Integer accountid) {

        if (rulecriteriasRepository.existsByCriteriamatchvalue(address)) {
            return "duplicate";
        } else {
            Hm_rule_criterias hm_rule_criterias = Hm_rule_criterias.builder()
                    .criteriaruleid(accountid)
                    .criteriausepredefined(1)
                    .criteriapredefinedfield(1)
                    .criteriamatchtype(2)
                    .criteriamatchvalue(address)
                    .criteriaheadername("")
                    .setdate(Instant.now())
                    .build();
            rulecriteriasRepository.save(hm_rule_criterias);
            return "success";
        }

    }

    //spam 목록 삭제
    public void deleteSpamList(Integer accountid, List<String> addressList) {
        Integer ruleid = rulesRepository.findByRuleaccountid(accountid).get().getRuleid();

        for (String address : addressList) {
            rulecriteriasRepository.deleteByCriteriaruleidAndCriteriamatchvalue(ruleid, address);
        }
    }

    //등록된 spam 리스트 출력
    public List<RuleDto> getSpamList(Integer accountid) {
        return readIMAP.registerSpamList(rulecriteriasRepository.findAllByCriteriaruleid(accountid)).stream().
                sorted(Comparator.comparing(RuleDto::getSetdate).reversed())
                .collect(Collectors.toList());
    }

    //메일 이동 (보낸, 받은, 스팸, 휴지통)
    public String moveMail(List<Long> msgList, GetImapMail getImapMail, String target) throws Exception {

        String err = "null";

        for (Long uid : msgList) {
            Optional<Hm_messages> msg = messagesRepository.findByMessageuid(uid);
            Integer folderid = msg.get().getMessagefolderid();
            String folderName = imapfolderRepository.findById(folderid).get().getFoldername().toUpperCase();

            err = readIMAP.movemail(uid, getImapMail, folderName, target);

            if (!(Objects.equals(err, null))) {
                return err;
            }
        }
        return err;
    }

    //첨부파일 목록
    public String downloadAttachFiles(GetImapMail getImapMail, String username, Long uid, Integer attachIndex) throws MessagingException, IOException {

        String path = getImapMail.downloadAttachFiles(getImapMail, username, uid, attachIndex);

        if (path.length()>0){
            return path;
        } else {
            System.out.println("not found files");
            return null;
        }
    }

    //메일 답장
    public ReadVo replyMsg(GetImapMail getImapMail, Long uid) throws Exception {
        Message msg = getImapMail.getMessageByUID(uid, false);

        ReadVo readVo = readIMAP.msgToVo("SENT", msg);

        readVo.setToaddress(((InternetAddress) msg.getReplyTo()[0]).getAddress());
        readVo.setSubject("RE: " + readVo.getSubject());

        return readVo;
    }

    //메일 전달
    public ReadVo forwardMsg(GetImapMail getImapMail, Long uid) throws Exception {
        Message msg = getImapMail.getMessageByUID(uid, false);

        ReadVo readVo = readIMAP.msgToVo("SENT", msg);

        readVo.setFromaddress("");
        readVo.setSubject("FW: " + readVo.getSubject());
        return readVo;
    }

}