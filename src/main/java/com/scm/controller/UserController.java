package com.scm.controller;

import com.scm.dao.ContactRepository;
import com.scm.dao.UserRepository;
import com.scm.entities.Contact;
import com.scm.entities.User;
import com.scm.helper.Message;
import com.scm.services.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    //Method for adding common data for response
    @ModelAttribute
    public void getUserData(Model model, Principal principal) {
        String userName = principal.getName();
        //get the user using Username(Email)
        User user = userRepository.getUserByUsername(userName);
        model.addAttribute("user", user);
    }

    //dashboard
    @GetMapping("/index")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("title", "Dashboard");
        User user = userRepository.getUserByUsername(principal.getName());
        Pageable pageable = PageRequest.of(0, 5);
        Page<Contact> contacts = contactRepository.findContactByUser(user.getId(), pageable);
        model.addAttribute("contacts", contacts);
        model.addAttribute("user", userRepository.getUserByUsername(principal.getName()));

        return "/normal/user_dashboard";
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        model.addAttribute("title", "Profile");

        // Fetch the logged-in user
        User user = userRepository.getUserByUsername(principal.getName());

        // Add user details to the model
        model.addAttribute("user", user);

        return "/normal/profile";
    }

    //Add contact handler
    @GetMapping("/add-contact")
    public String newContact(Model model) {
        model.addAttribute("title", "New Contact");
        try {
            // Save contact logic
            model.addAttribute("successMessage", "Contact saved successfully!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to save contact: " + e.getMessage());
        }

        model.addAttribute("contact", new Contact());
        return "normal/add_contact";
    }

    //saving contact
    @PostMapping("/process-contact")
    public String processContact(
            @ModelAttribute Contact contact,
            @RequestParam("images") MultipartFile file,
            Principal principal,
            HttpSession session,
            Model model
    ) {
        model.addAttribute("title", "New Contact");
        try {
            if (file.isEmpty()) {
                System.out.println("File is empty");
                contact.setImage("default-contact.png");
            } else {
                contact.setImage(file.getOriginalFilename());
                File file1 = new ClassPathResource("static/image").getFile();
                Path path = Paths.get(file1.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image is uploaded");
            }

            User user = this.userRepository.getUserByUsername(principal.getName());
            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);

            // Add success message to attributes
            session.setAttribute("message", new Message("Contact saved successfully!", "alert-success"));

        } catch (Exception e) {
            // Add error message to flash attributes
            session.setAttribute("message", new Message("Something went wrong!! Try again.", "alert-danger"));
        }

        // Redirect to the add-contact page
        return "redirect:/user/add-contact";
    }

    // show contact handler
    //per page = 7 [contacts]
    //current page = 0 [page]
    @GetMapping("/view-contacts/{page}")
    public String viewContacts(
            Principal principal,
            Model model,
            @PathVariable("page") Integer page
    ) {
        model.addAttribute("title", "Contacts");
        // Get the current user
        User user = userRepository.getUserByUsername(principal.getName());
        Pageable pageable = PageRequest.of(page, 5);
        Page<Contact> contacts = contactRepository.findContactByUser(user.getId(), pageable);
        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contacts.getTotalPages());
        return "/normal/show_contacts";
    }

    // Show individual contact details
    @GetMapping("/contact/{cId}")
    public String showContactDetails(
            @PathVariable("cId") Long cId,
            Principal principal,
            Model model
    ) {
        model.addAttribute("title", "Contact Details");

        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();

        // Verify that the contact belongs to the logged-in user
        User user = userRepository.getUserByUsername(principal.getName());
        if (!contact.getUser().getId().equals(user.getId())) {
            model.addAttribute("message", "denied");
        } else {
            //Add contact to the model
            model.addAttribute("message", "allowed");
        }
        model.addAttribute("contact", contact);
        return "/normal/contact_details";
    }

    //delete contact handler
    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable("cId") Long cId, Principal principal, HttpSession session) {

        Contact contact = this.contactRepository.findById(cId).get();
        // Verify that the contact belongs to the logged-in user
        User user = userRepository.getUserByUsername(principal.getName());

        if (!contact.getUser().getId().equals(user.getId())) {
            session.setAttribute("message", new Message("You are not authorized to delete this contact.", "alert-danger"));
        } else {
            contact.setUser(null);
            // also can remove images
            contactRepository.deleteContactByCId(cId);
            session.setAttribute("message", new Message("Successfully deleted the contact", "alert-success"));
        }
        return "redirect:/user/view-contacts/0";
    }

    //open update from handler
    @PostMapping("/update-contact/{cId}")
    public String updateForm(@PathVariable("cId") Long cId, Principal principal, Model model) {
        model.addAttribute("title", "Update Form");
        Contact contact = this.contactRepository.findById(cId).get();
        model.addAttribute("contact", contact);
        return "/normal/update_contact";
    }


    @PostMapping("/process-update")
    public String updateContact(
            @ModelAttribute Contact updatedContact,
            @RequestParam("images") MultipartFile file,
            Principal principal,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        try {
            System.out.println(updatedContact.getcId());
            System.out.println(updatedContact);
            // Fetch the existing contact
            Contact existingContact = contactRepository.findById(updatedContact.getcId()).get();

            // Verify that the contact belongs to the logged-in user
            User user = userRepository.getUserByUsername(principal.getName());
            if (!existingContact.getUser().getId().equals(user.getId())) {
                session.setAttribute("message", new Message("You are not authorized to delete this contact.", "alert-danger"));
                return "redirect:/user/view-contacts/0";
            }

            // Update contact details
            existingContact.setName(updatedContact.getName());
            existingContact.setSecondName(updatedContact.getSecondName());
            existingContact.setWork(updatedContact.getWork());
            existingContact.setEmail(updatedContact.getEmail());
            existingContact.setPhone(updatedContact.getPhone());
            existingContact.setDescription(updatedContact.getDescription());

            // Handle image upload
            if (!file.isEmpty()) {
                // Save the new image
                existingContact.setImage(file.getOriginalFilename());
                File newImageFile = new ClassPathResource("static/image").getFile();
                Path newImagePath = Paths.get(newImageFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), newImagePath, StandardCopyOption.REPLACE_EXISTING);

            }

            // Save the updated contact
            contactRepository.save(existingContact);

            // Add success message
            session.setAttribute("message", new Message("Successfully deleted the contact", "alert-success"));

        } catch (Exception e) {
            e.printStackTrace();
            // Add error message
            session.setAttribute("message", new Message("Something went wrong!!", "alert-danger"));
        }

        // Redirect to the view contacts page
        return "redirect:/user/view-contacts/0";
    }

    @GetMapping("/live-search")
    public String liveSearch(
            @RequestParam("query") String query,
            Principal principal,
            Model model
    ) {
        System.out.println(query);
        User user = userRepository.getUserByUsername(principal.getName());
        List<Contact> results = contactRepository.findByNameContainingAndUser(query, user);

        model.addAttribute("contacts", results);
        return "/normal/search_results_fragment"; // Thymeleaf fragment
    }

    @GetMapping("/password")
    public String changePassword(Model model) {
        model.addAttribute("title", "Change Password");
        return "/normal/change_password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Principal principal,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        // Fetch the logged-in user
        User user = userRepository.getUserByUsername(principal.getName());

        // Verify current password
        if (!bCryptPasswordEncoder.matches(currentPassword, user.getPassword())) {
            session.setAttribute("message", new Message("Current password is incorrect.", "alert-danger"));
            return "redirect:/user/password";
        }

        // Validate new password and confirmation
        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("message", new Message("New password and confirmation password is not same.", "alert-danger"));
            return "redirect:/user/password";
        }

        // Update password
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);

        // Add success message
        session.setAttribute("message", new Message("Password updated successfully!", "alert-success"));
        return "redirect:/user/password";
    }
}
