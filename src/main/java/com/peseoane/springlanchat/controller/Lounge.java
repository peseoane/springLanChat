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
import java.util.Optional;

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

    @RequestMapping(value = "/añadirUsuario", method = RequestMethod.POST)
    public String añadirUsuario(
            @RequestParam("username") String username,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
            @RequestParam("pin") String pin, Model model) {

        if (userRepository.existsByUsername(username)) {
            User existingUser = userRepository.getUserByUsername(username);


            if (avatarFile != null && !avatarFile.isEmpty()) {
                existingUser.setAvatar(saveAvatar(avatarFile));
            } else {
                existingUser.setAvatar("avatar/default.svg");
            }

            existingUser.setPin(pin);
            userRepository.save(existingUser);
            model.addAttribute("respuesta", "Usuario actualizado: " + existingUser);
            model.addAttribute("listaUsuarios", userRepository.findAll());
            return "index";
        } else {
            User newUser = new User();
            newUser.setUsername(username);
            if (avatarFile != null && !avatarFile.isEmpty()) {
                newUser.setAvatar(saveAvatar(avatarFile));
            }
            newUser.setPin(pin);
            userRepository.save(newUser);
            model.addAttribute("respuesta", "Usuario creado: " + newUser);
            model.addAttribute("listaUsuarios", userRepository.findAll());
            return "index";
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

