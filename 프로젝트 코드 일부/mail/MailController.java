package MailDBweb.MailDBweb.Mail;

import MailDBweb.MailDBweb.account.AccountRepository;
import MailDBweb.MailDBweb.account.LoginForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Null;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;
    private final AccountRepository accountRepository;
    private LoginForm loginForm;
    private GetImapMail imapMail;
    private String EmailAddress;

    /*@GetMapping("/email")
    public String mail(Model model, Principal principal) {
        String file = "INBOX";
        String EmailAddress = accountRepository.findByUsername(principal.getName()).getAccountaddress();
        String pass = accountRepository.findByUsername(principal.getName()).getAccountpassword();

        try {
            List<ReadVo> readVo = mailService.getMailList(EmailAddress, pass, file);
            model.addAttribute("readVo", readVo);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        model.addAttribute("mode", "mailbox");
        return "email";
    }*/

    @GetMapping("/log-in")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "exception", required = false) String exception,
                        Model model) {

        model.addAttribute("error", error);
        model.addAttribute("exception", exception);
        model.addAttribute("loginForm", new LoginForm());

        return "log-in";
    }

    @PostMapping("/log-in")
    public String getloginform(LoginForm loginForm) {
        String EmailAddress = accountRepository.findByUsername(loginForm.getUsername()).getAccountaddress();

        GetImapMail getImapMail = new GetImapMail();

        try {
            getImapMail.LoginMailFolder(EmailAddress, loginForm.getPassword(), true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        this.loginForm = loginForm;
        this.EmailAddress = EmailAddress;

        return "redirect:/";
    }

    @GetMapping("/log-out")
    public String getLogOut() throws MessagingException {
        imapMail.logoutIMAP(false);
        return "/log-out";
    }

    @GetMapping("/mail")
    public String mail(Model model) throws MessagingException {
        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(), true);

        try {
            List<ReadVo> readVo = mailService.getMailList(getImapMail, "INBOX", false);
            model.addAttribute("readVo", readVo);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        model.addAttribute("mode", "INBOX");
        return "/mail";
    }

    @GetMapping("/mail/all")
    public String allmall(Model model) throws MessagingException {
        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(),true);

        try {
            List<ReadVo> readVo = mailService.getMailList(getImapMail, "ALL", false);
            model.addAttribute("readVo", readVo);
        } catch (Exception ignored) {
        }

        model.addAttribute("mode", "ALL");

        return "/mail";
    }

    @GetMapping("/mail/receive")
    public String receivemailbox(Model model) throws MessagingException {
        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(),true);

        try {
            List<ReadVo> readVo = mailService.getMailList(getImapMail, "INBOX", false);
            model.addAttribute("readVo", readVo);
        } catch (Exception ignored) {
        }

        model.addAttribute("mode", "INBOX");

        return "/mail";
    }

    @GetMapping("/mail/notread")
    public String onlyNotRead(Model model) throws MessagingException {
        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(),true);

        try {
            List<ReadVo> readVo = mailService.getMailList(getImapMail, "INBOX", true);
            model.addAttribute("readVo", readVo);
        } catch (Exception ignored) {
        }

        model.addAttribute("mode", "notread");

        return "/mail";
    }

    @GetMapping("/mail/sent")
    public String sendMailBox(Model model) throws MessagingException {

        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(),true);

        try {
            List<ReadVo> readVo = mailService.getMailList(getImapMail, "SENT", false);
            model.addAttribute("readVo", readVo);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        model.addAttribute("mode", "SENT");

        return "/mail";
    }

    @GetMapping("/mail/{uid}")
    public String ReadMail(@PathVariable("uid") Long id, Model model, HttpServletRequest request) throws MessagingException {
        GetImapMail getImapMail = new GetImapMail();
        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(),false);

        try {
            ReadVo readVo = mailService.getMail(getImapMail, id);
            model.addAttribute("readVo", readVo);
            model.addAttribute("username", loginForm.getUsername());
            model.addAttribute("mode", "readmail");

        } catch (Exception ignored) {
        }

        request.getSession().setAttribute("redirectURL", "/mail/" + id);

        return "/mail";
    }

    @RequestMapping(value = "/downloadattach/{uid}&attachIndex={index}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public void downloadfile(@PathVariable("uid") Long uid, @PathVariable("index") Integer index, HttpServletResponse response) throws Exception{
        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(),true);

        String path = mailService.downloadAttachFiles(getImapMail, loginForm.getUsername(), uid, index);

        String[] a = path.split("\\\\");
        String filename = a[a.length -1];

        filename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        File file = new File(path);

        try {
            response.setHeader("content-Disposition", "attachment;filename="+filename);
            FileInputStream fileInputStream = new FileInputStream(file);
            OutputStream outputStream = response.getOutputStream();
            FileCopyUtils.copy(fileInputStream, outputStream);
            response.flushBuffer();

            fileInputStream.close();
            outputStream.flush();

        } catch (Exception ignored){
        }
    }

    @GetMapping("/mail/send")
    public String mailSendPage(Model model) {

        model.addAttribute("mode", "send");
        model.addAttribute("readVo", new ReadVo());
        return "/mail";
    }

    @GetMapping("/mail/send/{uid}")
    public String TempMailSendPage(@PathVariable("uid") Long uid, Model model) throws MessagingException {

        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(),true);

        try {
            ReadVo readVo = mailService.getMail(getImapMail, uid);
            model.addAttribute("id", uid);
            model.addAttribute("To", readVo.getToname());
            model.addAttribute("Body", readVo.getBody());
            model.addAttribute("Sub", readVo.getSubject());
        } catch (Exception ignored) {
        }

        model.addAttribute("fromAddress", this.EmailAddress);
        model.addAttribute("mode", "sendmail");

        return "/mail";
    }

    @PostMapping("/mail/send")
    public String SendMail(@RequestParam("mode") String mode, @RequestParam("file") MultipartFile[] uploadfile, SendMailDto sendMailDto,
                           Model model, @Nullable @RequestParam("uid") Long uid, @Nullable @RequestParam("reply") String reply) throws Exception {

        String[] path = new String[uploadfile.length];
        System.out.println("path: " + path[0]);
        if (path[0] != null) {

            int i = 0;

            for (MultipartFile file : uploadfile) {
                String a = "C:\\Users\\kd706\\Desktop\\upload\\" + file.getOriginalFilename();
                path[i] = "C:\\Users\\kd706\\Desktop\\upload\\" + file.getOriginalFilename();
                file.transferTo(new File(a));
                i++;
            }
        } else {
            System.out.println("null!");
        }

        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(),false);

        sendMailDto.setFrom(loginForm.getUsername());


        if (reply != null){
            System.out.println("reply msg");
            System.out.println("uid: " + uid);
            mailService.reply(sendMailDto, loginForm.getPassword(),getImapMail, uid);
        }
        else {
            if (Objects.equals(mode, "send")) {
                mailService.setMailSender(sendMailDto, loginForm.getPassword(), getImapMail, path);
                if (uid != null) {
                    mailService.deleteMail(Collections.singletonList(uid), getImapMail, loginForm.getUsername());
                }
            } else if (Objects.equals(mode, "save")) {
                mailService.saveTemp(sendMailDto, loginForm.getPassword(), getImapMail, path);
            }
        }

        model.addAttribute("fromAddress", EmailAddress);
        model.addAttribute("mode", "INBOX");

        return "/mail";
    }

    @RequestMapping(value = "/mail", params = "delete", method = RequestMethod.POST)
    public String deletemail(@RequestParam("uid") List<String> uid, Model model) throws Exception {
        List<Long> uidList = new ArrayList<>();

        for (int i = 0; i < uid.size(); i++){
            Long id = Long.valueOf(uid.get(i).split(",")[0]);
            uidList.add(id);
        }
        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(), false);

        mailService.deleteMail(uidList, getImapMail, loginForm.getUsername());
