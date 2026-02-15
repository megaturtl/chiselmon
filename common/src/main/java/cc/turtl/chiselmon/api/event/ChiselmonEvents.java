package cc.turtl.chiselmon.api.event;

public class ChiselmonEvents {
    public static final Observable<PokemonLoadedEvent> POKEMON_LOADED = new Observable<>();
    public static final Observable<PokemonUnloadedEvent> POKEMON_UNLOADED = new Observable<>();

    public static final Observable<LevelConnectedEvent> LEVEL_CONNECTED = new Observable<>();
    public static final Observable<LevelDisconnectedEvent> LEVEL_DISCONNECTED = new Observable<>();
    public static final Observable<GameStoppingEvent> GAME_STOPPING = new Observable<>();

    public static final Observable<ClientPostTickEvent> CLIENT_POST_TICK = new Observable<>();
}