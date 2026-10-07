<h1 align="center">SpotiTheme</h1>

<p align="center"><strong>Give Spotify on Android a new look with 29 themes and custom JSON palettes.</strong></p>

<p align="center">
  <a href="#compatibility"><img src="https://img.shields.io/badge/Spotify-9.1.86.2432-brightgreen?style=flat-square" alt="Spotify 9.1.86.2432"/></a>
  <a href="#theme-library"><img src="https://img.shields.io/badge/Themes-29-cba6f7?style=flat-square" alt="29 themes"/></a>
  <a href="#visual-showcase"><img src="https://img.shields.io/badge/Default-Catppuccin%20Mocha-89b4fa?style=flat-square" alt="Default theme Catppuccin Mocha"/></a>
  <a href="#architecture"><img src="https://img.shields.io/badge/Runtime%20DexKit-none-a6e3a1?style=flat-square" alt="No runtime DexKit"/></a>
  <a href="#installation"><img src="https://img.shields.io/badge/Framework-Vector%20%2F%20LSPatch-fab387?style=flat-square" alt="Vector and LSPatch"/></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-f9e2af?style=flat-square" alt="MIT license"/></a>
</p>

<table align="center">
  <tr>
    <th align="center">Catppuccin Mocha · Home</th>
    <th align="center">Catppuccin Latte · Home</th>
  </tr>
  <tr>
    <td align="center"><img src="docs/screenshots/catppuccin-mocha-home.png" alt="Spotify Home using Catppuccin Mocha" width="320"/></td>
    <td align="center"><img src="docs/screenshots/catppuccin-latte-home.png" alt="Spotify Home using Catppuccin Latte" width="320"/></td>
  </tr>
</table>

<p align="center"><em>Captures from Spotify on Android 16.</em></p>

> [!IMPORTANT]
> SpotiTheme targets <strong>Spotify 9.1.86.2432 / version code 146555520</strong>. A different Spotify build needs a separately verified reflection profile and fresh device acceptance; this release does not claim compatibility with newer versions.

## What it does

Choose from 29 built-in themes or load your own JSON palettes from a folder on your phone. Catppuccin Mocha is the default.

- Change colors across Home, Library, Search, playlists and the player, as well as supported SongDNA, Credits and Connect views.
- Switch between light and dark themes in the SpotiTheme app.
- Use a fixed palette or let **Auto Theme** pick colors from the current album artwork.
- Control whether Spotify uses album artwork colors on supported surfaces.
- Install with Vector on a rooted phone, or use the rootless installer on a computer.

The expanded player keeps album covers visible against the theme background and preserves Spotify's backdrop for featured videos. Media layouts that aren't supported keep their original appearance. Animated backgrounds aren't included.

Some Spotify elements may keep their original colors. See <a href="docs/VALIDATION.md">the validation notes</a> for coverage and known limitations.

## Visual showcase

### Theme and player surfaces

<table align="center">
  <tr>
    <th align="center">Mocha · Expanded player</th>
    <th align="center">Latte · Expanded player</th>
  </tr>
  <tr>
    <td align="center"><img src="docs/screenshots/catppuccin-mocha-player.png" alt="Expanded Spotify player in Catppuccin Mocha" width="320"/></td>
    <td align="center"><img src="docs/screenshots/catppuccin-latte-player.png" alt="Expanded Spotify player in Catppuccin Latte" width="320"/></td>
  </tr>
  <tr>
    <th align="center">Mocha · Theme manager</th>
    <th align="center">Latte · Theme manager</th>
  </tr>
  <tr>
    <td align="center"><img src="docs/screenshots/catppuccin-mocha-settings.png" alt="SpotiTheme manager showing 29 themes and regular Catppuccin Mocha selected" width="320"/></td>
    <td align="center"><img src="docs/screenshots/catppuccin-latte-settings.png" alt="SpotiTheme manager using Catppuccin Latte" width="320"/></td>
  </tr>
</table>

<p align="center"><strong>Library · Mocha</strong><br/><img src="docs/screenshots/catppuccin-mocha-library.png" alt="Spotify Library album list in Catppuccin Mocha" width="320"/></p>

### Popular colorways

