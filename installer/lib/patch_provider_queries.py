#!/usr/bin/env python3
"""Add a narrow Android package-visibility query to a Spotify base APK."""
from __future__ import annotations

import argparse
import struct
import zipfile
from pathlib import Path

NO_INDEX = 0xFFFFFFFF
RES_XML_TYPE = 0x0003
RES_STRING_POOL_TYPE = 0x0001
RES_XML_RESOURCE_MAP_TYPE = 0x0180
RES_XML_START_ELEMENT_TYPE = 0x0102
RES_XML_END_ELEMENT_TYPE = 0x0103
UTF8_FLAG = 0x00000100
TYPE_STRING = 0x03
ANDROID_NS = "http://schemas.android.com/apk/res/android"
SPOTITHEME_SETTINGS_AUTHORITY = "com.spotitheme.settings"


def _u16(data: bytes, offset: int) -> int:
    return struct.unpack_from("<H", data, offset)[0]


def _u32(data: bytes, offset: int) -> int:
    return struct.unpack_from("<I", data, offset)[0]


def _p16(value: int) -> bytes:
    return struct.pack("<H", value)


def _p32(value: int) -> bytes:
    return struct.pack("<I", value)


def _read_len8(data: bytes, offset: int) -> tuple[int, int]:
    first = data[offset]
    if first & 0x80:
        return ((first & 0x7F) << 8) | data[offset + 1], offset + 2
    return first, offset + 1


def _read_len16(data: bytes, offset: int) -> tuple[int, int]:
    first = _u16(data, offset)
    if first & 0x8000:
        return ((first & 0x7FFF) << 16) | _u16(data, offset + 2), offset + 4
    return first, offset + 2


def _decode_string(pool: bytes, index: int) -> str:
    string_count = _u32(pool, 8)
    if index >= string_count:
        raise ValueError(f"string index {index} exceeds pool size {string_count}")
    flags = _u32(pool, 16)
    strings_start = _u32(pool, 20)
    offset = _u32(pool, 28 + index * 4)
    position = strings_start + offset
    if flags & UTF8_FLAG:
        _, position = _read_len8(pool, position)
        byte_length, position = _read_len8(pool, position)
        return pool[position : position + byte_length].decode("utf-8")
    character_length, position = _read_len16(pool, position)
    return pool[position : position + character_length * 2].decode("utf-16le")


def _encode_string(value: str, utf8: bool) -> bytes:
    if utf8:
        encoded = value.encode("utf-8")
        character_count = len(value.encode("utf-16le")) // 2

        def length8(length: int) -> bytes:
            if length < 0x80:
                return bytes((length,))
            return bytes((0x80 | (length >> 8), length & 0xFF))

        return length8(character_count) + length8(len(encoded)) + encoded + b"\0"

    encoded = value.encode("utf-16le")
    character_count = len(encoded) // 2
    if character_count < 0x8000:
        prefix = _p16(character_count)
    else:
        prefix = _p16(0x8000 | (character_count >> 16)) + _p16(character_count & 0xFFFF)
    return prefix + encoded + b"\0\0"


def _add_string(pool: bytes, value: str) -> tuple[bytes, int]:
    count = _u32(pool, 8)
    for index in range(count):
        if _decode_string(pool, index) == value:
            return pool, index

    header_size = _u16(pool, 2)
    style_count = _u32(pool, 12)
    flags = _u32(pool, 16)
    strings_start = _u32(pool, 20)
    styles_start = _u32(pool, 24)
    strings_offsets_end = header_size + count * 4
    strings_offsets = pool[header_size:strings_offsets_end]
    styles_offsets_end = strings_offsets_end + style_count * 4
    styles_offsets = pool[strings_offsets_end:styles_offsets_end]
    strings_data_end = styles_start or len(pool)
    strings_data = pool[strings_start:strings_data_end]
    styles_data = pool[styles_start:] if styles_start else b""
    encoded = _encode_string(value, bool(flags & UTF8_FLAG))
    new_index = count

    padding = -(
        header_size
        + len(strings_offsets)
        + 4
        + len(styles_offsets)
        + len(strings_data)
        + len(encoded)
        + len(styles_data)
    ) % 4
    header = bytearray(pool[:header_size])
    struct.pack_into("<I", header, 4, len(pool) + 4 + len(encoded) + padding)
    struct.pack_into("<I", header, 8, count + 1)
    struct.pack_into("<I", header, 20, strings_start + 4)
    if styles_start:
        struct.pack_into("<I", header, 24, styles_start + 4 + len(encoded) + padding)

    new_pool = (
        bytes(header)
        + strings_offsets
        + _p32(len(strings_data))
        + styles_offsets
        + strings_data
        + encoded
        + b"\0" * padding
        + styles_data
    )
    return new_pool, new_index


