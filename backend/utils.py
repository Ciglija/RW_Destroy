import os
from sqlalchemy import create_engine, text

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_DIR = os.path.join(BASE_DIR, 'database')
os.makedirs(DB_DIR, exist_ok=True)
engine = create_engine(f'sqlite:///{os.path.join(DB_DIR, "RWdestroydb.db")}')

def setup_box_stats_and_triggers():
    with engine.begin() as connection:
        connection.execute(text("""
            CREATE TABLE IF NOT EXISTS box_stats (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                unscanned_count INTEGER DEFAULT 0
            );
        """))

        connection.execute(text("""
            INSERT OR IGNORE INTO box_stats (id, unscanned_count)
            SELECT 1, COUNT(*) FROM boxes WHERE status = FALSE;
        """))

        connection.execute(text("DROP TRIGGER IF EXISTS update_unscanned_decrement;"))

        connection.execute(text("""
            CREATE TRIGGER update_unscanned_decrement
            AFTER UPDATE ON boxes
            WHEN OLD.status = FALSE AND NEW.status = TRUE
            BEGIN
                UPDATE box_stats SET unscanned_count = unscanned_count - 1 WHERE id = 1;
            END;
        """))