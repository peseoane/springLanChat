package com.peseoane.springlanchat.controller;

import com.peseoane.springlanchat.model.*;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;

@Slf4j
@Controller
@SessionAttributes("user")
public class Lounge {

    private final AuthTokenManager authTokenManager;
    private final MessageRepository messageRepository;

    private final UserRepository userRepository;
    private final HttpSession httpSession;
    private final User admin;

    public Lounge(AuthTokenManager authTokenManager, MessageRepository messageRepository, UserRepository userRepository, HttpSession httpSession) {
        this.authTokenManager = authTokenManager;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.httpSession = httpSession;
        this.admin = userRepository.getUserByUsername("admin");
    }

    @GetMapping(path = "/")
    public String landingPage(Model model) {
        model.addAttribute("listaUsuarios", userRepository.findAll());
        return "index";
    }

    @GetMapping(path = "/bye")
    public String bye() {
        return "bye";
    }

    private String saveAvatar(MultipartFile avatar) {
        try {
            String avatarName = avatar.getOriginalFilename();
            // create folder if not exists
            File avatarFolder = new File(System.getProperty("user.dir") + "/public/avatar");
            if (!avatarFolder.exists()) {
                avatarFolder.mkdir();
            }
            String avatarPath = System.getProperty("user.dir") + "/public/avatar/" + avatarName;
            File avatarFile = new File(avatarPath);
            avatar.transferTo(avatarFile);
            return "avatar/" + avatarName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/añadirUsuario", method = RequestMethod.POST)
    public String añadirUsuario(
            @RequestParam("username") String username,
            @RequestParam("avatar") MultipartFile avatar,
            @RequestParam("pin") String pin) {

        if (userRepository.existsByUsername(username)) {
            User exisingUser = userRepository.getUserByUsername(username);
            exisingUser.setAvatar(saveAvatar(avatar));
            exisingUser.setPin(pin);
            userRepository.save(exisingUser);
            return "Usuario actualizado: " + exisingUser;
        } else {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setAvatar(saveAvatar(avatar));
            newUser.setPin(pin);
            userRepository.save(newUser);
            return "Usuario creado: " + newUser;
        }
    }


    @PostMapping("/login")
    public String login(
            @RequestParam("username") String username,
            @RequestParam("pin") String pin,
            HttpSession session
            ) {

        if (verificarCredenciales(username, pin)) {
            User currentUser = userRepository.getUserByUsername(username);
            String authToken = authTokenManager.generateAuthToken(currentUser);

            Message message = new Message();
            message.setMessage("Se ha conectado " + currentUser.getUsername());
            message.setSender(admin);
            messageRepository.save(message);
            httpSession.setAttribute("authToken", authToken);
            httpSession.setAttribute("user", currentUser);
            return "redirect:/chat";

        } else {
            return "redirect:/error";
        }
    }

    private boolean verificarCredenciales(String username, String pin) {
        return userRepository.existsByUsername(username) && userRepository.getUserByUsername(username).getPin().equals(pin);
    }

}

