<h1 align="center">SpotiTheme</h1>

<p align="center"><strong>29 JSON themes for Spotify on Android, applied through a pinned Xposed reflection profile.</strong></p>

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

SpotiTheme applies semantic color palettes to supported Spotify Android surfaces. It packages 29 JSON palettes, can read additional <code>.json</code> files from a user-selected folder, and can optionally derive a static color palette from the current artwork with <strong>Auto Theme</strong>.

- <strong>Pinned reflection profile:</strong> resolves exact members for Spotify 9.1.86.2432 instead of searching Spotify bytecode at runtime.
- <strong>Broad surface coverage:</strong> Encore and Compose palettes, Home, Library, Search, playlist creation, mini-player, expanded player, selected media cards, SongDNA, Credits, and Spotify's in-app Connect indicator.
- <strong>Theme library:</strong> 29 bundled themes, with regular Catppuccin Mocha as the default and only bundled Mocha variant.
- <strong>User themes:</strong> choose a folder with Android's document picker; refresh to import valid version-1 JSON palettes.
- <strong>Two installation paths:</strong> Vector/Zygisk for rooted devices or a prepared LSPatch install for rootless devices.

Animated artwork backgrounds and their dependent motion, drift, blur, and quality controls are excluded from this MVP. The expanded player handles its backdrop automatically: confirmed square album artwork uses a clear theme background, while a confirmed featured-video surface keeps Spotify's native treatment. Unrecognized media paths keep native behavior. Static Auto Theme color extraction and Use album artwork colors remain separate color controls.

The former built-in Mocha Mauve entry is not included. A saved selection using its retired identifier falls back to regular Catppuccin Mocha.

> [!NOTE]
> The screenshots are examples, not a claim that every Spotify renderer is covered. Spotify Home content is live; its items and network status can change between captures.

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

Startup timing is informational rather than a release threshold. The latest clean start of the final rootless build registered the module in <strong>241.164 ms</strong> cumulative main-thread time on the pinned Android 16/API 36 device. SpotiTheme makes no sub-50 ms claim.

## Compatibility

| Requirement   | Supported / tested value                                                                                                                                                     |
| ------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Spotify       | <strong>9.1.86.2432</strong>, version code <strong>146555520</strong> only                                                                                                   |
| Android       | Module minimum API 27; Android 10+ is a practical baseline. On-device validation was performed on <strong>Android 16 / API 36</strong>                                       |
| Rooted mode   | Magisk with Zygisk and an operational Vector installation                                                                                                                    |
| Rootless mode | LSPatch injection and manager-selected palette switching verified on Android 16/API 36 with Spotify outside Vector scope; a separate truly-unrooted device test remains open |
| Theme files   | Version-1 JSON, UTF-8, at most 64 KiB per file; up to 128 files in the selected folder                                                                                       |
| Build         | JDK 21, Android SDK/`apksigner`, ADB, Gradle wrapper; Python 3 is required for rootless preparation                                                                          |

## Pin Spotify to the supported build

> [!TIP]
> On a rooted device, <a href="https://github.com/j-hc/zygisk-detach">zygisk-detach</a> can hide Spotify from Play Store update checks. Install and configure that module separately, reboot if its installation requests it, then run the CLI and select <code>com.spotify.music</code>:
>
> <pre><code>su -c /data/adb/modules/zygisk-detach/detach</code></pre>
>
> This controls Play Store visibility; it does not prevent a manually installed APK from replacing Spotify.

Rootless installs use an LSPatch-signed APK set. Preparation adds only a narrow package-visibility query for SpotiTheme's settings provider; it does not use `QUERY_ALL_PACKAGES`. On the test handset, Spotify was removed from Vector scope for rootless validation. The handset still has Magisk, so acceptance on a separate truly-unrooted device remains open.

## Installation

Clone or open this repository, then use the interactive installer from its root:

<pre><code>./installer/install.sh --check
./installer/install.sh --root
./installer/install.sh --rootless</code></pre>

- <code>--check</code> verifies the connected device, pinned Spotify identity, and available framework prerequisites without installing or changing scope.
- <code>--root</code> builds and tests the module, installs it, enables <code>com.spotitheme</code> in Vector, and adds Spotify to the module scope after confirmation. Restart Spotify to load a newly installed module.
- <code>--rootless</code> pulls Spotify split APKs, prepares a local LSPatch build, verifies the result, then prompts before replacing the store-signed app.

