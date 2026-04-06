package com.cydeo.controller;

import com.cydeo.client.ProjectGatewayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectViewController {

    private final ProjectGatewayClient projectGatewayClient;

    @GetMapping
    public String listProjects(Model model) {
        try {
            model.addAttribute("projects", projectGatewayClient.getProjects());
        } catch (Exception e) {
            log.error("Could not load projects", e);
            model.addAttribute("fetchError", "Could not reach the Project Service. Please try again later.");
        }
        return "projects/list";
    }
}
