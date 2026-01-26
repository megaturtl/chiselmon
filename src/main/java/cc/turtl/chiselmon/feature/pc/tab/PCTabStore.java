package cc.turtl.chiselmon.feature.pc.tab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PCTabStore {
    public static final int MAX_TABS = 5;
    private final List<PCTab> tabs = new ArrayList<>();

    public PCTabStore() {
    }

    public List<PCTab> getTabs() {
        return Collections.unmodifiableList(tabs);
    }

    public void addTab(int boxNumber) {
        if (this.isFull() || hasBoxNumber(boxNumber)) {
            return;
        }
        tabs.add(new PCTab(boxNumber));
    }

    public void removeTab(int boxNumber) {
        tabs.removeIf(tab -> tab.boxNumber == boxNumber);
    }

    public boolean isFull() {
        return (tabs.size() >= MAX_TABS);
    }

    public boolean hasBoxNumber(int boxNumber) {
        return tabs.stream().anyMatch(tab -> tab.boxNumber == boxNumber);
    }
}