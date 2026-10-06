#!/usr/bin/env python3
"""Select a SpotiTheme palette and capture a real Spotify screen over ADB."""

from __future__ import annotations

import argparse
import io
import os
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

try:
    from PIL import Image
except ImportError as exc:  # pragma: no cover - depends on host setup
    raise SystemExit("Screenshot optimization requires Pillow: python3 -m pip install Pillow") from exc


ROOT = Path(__file__).resolve().parents[1]
THEME_ACTIVITY = "com.spotitheme/.settings.ThemeActivity"
UI_DUMP = "/sdcard/spottheme-showcase-ui.xml"


def run_adb(serial: str, *args: str, timeout: int = 30) -> bytes:
    return subprocess.run(
        ["adb", "-s", serial, *args],
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        timeout=timeout,
    ).stdout


def verify_device(serial: str) -> None:
    output = subprocess.run(
        ["adb", "devices", "-l"], check=True, text=True, capture_output=True
    ).stdout
    for line in output.splitlines()[1:]:
        fields = line.split()
        if len(fields) >= 2 and fields[0] == serial and fields[1] == "device":
            return
    raise RuntimeError(
        f"ADB device {serial!r} is not online. Connect it and check `adb devices -l`."
    )


def dump_ui(serial: str) -> ET.Element:
    result = run_adb(serial, "shell", "uiautomator", "dump", UI_DUMP, timeout=45)
    xml = run_adb(serial, "shell", "cat", UI_DUMP)
    if b"<hierarchy" not in xml or b"dumped to:" not in result:
        raise RuntimeError("Could not obtain a fresh UIAutomator hierarchy")
    return ET.fromstring(xml)


def node_bounds(node: ET.Element) -> tuple[int, int, int, int]:
    values = [int(value) for value in re.findall(r"\d+", node.get("bounds", ""))]
    if len(values) != 4 or values[2] <= values[0] or values[3] <= values[1]:
        raise RuntimeError("The selected UI element has no usable bounds")
    return tuple(values)  # type: ignore[return-value]


