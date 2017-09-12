package org.opendata.domain;


public class Menu {
    private MenuItem menuItem[];
    public Menu(MenuItem[] mi) {
        this.menuItem = mi;
    }
    public MenuItem[] getMenuItem() {
        return menuItem;
    }
}