<table align="center">
  <tr><th align="center">Dracula</th><th align="center">Rose Pine Dawn</th></tr>
  <tr>
    <td align="center"><img src="docs/screenshots/dracula-home.png" alt="Dracula theme on Spotify Home" width="280"/></td>
    <td align="center"><img src="docs/screenshots/rose-pine-dawn-home.png" alt="Rose Pine Dawn theme on Spotify Home" width="280"/></td>
  </tr>
  <tr><th align="center">Tokyo Night</th><th align="center">Gruvbox Dark</th></tr>
  <tr>
    <td align="center"><img src="docs/screenshots/tokyo-night-home.png" alt="Tokyo Night theme on Spotify Home" width="280"/></td>
    <td align="center"><img src="docs/screenshots/gruvbox-dark-home.png" alt="Gruvbox Dark theme on Spotify Home" width="280"/></td>
  </tr>
</table>

## Theme library

29 themes.

<table>
  <thead>
    <tr><th>Palette</th><th>Base</th><th>Background</th><th>Surface</th><th>Accent</th><th>File</th></tr>
  </thead>
  <tbody>
<tr><td>Ayu Dark</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#0B0E14;border:1px solid #888;border-radius:50%"></span> <code>#0B0E14</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#080B0F;border:1px solid #888;border-radius:50%"></span> <code>#080B0F</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#E6B450;border:1px solid #888;border-radius:50%"></span> <code>#E6B450</code></td><td><a href="app/src/main/assets/themes/Ayu%20Dark.json">JSON</a></td></tr>
<tr><td>Ayu Mirage</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#212733;border:1px solid #888;border-radius:50%"></span> <code>#212733</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#191E2A;border:1px solid #888;border-radius:50%"></span> <code>#191E2A</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#FFCC66;border:1px solid #888;border-radius:50%"></span> <code>#FFCC66</code></td><td><a href="app/src/main/assets/themes/Ayu%20Mirage.json">JSON</a></td></tr>
<tr><td>Catppuccin</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#1E1E2E;border:1px solid #888;border-radius:50%"></span> <code>#1E1E2E</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#232434;border:1px solid #888;border-radius:50%"></span> <code>#232434</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#89B4FA;border:1px solid #888;border-radius:50%"></span> <code>#89B4FA</code></td><td><a href="app/src/main/assets/themes/Catppuccin.json">JSON</a></td></tr>
<tr><td>Catppuccin Frappe</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#303446;border:1px solid #888;border-radius:50%"></span> <code>#303446</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#292C3C;border:1px solid #888;border-radius:50%"></span> <code>#292C3C</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#A6D189;border:1px solid #888;border-radius:50%"></span> <code>#A6D189</code></td><td><a href="app/src/main/assets/themes/Catppuccin%20Frappe.json">JSON</a></td></tr>
<tr><td>Catppuccin Latte</td><td>Light</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#EFF1F5;border:1px solid #888;border-radius:50%"></span> <code>#EFF1F5</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#E6E9EF;border:1px solid #888;border-radius:50%"></span> <code>#E6E9EF</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#40A02B;border:1px solid #888;border-radius:50%"></span> <code>#40A02B</code></td><td><a href="app/src/main/assets/themes/Catppuccin%20Latte.json">JSON</a></td></tr>
<tr><td>Catppuccin Macchiato</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#24273A;border:1px solid #888;border-radius:50%"></span> <code>#24273A</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#1E2030;border:1px solid #888;border-radius:50%"></span> <code>#1E2030</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#A6DA95;border:1px solid #888;border-radius:50%"></span> <code>#A6DA95</code></td><td><a href="app/src/main/assets/themes/Catppuccin%20Macchiato.json">JSON</a></td></tr>
<tr><td>Catppuccin Mocha</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#1E1E2E;border:1px solid #888;border-radius:50%"></span> <code>#1E1E2E</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#181825;border:1px solid #888;border-radius:50%"></span> <code>#181825</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#A6E3A1;border:1px solid #888;border-radius:50%"></span> <code>#A6E3A1</code></td><td><a href="app/src/main/assets/themes/Catppuccin%20Mocha.json">JSON</a></td></tr>
<tr><td>Dracula</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#282A36;border:1px solid #888;border-radius:50%"></span> <code>#282A36</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#21222C;border:1px solid #888;border-radius:50%"></span> <code>#21222C</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#BD93F9;border:1px solid #888;border-radius:50%"></span> <code>#BD93F9</code></td><td><a href="app/src/main/assets/themes/Dracula.json">JSON</a></td></tr>
<tr><td>Everforest Dark</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#2D353B;border:1px solid #888;border-radius:50%"></span> <code>#2D353B</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#232A2E;border:1px solid #888;border-radius:50%"></span> <code>#232A2E</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#A7C080;border:1px solid #888;border-radius:50%"></span> <code>#A7C080</code></td><td><a href="app/src/main/assets/themes/Everforest%20Dark.json">JSON</a></td></tr>
<tr><td>Everforest Light</td><td>Light</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#FDF6E3;border:1px solid #888;border-radius:50%"></span> <code>#FDF6E3</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#F4F0D9;border:1px solid #888;border-radius:50%"></span> <code>#F4F0D9</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#8DA101;border:1px solid #888;border-radius:50%"></span> <code>#8DA101</code></td><td><a href="app/src/main/assets/themes/Everforest%20Light.json">JSON</a></td></tr>
<tr><td>Gruvbox Dark</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#282828;border:1px solid #888;border-radius:50%"></span> <code>#282828</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#1D2021;border:1px solid #888;border-radius:50%"></span> <code>#1D2021</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#B8BB26;border:1px solid #888;border-radius:50%"></span> <code>#B8BB26</code></td><td><a href="app/src/main/assets/themes/Gruvbox%20Dark.json">JSON</a></td></tr>
<tr><td>Gruvbox Light</td><td>Light</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#FBF1C7;border:1px solid #888;border-radius:50%"></span> <code>#FBF1C7</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#F2E5BC;border:1px solid #888;border-radius:50%"></span> <code>#F2E5BC</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#79740E;border:1px solid #888;border-radius:50%"></span> <code>#79740E</code></td><td><a href="app/src/main/assets/themes/Gruvbox%20Light.json">JSON</a></td></tr>
<tr><td>Gruvbox Material</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#282828;border:1px solid #888;border-radius:50%"></span> <code>#282828</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#1E2022;border:1px solid #888;border-radius:50%"></span> <code>#1E2022</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#A9B665;border:1px solid #888;border-radius:50%"></span> <code>#A9B665</code></td><td><a href="app/src/main/assets/themes/Gruvbox%20Material.json">JSON</a></td></tr>
<tr><td>Kanagawa Dragon</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#181616;border:1px solid #888;border-radius:50%"></span> <code>#181616</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#12120F;border:1px solid #888;border-radius:50%"></span> <code>#12120F</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#8BA4B0;border:1px solid #888;border-radius:50%"></span> <code>#8BA4B0</code></td><td><a href="app/src/main/assets/themes/Kanagawa%20Dragon.json">JSON</a></td></tr>
<tr><td>Kanagawa Wave</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#1F1F28;border:1px solid #888;border-radius:50%"></span> <code>#1F1F28</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#16161D;border:1px solid #888;border-radius:50%"></span> <code>#16161D</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#98BB6C;border:1px solid #888;border-radius:50%"></span> <code>#98BB6C</code></td><td><a href="app/src/main/assets/themes/Kanagawa%20Wave.json">JSON</a></td></tr>
<tr><td>Monokai Pro</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#2D2A2E;border:1px solid #888;border-radius:50%"></span> <code>#2D2A2E</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#221F22;border:1px solid #888;border-radius:50%"></span> <code>#221F22</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#A9DC76;border:1px solid #888;border-radius:50%"></span> <code>#A9DC76</code></td><td><a href="app/src/main/assets/themes/Monokai%20Pro.json">JSON</a></td></tr>
<tr><td>Nord</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#2E3440;border:1px solid #888;border-radius:50%"></span> <code>#2E3440</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#242933;border:1px solid #888;border-radius:50%"></span> <code>#242933</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#88C0D0;border:1px solid #888;border-radius:50%"></span> <code>#88C0D0</code></td><td><a href="app/src/main/assets/themes/Nord.json">JSON</a></td></tr>
<tr><td>Nord Light</td><td>Light</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#ECEFF4;border:1px solid #888;border-radius:50%"></span> <code>#ECEFF4</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#E5E9F0;border:1px solid #888;border-radius:50%"></span> <code>#E5E9F0</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#5E81AC;border:1px solid #888;border-radius:50%"></span> <code>#5E81AC</code></td><td><a href="app/src/main/assets/themes/Nord%20Light.json">JSON</a></td></tr>
<tr><td>One Dark</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#282C34;border:1px solid #888;border-radius:50%"></span> <code>#282C34</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#21252B;border:1px solid #888;border-radius:50%"></span> <code>#21252B</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#61AFEF;border:1px solid #888;border-radius:50%"></span> <code>#61AFEF</code></td><td><a href="app/src/main/assets/themes/One%20Dark.json">JSON</a></td></tr>
<tr><td>Ristretto</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#2C2525;border:1px solid #888;border-radius:50%"></span> <code>#2C2525</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#322A2A;border:1px solid #888;border-radius:50%"></span> <code>#322A2A</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#F38D70;border:1px solid #888;border-radius:50%"></span> <code>#F38D70</code></td><td><a href="app/src/main/assets/themes/Ristretto.json">JSON</a></td></tr>
<tr><td>Rose Pine</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#191724;border:1px solid #888;border-radius:50%"></span> <code>#191724</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#16141F;border:1px solid #888;border-radius:50%"></span> <code>#16141F</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#EBBCBA;border:1px solid #888;border-radius:50%"></span> <code>#EBBCBA</code></td><td><a href="app/src/main/assets/themes/Rose%20Pine.json">JSON</a></td></tr>
<tr><td>Rose Pine Dawn</td><td>Light</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#FAF4ED;border:1px solid #888;border-radius:50%"></span> <code>#FAF4ED</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#FFFAF3;border:1px solid #888;border-radius:50%"></span> <code>#FFFAF3</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#D7827E;border:1px solid #888;border-radius:50%"></span> <code>#D7827E</code></td><td><a href="app/src/main/assets/themes/Rose%20Pine%20Dawn.json">JSON</a></td></tr>
<tr><td>Rose Pine Moon</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#232136;border:1px solid #888;border-radius:50%"></span> <code>#232136</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#1F1D30;border:1px solid #888;border-radius:50%"></span> <code>#1F1D30</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#EA9A97;border:1px solid #888;border-radius:50%"></span> <code>#EA9A97</code></td><td><a href="app/src/main/assets/themes/Rose%20Pine%20Moon.json">JSON</a></td></tr>
<tr><td>Solarized Dark</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#002B36;border:1px solid #888;border-radius:50%"></span> <code>#002B36</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#00212B;border:1px solid #888;border-radius:50%"></span> <code>#00212B</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#2AA198;border:1px solid #888;border-radius:50%"></span> <code>#2AA198</code></td><td><a href="app/src/main/assets/themes/Solarized%20Dark.json">JSON</a></td></tr>
<tr><td>Solarized Light</td><td>Light</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#FDF6E3;border:1px solid #888;border-radius:50%"></span> <code>#FDF6E3</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#EEE8D5;border:1px solid #888;border-radius:50%"></span> <code>#EEE8D5</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#2AA198;border:1px solid #888;border-radius:50%"></span> <code>#2AA198</code></td><td><a href="app/src/main/assets/themes/Solarized%20Light.json">JSON</a></td></tr>
<tr><td>Spotify Dark</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#121212;border:1px solid #888;border-radius:50%"></span> <code>#121212</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#1F1F1F;border:1px solid #888;border-radius:50%"></span> <code>#1F1F1F</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#1ED760;border:1px solid #888;border-radius:50%"></span> <code>#1ED760</code></td><td><a href="app/src/main/assets/themes/spotify-dark.json">JSON</a></td></tr>
<tr><td>Synthwave 84</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#262335;border:1px solid #888;border-radius:50%"></span> <code>#262335</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#1A1625;border:1px solid #888;border-radius:50%"></span> <code>#1A1625</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#36F9F6;border:1px solid #888;border-radius:50%"></span> <code>#36F9F6</code></td><td><a href="app/src/main/assets/themes/Synthwave%2084.json">JSON</a></td></tr>
<tr><td>Tokyo Night</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#1A1B26;border:1px solid #888;border-radius:50%"></span> <code>#1A1B26</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#16161E;border:1px solid #888;border-radius:50%"></span> <code>#16161E</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#7AA2F7;border:1px solid #888;border-radius:50%"></span> <code>#7AA2F7</code></td><td><a href="app/src/main/assets/themes/Tokyo%20Night.json">JSON</a></td></tr>
<tr><td>Tokyo Night Storm</td><td>Dark</td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#24283B;border:1px solid #888;border-radius:50%"></span> <code>#24283B</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#1F2335;border:1px solid #888;border-radius:50%"></span> <code>#1F2335</code></td><td><span style="display:inline-block;width:0.85em;height:0.85em;background:#BB9AF7;border:1px solid #888;border-radius:50%"></span> <code>#BB9AF7</code></td><td><a href="app/src/main/assets/themes/Tokyo%20Night%20Storm.json">JSON</a></td></tr>
  </tbody>
