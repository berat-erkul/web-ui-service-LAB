package com.cydeo.controller;

import com.cydeo.client.ClientResult;
import com.cydeo.client.UserGatewayClient;
import com.cydeo.dto.UserDto;
import com.cydeo.dto.UserForm;
import com.cydeo.enums.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('Admin')")          // all user operations are Admin-only
public class UserViewController {

    private final UserGatewayClient userGatewayClient;

    private static final List<String> ROLE_OPTIONS   = List.of("Admin", "Manager", "Employee");
    private static final Gender[]     GENDER_OPTIONS = Gender.values();

    // ── LIST ─────────────────────────────────────────────────────────────────

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

    // ── CREATE ────────────────────────────────────────────────────────────────

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        addDropdowns(model);
        return "users/create";
    }

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("userForm") UserForm userForm,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addDropdowns(model);
            return "users/create";
        }
        ClientResult<UserDto> result = userGatewayClient.createUser(userForm);
        if (!result.isSuccess()) {
            model.addAttribute("apiError", result.getErrorMessage());
            addDropdowns(model);
            return "users/create";
        }
        redirectAttributes.addFlashAttribute("successMessage", "User created successfully.");
        return "redirect:/users";
    }

    // ── EDIT ──────────────────────────────────────────────────────────────────

    @GetMapping("/edit/{username}")
    public String showEditForm(@PathVariable String username,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        ClientResult<UserDto> result = userGatewayClient.getUserByUsername(username);
        if (!result.isSuccess()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getErrorMessage());
            return "redirect:/users";
        }
        model.addAttribute("userForm", toForm(result.getData()));
        model.addAttribute("username", username);
        addDropdowns(model);
        return "users/edit";
    }

    @PostMapping("/edit/{username}")
    public String updateUser(@PathVariable String username,
                             @Valid @ModelAttribute("userForm") UserForm userForm,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("username", username);
            addDropdowns(model);
            return "users/edit";
        }
        ClientResult<UserDto> result = userGatewayClient.updateUser(username, userForm);
        if (!result.isSuccess()) {
            model.addAttribute("apiError", result.getErrorMessage());
            model.addAttribute("username", username);
            addDropdowns(model);
            return "users/edit";
        }
        redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
        return "redirect:/users";
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @PostMapping("/delete/{username}")
    public String deleteUser(@PathVariable String username,
                             RedirectAttributes redirectAttributes) {
        ClientResult<Void> result = userGatewayClient.deleteUser(username);
        if (!result.isSuccess()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getErrorMessage());
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        }
        return "redirect:/users";
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void addDropdowns(Model model) {
        model.addAttribute("roleOptions", ROLE_OPTIONS);
        model.addAttribute("genderOptions", GENDER_OPTIONS);
    }

    /** Converts a read-DTO from the API into a pre-populated form model. */
    private UserForm toForm(UserDto dto) {
        UserForm form = new UserForm();
        form.setFirstName(dto.getFirstName());
        form.setLastName(dto.getLastName());
        form.setUserName(dto.getUserName());
        // password is WRITE_ONLY — never returned by backend; user must re-enter
        form.setPhone(dto.getPhone());
        form.setEnabled(dto.isEnabled());
        if (dto.getRole() != null) {
            form.setRoleDescription(dto.getRole().getDescription());
        }
        if (dto.getGender() != null) {
            try {
                form.setGender(Gender.valueOf(dto.getGender()));
            } catch (IllegalArgumentException ignored) {
                // leave null if backend returns an unrecognised value
            }
        }
        return form;
    }
}
