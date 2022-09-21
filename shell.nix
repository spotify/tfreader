let

  # use a pinned version of nixpkgs for reproducability
  nixpkgs-version = "22.05";
  pkgs = import
    (builtins.fetchTarball {
      # Descriptive name to make the store path easier to identify
      name = "nixpkgs-${nixpkgs-version}";
      url = "https://github.com/nixos/nixpkgs/archive/${nixpkgs-version}.tar.gz";
      # Hash obtained using `nix-prefetch-url --unpack <url>`
      sha256 = "0d643wp3l77hv2pmg2fi7vyxn4rwy0iyr8djcw1h5x72315ck9ik";
    })
    { };
in
with pkgs;
stdenv.mkDerivation {
  name = "tfreader-dev-env";

  buildInputs = [
    (sbt.override { jre = graalvm11-ce; })
    graalvm11-ce
  ];
}