</table>

<details>
  <summary><strong>23 more palette previews</strong></summary>
  <br/>
  <table align="center">
    <tr><td align="center" valign="top"><img src="docs/screenshots/ayu-dark-home.png" alt="Ayu Dark theme on Spotify Home" width="280"/><br/><strong>Ayu Dark</strong></td><td align="center" valign="top"><img src="docs/screenshots/ayu-mirage-home.png" alt="Ayu Mirage theme on Spotify Home" width="280"/><br/><strong>Ayu Mirage</strong></td></tr>
    <tr><td align="center" valign="top"><img src="docs/screenshots/catppuccin-frappe-home.png" alt="Catppuccin Frappe theme on Spotify Home" width="280"/><br/><strong>Catppuccin Frappe</strong></td><td align="center" valign="top"><img src="docs/screenshots/catppuccin-macchiato-home.png" alt="Catppuccin Macchiato theme on Spotify Home" width="280"/><br/><strong>Catppuccin Macchiato</strong></td></tr>
    <tr><td align="center" valign="top"><img src="docs/screenshots/catppuccin-home.png" alt="Catppuccin theme on Spotify Home" width="280"/><br/><strong>Catppuccin</strong></td><td align="center" valign="top"><img src="docs/screenshots/everforest-dark-home.png" alt="Everforest Dark theme on Spotify Home" width="280"/><br/><strong>Everforest Dark</strong></td></tr>
    <tr><td align="center" valign="top"><img src="docs/screenshots/everforest-light-home.png" alt="Everforest Light theme on Spotify Home" width="280"/><br/><strong>Everforest Light</strong></td><td align="center" valign="top"><img src="docs/screenshots/gruvbox-light-home.png" alt="Gruvbox Light theme on Spotify Home" width="280"/><br/><strong>Gruvbox Light</strong></td></tr>
    <tr><td align="center" valign="top"><img src="docs/screenshots/gruvbox-material-home.png" alt="Gruvbox Material theme on Spotify Home" width="280"/><br/><strong>Gruvbox Material</strong></td><td align="center" valign="top"><img src="docs/screenshots/kanagawa-dragon-home.png" alt="Kanagawa Dragon theme on Spotify Home" width="280"/><br/><strong>Kanagawa Dragon</strong></td></tr>
    <tr><td align="center" valign="top"><img src="docs/screenshots/kanagawa-wave-home.png" alt="Kanagawa Wave theme on Spotify Home" width="280"/><br/><strong>Kanagawa Wave</strong></td><td align="center" valign="top"><img src="docs/screenshots/monokai-pro-home.png" alt="Monokai Pro theme on Spotify Home" width="280"/><br/><strong>Monokai Pro</strong></td></tr>
    <tr><td align="center" valign="top"><img src="docs/screenshots/nord-light-home.png" alt="Nord Light theme on Spotify Home" width="280"/><br/><strong>Nord Light</strong></td><td align="center" valign="top"><img src="docs/screenshots/one-dark-home.png" alt="One Dark theme on Spotify Home" width="280"/><br/><strong>One Dark</strong></td></tr>
    <tr><td align="center" valign="top"><img src="docs/screenshots/ristretto-home.png" alt="Ristretto theme on Spotify Home" width="280"/><br/><strong>Ristretto</strong></td><td align="center" valign="top"><img src="docs/screenshots/rose-pine-dawn-home.png" alt="Rose Pine Dawn theme on Spotify Home" width="280"/><br/><strong>Rose Pine Dawn</strong></td></tr>
    <tr><td align="center" valign="top"><img src="docs/screenshots/rose-pine-moon-home.png" alt="Rose Pine Moon theme on Spotify Home" width="280"/><br/><strong>Rose Pine Moon</strong></td><td align="center" valign="top"><img src="docs/screenshots/rose-pine-home.png" alt="Rose Pine theme on Spotify Home" width="280"/><br/><strong>Rose Pine</strong></td></tr>
    <tr><td align="center" valign="top"><img src="docs/screenshots/solarized-dark-home.png" alt="Solarized Dark theme on Spotify Home" width="280"/><br/><strong>Solarized Dark</strong></td><td align="center" valign="top"><img src="docs/screenshots/solarized-light-home.png" alt="Solarized Light theme on Spotify Home" width="280"/><br/><strong>Solarized Light</strong></td></tr>
    <tr><td align="center" valign="top"><img src="docs/screenshots/spotify-dark-home.png" alt="Spotify Dark theme on Spotify Home" width="280"/><br/><strong>Spotify Dark</strong></td><td align="center" valign="top"><img src="docs/screenshots/synthwave-84-home.png" alt="Synthwave 84 theme on Spotify Home" width="280"/><br/><strong>Synthwave 84</strong></td></tr>
    <tr><td align="center" valign="top"><img src="docs/screenshots/tokyo-night-storm-home.png" alt="Tokyo Night Storm theme on Spotify Home" width="280"/><br/><strong>Tokyo Night Storm</strong></td><td></td></tr>
  </table>