def _chunk_type(chunk: bytes) -> int:
    declared_size = _u32(chunk, 4)
    if declared_size != len(chunk):
        raise ValueError("invalid binary XML chunk size")
    return _u16(chunk, 0)


def _name_index(chunk: bytes) -> int:
    return _u32(chunk, 20)


def _element_attributes(chunk: bytes, pool: bytes):
    attribute_start = _u16(chunk, 24)
    attribute_size = _u16(chunk, 26)
    attribute_count = _u16(chunk, 28)
    offset = 16 + attribute_start
    for _ in range(attribute_count):
        namespace, name, raw_value = struct.unpack_from("<III", chunk, offset)
        _, _, value_type, typed_value = struct.unpack_from("<HBBI", chunk, offset + 12)
        if raw_value != NO_INDEX:
            value = _decode_string(pool, raw_value)
        elif value_type == TYPE_STRING:
            value = _decode_string(pool, typed_value)
        else:
            value = None
        namespace_name = _decode_string(pool, namespace) if namespace != NO_INDEX else None
        yield namespace_name, _decode_string(pool, name), value
        offset += attribute_size


def _start_element(name_index: int, attribute: tuple[int, int, int] | None = None) -> bytes:
    attributes = b""
    attribute_count = 0
    if attribute is not None:
        namespace, name, value = attribute
        attributes = struct.pack("<IIIHBBI", namespace, name, value, 8, 0, TYPE_STRING, value)
        attribute_count = 1
    extension = struct.pack(
        "<IIHHHHHH", NO_INDEX, name_index, 20, 20, attribute_count, 0, 0, 0
    )
    size = 16 + len(extension) + len(attributes)
    return struct.pack("<HHIII", RES_XML_START_ELEMENT_TYPE, 16, size, 1, NO_INDEX) + extension + attributes


def _end_element(name_index: int) -> bytes:
    return struct.pack("<HHIIIII", RES_XML_END_ELEMENT_TYPE, 16, 24, 1, NO_INDEX, NO_INDEX, name_index)


def _split_xml(data: bytes) -> tuple[bytes, list[bytes]]:
    if len(data) < 8 or _u16(data, 0) != RES_XML_TYPE or _u32(data, 4) != len(data):
        raise ValueError("input is not a valid Android binary XML document")
    chunks = []
    offset = 8
    while offset < len(data):
        if offset + 8 > len(data):
            raise ValueError("truncated Android binary XML chunk header")
        size = _u32(data, offset + 4)
        if size < 8 or offset + size > len(data):
            raise ValueError("invalid Android binary XML chunk length")
        chunks.append(data[offset : offset + size])
        offset += size
    return data[:8], chunks


