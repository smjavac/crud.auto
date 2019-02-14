package my.vaadin;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.*;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import org.apache.log4j.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;



@Theme("mytheme")
public class MyUI extends UI {
    private ListDataProvider<Automobiles> dataProvider;
    private List<Automobiles> autoList;
    static final String JDBC_DRIVER = "org.postgresql.Driver";
    static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/postgres";
    private Binder<Automobiles> binder = new Binder<>();
    static final String USER = "postgres";
    static final String PASSWORD = "5099";
    private Grid<Automobiles> grid = new Grid<>();
    private TextField name1;
    private TextField name2;
    private TextField search;
    private static final org.apache.log4j.Logger logger =  Logger.getLogger(MyUI.class);
   // FilterGrid<Automobiles> grid2 = new FilterGrid<>(Automobiles.class);

   private void onNameFilterTextChange(HasValue.ValueChangeEvent<String> event) {
       ListDataProvider<Automobiles> dataProvider2 = (ListDataProvider<Automobiles>) grid.getDataProvider();
       dataProvider.setFilter(Automobiles::getModel, s -> caseInsensitiveContains(s, event.getValue()));
   }

    private Boolean caseInsensitiveContains(String where, String what) {
        return where.toLowerCase().contains(what.toLowerCase());
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        //  Window subWindow = new Window("sub window");

        final VerticalLayout verticalLayout = new VerticalLayout();
        final HorizontalLayout horizontaLayout = new HorizontalLayout();
        //       addWindow(subWindow);
        Button add = new Button("Добавить");
        Button delete = new Button("Удалить");
        Button edit = new Button("Изменить");

        // здесь логика ИЗМЕНИТЬ
        edit.addClickListener(clickEvent -> {
         //   logger.info("button edit");
            MySub subEditRow = new MySub();

            subEditRow.editRow();
            UI.getCurrent().addWindow(subEditRow);
        });
        // здесь логика кнопки DELETE
        delete.addClickListener(clickEvent -> {
           // logger.info("button delete");
            MySub subDeleteRow = new MySub();
            subDeleteRow.deleteRow();
         });

        // здесь кнопака ДОБАВИТЬ
        add.addClickListener(clickEvent -> {
         //   logger.info("button add");
            MySub subAddRow = new MySub();
            subAddRow.addAuto();
            UI.getCurrent().addWindow(subAddRow);
        });


        initDataProvider();
        initGrid();

        search = new TextField();
        search.setPlaceholder("Filter by model...");
        search.setWidth("157");
        search.addValueChangeListener(this::onNameFilterTextChange);

        //Automobiles auto2 = new Automobiles();
       // dataProvider.addFilter(auto -> auto.getId() > 3);

        horizontaLayout.addComponents(add, delete, edit, search);
        verticalLayout.addComponents(horizontaLayout, grid);
        //  subWindow.setContent(layout);
        //  subWindow.center();

        setContent(verticalLayout);
        //   setContent(hLayout);

    }





    private void initGrid () {
        grid.addColumn(Automobiles::getId).setCaption("Id");
        grid.addColumn(Automobiles::getModel).setCaption("Model");
        grid.addColumn(Automobiles::getBody).setCaption("Body");
    }

