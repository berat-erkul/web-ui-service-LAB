package com.cydeo.controller;

import com.cydeo.client.ClientResult;
import com.cydeo.client.ProjectGatewayClient;
import com.cydeo.dto.ProjectDto;
import com.cydeo.dto.ProjectForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectViewController {

    private final ProjectGatewayClient projectGatewayClient;

    // ── LIST ──────────────────────────────────────────────────────────────────
    //
    // Admin  → GET /project-service/api/v1/project/read/all/admin   (all projects)
    // Manager→ GET /project-service/api/v1/project/read/all/manager (own projects)
    // URL-level security (/projects/**  → hasAnyRole(Admin,Manager)) is enforced
    // by SecurityConfig; no @PreAuthorize needed here.

    @GetMapping
    public String listProjects(Model model,
                               @AuthenticationPrincipal OidcUser principal) {
        boolean isAdmin = hasRole(principal, "Admin");
        try {
            // Choose the endpoint that matches the caller's role
            List<ProjectDto> projects = isAdmin
                    ? projectGatewayClient.getProjects()          // Admin-only endpoint
                    : projectGatewayClient.getManagerProjects();  // Manager-only endpoint
            model.addAttribute("projects", projects);
        } catch (Exception e) {
            log.error("Could not load projects", e);
            model.addAttribute("fetchError", "Could not reach the Project Service. Please try again later.");
        }
        return "projects/list";
    }

    // ── CREATE (Admin + Manager) ───────────────────────────────────────────────

    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('Admin', 'Manager')")
    public String showCreateForm(Model model) {
        model.addAttribute("projectForm", new ProjectForm());
        return "projects/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('Admin', 'Manager')")
    public String createProject(@Valid @ModelAttribute("projectForm") ProjectForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "projects/create";
        }
        ClientResult<ProjectDto> result = projectGatewayClient.createProject(form);
        if (!result.isSuccess()) {
            model.addAttribute("apiError", result.getErrorMessage());
            return "projects/create";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Project created successfully.");
        return "redirect:/projects";
    }

    // ── EDIT (Manager only) ───────────────────────────────────────────────────

    @GetMapping("/edit/{projectCode}")
    @PreAuthorize("hasRole('Manager')")
    public String showEditForm(@PathVariable String projectCode,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        ClientResult<ProjectDto> result = projectGatewayClient.getProjectByCode(projectCode);
        if (!result.isSuccess()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getErrorMessage());
            return "redirect:/projects";
        }
        model.addAttribute("projectForm", toForm(result.getData()));
        model.addAttribute("projectCode", projectCode);
        return "projects/edit";
    }

    @PostMapping("/edit/{projectCode}")
    @PreAuthorize("hasRole('Manager')")
    public String updateProject(@PathVariable String projectCode,
                                @Valid @ModelAttribute("projectForm") ProjectForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("projectCode", projectCode);
            return "projects/edit";
        }
        ClientResult<ProjectDto> result = projectGatewayClient.updateProject(projectCode, form);
        if (!result.isSuccess()) {
            model.addAttribute("apiError", result.getErrorMessage());
            model.addAttribute("projectCode", projectCode);
            return "projects/edit";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Project updated successfully.");
        return "redirect:/projects";
    }

    // ── STATE TRANSITIONS (Manager only) ─────────────────────────────────────

    @PostMapping("/start/{projectCode}")
    @PreAuthorize("hasRole('Manager')")
    public String startProject(@PathVariable String projectCode,
                               RedirectAttributes redirectAttributes) {
        ClientResult<ProjectDto> result = projectGatewayClient.startProject(projectCode);
        if (!result.isSuccess()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getErrorMessage());
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Project started.");
        }
        return "redirect:/projects";
    }

    @PostMapping("/complete/{projectCode}")
    @PreAuthorize("hasRole('Manager')")
    public String completeProject(@PathVariable String projectCode,
                                  RedirectAttributes redirectAttributes) {
        ClientResult<ProjectDto> result = projectGatewayClient.completeProject(projectCode);
        if (!result.isSuccess()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getErrorMessage());
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Project completed.");
        }
        return "redirect:/projects";
    }

    // ── DELETE (Manager only) ─────────────────────────────────────────────────

    @PostMapping("/delete/{projectCode}")
    @PreAuthorize("hasRole('Manager')")
    public String deleteProject(@PathVariable String projectCode,
                                RedirectAttributes redirectAttributes) {
        ClientResult<Void> result = projectGatewayClient.deleteProject(projectCode);
        if (!result.isSuccess()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getErrorMessage());
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Project deleted.");
        }
        return "redirect:/projects";
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private boolean hasRole(OidcUser principal, String role) {
        return principal != null && principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    /** Copies readable fields from the API response DTO into the form model. */
    private ProjectForm toForm(ProjectDto dto) {
        ProjectForm form = new ProjectForm();
        form.setProjectName(dto.getProjectName());
        form.setProjectCode(dto.getProjectCode());
        form.setStartDate(dto.getStartDate());
        form.setEndDate(dto.getEndDate());
        form.setProjectDetail(dto.getProjectDetail());
        return form;
    }
}
