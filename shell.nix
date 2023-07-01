{ pkgs ? import <nixpkgs-unstable> { } }:

pkgs.mkShell {
  buildInputs = [
    (pkgs.sbt.override { jre = pkgs.graalvm17-ce; })
    pkgs.graalvm17-ce
  ];
}