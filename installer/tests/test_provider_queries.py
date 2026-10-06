"""Exercise narrow provider-query insertion into Android binary XML and APKs."""
from __future__ import annotations

import struct
import sys
import tempfile
import unittest
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "lib"))
import patch_provider_queries as patcher


STRINGS = [
    "manifest",
    "queries",
    "provider",
    "authorities",
    "application",
    "android",
    patcher.ANDROID_NS,
    "com.example.other.provider",
]


def make_string_pool(strings):
    encoded = [patcher._encode_string(value, True) for value in strings]
    offsets = []
    string_data = b""
    for value in encoded:
        offsets.append(len(string_data))
        string_data += value
    header_size = 28
    strings_start = header_size + 4 * len(strings)
    padding = -(strings_start + len(string_data)) % 4
    string_data += b"\0" * padding
    chunk_size = strings_start + len(string_data)
    header = struct.pack(
        "<HHIIIIII",
        patcher.RES_STRING_POOL_TYPE,
        header_size,
        chunk_size,
        len(strings),
        0,
        patcher.UTF8_FLAG,
        strings_start,
        0,
    )
    return header + b"".join(struct.pack("<I", offset) for offset in offsets) + string_data


def make_manifest(include_queries=True):
    indexes = {value: i for i, value in enumerate(STRINGS)}
    pool = make_string_pool(STRINGS)
    resource_ids = [0] * len(STRINGS)
    resource_ids[indexes["authorities"]] = 0x01010018
    resource_map = struct.pack(
        "<HHI", patcher.RES_XML_RESOURCE_MAP_TYPE, 8, 8 + len(resource_ids) * 4
    ) + b"".join(struct.pack("<I", value) for value in resource_ids)
    namespace = struct.pack(
        "<HHIIIII",
        0x0100,
        16,
        24,
        1,
        patcher.NO_INDEX,
        indexes["android"],
        indexes[patcher.ANDROID_NS],
    )
    chunks = [pool, resource_map, namespace]
    chunks.append(patcher._start_element(indexes["manifest"]))
    if include_queries:
        chunks.append(patcher._start_element(indexes["queries"]))
        chunks.append(
            patcher._start_element(
                indexes["provider"],
                (indexes[patcher.ANDROID_NS], indexes["authorities"], indexes["com.example.other.provider"]),
            )
        )
        chunks.append(patcher._end_element(indexes["provider"]))
        chunks.append(patcher._end_element(indexes["queries"]))
    chunks.append(patcher._start_element(indexes["application"]))
    chunks.append(patcher._end_element(indexes["application"]))
    chunks.append(patcher._end_element(indexes["manifest"]))
    namespace_end = struct.pack(
        "<HHIIIII",
        0x0101,
        16,
        24,
        1,
        patcher.NO_INDEX,
        indexes["android"],
        indexes[patcher.ANDROID_NS],
    )
    chunks.append(namespace_end)
    body = b"".join(chunks)
    return struct.pack("<HHI", patcher.RES_XML_TYPE, 8, 8 + len(body)) + body


def queried_authorities(xml):
    _, chunks = patcher._split_xml(xml)
    pool = next(chunk for chunk in chunks if patcher._chunk_type(chunk) == patcher.RES_STRING_POOL_TYPE)
    strings = [patcher._decode_string(pool, i) for i in range(patcher._u32(pool, 8))]
    stack = []
    result = []
    for chunk in chunks:
        kind = patcher._chunk_type(chunk)
        if kind == patcher.RES_XML_START_ELEMENT_TYPE:
            name = strings[patcher._name_index(chunk)]
            if stack and stack[-1] == "queries" and name == "provider":
                result.extend(
                    value
                    for namespace, attr_name, value in patcher._element_attributes(chunk, pool)
                    if namespace == patcher.ANDROID_NS and attr_name == "authorities"
                )
            stack.append(name)
        elif kind == patcher.RES_XML_END_ELEMENT_TYPE:
            stack.pop()
    return result


class ProviderQueryTest(unittest.TestCase):
    def test_adds_authority_to_existing_queries_and_is_idempotent(self):
        original = make_manifest(include_queries=True)
        patched, changed = patcher.add_provider_query(original)
        self.assertTrue(changed)
        self.assertEqual(
            queried_authorities(patched),
            ["com.example.other.provider", patcher.SPOTITHEME_SETTINGS_AUTHORITY],
        )
        repeated, changed_again = patcher.add_provider_query(patched)
        self.assertFalse(changed_again)
        self.assertEqual(repeated, patched)

    def test_creates_queries_before_application_when_missing(self):
        patched, changed = patcher.add_provider_query(make_manifest(include_queries=False))
        self.assertTrue(changed)
        self.assertEqual(queried_authorities(patched), [patcher.SPOTITHEME_SETTINGS_AUTHORITY])
        _, chunks = patcher._split_xml(patched)
        pool = next(chunk for chunk in chunks if patcher._chunk_type(chunk) == patcher.RES_STRING_POOL_TYPE)
        strings = [patcher._decode_string(pool, i) for i in range(patcher._u32(pool, 8))]
        top_level = []
        stack = []
        for chunk in chunks:
            kind = patcher._chunk_type(chunk)
            if kind == patcher.RES_XML_START_ELEMENT_TYPE:
                name = strings[patcher._name_index(chunk)]
                if stack == ["manifest"]:
                    top_level.append(name)
                stack.append(name)
            elif kind == patcher.RES_XML_END_ELEMENT_TYPE:
                stack.pop()
        self.assertEqual(top_level, ["queries", "application"])

    def test_patches_only_manifest_entry_in_apk(self):
        with tempfile.TemporaryDirectory() as temporary:
            source = Path(temporary) / "source.apk"
            destination = Path(temporary) / "patched.apk"
            manifest = make_manifest()
            with zipfile.ZipFile(source, "w") as apk:
                apk.writestr("AndroidManifest.xml", manifest)
                apk.writestr("assets/keep.bin", b"unchanged")
            self.assertTrue(patcher.patch_apk(source, destination))
            with zipfile.ZipFile(source) as original, zipfile.ZipFile(destination) as patched:
                self.assertEqual(original.read("assets/keep.bin"), patched.read("assets/keep.bin"))
                self.assertEqual(queried_authorities(patched.read("AndroidManifest.xml"))[-1], patcher.SPOTITHEME_SETTINGS_AUTHORITY)
                patched_manifest = patched.read("AndroidManifest.xml")
            self.assertFalse(patcher.patch_apk(destination, Path(temporary) / "patched-again.apk"))
            with zipfile.ZipFile(Path(temporary) / "patched-again.apk") as repeated:
                self.assertEqual(repeated.read("AndroidManifest.xml"), patched_manifest)


if __name__ == "__main__":
    unittest.main()