</details>

## Architecture

<pre><code>Spotify 9.1.86.2432
  └─ Vector/Zygisk (root) or LSPatch (rootless)
       └─ XposedLoader
            └─ Profile_9_1_86_2432
                 └─ exact methods and fields
                      └─ semantic palette hooks
                           ├─ Encore / Android Views
                           └─ Jetpack Compose
                                └─ selected JSON palette or static Auto Theme palette</code></pre>

There is no runtime DexKit dependency or candidate-member scan. The profile names the expected methods and fields for this exact Spotify build; a new Spotify release requires re-verifying those identities instead of falling back to discovery. See <a href="docs/REFLECTION_PROFILE.md">the reflection profile</a> for the mapping and its validation boundaries.

## Compatibility

| Requirement   | Supported / tested value                                                                                                                                                     |
| ------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Spotify       | <strong>9.1.86.2432</strong>, version code <strong>146555520</strong> only                                                                                                   |
| Android       | Module minimum API 27; Android 10+ is a practical baseline. On-device validation was performed on <strong>Android 16 / API 36</strong>                                       |
| Rooted mode   | Magisk with Zygisk and an operational Vector installation                                                                                                                    |
| Rootless mode | LSPatch, prepared with the CLI installer; Android 16/API 36 |
| Theme files   | Version-1 JSON, UTF-8, at most 64 KiB per file; up to 128 files in the selected folder                                                                                       |
| Build         | JDK 21, Android SDK/`apksigner`, ADB, Gradle wrapper; Python 3 is required for rootless preparation                                                                          |

