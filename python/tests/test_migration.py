async def test_migration_creates_tables(pool):
    names = await pool.fetch(
        "SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename"
    )
    tables = {r["tablename"] for r in names}
    assert {"brands", "creators", "campaigns", "submissions"}.issubset(tables)
