package com.cydeo.controller;

import com.cydeo.client.UserGatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserViewController {

    private final UserGatewayClient userGatewayClient;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userGatewayClient.getUsers());
        return "users/list";
    }
}
