package com.cydeo.controller;

import com.cydeo.client.ProjectGatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectViewController {

    private final ProjectGatewayClient projectGatewayClient;

    @GetMapping
    public String listProjects(Model model) {
        model.addAttribute("projects", projectGatewayClient.getProjects());
        return "projects/list";
    }
}