The installer refuses other Spotify versions rather than upgrading or downgrading the app. It does not start playback. See <a href="installer/README.md">the installation guide</a> for prerequisites, prompts, signing details, and recovery behavior.

### Manual setup (without the installer)

The interactive installer is optional. The steps below perform the same setup manually. Use Bash for the rootless commands. If more than one device is connected, add <code>-s &lt;serial&gt;</code> after each <code>adb</code> command.

First confirm the connected device and pinned Spotify build, then build the manager/module APK:

<pre><code>adb devices -l
adb shell dumpsys package com.spotify.music | grep -E 'versionName=|versionCode='
./gradlew testDebugUnitTest assembleDebug</code></pre>

The package details must report Spotify <strong>9.1.86.2432 / 146555520</strong>. Stop if the installed build differs; this module does not discover profiles for other Spotify versions. The APK used below is <code>app/build/outputs/apk/debug/app-debug.apk</code>.

#### Rooted device with Vector

Requirements: Magisk with Zygisk enabled, Vector installed and operational, and its CLI at <code>/data/adb/lspd/cli</code>. Install the APK, enable the module, and add only Spotify user 0 to its scope:

<pre><code>adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell su -c '/data/adb/lspd/cli status'
adb shell su -c '/data/adb/lspd/cli modules enable com.spotitheme'
adb shell su -c '/data/adb/lspd/cli scope add com.spotitheme com.spotify.music/0'
adb shell su -c '/data/adb/lspd/cli modules --json ls'
adb shell su -c '/data/adb/lspd/cli scope --json ls com.spotitheme'</code></pre>

Confirm that Vector reports <code>com.spotitheme</code> enabled and <code>com.spotify.music/0</code> in its scope. You can perform the same actions in Vector’s UI under its module and scope controls. If another theming module also targets Spotify, remove only that module’s Spotify scope in Vector before acceptance; do not disable or remove the other module globally. Restart Spotify after changing scope so the running process loads the configured hooks.

#### Rootless device with LSPatch

> [!WARNING]
> Replacing the store-signed Spotify app with its first LSPatch build requires uninstalling Spotify. Android deletes its local app data, and you will need to sign in again. Preserve the original APK splits before continuing. Later updates can use `adb install-multiple -r` when both patched builds use the same LSPatch signing key. The test handset has Magisk; a separate truly-unrooted device test remains open.

Build the manager APK as above. These manual commands use the installer's preparation helpers to preserve every split, add only SpotiTheme's provider-authority visibility query, stage and sign the changed set, run the pinned LSPatch build, and verify all patched signatures for the connected Android API. For a later rebuild, set <code>SPOTIFY_ORIGINAL_DIR</code> to the saved store-signed splits from the first preparation; do not patch the already-patched Spotify splits again. Continue in the same Bash session:

