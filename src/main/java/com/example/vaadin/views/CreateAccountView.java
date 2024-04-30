package com.example.vaadin.views;

import com.example.vaadin.entities.Person;
import com.example.vaadin.services.PersonService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Route("/createAccount")
public class CreateAccountView extends VerticalLayout {

    private String username;

    @Value(value = "${image.path.absolute}")
    private String imageAbsolutePath;

    private final String fieldValidationJs = """
            this.validate = function() {
                                return !(this.invalid = !this.checkValidity());
                           }
            """;

    private final PersonService personService;


    public CreateAccountView(PersonService personService) {
        this.personService = personService;
        addAccountForm();
    }

    public void addAccountForm() {
        H2 heading = getFormHeading();

        TextField usernameField = getUsernameField();
        PasswordField passwordField = getPasswordField();
        TextField contactField = getContactField();
        Button button = createAccountButton(contactField, passwordField, usernameField, uploadImage());

        this.add(wrapFormComponents(heading, usernameField, passwordField, contactField, button));
    }

    private H2 getFormHeading() {
        H2 heading = new H2("Create Account");
        heading.getStyle().setMargin("20px 0px 25px 2px");
        this.setAlignSelf(Alignment.CENTER, heading);
        return heading;
    }

    private TextField getUsernameField() {
        TextField usernameField = new TextField("Name");
        usernameField.setRequiredIndicatorVisible(true);
        usernameField.setErrorMessage("Username is required");
        usernameField.addAttachListener(event -> event.getSource().getElement().executeJs(fieldValidationJs));
        this.setAlignSelf(Alignment.CENTER, usernameField);
        return usernameField;
    }

    private PasswordField getPasswordField() {
        PasswordField passwordField = new PasswordField("Password");
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setErrorMessage("Password is required");
        passwordField.addAttachListener(event -> event.getSource().getElement().executeJs(fieldValidationJs));
        this.setAlignSelf(Alignment.CENTER, passwordField);
        return passwordField;
    }

    private TextField getContactField() {
        TextField contactField = new TextField("Contact Number");
        contactField.setRequiredIndicatorVisible(true);
        contactField.setAllowedCharPattern("[0-9()+-]");
        contactField.setPlaceholder("e.g., +923252424212");
        contactField.setMinLength(11);
        contactField.setMaxLength(15);
        contactField.setErrorMessage("Appropriate number is required");
        contactField.addAttachListener(event -> event.getSource().getElement().executeJs(fieldValidationJs));
        this.setAlignSelf(Alignment.CENTER, contactField);
        return contactField;
    }

    private Button createAccountButton(TextField contactField, PasswordField passwordField, TextField usernameField, Upload upload) {
        Button button = new Button("Create", VaadinIcon.BOOK.create());
        button.setClassName("create-account-button");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        button.addClickListener(event -> validateUserInput(contactField, passwordField, usernameField, upload, button));
        this.setAlignSelf(Alignment.CENTER, button);
        return button;
    }

    private void validateUserInput(TextField contactField, PasswordField passwordField, TextField usernameField, Upload upload, Button button) {
        Notification notification = null;
        this.username = usernameField.getValue();
        String password = passwordField.getValue();
        String contactNo = contactField.getValue();

        if (username.isEmpty() || password.isEmpty() || contactNo.isEmpty())
            notification = notify("Fill out all the fields");
        else if (contactNo.length() < 10 || contactNo.charAt(0) != '+')
            notification = notify("Enter a valid contact number");
        else if (personService.containsPhoneNumber(Long.parseLong(contactNo)))
            notification = notify("Entered number already exist");

        if (personService.containsName(this.username))
            notification = notify("Entered username already exist");

        if (notification != null)
            return;

        saveUser(password, contactNo);
        disableFormComponents(contactField, passwordField, usernameField, button);
        addUploadImageComponent(upload);
    }

    private void addUploadImageComponent(Upload upload) {
        this.add(upload);
        this.setAlignSelf(Alignment.CENTER, upload);
    }

    private void disableFormComponents(TextField contactField, PasswordField passwordField, TextField usernameField, Button button) {
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        contactField.setEnabled(false);
        button.setEnabled(false);
    }

    private void saveUser(String password, String contactNo) {
        Person person = new Person();
        person.setName(username);
        person.setPassword(password);
        person.setPhoneNumber((Long.valueOf(contactNo)));
        person.setStatus("Offline");
        personService.save(person);
        this.generateNotification("Your account has been created.");
    }

    private Notification notify(String text) {
        Notification notification;
        notification = this.generateNotification(text);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setPosition(Notification.Position.TOP_CENTER);
        return notification;
    }

    private Upload uploadImage() {
        FileBuffer fileBuffer = new FileBuffer();
        Upload upload = new Upload(fileBuffer);
        upload.setUploadButton(new Button("Upload Image"));
        upload.setDropLabel(new Span("Drop Image here"));
        upload.addSucceededListener(event -> saveUploadedImage(fileBuffer));
        return upload;
    }

    private void saveUploadedImage(FileBuffer fileBuffer) {
        try (InputStream inputStream = fileBuffer.getInputStream()) {
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            File file = new File(imageAbsolutePath + "\\" + username.toLowerCase().concat(".jpg"));
            ImageIO.write(bufferedImage, "jpg", file);
            this.generateNotification("Image Uploaded");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Notification generateNotification(String text) {
        Div div = new Div(new Text("Info"));
        div.getStyle().setFontWeight("bolder").setFontSize("20px");
        return getNotification(text, div);
    }

    private Notification getNotification(String text, Div div) {
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setPosition(Notification.Position.TOP_STRETCH);
        notification.setDuration(2000);
        notification.add(new HorizontalLayout(VaadinIcon.INFO.create(), new Div(div, new Div(text))));
        notification.open();
        this.add(notification);
        return notification;
    }

    private Div wrapFormComponents(H2 heading, TextField usernameField, PasswordField passwordField, TextField contactField, Button button) {
        Div div = new Div(heading, usernameField, passwordField, contactField, button);
        div.setClassName("account-form");
        this.setAlignSelf(Alignment.CENTER, div);
        return div;
    }
}
