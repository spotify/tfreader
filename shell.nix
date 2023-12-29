{ pkgs ? import <nixpkgs-unstable> { } }:

pkgs.mkShell {
  buildInputs = [
    (pkgs.sbt.override { jre = pkgs.graalvm-ce; })
    pkgs.graalvm-ce
  ];
}