    public void initDataProvider () {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String sql_2 = "SELECT * FROM auto";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            // statement.executeQuery(sql_2);
            //  ResultSet resultSet = statement.getResultSet();
            ResultSet resultSet = statement.executeQuery(sql_2);
            logger.debug(sql_2);
            autoList = new ArrayList<>();
            while (resultSet.next()) {
                Automobiles auto = new Automobiles();
                auto.setId(resultSet.getInt("id"));
                auto.setModel(resultSet.getString("model"));
                auto.setBody(resultSet.getString("body"));
                autoList.add(auto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error(e);
        }

        dataProvider = new ListDataProvider<Automobiles>(autoList) {
            @Override
            public Object getId(Automobiles item) {
                return item.getId();
            }
        };
        grid.setDataProvider(dataProvider);
    }

    class MySub extends Window {

        public void addAuto(){
            // super("новый автомобиль"); // Set window caption
            center();

            // Disable the close button
            // setClosable(false);
            final VerticalLayout layout2 = new VerticalLayout();
            final TextField modelTipeTxt = new TextField();
            final TextField modelTxt = new TextField();
            modelTipeTxt.setCaption("Марка");
            modelTxt.setCaption("Модель");
            Button button2 = new Button("Сохранить");
            button2.addClickListener(clickEvent -> {
                binder.forField(modelTipeTxt)
                        .withValidator(value -> value.length() > 0, "Поле не должно быть пустым")
                        //.withConverter(new StringToIntegerConverter("Input must be Integer"))
                        //.withValidator(afterConversion)
                        .bind(Automobiles::getModel, Automobiles::setModel);

                binder.forField(modelTxt)
                        .withValidator(value -> value.length() > 0, "Поле не должно быть пустым")
                        .bind(Automobiles::getBody, Automobiles::setBody);
                try {
                    Class.forName("org.postgresql.Driver");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                String sql = "INSERT into  auto ( model, body) VALUES ('" + modelTipeTxt.getValue() + "',  '" + modelTxt.getValue() + "')";
                try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
                     Statement statement = connection.createStatement()) {
                    BinderValidationStatus<Automobiles> status = binder.validate();
                    if (!status.hasErrors()){
                        statement.executeUpdate(sql);
                        logger.debug(sql);
                        close();
                    }
                    initDataProvider();
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.error(e);
                }
            });

            layout2.addComponents(modelTipeTxt, modelTxt, button2);
            setContent(layout2);
        }

        public void editRow() {
            center();
            final VerticalLayout layout3 = new VerticalLayout();
            name1 = new TextField();
            name2 = new TextField();
            name1.setCaption("Марка");
            name2.setCaption("Модель");
            name1.setValue(grid.getSelectedItems().iterator().next().getModel());
            name2.setValue(grid.getSelectedItems().iterator().next().getBody());

            Button save = new Button("Сохранить");
            save.addClickListener(clickEvent -> {
//                    binder.forField(name1)
//                            .withValidator(value -> value.length() > 0, "Поле не должно быть пустым")
//                            .bind(Automobiles::getModel, Automobiles::setModel);
//
//                    binder.forField(name2)
//                            .withValidator(value -> value.length() > 0, "Поле не должно быть пустым")
//                            .bind(Automobiles::getBody, Automobiles::setBody);
                try {
                    Class.forName("org.postgresql.Driver");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                String model = name1.getValue();
                String body = name2.getValue();

                String sqlGridEdit = "UPDATE auto SET model= '" + model + "', body= '" + body + "' where id='"+ grid.getSelectionModel().getFirstSelectedItem().get().getId()+ "'";


                try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
                     Statement statement = connection.createStatement()) {
                    //BinderValidationStatus<Automobiles> status = binder.validate();
                    //if (!status.hasErrors())
                    statement.executeUpdate(sqlGridEdit);
                    logger.debug(sqlGridEdit);
                    initDataProvider();
                    close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.error(e);
                }
            });
            layout3.addComponents(name1, name2, save);
            setContent(layout3);
        }

        public void deleteRow(){
            center();
            String sqlDelete = "DELETE from auto where id='"+grid.getSelectionModel().getFirstSelectedItem().get().getId()+ "'";
            //    String sqlGridDelete = "DELETE from auto where model= '" + grid.getSelectionModel().getFirstSelectedItem().get().getModel() + "'";
            // "SELECT dss_attr_name FROM dm_type_attribute WHERE dss_type_name = '" + view.typeName.getValue() + "'"
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                logger.error(e);
            }

            try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate(sqlDelete);
                logger.debug(sqlDelete);
                initDataProvider();
            } catch (SQLException e) {
                e.printStackTrace();
                logger.error(e);
                initDataProvider();
            }
        }

        public MySub() {
            super("новый автомобиль"); // Set window caption
//                center();
//
//                // Disable the close button
//                // setClosable(false);
//                final VerticalLayout layout2 = new VerticalLayout();
//                final TextField name1 = new TextField();
//                final TextField name2 = new TextField();
//                name1.setCaption("Марка");
//                name2.setCaption("Модель");
//                Button button2 = new Button("Сохранить");
//                button2.addClickListener(clickEvent -> {
//                    binder.forField(name1)
//                            .withValidator(value -> value.length() > 0, "Поле не должно быть пустым")
//                            //.withConverter(new StringToIntegerConverter("Input must be Integer"))
//                            //.withValidator(afterConversion)
//                            .bind(Automobiles::getModel, Automobiles::setModel);
//
//                    binder.forField(name2)
//                            .withValidator(value -> value.length() > 0, "Поле не должно быть пустым")
//                            .bind(Automobiles::getBody, Automobiles::setBody);
//                    try {
//                        Class.forName("org.postgresql.Driver");
//                    } catch (ClassNotFoundException e) {
//                        e.printStackTrace();
//                    }
//
//                    String sql = "INSERT into  auto (id, model, body) VALUES (1,'" + name1.getValue() + "',  '" + name2.getValue() + "')";
//                    try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
//                         Statement statement = connection.createStatement()) {
//                        BinderValidationStatus<Automobiles> status = binder.validate();
//                        if (!status.hasErrors())
//                            statement.executeUpdate(sql);
//
//                        initDataProvider();
//                        close();
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                    }
//
//                });
//
//                layout2.addComponents(name1, name2, button2);
//                setContent(layout2);

        }
    }


    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