## Pin Spotify to the supported build

> [!TIP]
> On a rooted device, <a href="https://github.com/j-hc/zygisk-detach">zygisk-detach</a> can hide Spotify from Play Store update checks. Install and configure that module separately, reboot if its installation requests it, then run the CLI and select <code>com.spotify.music</code>:
>
> <pre><code>su -c /data/adb/modules/zygisk-detach/detach</code></pre>
>
> This controls Play Store visibility; it does not prevent a manually installed APK from replacing Spotify.

Rootless installs use LSPatch to embed SpotiTheme into Spotify. The installer preserves Spotify's split APKs and adds access to SpotiTheme's settings provider.

<details>
<summary><strong>Get the supported Spotify version and check your download</strong></summary>

SpotiTheme targets <strong>com.spotify.music · 9.1.86.2432 · version code 146555520</strong>. Start with the original, unpatched Spotify app. These are third-party download locations, separate from SpotiTheme's releases:

- [Uptodown](https://spotify.br.uptodown.com/android/download/1220860751)
- APKMirror: [variant 1](https://www.apkmirror.com/apk/spotify-ab/spotify-music-podcasts/spotify-music-and-podcasts-9-1-86-2432-release/spotify-music-and-podcasts-9-1-86-2432-android-apk-download/) · [variant 2](https://www.apkmirror.com/apk/spotify-ab/spotify-music-podcasts/spotify-music-and-podcasts-9-1-86-2432-release/spotify-music-and-podcasts-9-1-86-2432-2-android-apk-download/)
- [Google Drive mirror](https://drive.google.com/file/d/1I7SIPsDTGCzqCmSUK_weS_gGiD5T0mgv/view?usp=sharing)

Verified file hashes for the downloaded <strong>Uptodown, APKMirror and Drive APKs</strong>, which are byte-identical:

<pre><code>SHA-256: 17b8672c665eeda6cf5b348a426678f0c49eac15c0d6c573cc8daff515c1bee8
SHA-1:   56ea2286436f7046d1e68505c7eaecda022b6978</code></pre>

Expected <strong>Spotify signing-certificate SHA-256</strong>, matching the saved acceptance build:

<pre><code>6505b181933344f93893d586e399b94616183f04349cb572a9e81a3335e28ffd</code></pre>

These file hashes apply to the verified APK, not to every variant or APKM/XAPK archive. Split packages can have different hashes and require all their parts; a <code>base.apk</code> alone isn't enough. A matching version and signature do not guarantee that every variant works with SpotiTheme.

After installing Spotify, check its version in <strong>Settings → Apps → Spotify</strong> before setting up SpotiTheme.

</details>

## Installation

Choose the setup that matches your device. Spotify **9.1.86.2432 / 146555520** must already be installed. See <a href="#pin-spotify-to-the-supported-build">how to get the supported Spotify version</a> if needed.

### Rooted phone (no computer)

1. Download the <strong>SpotiTheme APK</strong> from <a href="https://github.com/Nerver-zip/SpotiTheme/releases/latest">GitHub Releases</a> and install it using your phone's package installer.
2. Open Vector, enable SpotiTheme, and select Spotify in its scope. See <a href="#manual-installation-through-the-phone-ui">the phone UI guide</a> for details.
3. Force-stop Spotify in Android Settings, reopen it, then open SpotiTheme to choose a theme.

### Install from a computer

1. Download and extract <strong>SpotiTheme-installer-vMAJOR.MINOR.PATCH.tar.gz</strong> from <a href="https://github.com/Nerver-zip/SpotiTheme/releases/latest">GitHub Releases</a>.
2. Install the host tools: Bash, ADB and Python 3. For rootless setup, also install JDK 21 and Android SDK build-tools 35.0.0 (<code>apksigner</code>).
3. On your phone, enable USB debugging, connect it to the computer and approve the debugging prompt. Keep the supported Spotify version installed.
4. Open a terminal in the extracted <code>SpotiTheme-installer</code> folder. Run the check and install commands for your setup below.

**Rooted with Vector** — installs the included SpotiTheme APK and configures its Vector scope:

```sh
bash install.sh --root --check
bash install.sh --root
```

**Rootless with LSPatch** — prepares Spotify with SpotiTheme embedded and installs the theme manager:

```sh
bash install.sh --rootless --check
bash install.sh --rootless
```

The archive includes SpotiTheme and the patching tools; it does not include Spotify. The rootless installer saves the original Spotify APK files in the extracted folder for recovery. Replacing store-signed Spotify clears its local data, so you will need to sign in again. See the <a href="installer/README.md">installer guide</a> for recovery details.

### Manual installation through the phone UI (Root require required)

These steps use Android screens and Vector, with no terminal or ADB commands. Download the <strong>SpotiTheme APK</strong> from <a href="https://github.com/Nerver-zip/SpotiTheme/releases/latest">GitHub Releases</a> to your phone. Building from source is a separate developer task; the source repository itself is not an installable APK. Spotify must already be the supported <strong>9.1.86.2432 / 146555520</strong> build. Check its version in Android <strong>Settings → Apps → Spotify</strong>; stop if it differs.

#### Rooted device with Vector

1. Ensure Magisk has Zygisk enabled and Vector is operational. If Vector is not installed, download its module from the <a href="https://github.com/JingMatrix/Vector/releases">official releases</a>, open <strong>Magisk → Modules → Install from storage</strong>, select the module ZIP and reboot. Follow <a href="https://github.com/JingMatrix/Vector#installation">Vector's installation instructions</a> for the Zygisk environment required by your setup.
2. Open the SpotiTheme APK in your file manager and tap <strong>Install</strong>. If Android asks, allow that file manager to install unknown apps. SpotiTheme is an APK, not a Magisk module ZIP.
3. Open <strong>Vector</strong> from its manager shortcut or system notification. Go to <strong>Modules → SpotiTheme</strong> and enable the module.
4. In SpotiTheme's scope, select <strong>Spotify (com.spotify.music)</strong> for the Android user where it is installed. This project's validated setup uses the primary user (user 0). Do not select unrelated apps or system processes.
5. If another theming module targets Spotify, open that module's scope and uncheck only Spotify to avoid competing hooks.
6. Open Android <strong>Settings → Apps → Spotify → Force stop</strong>, then launch Spotify again. Removing it from Recents alone does not reliably reload hooks. Do not clear its storage or uninstall it.
7. Open <strong>SpotiTheme</strong> from the launcher and configure the theme as described below.

The Vector CLI and USB debugging are not required for this route. Installing an updated SpotiTheme APK with the same signing key follows the same Android installation flow; force-stop and reopen Spotify afterward.

### Choose a theme

Open <strong>SpotiTheme</strong>, turn on <strong>Enable theme</strong> and select a palette. Leave <strong>Use album artwork colors</strong> off to keep your chosen colors; turn it on to use Spotify's artwork colors or enable <strong>Auto Theme</strong> to generate a palette from the artwork. The player background adjusts automatically to album covers and featured videos.

To add your own themes, tap <strong>Choose theme folder</strong>, select a folder containing JSON palettes and allow read access. Tap <strong>Refresh themes</strong> after adding or editing files.

After installing or updating SpotiTheme, force-stop Spotify in <strong>Settings → Apps → Spotify</strong> and reopen it. On rooted devices, SpotiTheme must also be enabled in Vector with Spotify selected in its scope.

### JSON theme format

Choose a folder in the manager, grant read access, and refresh after adding or changing theme files. Each file requires <code>schemaVersion</code>, a lowercase-hyphen <code>id</code>, a dark/light <code>base</code>, and a <code>colors</code> object. The display <code>name</code> is optional. Colors accept <code>#RRGGBB</code> or <code>#RRGGBBAA</code>; omitted color roles use defaults from the selected base palette. Each file is limited to 64 KiB.

<pre><code>{
  "schemaVersion": 1,
  "id": "my-mocha",
  "name": "My Mocha",
  "base": "dark",
  "colors": {
    "background": "#1E1E2E",
    "surface": "#181825",
    "text": "#CDD6F4",
    "accent": "#A6E3A1",
    "savedIndicator": "#A6E3A1"
  }
}</code></pre>

The saved-indicator color can differ from the accent. It controls the mini-player saved check and related save affordances where Spotify exposes a supported path.

## Validation boundaries

SpotiTheme supports the pinned Spotify build listed above. Some Spotify elements retain their original appearance; see <a href="docs/VALIDATION.md">validation notes</a> for details.

## Acknowledgements

- <a href="https://github.com/LeNerd46/SpotifyPlus">LeNerd46/SpotifyPlus</a> — original hook foundations, retained under its MIT license; see <a href="LICENSE">LICENSE</a>.
- <a href="https://github.com/JingMatrix/Vector">JingMatrix/Vector</a> — modern Zygisk/Xposed framework.
- <a href="https://github.com/LuckyPray/DexKit">LuckyPray/DexKit</a> — bytecode-analysis tooling used during research; DexKit is not included as a runtime dependency.
