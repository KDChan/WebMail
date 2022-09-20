package MailDBweb.MailDBweb.Manager;

import MailDBweb.MailDBweb.account.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ManagerPageController {

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final SignUpFormValidator signUpFormValidator;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/managerPage")
    public String managerPage(@CurrentUser Account account, Model model) {

        String jus = account.getPosit();

        if (jus.equals("사장")) {
            model.addAttribute("auth_data", "success");
        } else {
            model.addAttribute("auth_data", "fail");
        }

        return "managerPage";
    }

    @GetMapping("/member-list")
    public String list(Model model) {
        List<Account> memberList = accountRepository.findAll();
        model.addAttribute("memberList", memberList);
        return "member-list";
    }

    @GetMapping("/memberInfo/{id}")
    public String Info(@PathVariable("id") Integer id, Model model) {
        Optional<Account> memberInfo = accountRepository.findById(id);

        model.addAttribute("memberInfo", memberInfo.get());
        return "memberInfo";
    }

    @GetMapping("memberInfo/updateInfo/{id}")
    public String updateInfo(@PathVariable("id") Integer id, Model model) {

        Optional<Account> updateInfo = accountRepository.findById(id);

        model.addAttribute("updateInfo", updateInfo.get());
        return "updateInfo";
    }

    @RequestMapping(value = "/updateInfo", params = "save", method = RequestMethod.POST)
    public String saveMember(@RequestParam("id") Integer id, @RequestParam("username") String username, @RequestParam("nickname") String nickname,
                             @RequestParam("posit") String posit, @RequestParam("dep") String dep, @RequestParam("phone") String phone,
                             @RequestParam("birth") String birth, @RequestParam("entry") String entry, Model model){
        Optional<Account> updateInfo = accountRepository.findById(id);

        model.addAttribute("updateInfo", updateInfo.get());
        accountService.updateInfo(id, username, nickname, posit, dep, phone, birth, entry);
        return "redirect:/member-list";
    }

    @RequestMapping(value = "/updateInfo", params = "delete", method = RequestMethod.POST)
    public String deleteMember(@RequestParam("id") Integer idx){

        accountRepository.deleteById(idx);

        return "redirect:/member-list";
    }

    @GetMapping("/updatePass/{id}")
    public String updatePass(@PathVariable("id") Integer id, Model model){
        Optional<Account> updatePass = accountRepository.findById(id);

        model.addAttribute("updatePass", updatePass.get());

        return "updatePass";
    }

    @RequestMapping(value = "/updatePass", params = "done", method = RequestMethod.POST)
    public String newPass(@RequestParam("id") Integer id, @RequestParam("newPass") String pass, @RequestParam("checkPass") String checkPass, RedirectAttributes redirectAttributes){

        String jd = accountService.updatePass(id, pass, checkPass);

        if(jd.equals("fail")){
            redirectAttributes.addFlashAttribute("message", "fail");
            return "redirect:/updatePass/" + id;
        }

        return "redirect:/member-list";
    }

}
