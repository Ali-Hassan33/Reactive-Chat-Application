package com.example.vaadin;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme(value = "vaadinapp", variant = Lumo.DARK)
@Push
public class ChatApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

}
