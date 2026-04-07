package com.cydeo.controller;

import com.cydeo.client.ClientResult;
import com.cydeo.client.TaskGatewayClient;
import com.cydeo.dto.TaskDto;
import com.cydeo.dto.TaskForm;
import com.cydeo.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskViewController {

    private final TaskGatewayClient taskGatewayClient;

    // ── Manager/Admin: task list by project ───────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin','Manager')")
    public String listTasks(@RequestParam(required = false) String projectCode,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (projectCode == null || projectCode.isBlank()) {
            return "tasks/list";   // empty state, no projectCode given
        }
        try {
            List<TaskDto> tasks = taskGatewayClient.getTasksByProject(projectCode);
            model.addAttribute("tasks", tasks);
            model.addAttribute("projectCode", projectCode);
        } catch (Exception e) {
            log.error("Could not load tasks for project {}", projectCode, e);
            model.addAttribute("fetchError", "Could not reach the Task Service. Please try again later.");
        }
        return "tasks/list";
    }

    // ── Employee: pending tasks ────────────────────────────────────────────

    @GetMapping("/pending")
    @PreAuthorize("hasRole('Employee')")
    public String pendingTasks(Model model) {
        try {
            List<TaskDto> tasks = taskGatewayClient.getPendingTasksForEmployee();
            model.addAttribute("tasks", tasks);
        } catch (Exception e) {
            log.error("Could not load pending tasks", e);
            model.addAttribute("fetchError", "Could not reach the Task Service. Please try again later.");
        }
        return "tasks/pending";
    }

    // ── Employee: archived tasks ───────────────────────────────────────────

    @GetMapping("/archive")
    @PreAuthorize("hasRole('Employee')")
    public String archivedTasks(Model model) {
        try {
            List<TaskDto> tasks = taskGatewayClient.getArchivedTasksForEmployee();
            model.addAttribute("tasks", tasks);
        } catch (Exception e) {
            log.error("Could not load archived tasks", e);
            model.addAttribute("fetchError", "Could not reach the Task Service. Please try again later.");
        }
        return "tasks/archive";
    }

    // ── Employee: status transition ────────────────────────────────────────

    @PostMapping("/update-status/{taskCode}")
    @PreAuthorize("hasRole('Employee')")
    public String updateStatus(@PathVariable String taskCode,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        Status statusEnum;
        try {
            statusEnum = Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid status value: " + status);
            return "redirect:/tasks/pending";
        }
        ClientResult<TaskDto> result = taskGatewayClient.updateTaskStatus(taskCode, statusEnum);
        if (!result.isSuccess()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getErrorMessage());
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Task status updated.");
        }
        return "redirect:/tasks/pending";
    }

    // ── Manager: create ────────────────────────────────────────────────────

    @GetMapping("/create")
    @PreAuthorize("hasRole('Manager')")
    public String showCreateForm(@RequestParam(required = false) String projectCode,
                                 Model model) {
        TaskForm form = new TaskForm();
        if (projectCode != null) {
            form.setProjectCode(projectCode);
        }
        model.addAttribute("taskForm", form);
        model.addAttribute("statusOptions", Status.values());
        return "tasks/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('Manager')")
    public String createTask(@Valid @ModelAttribute("taskForm") TaskForm form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("statusOptions", Status.values());
            return "tasks/create";
        }
        ClientResult<TaskDto> result = taskGatewayClient.createTask(form);
        if (!result.isSuccess()) {
            model.addAttribute("apiError", result.getErrorMessage());
            model.addAttribute("statusOptions", Status.values());
            return "tasks/create";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Task created successfully.");
        return "redirect:/tasks?projectCode=" + form.getProjectCode();
    }

    // ── Manager: edit ──────────────────────────────────────────────────────

    @GetMapping("/edit/{taskCode}")
    @PreAuthorize("hasRole('Manager')")
    public String showEditForm(@PathVariable String taskCode,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        ClientResult<TaskDto> result = taskGatewayClient.getTaskByCode(taskCode);
        if (!result.isSuccess()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getErrorMessage());
            return "redirect:/tasks";
        }
        model.addAttribute("taskForm", toForm(result.getData()));
        model.addAttribute("taskCode", taskCode);
        model.addAttribute("statusOptions", Status.values());
        return "tasks/edit";
    }

    @PostMapping("/edit/{taskCode}")
    @PreAuthorize("hasRole('Manager')")
    public String updateTask(@PathVariable String taskCode,
                             @Valid @ModelAttribute("taskForm") TaskForm form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("taskCode", taskCode);
            model.addAttribute("statusOptions", Status.values());
            return "tasks/edit";
        }
        ClientResult<TaskDto> result = taskGatewayClient.updateTask(taskCode, form);
        if (!result.isSuccess()) {
            model.addAttribute("apiError", result.getErrorMessage());
            model.addAttribute("taskCode", taskCode);
            model.addAttribute("statusOptions", Status.values());
            return "tasks/edit";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Task updated successfully.");
        return "redirect:/tasks?projectCode=" + form.getProjectCode();
    }

    // ── Manager: delete ────────────────────────────────────────────────────

    @PostMapping("/delete/{taskCode}")
    @PreAuthorize("hasRole('Manager')")
    public String deleteTask(@PathVariable String taskCode,
                             @RequestParam(required = false) String projectCode,
                             RedirectAttributes redirectAttributes) {
        ClientResult<Void> result = taskGatewayClient.deleteTask(taskCode);
        if (!result.isSuccess()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getErrorMessage());
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Task deleted.");
        }
        String redirect = (projectCode != null && !projectCode.isBlank())
                ? "redirect:/tasks?projectCode=" + projectCode
                : "redirect:/tasks";
        return redirect;
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private TaskForm toForm(TaskDto dto) {
        TaskForm form = new TaskForm();
        form.setTaskCode(dto.getTaskCode());
        form.setTaskSubject(dto.getTaskSubject());
        form.setTaskDetail(dto.getTaskDetail());
        if (dto.getTaskStatus() != null) {
            try {
                form.setTaskStatus(Status.valueOf(dto.getTaskStatus()));
            } catch (IllegalArgumentException ignored) {}
        }
        form.setProjectCode(dto.getProjectCode());
        form.setAssignedEmployee(dto.getAssignedEmployee());
        return form;
    }
}
