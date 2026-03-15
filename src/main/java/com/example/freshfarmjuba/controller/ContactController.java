package com.example.freshfarmjuba.controller;

import com.example.freshfarmjuba.model.Message;
import com.example.freshfarmjuba.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;

@Controller
public class ContactController {

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/contact")
    public String showContactForm(Model model) {
        model.addAttribute("message", new Message());
        return "contact";
    }

    @PostMapping("/contact")
    public String saveMessage(@ModelAttribute Message message) {
        message.setCreatedAt(LocalDateTime.now());
        messageRepository.save(message);
        return "redirect:/contact?success";
    }
}