<pre><code>set -euo pipefail
REPO="$PWD"
INSTALLER="$REPO/installer"
for library in ui adb spotify vector lspatch; do source "$INSTALLER/lib/$library.sh"; done
select_device
verify_spotify
verify_lspatch
MODULE_APK="$REPO/app/build/outputs/apk/debug/app-debug.apk"
mkdir -p "$REPO/.project"
WORK="$(mktemp -d "$REPO/.project/manual-lspatch.XXXXXX")"
if [[ -n "${SPOTIFY_ORIGINAL_DIR:-}" ]]; then
  mkdir -p "$WORK/original"
  cp "$SPOTIFY_ORIGINAL_DIR"/*.apk "$WORK/original/"
  INPUT_APKS=("$WORK/original/base.apk")
  for apk in "$WORK"/original/*.apk; do
    [[ "$apk" == "$WORK/original/base.apk" ]] || INPUT_APKS+=("$apk")
  done
else
  extract_spotify
fi
mkdir -p "$WORK/patched"
patch_split_set
mapfile -t PATCHED_APKS < <(find "$WORK/patched" -maxdepth 1 -type f -name '*.apk' | sort)
[[ ${#PATCHED_APKS[@]} -eq ${#INPUT_APKS[@]} ]]
validate_patched "${PATCHED_APKS[@]}"
adb_device install -r "$MODULE_APK"</code></pre>

For an existing LSPatch install made with the same signing key, update Spotify in place and keep its data:

<pre><code>adb -s "$SERIAL" install-multiple -r "${PATCHED_APKS[@]}"</code></pre>

For the first install replacing the store-signed app, review the warning above, then uninstall before installing the patched set. This deletes local Spotify data:

<pre><code>adb -s "$SERIAL" uninstall --user 0 com.spotify.music
adb -s "$SERIAL" install-multiple "${PATCHED_APKS[@]}"</code></pre>

The original split APKs remain under <code>$WORK/original</code> for recovery. If installation fails, restore them with <code>adb -s "$SERIAL" install-multiple "$WORK"/original/*.apk</code>. Continue to use the same LSPatch signing key for later updates; see <a href="installer/README.md">the installation guide</a> before using a custom keystore.

#### Configure the theme and verify startup

Open <strong>SpotiTheme</strong> from the launcher (or run <code>adb shell monkey -p com.spotitheme 1</code>). Choose a palette in the theme list and leave <strong>Enable theme</strong> on. For a fixed palette, leave <strong>Use album artwork colors</strong> off; Auto Theme is then cleared and disabled. Player backdrop handling is automatic and has no switch. To import JSON themes, choose the folder containing them and refresh the list. Restart Spotify after installing a changed hook build:

<pre><code>adb shell am force-stop com.spotify.music
adb shell monkey -p com.spotify.music 1</code></pre>

For a startup diagnostic, clear the log before launching Spotify and inspect the module tag afterward:

<pre><code>adb logcat -c
adb shell am force-stop com.spotify.music
adb shell monkey -p com.spotify.music 1
adb logcat -d -v threadtime -s SpotiTheme</code></pre>

Look for <code>Module initialized in Spotify</code> and <code>Base palette hook executed</code>, then verify the palette on Spotify surfaces visually. Logs confirm initialization, not complete surface coverage. See <a href="docs/MVP_VALIDATION.md">MVP validation</a> for known acceptance limits.

### Build and validate

<pre><code>./gradlew testDebugUnitTest assembleDebug
python3 -m unittest discover -s installer/tests -v
bash -n installer/install.sh installer/lib/*.sh
./installer/install.sh --check</code></pre>

LSPatch injection and manager-selected Latte/Mocha palettes were confirmed on Android 16/API 36 with Spotify removed from Vector scope. Rootless preparation adds a narrow provider query, with no `QUERY_ALL_PACKAGES` permission. The handset itself still has Magisk, so this does not count as acceptance on a separate truly-unrooted device.

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

This is a pinned-build MVP, not a guarantee that every Spotify-owned color or renderer is themeable.

- Auto Theme is normalized to off whenever fixed-palette mode is active. Its switch is unchecked and disabled in that mode. Automatic player backdrop handling does not change either color-mode preference.
- The verified square-cover player path preserves foreground artwork and uses the theme background. The pinned main video surface retains Spotify's native backdrop; full-screen image/Canvas paths that lack a confirmed binding keep native behavior.
- Some native or alternate Spotify renderers may retain their original foreground colors.
- Related-video overflow, selected Repeat, and a positive queued-badge state had no available sample for SpotiTheme acceptance.
- Rootless LSPatch preparation adds the narrow SpotiTheme provider query and supports palette switching without `QUERY_ALL_PACKAGES`; Spotify was scoped out of Vector during the Android 16/API 36 test. A separate truly-unrooted device test remains open.
- Spotify Connect was visible as Spotifast in the app; Android's system route owner remained unverified.

See <a href="docs/MVP_VALIDATION.md">MVP validation</a>, <a href="docs/STARTUP_MEASUREMENT.md">startup measurement</a>, and <a href="docs/REFLECTION_PROFILE.md">reflection profile</a> for evidence and limitations.

## Acknowledgements

- <a href="https://github.com/LeNerd46/SpotifyPlus">LeNerd46/SpotifyPlus</a> — original hook foundations, retained under its MIT license; see <a href="LICENSE">LICENSE</a>.
- <a href="https://github.com/JingMatrix/Vector">JingMatrix/Vector</a> — modern Zygisk/Xposed framework.
- <a href="https://github.com/LuckyPray/DexKit">LuckyPray/DexKit</a> — bytecode-analysis tooling used during research; DexKit is not included as a runtime dependency.
