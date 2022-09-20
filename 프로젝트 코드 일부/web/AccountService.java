package MailDBweb.MailDBweb.account;

import MailDBweb.MailDBweb.Mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    public MailService mailService;
    public static PasswordEncoder passwordEncoder(String type){

        Map<String, PasswordEncoder> encoderMap = new HashMap<>();

        encoderMap.put("bcrypt", new BCryptPasswordEncoder());  //8

        return new DelegatingPasswordEncoder(type, encoderMap);
    }

    public String encodePass(String password) throws NoSuchAlgorithmException {
        SHA256 sha256 = new SHA256();

        return sha256.encodePassword(password);
    }

    public void signUp(SignUpForm signUpForm) throws NoSuchAlgorithmException {
        Account account = Account.builder()
                .accountaddress(signUpForm.getUsername() + "@stsoft.kr")
                .accountpassword(encodePass(signUpForm.getPassword()))
                .password(passwordEncoder("bcrypt").encode(signUpForm.getPassword()))
                .username(signUpForm.getUsername())
                .nickname(signUpForm.getNickname())
                .accountadusername("")
                .posit(signUpForm.getPosit())
                .dep(signUpForm.getDep())
                .phone(signUpForm.getPhone())
                .birth(signUpForm.getBirth())
                .entry(signUpForm.getEntry())
                .accountpersonfirstname("")
                .accountpersonlastname("")
                .accountaddomain("")
                .accountvacationmessage("")
                .accountforwardaddress("")
                .accountvacationsubject("")
                .accountsignaturehtml("")
                .accountactive(1)
                .accountdomainid(1)
                .accountadminlevel(0)
                .accountisad(0)
                .accountmaxsize(0)
                .accountvacationmessageon(0)
                .accountpwencryption(3)
                .accountforwardenabled(0)
                .accountforwardkeeporiginal(0)
                .accountenablesignature(0)
                .accountvacationexpires(0)
                .accountsignatureplaintext("")
                .accountvacationexpiredate(Instant.now()) // instant type
                .accountlastlogontime(Instant.now()) // instant type
                .build();
        accountRepository.save(account);

        //set inbox folder id
        Account Newaccount = accountRepository.findByUsername(signUpForm.getUsername());
        Integer id = Newaccount.getId();
        mailService.SetMailFolder(id, "INBOX");
        mailService.SetMailFolder(id, "SENT");
        mailService.SetMailFolder(id, "WASTE");
        mailService.SetMailFolder(id, "TEMPORARY");
        mailService.SetMailFolder(id, "SPAM");
        mailService.SpamRule(id, account.getAccountaddress());
    }

    public void updateInfo(Integer idx, String username, String nickname, String posit, String dep, String phone, String birth, String entry) {
        Optional<Account> Info = accountRepository.findById(idx);
        Info.ifPresent(account -> {
            account.setUsername(username);
            account.setNickname(nickname);
            account.setAccountaddress(username + "@stsoft.kr");
            account.setPosit(posit);
            account.setDep(dep);
            account.setPhone(phone);
            account.setBirth(birth);
            account.setEntry(entry);
            accountRepository.save(account);
        });
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username);
        if(account == null){
            throw new UsernameNotFoundException(username);
        }
        return new UserAccount(account);
    }

    public String updatePass(Integer id, String pass, String checkPass){
        Optional<Account> updatePass = accountRepository.findById(id);

        if (pass.equals(checkPass)) {
            updatePass.ifPresent(account -> {
                try {
                    account.setAccountpassword(encodePass(pass));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                account.setPassword(passwordEncoder("bcrypt").encode(pass));
                accountRepository.save(account);
            });

            return "suc";
        }

        else{
            return "fail";
        }
    }

}