package cc.turtl.cobbleaid;

import cc.turtl.cobbleaid.feature.pc.tabs.PCTabStore;

public class WorldDataStore {
    private PCTabStore pcTabStore;

    public WorldDataStore() {
        this.pcTabStore = new PCTabStore();
    }

    public PCTabStore getPcTabStore() {
        return pcTabStore;
    }
}
