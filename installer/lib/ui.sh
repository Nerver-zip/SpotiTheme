#!/usr/bin/env bash
say() { printf '%s\n' "$*"; }
die() { printf 'Error: %s\n' "$*" >&2; exit 1; }
confirm() {
    local answer
    read -r -p "$1 [y/N] " answer
    [[ "$answer" == y || "$answer" == Y || "$answer" == yes ]]
}
require_command() { command -v "$1" >/dev/null || die "Required command not found: $1"; }