def add_provider_query(xml: bytes) -> tuple[bytes, bool]:
    outer_header, chunks = _split_xml(xml)
    pool_index = next((i for i, chunk in enumerate(chunks) if _chunk_type(chunk) == RES_STRING_POOL_TYPE), None)
    if pool_index is None:
        raise ValueError("Android binary XML has no string pool")
    pool = chunks[pool_index]
    strings = [_decode_string(pool, i) for i in range(_u32(pool, 8))]
    indexes = {value: index for index, value in enumerate(strings)}
    for required in ("manifest", "queries", "provider", "authorities", "application", ANDROID_NS):
        if required not in indexes:
            raise ValueError(f"manifest string pool lacks {required!r}")

    resource_map = next((chunk for chunk in chunks if _chunk_type(chunk) == RES_XML_RESOURCE_MAP_TYPE), None)
    authority_index = indexes["authorities"]
    if (
        resource_map is None
        or len(resource_map) < 8 + (authority_index + 1) * 4
        or _u32(resource_map, 8 + authority_index * 4) != 0x01010018
    ):
        raise ValueError("manifest does not map android:authorities to its framework attribute")

    pool, value_index = _add_string(pool, SPOTITHEME_SETTINGS_AUTHORITY)
    strings = [_decode_string(pool, i) for i in range(_u32(pool, 8))]
    indexes = {value: index for index, value in enumerate(strings)}
    chunks[pool_index] = pool

    depth_names: list[str] = []
    queries_start = None
    queries_end = None
    application_start = None
    provider_exists = False
    for index, chunk in enumerate(chunks):
        kind = _chunk_type(chunk)
        if kind == RES_XML_START_ELEMENT_TYPE:
            name = strings[_name_index(chunk)]
            parent = depth_names[-1] if depth_names else None
            if parent == "manifest" and name == "queries" and queries_start is None:
                queries_start = index
            elif parent == "manifest" and name == "application" and application_start is None:
                application_start = index
            elif parent == "queries" and name == "provider":
                for namespace, attr_name, value in _element_attributes(chunk, pool):
                    if namespace == ANDROID_NS and attr_name == "authorities" and value == SPOTITHEME_SETTINGS_AUTHORITY:
                        provider_exists = True
            depth_names.append(name)
        elif kind == RES_XML_END_ELEMENT_TYPE:
            name = strings[_name_index(chunk)]
            if not depth_names or depth_names[-1] != name:
                raise ValueError("unbalanced binary XML element nesting")
            if name == "queries" and queries_start is not None and queries_end is None:
                queries_end = index
            depth_names.pop()

    if provider_exists:
        return xml, False
    if application_start is None:
        raise ValueError("manifest has no top-level application element")

    provider_open = _start_element(
        indexes["provider"],
        (indexes[ANDROID_NS], indexes["authorities"], value_index),
    )
    provider_close = _end_element(indexes["provider"])
    if queries_start is not None and queries_end is not None:
        insertion = [provider_open, provider_close]
        insertion_at = queries_end
    else:
        insertion = [
            _start_element(indexes["queries"]),
            provider_open,
            provider_close,
            _end_element(indexes["queries"]),
        ]
        insertion_at = application_start

    chunks[insertion_at:insertion_at] = insertion
    output = bytearray(outer_header + b"".join(chunks))
    struct.pack_into("<I", output, 4, len(output))
    return bytes(output), True


def patch_apk(source: Path, destination: Path) -> bool:
    if source.resolve() == destination.resolve():
        raise ValueError("source and destination APK paths must differ")
    with zipfile.ZipFile(source, "r") as source_apk:
        infos = source_apk.infolist()
        if "AndroidManifest.xml" not in source_apk.namelist():
            raise ValueError("APK has no AndroidManifest.xml entry")
        manifest, changed = add_provider_query(source_apk.read("AndroidManifest.xml"))
        with zipfile.ZipFile(destination, "w") as output_apk:
            output_apk.comment = source_apk.comment
            for info in infos:
                contents = manifest if info.filename == "AndroidManifest.xml" else source_apk.read(info.filename)
                output_apk.writestr(info, contents)
    return changed


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path, help="original Spotify base APK")
    parser.add_argument("destination", type=Path, help="staged base APK output")
    args = parser.parse_args()
    if patch_apk(args.source, args.destination):
        print("Added visibility for com.spotitheme.settings to the Spotify base manifest.")
    else:
        print("Spotify base manifest already queries com.spotitheme.settings.")


if __name__ == "__main__":
    main()
