package com.example.vaadin.views;

import com.example.vaadin.dtos.LoginDetails;
import com.example.vaadin.entities.Contact;
import com.example.vaadin.entities.Person;
import com.example.vaadin.services.ContactService;
import com.example.vaadin.services.PersonService;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;

@Route("home")
@PageTitle("Chatify")
public class HomeView extends VerticalLayout {

    private final Grid<Contact> grid = new Grid<>();

    private final Component nameSearchField;

    private final ContactService contactService;

    private final PersonService personService;

    private final Person person;

    public HomeView(ContactService contactService, PersonService personService, LoginDetails loginDetails) {
        this.contactService = contactService;
        this.personService = personService;
        this.nameSearchField = getContactSearchField();
        this.person = this.personService.getByUsername(loginDetails.getUsername());

        this.setClassName("home-view");
        this.setHeightFull();
        this.addComponents();
    }

    private void addComponents() {
        this.add(getHorizontalLayoutWrappingDiv(titleBar()));
        this.add(contactsTabSheet());
        this.add(this.nameSearchField, getContactGrid());
    }

    private TabSheet contactsTabSheet() {
        TabSheet tabSheet = new TabSheet();

        Tab contactsTab = tabSheet.add(VaadinIcon.USERS.create(), new Span());
        contactsTab.add("Contacts");
        tabSheet.addSelectedChangeListener(event -> addOrRemoveContactsTabComponent(contactsTab));

        Tab newContactTab = tabSheet.add(LumoIcon.PLUS.create(), addNewContact());
        newContactTab.add("Add Contact");

        return tabSheet;
    }

    private void addOrRemoveContactsTabComponent(Tab contactsTab) {
        if (contactsTab.isSelected())
            this.add(this.nameSearchField, this.grid);
        else
            this.remove(this.nameSearchField, this.getGrid());
    }

    private Grid<Contact> getGrid() {
        return this.grid;
    }

    private Component getContactSearchField() {
        TextField searchField = new TextField();
        searchField.setClassName("search-field");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setPlaceholder("Search by name");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(this::manageSearchFieldUserInput);
        return getHorizontalLayoutWrappingField(searchField);
    }

    private HorizontalLayout getHorizontalLayoutWrappingField(TextField textField) {
        HorizontalLayout horizontalLayout = new HorizontalLayout(textField);
        horizontalLayout.setWidthFull();
        horizontalLayout.setJustifyContentMode(JustifyContentMode.END);
        return horizontalLayout;
    }

    private void manageSearchFieldUserInput(AbstractField.ComponentValueChangeEvent<TextField, String> event) {
        final GridListDataView<Contact> gridListDataView = this.grid.getListDataView();
        gridListDataView.removeFilters();
        final String value = event.getValue().trim();
        if (!value.isEmpty())
            gridListDataView.setFilter(person -> matches(person.getName(), value));
    }

    private Grid<Contact> getContactGrid() {
        this.grid.setItems(person.getContactsList());

        gridColumn(contactsNameAndImageRenderer(), "Name", Contact::getName);

        ValueProvider<Contact, String> status = contact -> this.personService
                .getByUsername(contact.getName())
                .getStatus();

        gridColumn(availabilityStatusRenderer(status), "Status", status);

        this.grid.addColumn(trashButtonRenderer())
                .setHeader("Remove")
                .setAutoWidth(true);

        this.grid.addItemClickListener(
                event -> event
                        .getSource()
                        .getUI()
                        .ifPresent(ui -> ui.navigate("chat/" + person.getName() + "/" + event.getItem().getName()))
        );

        this.setAlignSelf(Alignment.CENTER, this.grid);
        return this.grid;
    }

    private void gridColumn(Renderer<Contact> renderer, String header, ValueProvider<Contact, String> keyExtractor) {
        grid.addColumn(renderer)
                .setHeader(header)
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(keyExtractor);
    }

    private static LitRenderer<Contact> contactsNameAndImageRenderer() {
        String templateExpression = "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                + "<vaadin-avatar name=\"${item.name}\" alt=\"User avatar\"  img=\"/images/${item.img}.jpg\"></vaadin-avatar>" + "  <vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">" + "    <span> ${item.name} </span>" + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">" + "  </vaadin-vertical-layout>" + "</vaadin-horizontal-layout>";
        return LitRenderer.<Contact>of(templateExpression)
                .withProperty("name", Contact::getName)
                .withProperty("img", contact -> contact.getName().toLowerCase());
    }

    private ComponentRenderer<Span, Contact> availabilityStatusRenderer(ValueProvider<Contact, String> status) {
        return new ComponentRenderer<>(Span::new, (span, contact) -> {
            String contactStatus = status.apply(contact);

            if (contactStatus.equals("Online"))
                spanThemeAttribute(span, "success");
            else
                spanThemeAttribute(span, "error");

            span.setText(contactStatus);
        });
    }

    private ComponentRenderer<Button, Contact> trashButtonRenderer() {
        return new ComponentRenderer<>(Button::new, (button, contact) -> {
            button.setIcon(VaadinIcon.TRASH.create());
            button.setThemeName("error");
            button.addClickListener(event -> getDeleteContactConfirmDialog(contact).open());
        });
    }

    private ConfirmDialog getDeleteContactConfirmDialog(Contact contact) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Contact");
        confirmDialog.setText("Are you sure you want to permanently delete this contact?");
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");

