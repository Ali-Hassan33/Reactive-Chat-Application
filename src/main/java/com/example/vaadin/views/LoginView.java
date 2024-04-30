package com.example.vaadin.views;

import com.example.vaadin.dtos.LoginDetails;
import com.example.vaadin.entities.Person;
import com.example.vaadin.services.PersonService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("login")
@PageTitle("Chatify")
public class LoginView extends VerticalLayout {

    private final PersonService personService;

    private final LoginDetails loginDetails;

    private Person person;

    @Autowired
    public LoginView(PersonService personService, LoginDetails loginDetails) {
        this.personService = personService;
        this.loginDetails = loginDetails;
        this.setClassName("layout-view");
        this.setHeightFull();
        this.addLoginForm();
        this.addCreateAnAccountLink();
    }

    private void addCreateAnAccountLink() {
        Anchor anchor = new Anchor("/createAccount", "Create an account");
        this.add(anchor);
        this.centerAlign(anchor);
    }

    private void addLoginForm() {
        LoginForm loginForm = new LoginForm();
        this.centerAlign(loginForm);
        loginForm.setForgotPasswordButtonVisible(false);
        this.add(loginForm);
        loginForm.addLoginListener(
                event -> {
                    String providedUsername = event.getUsername();
                    String providedPassword = event.getPassword();

                    if (this.matches(providedUsername, providedPassword)) {
                        loginDetails.setUsername(providedUsername);
                        loginDetails.setPassword(providedPassword);
                        this.person.setStatus("Online");
                        this.personService.save(person);
                        event.getSource()
                                .getUI()
                                .ifPresent(ui -> ui.navigate("home"));
                        return;
                    }
                    Notification notification = new Notification("Invalid Password", 2000, Notification.Position.BOTTOM_CENTER);
                    notification.open();
                    this.add(notification);
                }
        );

    }

    private void centerAlign(Component component) {
        this.setAlignSelf(Alignment.CENTER, component);
    }

    private boolean matches(String providedUsername, String providedPassword) {
        this.person = personService.getByUsername(providedUsername);
        return person != null && person.getPassword().equals(providedPassword);
    }
}
