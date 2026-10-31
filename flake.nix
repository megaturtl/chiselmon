{
  description = "Chiselmon development environment";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-26.05";

  outputs = { nixpkgs, ... }:
    let
      system = "x86_64-linux";
      pkgs = nixpkgs.legacyPackages.${system};
      nativeLibraryPath = pkgs.lib.makeLibraryPath (with pkgs; [
        libglvnd
        libpulseaudio
        alsa-lib
        flite.lib
      ]);
    in
    {
      devShells.${system}.default = pkgs.mkShell {
        packages = with pkgs; [ jdk21 git nodejs_24 ];
        JAVA_HOME = "${pkgs.jdk21}";
        # Minecraft's bundled natives load graphics, audio, and narrator libraries dynamically.
        shellHook = ''
          export LD_LIBRARY_PATH="${nativeLibraryPath}''${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
          if [ -d /run/opengl-driver/lib ]; then
            export LD_LIBRARY_PATH="/run/opengl-driver/lib:$LD_LIBRARY_PATH"
          fi
        '';
      };
    };
}
