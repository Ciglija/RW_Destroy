import pandas as pd
from sqlalchemy import create_engine


def import_excel_to_db(file_path, table_name, db_engine):
    df = pd.read_excel(file_path)
    df.to_sql(table_name, con=db_engine, if_exists='replace', index=False)