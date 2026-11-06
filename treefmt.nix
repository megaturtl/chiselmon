{...}: {
  projectRootFile = "flake.nix";

  programs = {
    alejandra.enable = true;
    ktfmt.enable = true;
    google-java-format = {
      enable = true;
      aospStyle = true;
    };
    prettier = {
      enable = true;
      settings.overrides = [
        {
          files = "*.mcmeta";
          options.parser = "json";
        }
      ];
    };
    taplo.enable = true;
  };

  settings.formatter = {
    ktfmt.options = ["--kotlinlang-style"];
    google-java-format.options = [
      "--skip-reflowing-long-strings"
      "--cleanup=false"
    ];
    prettier.includes = ["*.mcmeta"];
  };

  settings.excludes = [
    "build/**"
    "**/build/**"
    ".gradle/**"
    "**/.gradle/**"
    ".fabric/**"
    "**/.fabric/**"
    "runClient/**"
    "**/runClient/**"
    "runServer/**"
    "**/runServer/**"
    "fabric/src/generated/**"
    "gradle/wrapper/**"
    "gradlew"
    "gradlew.bat"
    "**/*.kt.peb"
  ];
}
