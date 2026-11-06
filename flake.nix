{
  description = "Chiselmon development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs?ref=nixos-unstable";
    treefmt-nix = {
      url = "github:numtide/treefmt-nix";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = {
    self,
    nixpkgs,
    treefmt-nix,
    ...
  }: let
    system = "x86_64-linux";
    pkgs = nixpkgs.legacyPackages.${system};
    treefmtEval = treefmt-nix.lib.evalModule pkgs ./treefmt.nix;
    nativeLibraryPath = pkgs.lib.makeLibraryPath (
      with pkgs; [
        libglvnd
        libpulseaudio
        alsa-lib
        flite.lib
      ]
    );
  in {
    formatter.${system} = treefmtEval.config.build.wrapper;
    checks.${system}.formatting = treefmtEval.config.build.check self;

    devShells.${system}.default = pkgs.mkShell {
      packages = with pkgs; [
        jdk21
        git
        nodejs_24
        treefmtEval.config.build.wrapper
      ];
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
