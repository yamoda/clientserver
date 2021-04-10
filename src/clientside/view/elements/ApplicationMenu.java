package clientside.view.elements;

import javax.swing.*;

public class ApplicationMenu extends JMenuBar{
    JMenuItem saveItem;
    JMenuItem openItem;
    JMenuItem deleteFileItem;

    JMenuItem newEntryItem;
    JMenuItem searchItem;
    JMenuItem deleteItem;

    JMenuItem connectItem;


    public ApplicationMenu() {
        JMenu fileMenu = new JMenu("Файл");

        saveItem = new JMenuItem("Сохранить");
        openItem = new JMenuItem("Загрузить");
        deleteFileItem = new JMenuItem("Удалить");

        fileMenu.add(saveItem);
        fileMenu.add(openItem);
        fileMenu.add(deleteFileItem);

        JMenu toolsMenu = new JMenu("Инструменты");

        newEntryItem = new JMenuItem("Добавить");
        searchItem = new JMenuItem("Найти");
        deleteItem = new JMenuItem("Удалить");

        toolsMenu.add(newEntryItem);
        toolsMenu.add(searchItem);
        toolsMenu.add(deleteItem);

        JMenu serverMenu = new JMenu("Сервер");

        connectItem = new JMenuItem("Подключиться к серверу");

        serverMenu.add(connectItem);

        add(fileMenu);
        add(toolsMenu);
        add(serverMenu);
    }

    public JMenuItem getSaveItem() { return saveItem; }

    public JMenuItem getOpenItem() { return openItem; }

    public JMenuItem getDeleteFileItem() { return deleteFileItem; }

    public JMenuItem getNewEntryItem() { return newEntryItem; }

    public JMenuItem getSearchItem() { return searchItem; }

    public JMenuItem getDeleteItem() { return deleteItem; }

    public JMenuItem getConnectItem() { return connectItem; }
}