def tap_node(serial: str, node: ET.Element) -> None:
    left, top, right, bottom = node_bounds(node)
    run_adb(serial, "shell", "input", "tap", str((left + right) // 2), str((top + bottom) // 2))


def find_theme_row(root: ET.Element, theme: str) -> ET.Element | None:
    for node in root.iter("node"):
        if node.get("class") == "android.widget.CheckedTextView" and node.get("text") == theme:
            return node
    return None


def choose_theme(serial: str, theme: str) -> None:
    run_adb(serial, "shell", "am", "start", "-n", THEME_ACTIVITY)
    time.sleep(1.3)
    root = dump_ui(serial)

    # If the theme popup is closed, open the first (theme) spinner. The second
    # spinner on this screen selects an artwork palette and is not touched.
    popup_open = any(node.get("class") == "android.widget.ListView" for node in root.iter("node"))
    if find_theme_row(root, theme) is None and not popup_open:
        spinners = [
            node for node in root.iter("node")
            if node.get("package") == "com.spotitheme"
            and node.get("class") == "android.widget.Spinner"
        ]
        if not spinners:
            raise RuntimeError("Theme manager is open but its theme selector is missing")
        tap_node(serial, spinners[0])
        time.sleep(0.8)

    selected_row = None
    for direction in ("later", "earlier"):
        previous_rows = None
        for _ in range(6):
            root = dump_ui(serial)
            row = find_theme_row(root, theme)
            if row is not None:
                selected_row = row
                break
            popup = next(
                (node for node in root.iter("node") if node.get("class") == "android.widget.ListView"),
                None,
            )
            if popup is None:
                raise RuntimeError(f"Theme selector did not open for {theme!r}")
            rows = tuple(
                node.get("text", "") for node in popup.iter("node")
                if node.get("class") == "android.widget.CheckedTextView"
            )
            if rows == previous_rows:
                break  # Reached this end of the list; search in the opposite direction.
            previous_rows = rows
            left, top, right, bottom = node_bounds(popup)
            x = (left + right) // 2
            start_fraction, end_fraction = ((4, 1) if direction == "later" else (1, 4))
            run_adb(
                serial, "shell", "input", "swipe",
                str(x), str(top + (bottom - top) * start_fraction // 5),
                str(x), str(top + (bottom - top) * end_fraction // 5), "350",
            )
            time.sleep(0.25)
        if selected_row is not None:
            break

    if selected_row is None:
        raise RuntimeError(f"Theme {theme!r} was not found in the bundled/imported catalog")
    tap_node(serial, selected_row)
    time.sleep(0.55)

    root = dump_ui(serial)
    spinners = [
        node for node in root.iter("node")
        if node.get("package") == "com.spotitheme"
        and node.get("class") == "android.widget.Spinner"
    ]
    selected = [child.get("text") for child in spinners[0].iter("node") if child.get("text")] if spinners else []
    if theme not in selected:
        raise RuntimeError(f"Theme selection did not persist; selector now shows {selected!r}")


def screen_size(serial: str) -> tuple[int, int]:
    text = run_adb(serial, "shell", "wm", "size").decode("utf-8", "replace")
    matches = re.findall(r"(\d+)x(\d+)", text)
    if not matches:
        raise RuntimeError(f"Could not parse the device screen size: {text.strip()}")
    return int(matches[-1][0]), int(matches[-1][1])


def select_spotify_view(serial: str, view: str) -> None:
    if view == "settings":
        return  # Keep the SpotiTheme manager in the foreground.

    run_adb(
        serial, "shell", "am", "start", "-a", "android.intent.action.MAIN",
        "-c", "android.intent.category.LAUNCHER", "-p", "com.spotify.music",
    )
    time.sleep(1.1)
    root = dump_ui(serial)
    if root.find("node").get("package") != "com.spotify.music":
        raise RuntimeError("Spotify did not reach the foreground")

    if view in {"home", "library", "player"}:
        target = "Home, Tab 1 of 4" if view in {"home", "player"} else "Your Library, Tab 3 of 4"
        tab = next(
            (node for node in root.iter("node") if node.get("content-desc") == target),
            None,
        )
        if tab is None:
            raise RuntimeError(f"Spotify navigation tab {target!r} is not visible")
        tap_node(serial, tab)
        time.sleep(1.0)

    if view == "library":
        root = dump_ui(serial)
        albums = next(
            (node for node in root.iter("node") if node.get("content-desc") == "Albums, show only albums."),
            None,
        )
        if albums is None:
            raise RuntimeError("Spotify Library album filter is not visible")
        tap_node(serial, albums)
        time.sleep(0.8)

    if view == "player":
        session = run_adb(serial, "shell", "dumpsys", "media_session").decode("utf-8", "replace")
        if "active item id=" not in session:
            raise RuntimeError("No current Spotify item is available; capture is not starting playback")
        width, height = screen_size(serial)
        # Open the mini-player body, well away from its Play/Pause and Connect controls.
        run_adb(serial, "shell", "input", "tap", str(width * 42 // 100), str(height * 87 // 100))
        time.sleep(1.2)

    root = dump_ui(serial)
    if root.find("node").get("package") != "com.spotify.music":
        raise RuntimeError("A non-Spotify screen is in the foreground; no screenshot saved")


def optimized_capture(serial: str, destination: Path, width: int = 320) -> tuple[int, int]:
    raw = run_adb(serial, "exec-out", "screencap", "-p", timeout=45)
    image = Image.open(io.BytesIO(raw)).convert("RGB")
    source_width, source_height = image.size
    # Remove the status and gesture bars while retaining Spotify's app navigation.
    top = round(source_height * 0.034)
    bottom = round(source_height * 0.027)
    image = image.crop((0, top, source_width, source_height - bottom))
    new_height = round(image.height * width / image.width)
    image = image.resize((width, new_height), Image.Resampling.LANCZOS)
    destination.parent.mkdir(parents=True, exist_ok=True)
    image.save(destination, format="PNG", optimize=True, compress_level=9)
    return image.size


def slug(value: str) -> str:
    result = re.sub(r"[^a-z0-9]+", "-", value.casefold()).strip("-")
    if not result:
        raise ValueError("Theme and view must produce a non-empty filename")
    return result


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--theme", required=True, help='Exact theme label, e.g. "Catppuccin Mocha"')
    parser.add_argument("--view", required=True, choices=("home", "player", "library", "settings"))
    parser.add_argument("--serial", default=os.environ.get("SPOTITHEME_SERIAL", "17004dab"))
    parser.add_argument("--output", type=Path, help="Optional output path; default is docs/screenshots/<theme>-<view>.png")
    parser.add_argument("--settle-seconds", type=float, default=3.0, help="Wait after navigation before capture")
    args = parser.parse_args()

    try:
        verify_device(args.serial)
        choose_theme(args.serial, args.theme)
        select_spotify_view(args.serial, args.view)
        time.sleep(max(0.0, args.settle_seconds))
        destination = args.output or ROOT / "docs" / "screenshots" / f"{slug(args.theme)}-{args.view}.png"
        dimensions = optimized_capture(args.serial, destination)
    except (RuntimeError, subprocess.CalledProcessError, subprocess.TimeoutExpired, ET.ParseError) as exc:
        detail = getattr(exc, "stderr", b"")
        if isinstance(detail, bytes):
            detail = detail.decode("utf-8", "replace").strip()
        print(f"Capture failed: {exc}{': ' + detail if detail else ''}", file=sys.stderr)
        return 1

    print(f"Selected {args.theme!r}; captured {args.view} at {dimensions[0]}x{dimensions[1]}: {destination}")
    if args.view != "settings":
        print("No media transport control was sent. The selected theme remains active until changed again.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