//        mailService.setLastFolderName(uid);
        model.addAttribute("mode", "INBOX");

        return "/mail";
    }

    @RequestMapping(value = "/mail", params = "state", method = RequestMethod.POST)
    public String setImportantMail(@RequestParam("state") Long uid, Model model) throws Exception {

        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(), false);

        mailService.setImportant(getImapMail, uid);

        model.addAttribute("mode", "INBOX");

        return "redirect:/mail";
    }

    @GetMapping("/mail/send/reply/{uid}")
    public String replyMsg(@PathVariable("uid") Long uid, Model model) throws Exception {
        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(), false);

        ReadVo readVo = mailService.replyMsg(getImapMail, uid);

        model.addAttribute("readVo", readVo);
        model.addAttribute("reply", "true");
        model.addAttribute("uid", uid);
        model.addAttribute("mode", "send");

        return "/mail";
    }

    @GetMapping("/mail/send/forward/{uid}")
    public String forwardmsg(@PathVariable("uid") Long uid, Model model) throws Exception {
        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(), false);

        ReadVo readVo = mailService.forwardMsg(getImapMail, uid);

        model.addAttribute("readVo", readVo);
        model.addAttribute("uid", uid);
        model.addAttribute("mode", "send");

        return "/mail";
    }

    //메일 이동
    @RequestMapping(value = "/mail", params = "move", method = RequestMethod.POST)
    public String moveMail(@RequestParam("uid") List<Long> uid, @RequestParam("target") String target, @RequestParam("mode") String mode, Model model) throws Exception {
        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(), false);

        String errmsg = mailService.moveMail(uid, getImapMail, target);

        model.addAttribute("mode", mode);

        return "redirect:/mail";
    }

    @RequestMapping(value = "/mail", params = "read", method = RequestMethod.POST)
    public String setRead(@RequestParam("uid") List<Long> uid, @RequestParam("mode") String mode, Model model) throws Exception {

        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(), false);

        mailService.setRead(getImapMail, uid);

        model.addAttribute("mode", mode);

        return "redirect:/mail";
    }

    @GetMapping("/mail/waste/{clean}")
    public String wasteBox(@Nullable @PathVariable("clean") String clean, Model model) throws MessagingException {
        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(), false);

        if (Objects.equals(clean, "waste")) {
            try {
                List<ReadVo> readVo = mailService.getMailList(getImapMail, "WASTE", false);
                model.addAttribute("readVo", readVo);
            } catch (Exception ignored) {
            }
        } else {
            mailService.cleanUp(getImapMail, loginForm.getUsername());
        }

        model.addAttribute("mode", "WASTE");

        return "/mail";
    }

    @GetMapping("/mail/temp")
    public String tempBox(Model model) throws MessagingException {
        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(),true);

        try {
            List<ReadVo> readVo = mailService.getMailList(getImapMail, "TEMPORARY", false);
            model.addAttribute("readVo", readVo);
        } catch (Exception ignored) {
        }

        model.addAttribute("mode", "TEMPORARY");

        return "/mail";
    }

    @GetMapping("/mail/spam")
    public String spamMailBox(Model model) throws MessagingException {
        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(),true);

        try {
            List<ReadVo> readVo = mailService.getMailList(getImapMail, "SPAM", false);
            model.addAttribute("readVo", readVo);
        } catch (Exception ignored) {
        }

        model.addAttribute("mode", "SPAM");

        return "/mail";
    }

    @GetMapping("/spamlist")
    public String getSpamList(Model model, Principal principal) {
        Integer id = accountRepository.findByUsername(principal.getName()).getId();

        List<RuleDto> spamList = mailService.getSpamList(id);
        model.addAttribute("spamList", spamList);
        return "/spamlist";
    }

    @PostMapping("/spamlist")
    public String registerSpamAddress(@RequestParam String address, RedirectAttributes redirectAttributes) {

        Integer accountid = accountRepository.findByUsername(loginForm.getUsername()).getId();

        String SF = mailService.SpamRegister(address, accountid);

        redirectAttributes.addFlashAttribute("result", SF);

        return "redirect:/spamlist";
    }

    @GetMapping("/mail/important")
    public String getImportantMailBox(Model model) throws MessagingException {
        GetImapMail getImapMail = new GetImapMail();

        getImapMail.LoginMailFolder(this.EmailAddress, loginForm.getPassword(),true);

        try {
            List<ReadVo> readVo = mailService.getMailList(getImapMail, "IMPORTANT", false);
            model.addAttribute("readVo", readVo);
        } catch (Exception ignored) {
        }

        model.addAttribute("mode", "IMPORTANT");

        return "/mail";
    }

    @RequestMapping(value = "/List", params = "address", method = RequestMethod.POST)
    public String deleteSpamList(@RequestParam List<String> address, Principal principal) {

        Integer id = accountRepository.findByUsername(principal.getName()).getId();

        mailService.deleteSpamList(id, address);

        return "redirect:/spamlist";
    }
}