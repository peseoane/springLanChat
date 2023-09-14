package com.peseoane.springlanchat.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.peseoane.springlanchat.model.*;
import reactor.core.publisher.Flux;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.io.Flushable;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(path = "/chat")
public class Chat {

    private final AuthTokenManager authTokenManager;
    private final MessageRepository messageRepository;

    private final UserRepository userRepository;

    private User admin;

    private final HttpSession httpSession;

    public Chat(AuthTokenManager authTokenManager, MessageRepository messageRepository, UserRepository userRepository, HttpSession httpSession) {
        this.authTokenManager = authTokenManager;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.httpSession = httpSession;
        this.admin = userRepository.getUserByUsername("admin");
    }
    @GetMapping
    public String chat(Model model) {
        String authToken = (String) httpSession.getAttribute("authToken");
        if (authTokenManager.isValidAuthToken(authToken)) {
            User user = (User) httpSession.getAttribute("user");
            model.addAttribute("user", user);

            List<Message> messages = messageRepository.findAllByOrderByTimestampDesc();
            if (messages.size() > 200) {
                messages = messages.subList(0, 200);
            }

            model.addAttribute("messages", messages);
            return "chat";
        } else {
            log.info("Invalid token: " + authToken);
            return "redirect:/login?error=true";
        }
    }



    @GetMapping(path = "/bye")
    public String bye() {
        Message adminMsg = new Message();
        adminMsg.setMessage("Se ha desconectado del chat " + httpSession.getAttribute("user"));
        adminMsg.setSender(userRepository.getUserByUsername("admin"));
        messageRepository.save(adminMsg);
        return "redirect:/bye";
    }

    @PostMapping(path = "/send")
    public String send(String message) {
        String authToken = (String) httpSession.getAttribute("authToken");
        if (authTokenManager.isValidAuthToken(authToken)) {
            User user = (User) httpSession.getAttribute("user");
            Message userMsg = new Message();
            userMsg.setMessage(message);
            userMsg.setSender(user);
            messageRepository.save(userMsg);
            return "redirect:/chat";
        } else {
            log.info("Invalid token: " + authToken);
            return "redirect:/login?error=true";
        }
    }

    @GetMapping(path = "/logout")
    @RequestMapping(value = "/logout")
    public String logout() {
        try {
            String authToken = (String) httpSession.getAttribute("authToken");
            authTokenManager.invalidateAuthToken(authToken);
            httpSession.invalidate();
            return "redirect:/bye";
        } catch (Exception e) {
            return "redirect:/error";
        }

    }

}
