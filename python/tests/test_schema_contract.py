from pathlib import Path

from graphql import build_schema, lexicographic_sort_schema, print_schema

from src.graphql.schema import schema


def _normalize(sdl: str) -> str:
    return print_schema(lexicographic_sort_schema(build_schema(sdl)))


def test_schema_matches_shared_contract():
    reference = (Path(__file__).resolve().parent.parent / "schema.graphqls").read_text()
    actual = str(schema)
    assert _normalize(actual) == _normalize(reference)
