package cc.turtl.cobbleaid.feature.gui.pc.tab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PCTabStore {
    public static final int MAX_TABS = 5;
    private final List<PCTab> tabs = new ArrayList<>();

    public PCTabStore() {
    }

    public PCTabStore(int boxNumber) {
        addTab(boxNumber);
    }

    public List<PCTab> getTabs() {
        return Collections.unmodifiableList(tabs);
    }

    public PCTab getTab(int boxNumber) {
        return tabs.stream()
                .filter(tab -> tab.boxNumber == boxNumber)
                .findFirst()
                .orElse(null);
    }

    public boolean addTab(int boxNumber) {
        if (this.isFull() || hasBoxNumber(boxNumber)) {
            return false;
        }
        return tabs.add(new PCTab(boxNumber));
    }

    public void clear() {
        tabs.clear();
    }

    public boolean removeTab(int boxNumber) {
        return tabs.removeIf(tab -> tab.boxNumber == boxNumber);
    }

    public boolean isFull() {
        return (tabs.size() >= MAX_TABS);
    }

    public boolean hasBoxNumber(int boxNumber) {
        return tabs.stream().anyMatch(tab -> tab.boxNumber == boxNumber);
    }
}