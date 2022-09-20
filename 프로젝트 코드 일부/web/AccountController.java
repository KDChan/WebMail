package MailDBweb.MailDBweb.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final SignUpFormValidator signUpFormValidator;


    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute("signUpForm", new SignUpForm());
        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors, RedirectAttributes attributes) throws NoSuchAlgorithmException {
        if (errors.hasFieldErrors("username")) {
            return "sign-up";
        }
        if (errors.hasFieldErrors("match_password")) {
            attributes.addFlashAttribute("match_password", "비밀번호가 일치하지 않습니다.");
            return "sign-up";
        }

        attributes.addFlashAttribute("message", "회원가입 성공!");

        accountService.signUp(signUpForm);
        return "redirect:/";
    }


    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) throws NoSuchAlgorithmException {
        if (account != null) {
            model.addAttribute(account);
        }

        return "index";
    }



}