        confirmDialog.addConfirmListener(
                event -> {
                    String text = contact.getName() + " has been removed from your contacts.";
                    this.generateNotification(text, NotificationVariant.LUMO_PRIMARY, VaadinIcon.INFO.create());
                    contactService.delete(contact);
                    this.grid.getListDataView().removeItem(contact).refreshAll();
                }
        );
        return confirmDialog;
    }

    private Component addNewContact() {
        H2 heading = new H2(LumoIcon.USER.create());
        heading.setClassName("contact-user-icon");

        TextField nameField = getNameField();
        TextField phoneNumberField = getPhoneNumberField();
        Button saveButton = saveContactButton(nameField, phoneNumberField);

        Div contactForm = new Div(heading, getHr(), new Div(nameField), new Div(phoneNumberField), saveButton);
        contactForm.setClassName("contact-form");
        return contactForm;
    }

    private Hr getHr() {
        Hr hr = new Hr();
        hr.setClassName("hr");
        return hr;
    }

    private Button saveContactButton(TextField nameField, TextField phoneNumberField) {
        Button saveButton = new Button("Save", VaadinIcon.PLUS.create());
        saveButton.setClassName("save-button");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        saveButton.addClickListener(saveContactButtonListener(nameField, phoneNumberField));
        return saveButton;
    }

    private TextField getPhoneNumberField() {
        TextField phoneNumberField = getTextField("Contact Number", "Enter contact number");
        phoneNumberField.setAllowedCharPattern("[0-9()+-]");
        phoneNumberField.setMinLength(11);
        phoneNumberField.setMaxLength(15);
        phoneNumberField.setErrorMessage("Appropriate number is required");
        return phoneNumberField;
    }

    private TextField getNameField() {
        TextField nameField = getTextField("Name", "Enter name");
        nameField.setErrorMessage("Username is required");
        return nameField;
    }

    private ComponentEventListener<ClickEvent<Button>> saveContactButtonListener(TextField nameField, TextField phoneNumberField) {
        return event -> {
            if (phoneNumberField.getValue().isEmpty() || nameField.getValue().isEmpty()) {
                Notification notification = this.generateNotification("Fill out all the fields", NotificationVariant.LUMO_ERROR, VaadinIcon.WARNING.create());
                notification.setPosition(Notification.Position.BOTTOM_CENTER);
                return;
            }
            long phoneNumber = Long.parseLong(phoneNumberField.getValue());
            Person contactPerson = this.personService.getByPhoneNumber(phoneNumber);

            if (contactPerson != null) {
                if (!contactPerson.getName().equals(nameField.getValue())) {
                    Notification notification = this.generateNotification("Entered username does not exist", NotificationVariant.LUMO_WARNING, VaadinIcon.INFO.create());
                    notification.setPosition(Notification.Position.TOP_CENTER);
                    return;
                }
                personService.updateContacts(this.person, contactPerson);
                Contact contact = this.person.getContactsList().getLast();

                this.grid.getListDataView()
                        .addItem(contact)
                        .refreshAll();

                this.generateNotification("Added a Contact '" + nameField.getValue() + "'", NotificationVariant.LUMO_SUCCESS, VaadinIcon.INFO.create());
            } else {
                Notification notification;
                if (phoneNumberField.getValue().length() < 10 || phoneNumberField.isEmpty()) {
                    notification = this.generateNotification("Enter a valid number", NotificationVariant.LUMO_ERROR, VaadinIcon.INFO.create());
                } else {
                    notification = this.generateNotification("Entered number does not exist", NotificationVariant.LUMO_WARNING, VaadinIcon.INFO.create());
                }
                notification.setPosition(Notification.Position.TOP_CENTER);
            }
            nameField.clear();
            phoneNumberField.clear();
        };
    }

    private TextField getTextField(String label, String placeholder) {
        return new TextField(label, placeholder);
    }

    private static HorizontalLayout getHorizontalLayoutWrappingDiv(Div div) {
        HorizontalLayout horizontalLayout = new HorizontalLayout(div);
        horizontalLayout.setWidthFull();
        horizontalLayout.setSpacing(false);
        return horizontalLayout;
    }

    private Div titleBar() {
        Icon appIcon = getAppIcon();
        Span appTitle = getAppTitle();
        Button signOutButton = getSignOutButton();
        Div div = new Div(appIcon, new Span(appTitle, signOutButton));
        div.setClassName("title-bar");
        div.setWidthFull();
        return div;
    }

    private Button getSignOutButton() {
        Button button = new Button("Sign out");
        button.setClassName("sign_out-button");
        button.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        button.addClickListener(
                event -> {
                    event.getSource()
                            .getUI()
                            .ifPresent(ui -> ui.navigate("login"));
                    this.person.setStatus("Offline");
                    this.personService.save(this.person);
                }
        );
        return button;
    }

    private static Span getAppTitle() {
        Span title = new Span("Chatify");
        title.setClassName("title");
        return title;
    }

    private static Icon getAppIcon() {
        Icon icon = VaadinIcon.CHAT.create();
        icon.setClassName("app-icon");
        return icon;
    }

    private static void spanThemeAttribute(Span span, String themeAttribute) {
        span.getElement().setAttribute("theme", "badge " + themeAttribute);
    }

    private Notification generateNotification(String text, NotificationVariant notificationVariant, Icon icon) {
        Notification notification = new Notification();
        notification.addThemeVariants(notificationVariant);
        notification.setPosition(Notification.Position.BOTTOM_STRETCH);
        notification.setDuration(2500);
        Div info = new Div(new Text("Info"));
        info.getStyle().setFontWeight("bolder").setFontSize("20px");
        HorizontalLayout horizontalLayout = new HorizontalLayout(icon, new Div(info, new Div(text)));
        notification.add(horizontalLayout);
        notification.open();
        this.add(notification);
        return notification;
    }

    private static boolean matches(String str1, String str2) {
        return str1.toLowerCase().contains(str2.toLowerCase());
    }

}
