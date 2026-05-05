import mysql.connector

conn = mysql.connector.connect(
    host='localhost',
    user='root',
    password='',
    database='wellcare_db'
)
cursor = conn.cursor()
cursor.execute("UPDATE user SET diploma_url = 'dummy_diploma.pdf' WHERE role IN ('ROLE_MEDECIN', 'ROLE_COACH', 'ROLE_NUTRITIONIST');")
conn.commit()
print("Fixed diploma URLs successfully!")
