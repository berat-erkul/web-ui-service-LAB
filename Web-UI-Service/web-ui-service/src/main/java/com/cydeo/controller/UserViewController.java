package com.cydeo.controller;

import com.cydeo.client.UserGatewayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserViewController {

    private final UserGatewayClient userGatewayClient;

    @GetMapping
    public String listUsers(Model model) {
        try {
            model.addAttribute("users", userGatewayClient.getUsers());
        } catch (Exception e) {
            log.error("Could not load users", e);
            model.addAttribute("fetchError", "Could not reach the User Service. Please try again later.");
        }
        return "users/list";
    }
}
